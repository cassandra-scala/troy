package troy.schema

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.parser.ParserTestUtils.{ parseQuery, parseSchema }
import VTestUtils._
import troy.schema.SchemaEngine.{ Asterisk, Columns }

object SchemaTestUtils extends FlatSpec with Matchers {
  def buildSchema(versionedStatements: String*) =
    VersionedSchemaEngine(versionedStatements.map(parseSchema)).get

  def analyse(query: String)(implicit schema: VersionedSchemaEngine) =
    schema(parseQuery(query))

  def asteriskOf(query: String)(implicit schema: VersionedSchemaEngine) =
    analyse(query).get._1.asInstanceOf[Asterisk].types

  def columnsOf(query: String)(implicit schema: VersionedSchemaEngine) =
    analyse(query).get._1.asInstanceOf[Columns].types

  def errorsOf(query: String)(implicit schema: VersionedSchemaEngine) =
    analyse(query).getErrors

  def errorOf(query: String)(implicit schema: VersionedSchemaEngine) =
    analyse(query).getError.message
}
