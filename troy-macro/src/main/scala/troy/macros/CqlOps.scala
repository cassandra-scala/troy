package troy.macros

import java.io.InputStream
import troy.cql.ast.CqlParser
import troy.schema.Schema
import scala.io.Source

object CqlOps {
  val loadOrParseSchema = Memoize(parseSchemaFromFileName)

  private def parseSchemaFromFileName(path: String): Schema.Result[Schema] =
    Option(this.getClass.getResourceAsStream(path))
      .map(parseSchemaFromInputStream)
      .getOrElse(Schema.fail(s"Can't find schema file $path"))

  private def parseSchemaFromInputStream(schemaFile: InputStream) =
    parseSchemaFromSource(scala.io.Source.fromInputStream(schemaFile))

  private def parseSchemaFromSource(schema: Source) = {
    val lines = schema.getLines()
    val str = lines.mkString("\n")
    parseSchemaFromString(str)
  }

  private def parseSchemaFromString(schema: String) =
    CqlParser.parseSchema(schema) match {
      case CqlParser.Success(result, _) =>
        Schema(result)
      case CqlParser.Failure(msg, next) =>
        Schema.fail(s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
    }

  def parseQuery(queryString: String) = CqlParser.parseDML(queryString) match {
    case CqlParser.Success(result, _) =>
      Schema.success(result)
    case CqlParser.Failure(msg, _) =>
      Schema.fail(msg)
  }
}

case class Memoize[T, R](f: T => R) extends (T => R) {
  private val cache = scala.collection.mutable.Map.empty[T, R]
  def apply(x: T) = cache.getOrElseUpdate(x, f(x))
}