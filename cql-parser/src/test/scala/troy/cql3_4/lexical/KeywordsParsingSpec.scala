package troy.cql3_4.lexical

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.parser.ParserTestUtils._

// https://cassandra.apache.org/doc/latest/cql/appendices.html#appendix-a-cql-keywords
class KeywordsParsingSpec extends FlatSpec with Matchers {

  "not-any-more-reserved keyword AGGREGATE" should "be parsed" in {
    parseSelect("SELECT AGGREGATE FROM test;")
  }

  "not-any-more-reserved keyword ANY" should "be parsed" in {
    parseSelect("SELECT ANY FROM test;")
  }

  "not-any-more-reserved keyword EACH_QUORUM" should "be parsed" in {
    parseSelect("SELECT EACH_QUORUM FROM test;")
  }

  "not-any-more-reserved keyword INET" should "be parsed" in {
    parseSelect("SELECT INET FROM test;")
  }

  "not-any-more-reserved keyword KEYSPACES" should "be parsed" in {
    parseSelect("SELECT KEYSPACES FROM test;")
  }

  "not-any-more-reserved keyword LOCAL_ONE" should "be parsed" in {
    parseSelect("SELECT LOCAL_ONE FROM test;")
  }

  "not-any-more-reserved keyword LOCAL_QUORUM" should "be parsed" in {
    parseSelect("SELECT LOCAL_QUORUM FROM test;")
  }

  "not-any-more-reserved keyword MATERIALIZED" should "be parsed" in {
    parseSelect("SELECT MATERIALIZED FROM test;")
  }

  "not-any-more-reserved keyword ONE" should "be parsed" in {
    parseSelect("SELECT ONE FROM test;")
  }

  "not-any-more-reserved keyword PARTITION" should "be parsed" in {
    parseSelect("SELECT PARTITION FROM test;")
  }

  "not-any-more-reserved keyword PASSWORD" should "be parsed" in {
    parseSelect("SELECT PASSWORD FROM test;")
  }

  "not-any-more-reserved keyword PER" should "be parsed" in {
    parseSelect("SELECT PER FROM test;")
  }

  "not-any-more-reserved keyword QUORUM" should "be parsed" in {
    parseSelect("SELECT QUORUM FROM test;")
  }

  "not-any-more-reserved keyword TIME" should "be parsed" in {
    parseSelect("SELECT TIME FROM test;")
  }

  "not-any-more-reserved keyword THREE" should "be parsed" in {
    parseSelect("SELECT THREE FROM test;")
  }

  "not-any-more-reserved keyword TWO" should "be parsed" in {
    parseSelect("SELECT TWO FROM test;")
  }

  "not-any-more-reserved keyword VIEW" should "be parsed" in {
    parseSelect("SELECT VIEW FROM test;")
  }

  "reserved keyword DESCRIBE" should "not be parsed" in {
    queryError("SELECT DESCRIBE FROM test;")
  }

  "reserved keyword EXECUTE" should "not be parsed" in {
    queryError("SELECT EXECUTE FROM test;")
  }

  "reserved keyword NULL" should "not be parsed" in {
    schemaError("CREATE TABLE test ( NULL int PRIMARY KEY );")
  }

  "reserved keyword OR" should "not be parsed" in {
    queryError("SELECT OR FROM test;")
  }

  "reserved keyword REPLACE" should "not be parsed" in {
    queryError("SELECT REPLACE FROM test;")
  }

}
