package troy.meta

import java.io.InputStream

import troy.cql.ast.{DataType, CqlParser}
import troy.schema.Schema
import scala.collection.immutable.Seq

import scala.io.Source
import scala.meta._

class withSchema extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Defn) = meta {
    println(">>>>>>>>>>>>>>>>>>>>>>>>> 0")
    def findCqlQuery(expr: Term): (Seq[Term.Arg], Seq[Term.Arg]) = {
      println(expr)
      expr match {
        case query: Term.Interpolate if query.prefix.value == "cql" =>
          println(">>>>>>>>>>>>>>>>>>>>>>>>> 2.1")
          (query.parts, query.args)
        case q"$expr.$tname" =>
          println(">>>>>>>>>>>>>>>>>>>>>>>>> 2.2")
          findCqlQuery(expr)
        case q"$expr.$func[..$tpts]" =>
          println(">>>>>>>>>>>>>>>>>>>>>>>>> 2.3")
          findCqlQuery(expr)
        case q"$expr.$func(..$exprss)"	=>
          println(">>>>>>>>>>>>>>>>>>>>>>>>> 2.4")
          findCqlQuery(expr)
        case q"$expr.$func[..$tpts](..$exprss)"	=>
          println(">>>>>>>>>>>>>>>>>>>>>>>>> 2.5")
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
      val cdt = q"CassandraDataType"
      def translate(t: DataType) = translateNativeColumnType(t)
      typ match {
        case DataType.list(t) => t"CassandraDataType.List[${translate(t)}]"
        case DataType.set(t) => t"CassandraDataType.Set[${translate(t)}]"
        case DataType.map(k, v) => t"CassandraDataType.Map[${translate(k)}, ${translate(v)}]"
        //      case DataType.Tuple(ts: Seq[DataType]) => t"CassandraDataType."
        //      case DataType.Custom(javaClass: String) => t"CassandraDataType."
      }
    }

    def translateNativeColumnType(typ: DataType) =
    typ match {
      case DataType.ascii => t"CassandraDataType.Ascii"
      case DataType.bigint => t"CassandraDataType.BigInt"
      case DataType.blob => t"CassandraDataType.Blob"
      case DataType.boolean => t"CassandraDataType.Boolean"
      case DataType.counter => t"CassandraDataType.Counter"
      case DataType.date => t"CassandraDataType.Date"
      case DataType.decimal => t"CassandraDataType.Decimal"
      case DataType.double => t"CassandraDataType.Double"
      case DataType.float => t"CassandraDataType.Float"
      case DataType.inet => t"CassandraDataType.Inet"
      case DataType.int => t"CassandraDataType.Int"
      case DataType.smallint => t"CassandraDataType.SmallInt"
      case DataType.text => t"CassandraDataType.Text"
      case DataType.times => t"CassandraDataType.Time"
      case DataType.timestamp => t"CassandraDataType.Timestamp"
      case DataType.timeuuid => t"CassandraDataType.TimeUuid"
      case DataType.tinyint => t"CassandraDataType.TinyInt"
      case DataType.uuid => t"CassandraDataType.Uuid"
      case DataType.varchar => t"CassandraDataType.VarChar"
      case DataType.varint => t"CassandraDataType.VarInt"
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
          Schema(result) match {
            case Right(schema) => schema
            case Left(e)       => abort(e)
          }
        case CqlParser.Failure(msg, next) =>
          abort(s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
      }

    def parseQuery(queryString: String) = CqlParser.parseDML(queryString) match {
      case CqlParser.Success(result, _) =>
        result
      case CqlParser.Failure(msg, _) =>
        abort(msg)
    }

    def abort(msg: String) = throw new Exception(msg)
    def warn(msg: String) = println(msg)
    def log[T](o: T): T = {println(o); o}


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

    val (rowType, variableDataTypes) = schema(query) match {
      case Right(data) => data
      case Left(e)     => abort(e)
    }

    val parser = expr match {
      case q"$root.as[..$paramTypes](${f: Term.Name})" =>
        val columnTypes = translateColumnTypes(rowType match {
          case Schema.Columns(types) => types
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
      q"import _root_.troy.meta.InternalDsl._",
      q"import _root_.troy.driver.CassandraDataType",
      q"import _root_.troy.driver.codecs.HasTypeCodec._",
      q"import _root_.troy.meta.codecs.Primitives._")

    val stats = imports ++ Seq(parser, replacedExpr)
    log(q"""
      val prepared = implicitly[com.datastax.driver.core.Session].prepare($rawQuery)
      ..$mods def ${name: Term.Name}[..$tparams](..$params) = { ..$stats }
    """)
  }
}
