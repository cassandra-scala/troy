package troy
package cql3_3.lexical

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast.dml.Select.{ ColumnName, SelectionClauseItem, SelectClause }
import troy.cql.parser.ParserTestUtils._

// http://docs.datastax.com/en/cql/3.3/cql/cql_reference/ucase-lcase_r.html
class CaseSensitivityParsingSpec extends FlatSpec with Matchers {
  // TODO: https://github.com/cassandra-scala/troy/issues/128
  "lower case columns in queries" should "be parsed" in {
    parseSelect("SELECT foo FROM test;").selection shouldBe SelectClause(Seq(SelectionClauseItem(ColumnName("foo"), None)))
  }

  "capitalized columns in queries" should "be parsed as lowercase" ignore {
    parseSelect("SELECT Foo FROM test;").selection shouldBe SelectClause(Seq(SelectionClauseItem(ColumnName("foo"), None)))
  }

  "uppercase columns in queries" should "be parsed as lowercase" ignore {
    parseSelect("SELECT FOO FROM test;").selection shouldBe SelectClause(Seq(SelectionClauseItem(ColumnName("foo"), None)))
  }

  "quoted capitalized columns in queries" should "be parsed as capitalized" ignore {
    parseSelect("""SELECT "Bar" FROM test;""").selection shouldBe SelectClause(Seq(SelectionClauseItem(ColumnName("Bar"), None)))
  }

  "quoted uppercase columns in queries" should "be parsed as uppercase" ignore {
    parseSelect("""SELECT "BAR" FROM test;""").selection shouldBe SelectClause(Seq(SelectionClauseItem(ColumnName("BAR"), None)))
  }

  "quoted lowercase columns in queries" should "be parsed as lowercase" ignore {
    parseSelect("""SELECT "bar" FROM test;""").selection shouldBe SelectClause(Seq(SelectionClauseItem(ColumnName("bar"), None)))
  }

  "quoted lowercase columns in schema" should "be parsed as lowercase" ignore {
    val columns = parseCreateTable(
      """
        | CREATE TABLE test (
        |  Foo int PRIMARY KEY,
        |  "Bar" TEXT,
        |  "baR" UUID,
        |  "baz" timestamp
        | );
      """.stripMargin
    ).columns

    columns(0).name shouldBe "foo"
    columns(1).name shouldBe "Bar"
    columns(2).name shouldBe "baR"
    columns(3).name shouldBe "baz"
  }
}
