package troy
package schema

import java.io.InputStream
import troy.cql.ast.CqlParser
import troy.schema.V
import V.Implicits._
import troy.schema._
import scala.io.Source

trait SchemaLoader {
  def load: Result[SchemaEngine]
}

trait VersionedSchemaLoader extends SchemaLoader {
  def load: Result[VersionedSchemaEngine]
}

class StringSchemaLoader(schema: String) extends SchemaLoader {
  val load: Result[SchemaEngine] =
    CqlParser.parseSchema(schema) match {
      case CqlParser.Success(result, _) =>
        SchemaEngine(result)
      case CqlParser.Failure(msg, next) =>
        V.error(Messages.SchemaParseFailure(msg, next.pos.line, next.pos.column))
    }
}

class ResourceFileSchemaLoader(path: String) extends SchemaLoader {
  val load: Result[SchemaEngine] =
    Option(this.getClass.getResourceAsStream(path))
      .toV(Messages.SchemaNotFound(path))
      .flatMap(parseSchemaFromInputStream)

  private def parseSchemaFromInputStream(schemaFile: InputStream): Result[SchemaEngine] =
    parseSchemaFromSource(scala.io.Source.fromInputStream(schemaFile))

  private def parseSchemaFromSource(schema: Source) = {
    val lines = schema.getLines()
    val str = lines.mkString("\n")
    new StringSchemaLoader(str).load
  }
}

class ResourceFileSingleVersionSchemaLoader(path: String) extends VersionedSchemaLoader {
  val load: Result[VersionedSchemaEngine] =
    new ResourceFileSchemaLoader(path).load.map(VersionedSchemaEngine.wrap)
}

class ResourceFolderSchemaLoader(path: String) extends VersionedSchemaLoader {
  import VParseResultImplicits._

  val load: Result[VersionedSchemaEngine] =
    Option(this.getClass.getResourceAsStream(path))
      .toV(Messages.SchemaNotFound(path))
      .map(scala.io.Source.fromInputStream)
      .map(_.getLines().toSeq)
      .flatMap { r =>
        V.merge(r.map { file =>
          val source = scala.io.Source.fromInputStream(this.getClass.getResourceAsStream(path + file)).getLines().mkString("\n")
          CqlParser
            .parseSchema(source)
            .toV(f => Messages.SchemaParseFailure(f.msg, f.next.pos.line, f.next.pos.column))
        })
      }
      .flatMap(VersionedSchemaEngine.apply)
}

class ResourceFileOrFolderSchemaLoader(file: String, folder: String) extends VersionedSchemaLoader {
  val load: Result[VersionedSchemaEngine] =
    if (this.getClass.getResourceAsStream(file) != null)
      new ResourceFileSingleVersionSchemaLoader(file).load
    else
      new ResourceFolderSchemaLoader(folder).load
}

object VParseResultImplicits {
  implicit class VParseResultOps[T](val r: CqlParser.ParseResult[T]) extends AnyVal {
    def toV(e: CqlParser.Failure => Message): Result[T] =
      r match {
        case CqlParser.Success(result, _) =>
          V.success(result)
        case f: CqlParser.Failure =>
          V.error(e(f))
      }
  }
}
