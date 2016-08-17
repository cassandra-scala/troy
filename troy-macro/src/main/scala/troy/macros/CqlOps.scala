package troy.macros

import java.io.InputStream

import troy.cql.ast.CqlParser
import troy.schema.Schema

import scala.io.Source
import scala.reflect.macros.blackbox.Context

object CqlOps {
//  val loadOrParseSchema(path: String)
  
  def parseSchemaFromFileName(path: String) =
    parseSchemaFromInputStream(
      Option(this.getClass.getResourceAsStream(path))
        .getOrElse(c.abort(c.universe.NoPosition, s"Can't find schema file $path"))
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
          case Left(e)       => c.abort(c.enclosingPosition, e)
        }
      case CqlParser.Failure(msg, next) =>
        c.abort(c.universe.NoPosition, s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
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