package troy.cql3_3.lexical

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast.StringConstant
import troy.cql.parser.ParserTestUtils.InsertUtils._

// http://docs.datastax.com/en/cql/3.3/cql/cql_reference/escape_char_r.html
class EscapingCharactersParsingSpec extends FlatSpec with Matchers {
  "single quotation in string literal" should "be escaped by duplicate single quotation mark" in {
    parse(
      """
        | INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) VALUES
        |  (201, '2015-02-18', '2015-02-22', 'Women''s Tour of New Zealand');
      """.stripMargin
    ).values(3) shouldBe StringConstant("Women's Tour of New Zealand")
  }

  // TODO: https://github.com/cassandra-scala/troy/issues/130
  it should "be escaped by duplicate dollar signs" ignore {
    parse(
      """
        | INSERT INTO cycling.calendar (race_id, race_start_date, race_end_date, race_name) VALUES
        |  (201, '2015-02-18', '2015-02-22', $$Women's Tour of New Zealand$$);
      """.stripMargin
    ).values(3) shouldBe StringConstant("Women's Tour of New Zealand")
  }

}
