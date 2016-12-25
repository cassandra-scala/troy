package troy.cql3_3.lexical

import org.scalatest._
import troy.cql.ast.DataType
import troy.schema.SchemaTestUtils._

// http://docs.datastax.com/en/cql/3.3/cql/cql_reference/ucase-lcase_r.html
class CaseSensitivitySchemaSpec extends FlatSpec with Matchers {
  // TODO: https://github.com/cassandra-scala/troy/issues/128

  implicit lazy val schema = buildSchema(
    """
      | CREATE TABLE test (
      |  Foo int PRIMARY KEY,
      |  "Bar" TEXT,
      |  "baR" UUID,
      |  "baz" timestamp
      | );
    """.stripMargin
  )

  "lower case query" should "work with non-quoted capitalised schema" ignore {
    columnsOf("SELECT foo FROM test;").head shouldBe DataType.Int
  }

  "quoted capitalised query" should "not work with non-quoted capitalised schema" ignore {
    errorOf("""SELECT "Foo" FROM test;""") shouldBe "Undefined name Foo in selection clause"
  }

  "non-quoted capitalised query" should "work with non-quoted capitalised schema" ignore {
    columnsOf("SELECT Foo FROM test;").head shouldBe DataType.Int
  }

  "non-quoted uppercase query" should "work with non-quoted capitalised schema" ignore {
    columnsOf("SELECT FOO FROM test;").head shouldBe DataType.Int
  }

  "quoted capitalised query" should "work with quoted capitalised schema" ignore {
    columnsOf("""SELECT "Bar" FROM test;""").head shouldBe DataType.Text
  }

  "quoted uppercase query" should "not work with quoted capitalised schema" ignore {
    errorOf("""SELECT "BAR" FROM test;""") shouldBe "Undefined name BAR in selection clause"
  }

  "non-quoted capitalised query" should "not work with quoted capitalised schema" ignore {
    errorOf("SELECT Bar FROM test;") shouldBe "Undefined name bar in selection clause"
  }

  "quoted lowercase query" should "not work with quoted capitalised schema" ignore {
    errorOf("""SELECT "bar" FROM test;""") shouldBe "Undefined name bar in selection clause"
  }

  "quoted query" should "not mix similar columns but different case" ignore {
    columnsOf("""SELECT "baR" FROM test;""").head shouldBe DataType.Uuid
  }

  "non-quoted lowercase query" should "work with quoted lowercase column" ignore {
    columnsOf("""SELECT baz FROM test;""").head shouldBe DataType.Timestamp
  }

  "non-quoted capitalised query" should "work with quoted lowercase column" ignore {
    columnsOf("""SELECT Baz FROM test;""").head shouldBe DataType.Timestamp
  }

  "non-quoted uppercase query" should "work with quoted lowercase column" ignore {
    columnsOf("""SELECT BAZ FROM test;""").head shouldBe DataType.Timestamp
  }

}
