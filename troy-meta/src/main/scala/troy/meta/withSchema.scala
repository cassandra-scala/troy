package troy.meta

import java.io.InputStream

import troy.cql.ast.{DataType, CqlParser}
import troy.schema._
import scala.collection.immutable.Seq

import scala.io.Source
import scala.meta._

class withSchema extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Defn) = meta {

    def findCqlQuery(expr: Term): (Seq[Term.Arg], Seq[Term.Arg]) = {
      expr match {
        case query: Term.Interpolate if query.prefix.value == "cql" =>
          (query.parts, query.args)
        case q"$expr.$tname" =>
          findCqlQuery(expr)
        case q"$expr.$func[..$tpts]" =>
          findCqlQuery(expr)
        case q"$expr.$func(..$exprss)"	=>
          findCqlQuery(expr)
        case q"$expr.$func[..$tpts](..$exprss)"	=>
          findCqlQuery(expr)
      }
    }

    def translateColumnTypes(types: Iterable[DataType]) = {
      types map {
        case t: DataType.Native => translateNativeColumnType(t)
        case t: DataType.Collection => translateCollectionColumnType(t)
      }
    }

    def translateCollectionColumnType(typ: DataType) = {
      def translate(t: DataType) = translateNativeColumnType(t)
      typ match {
        case DataType.List(t) => t"CDT.List[${translate(t)}]"
        case DataType.Set(t) => t"CDT.Set[${translate(t)}]"
        case DataType.Map(k, v) => t"CDT.Map[${translate(k)}, ${translate(v)}]"
        //      case DataType.Tuple(ts: Seq[DataType]) => t"CDT."
        //      case DataType.Custom(javaClass: String) => t"CDT."
      }
    }

    def translateNativeColumnType(typ: DataType) =
    typ match {
      case DataType.Ascii => t"CDT.Ascii"
      case DataType.BigInt => t"CDT.BigInt"
      case DataType.Blob => t"CDT.Blob"
      case DataType.Boolean => t"CDT.Boolean"
      case DataType.Counter => t"CDT.Counter"
      case DataType.Date => t"CDT.Date"
      case DataType.Decimal => t"CDT.Decimal"
      case DataType.Double => t"CDT.Double"
      case DataType.Float => t"CDT.Float"
      case DataType.Inet => t"CDT.Inet"
      case DataType.Int => t"CDT.Int"
      case DataType.Smallint => t"CDT.SmallInt"
      case DataType.Text => t"CDT.Text"
      case DataType.Time => t"CDT.Time"
      case DataType.Timestamp => t"CDT.Timestamp"
      case DataType.Timeuuid => t"CDT.TimeUuid"
      case DataType.Tinyint => t"CDT.TinyInt"
      case DataType.Uuid => t"CDT.Uuid"
      case DataType.Varchar => t"CDT.VarChar"
      case DataType.Varint => t"CDT.VarInt"
    }

    def parseSchemaFromFileName(path: String) =
      parseSchemaFromInputStream(
        Option(this.getClass.getResourceAsStream(path))
          .getOrElse(abort(s"Can't find schema file $path"))
      )

    def parseSchemaFromInputStream(schemaFile: InputStream) =
      parseSchemaFromSource(scala.io.Source.fromInputStream(schemaFile))

    def parseSchemaFromSource(schema: Source) = {
      val lines = schema.getLines()
      val str = lines.mkString("\n")
      parseSchemaFromString(str)
    }

    def parseSchemaFromString(schema: String) =
      CqlParser.parseSchema(schema) match {
        case CqlParser.Success(result, _) =>
          getOrAbort(SchemaEngine(result))
        case CqlParser.Failure(msg, next) =>
          abort(s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
      }

    def parseQuery(queryString: String) = CqlParser.parseDML(queryString) match {
      case CqlParser.Success(result, _) =>
        result
      case CqlParser.Failure(msg, _) =>
        abort(msg)
    }

    def getOrAbort[T](result: Result[T]) =
      result match {
        case V.Success(schema, warns) =>
          warns.map(_.message).foreach(warn)
          schema
        case V.Error(e, warns) =>
          warns.map(_.message).foreach(warn)
          abort(e.head.message)
      }

    def abort(msg: String) = throw new Exception(msg)
    def warn(msg: String) = println(msg)

    def replaceCqlQuery(expr: Term, replacement: Term): Term =
      expr match {
        case q"${query: Term.Interpolate}.prepared" if query.prefix.value == "cql" =>
          replacement
        case q"$y(..$z)" =>
          val replaced = replaceCqlQuery(y, replacement)
          q"$replaced(..$z)"
        case q"$y.$z" =>
          val replaced = replaceCqlQuery(y, replacement)
          q"$replaced.$z"
        case q"$y[..$z]" =>
          val replaced = replaceCqlQuery(y, replacement)
          q"$replaced[..$z]"
      }


    def replaceTerm(original: Term)(handler: PartialFunction[Term, Term]): Term = {
      def expand(input: Term): Term = {
        handler.orElse[Term, Term] {
          case q"$expr.$tname" =>
            q"${expand(expr)}.$tname" // Select
        }(input)
      }

      expand(original)
    }

    val q"..$mods def ${name: Term.Name}[..$tparams](..$params) = $expr" = defn

    val (qParts, qParams) = findCqlQuery(expr)

    val rawQuery = qParts.map{case q"${p: String}" => p}.mkString("?")

    val schema = parseSchemaFromFileName("/schema.cql")

    val query = parseQuery(rawQuery)

    val (rowType, variableDataTypes) = getOrAbort(schema(query))

    val parser = expr match {
      case q"$root.as[..$paramTypes](${f: Term.Name})" =>
        val columnTypes = translateColumnTypes(rowType match {
          case SchemaEngine.Asterisk(_) => abort("Troy doesn't support using .as with Select * queries")
          case SchemaEngine.Columns(types) => types
        }).toSeq

        val params = (paramTypes zip columnTypes).zipWithIndex.map {
          case ((p, c), i) =>
            q"column[$p]($i)(row).as[$c]"
        }

        q"def parser(row: com.datastax.driver.core.Row) = $f(..$params)"
//      case _ =>
//        q"def parser(row: Row) = ???" // Parser is ignored if ".as(...)" was omitted.
    }

    val replacedExpr = {
      val variableTypes = translateColumnTypes(variableDataTypes)

      val bodyParams =
        qParams.zip(variableTypes).map{ case (p, t) => arg"param($p).as[$t]": Term.Arg}

      replaceCqlQuery(expr, q"bind(prepared, ..$bodyParams)") match {
        case q"$root.as[..$paramTypes]($f)" => q"$root.parseAs(parser)"
        case other => other
      }
    }

    val imports = Seq(
      q"import _root_.troy.driver.InternalDsl._",
      q"import _root_.troy.driver.codecs.PrimitivesCodecs._"
    )

    val stats = imports ++ Seq(parser, replacedExpr)
    q"""
      val prepared = implicitly[com.datastax.driver.core.Session].prepare($rawQuery)
      ..$mods def ${name: Term.Name}[..$tparams](..$params) = { ..$stats }
    """
  }
}
