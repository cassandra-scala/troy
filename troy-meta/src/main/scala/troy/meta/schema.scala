package troy
package meta

import java.io.InputStream

import troy.cql.ast.{CqlParser, DataType, KeyspaceName, TableName}
import troy.schema.{SchemaEngineImpl, VersionedSchemaEngine, VersionedSchemaEngineImpl}
import troy.tutils.TSome

import scala.io.Source
import scala.meta._



object SchemaUtils extends Utils {

  val imports = Seq(
    q"import troy.driver.schema._",
    q"import troy.driver.schema.column._",
    q"import troy.driver.InternalDsl.CDT",
    q"import troy.tast._",
    q"import troy.tutils._"
  )

  def writeSchema: Seq[Stat] = {
    val schemas = loadSchemaFromFileName("/schema.cql").asInstanceOf[VersionedSchemaEngineImpl].schemas

    val globalSchemaImplicits = Seq(
      //      q"implicit val minVersion = VersionExists.instance[${schema.schemas.keys.min}]",
      q"implicit val maxVersion = LatestVersion.instance[${literal(schemas.keys.max)}]"
    )

    def constructImplicitVal(identifier: String, value: Term) =
      q"implicit val ${Pat.Var.Term(Term.Name(identifier))} = $value"

    val versionsSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schemas
      vLit = literal(v)
    } yield constructImplicitVal(s"v${v}Exists", q"VersionExists.instance[$vLit]")

    val keyspacesSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schemas
      vLit = literal(v)
      (KeyspaceName(k), _) <- schema.schema.keyspaces
      kLit = literal(k)
    } yield constructImplicitVal(s"keyspace${k}ExistsInV$v", q"KeyspaceExists.instance[$vLit, $kLit]")

    val tablesSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schemas
      vLit = literal(v)
      (KeyspaceName(k), keyspace) <- schema.schema.keyspaces
      kLit = t[TSome[_]](literal(k))
      (TableName(_, tableName), table) <- keyspace.tables
      tLit = literal(tableName)
    } yield constructImplicitVal(s"table${tableName}ExistsInKeyspace${k}InV$v", q"TableExists.instance[$vLit, $kLit, $tLit]")

    val columnsSchemaImplicits = for {
      (v, schema: SchemaEngineImpl) <- schemas
      vLit = literal(v)
      (KeyspaceName(k), keyspace) <- schema.schema.keyspaces
      kLit = literal(k)
      (TableName(_, tName), table) <- keyspace.tables
      tLit = literal(tName)
      (cName, column) <- table.columns
      cLit = literal(cName)
      tableType = t[troy.tast.TableName[_, _]](t[TSome[_]](literal(k)), tLit)
      ct = translateColumnType(column.dataType)
    } yield constructImplicitVal(s"column${cName}ExistsInTable${tName}InKeyspace${k}InV$v", q"ColumnType.instance[$vLit, $tableType, $cLit, $ct]")

    imports ++ globalSchemaImplicits ++ versionsSchemaImplicits ++ keyspacesSchemaImplicits ++ tablesSchemaImplicits ++ columnsSchemaImplicits
  }

  def loadSchemaFromFileName(path: String) =
    loadSchemaFromInputStream(
      Option(this.getClass.getResourceAsStream(path))
        .getOrElse(abort(s"Can't find schema file $path"))
    )

  def loadSchemaFromInputStream(schemaFile: InputStream) =
    loadSchemaFromSource(scala.io.Source.fromInputStream(schemaFile))

  def loadSchemaFromSource(schema: Source) = {
    val lines = schema.getLines()
    val str = lines.mkString("\n")
    loadSchemaFromString(str)
  }

  def loadSchemaFromString(schema: String) =
    CqlParser.parseSchema(schema) match {
      case CqlParser.Success(result, _) =>
        getOrAbort(VersionedSchemaEngine(Seq(result)))
      case CqlParser.Failure(msg, next) =>
        abort(s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
    }

  private def translateColumnType(t: DataType) = {
    t match {
      case t: DataType.Native => translateNativeColumnType(t)
      case t: DataType.Collection => translateCollectionColumnType(t)
    }
  }

  private def translateCollectionColumnType(typ: DataType): Type = {
    def translate(t: DataType) = translateNativeColumnType(t)
    typ match {
      case DataType.List(t) => t"CDT.List[${translate(t)}]"
      case DataType.Set(t) => t"CDT.Set[${translate(t)}]"
      case DataType.Map(k, v) => t"CDT.Map[${translate(k)}, ${translate(v)}]"
      //      case DataType.Tuple(ts: Seq[DataType]) => t"CDT."
      //      case DataType.Custom(javaClass: String) => t"CDT."
    }
  }

  private def translateNativeColumnType(typ: DataType): Type =
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

}

class schema extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {
    val q"object $name { ..$stats }" = defn
    val newStats = stats ++ SchemaUtils.writeSchema
    newStats.foreach(println)
    q"object $name { ..$newStats }"
  }
}