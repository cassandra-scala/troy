package troy.cql3_3.lexical

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast.dml.Select._
import troy.cql.parser.ParserTestUtils._

class DataManipulationParsingSpec extends FlatSpec with Matchers {
  "SELECT parser" should "parse asterisk" in {
    parseSelect("SELECT * FROM test;").selection shouldBe Asterisk
  }

  it should "parse DISTINCT partition" in {
    parseSelect("SELECT DISTINCT * FROM test;").selection shouldBe Asterisk
  }

  it should "parse JSON partition" in {
    parseSelect("SELECT JSON * FROM test;").mod.get shouldBe Json
  }

  //  TODO: https://github.com/cassandra-scala/troy/issues/134
  "Standard aggregates" should "parse count" in {
    val selectClause: SelectClause = parseSelect("SELECT COUNT(*) FROM test;").selection.asInstanceOf[SelectClause]
    selectClause.items(0).asInstanceOf[SelectionClauseItem].selector shouldBe Count
  }

  it should "parse max" ignore {
    parseSelect("SELECT MAX(*) FROM test;")
  }

  it should "parse min" ignore {
    parseSelect("SELECT MIN(*) FROM test;")
  }

  it should "parse sum" ignore {
    parseSelect("SELECT SUM(*) FROM test;")
  }

  it should "parse avg" ignore {
    parseSelect("SELECT AVG(*) FROM test;")
  }

  //  TODO: https://github.com/cassandra-scala/troy/issues/137
  "User-defined aggregate" should "be parsed" ignore {
    parseQuery("CREATE AGGREGATE min_value(double) SFUNC state_min_int STYPE double INITCOND null;")
  }
}
