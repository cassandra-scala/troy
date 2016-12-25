package troy.cql3_3.lexical

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.parser.ParserTestUtils._

// http://docs.datastax.com/en/cql/3.3/cql/cql_reference/keywords_r.html
// https://cassandra.apache.org/doc/latest/cql/appendices.html#appendix-a-cql-keywords
class KeywordsParsingSpec extends FlatSpec with Matchers {

  "reserved keyword ADD" should "not be parsed" in {
    queryError("SELECT ADD FROM test;")
  }

  "reserved keyword ALLOW" should "not be parsed" in {
    queryError("SELECT ALLOW FROM test;")
  }

  "reserved keyword ALTER" should "not be parsed" in {
    queryError("SELECT ALTER FROM test;")
  }

  "reserved keyword AND" should "not be parsed" in {
    queryError("SELECT AND FROM test;")
  }

  "reserved keyword APPLY" should "not be parsed" in {
    queryError("SELECT APPLY FROM test;")
  }

  "reserved keyword ASC" should "not be parsed" in {
    queryError("SELECT ASC FROM test;")
  }

  "reserved keyword AUTHORIZE" should "not be parsed" in {
    queryError("SELECT AUTHORIZE FROM test;")
  }

  "reserved keyword BATCH" should "not be parsed" in {
    queryError("SELECT BATCH FROM test;")
  }

  "reserved keyword BEGIN" should "not be parsed" in {
    queryError("SELECT BEGIN FROM test;")
  }

  "reserved keyword BY" should "not be parsed" in {
    queryError("SELECT BY FROM test;")
  }

  "reserved keyword COLUMNFAMILY" should "not be parsed" in {
    queryError("SELECT COLUMNFAMILY FROM test;")
  }

  "reserved keyword CREATE" should "not be parsed" in {
    queryError("SELECT CREATE FROM test;")
  }

  "reserved keyword DELETE" should "not be parsed" in {
    queryError("SELECT DELETE FROM test;")
  }

  "reserved keyword DESC" should "not be parsed" in {
    queryError("SELECT DESC FROM test;")
  }

  "reserved keyword DROP" should "not be parsed" in {
    queryError("SELECT DROP FROM test;")
  }

  "reserved keyword ENTRIES" should "not be parsed" in {
    queryError("SELECT ENTRIES FROM test;")
  }

  "reserved keyword FROM" should "not be parsed" in {
    queryError("SELECT FROM FROM test;")
  }

  "reserved keyword FULL" should "not be parsed" in {
    queryError("SELECT FULL FROM test;")
  }

  "reserved keyword GRANT" should "not be parsed" in {
    queryError("SELECT GRANT FROM test;")
  }

  "reserved keyword IF" should "not be parsed" in {
    queryError("SELECT IF FROM test;")
  }

  "reserved keyword IN" should "not be parsed" in {
    queryError("SELECT IN FROM test;")
  }

  "reserved keyword INDEX" should "not be parsed" in {
    queryError("SELECT INDEX FROM test;")
  }

  "reserved keyword INFINITY" should "not be parsed" in {
    queryError("SELECT INFINITY FROM test;")
  }

  "reserved keyword INSERT" should "not be parsed" in {
    queryError("SELECT INSERT FROM test;")
  }

  "reserved keyword INTO" should "not be parsed" in {
    queryError("SELECT INTO FROM test;")
  }

  "reserved keyword KEYSPACE" should "not be parsed" in {
    queryError("SELECT KEYSPACE FROM test;")
  }

  "reserved keyword LIMIT" should "not be parsed" in {
    queryError("SELECT LIMIT FROM test;")
  }

  "reserved keyword MODIFY" should "not be parsed" in {
    queryError("SELECT MODIFY FROM test;")
  }

  "reserved keyword NAN" should "not be parsed" in {
    queryError("SELECT NAN FROM test;")
  }

  "reserved keyword NORECURSIVE" should "not be parsed" in {
    queryError("SELECT NORECURSIVE FROM test;")
  }

  "reserved keyword NOT" should "not be parsed" in {
    queryError("SELECT NOT FROM test;")
  }

  "reserved keyword OF" should "not be parsed" in {
    queryError("SELECT OF FROM test;")
  }

  "reserved keyword ON" should "not be parsed" in {
    queryError("SELECT ON FROM test;")
  }

  "reserved keyword ORDER" should "not be parsed" in {
    queryError("SELECT ORDER FROM test;")
  }

  "reserved keyword PRIMARY" should "not be parsed" in {
    queryError("SELECT PRIMARY FROM test;")
  }

  "reserved keyword RENAME" should "not be parsed" in {
    queryError("SELECT RENAME FROM test;")
  }

  "reserved keyword REVOKE" should "not be parsed" in {
    queryError("SELECT REVOKE FROM test;")
  }

  "reserved keyword SCHEMA" should "not be parsed" in {
    queryError("SELECT SCHEMA FROM test;")
  }

  "reserved keyword SELECT" should "not be parsed" in {
    queryError("SELECT SELECT FROM test;")
  }

  "reserved keyword SET" should "not be parsed" in {
    queryError("SELECT SET FROM test;")
  }

  "reserved keyword TABLE" should "not be parsed" in {
    queryError("SELECT TABLE FROM test;")
  }

  "reserved keyword TO" should "not be parsed" in {
    queryError("SELECT TO FROM test;")
  }

  // https://github.com/cassandra-scala/troy/issues/132
  "reserved keyword TOKEN" should "not be parsed" ignore {
    queryError("SELECT TOKEN FROM test;")
  }

  "reserved keyword TRUNCATE" should "not be parsed" in {
    queryError("SELECT TRUNCATE FROM test;")
  }

  "reserved keyword UNLOGGED" should "not be parsed" in {
    queryError("SELECT UNLOGGED FROM test;")
  }

  "reserved keyword UPDATE" should "not be parsed" in {
    queryError("SELECT UPDATE FROM test;")
  }

  "reserved keyword USE" should "not be parsed" in {
    queryError("SELECT USE FROM test;")
  }

  "reserved keyword USING" should "not be parsed" in {
    queryError("SELECT USING FROM test;")
  }

  "reserved keyword WHERE" should "not be parsed" in {
    queryError("SELECT WHERE FROM test;")
  }

  "reserved keyword WITH" should "not be parsed" in {
    queryError("SELECT WITH FROM test;")
  }

  "non-reserved keyword ALL" should "still parse" in {
    parseSelect("SELECT ALL FROM test;")
  }

  "non-reserved keyword AS" should "still parse" in {
    parseSelect("SELECT AS FROM test;")
  }

  "non-reserved keyword ASCII" should "still parse" in {
    parseSelect("SELECT ASCII FROM test;")
  }

  "non-reserved keyword BIGINT" should "still parse" in {
    parseSelect("SELECT BIGINT FROM test;")
  }

  "non-reserved keyword BLOB" should "still parse" in {
    parseSelect("SELECT BLOB FROM test;")
  }

  "non-reserved keyword BOOLEAN" should "still parse" in {
    parseSelect("SELECT BOOLEAN FROM test;")
  }

  "non-reserved keyword CLUSTERING" should "still parse" in {
    parseSelect("SELECT CLUSTERING FROM test;")
  }

  "non-reserved keyword COMPACT" should "still parse" in {
    parseSelect("SELECT COMPACT FROM test;")
  }

  "non-reserved keyword CONSISTENCY" should "still parse" in {
    parseSelect("SELECT CONSISTENCY FROM test;")
  }

  "non-reserved keyword COUNT" should "still parse" in {
    parseSelect("SELECT COUNT FROM test;")
  }

  "non-reserved keyword COUNTER" should "still parse" in {
    parseSelect("SELECT COUNTER FROM test;")
  }

  "non-reserved keyword CUSTOM" should "still parse" in {
    parseSelect("SELECT CUSTOM FROM test;")
  }

  "non-reserved keyword DECIMAL" should "still parse" in {
    parseSelect("SELECT DECIMAL FROM test;")
  }

  "non-reserved keyword DISTINCT" should "still parse" in {
    parseSelect("SELECT foo, DISTINCT FROM test;")
  }

  "non-reserved keyword DOUBLE" should "still parse" in {
    parseSelect("SELECT DOUBLE FROM test;")
  }

  "non-reserved keyword EXISTS" should "still parse" in {
    parseSelect("SELECT EXISTS FROM test;")
  }

  "non-reserved keyword FILTERING" should "still parse" in {
    parseSelect("SELECT FILTERING FROM test;")
  }

  "non-reserved keyword FLOAT" should "still parse" in {
    parseSelect("SELECT FLOAT FROM test;")
  }

  "non-reserved keyword FROZEN" should "still parse" in {
    parseSelect("SELECT FROZEN FROM test;")
  }

  "non-reserved keyword INT" should "still parse" in {
    parseSelect("SELECT INT FROM test;")
  }

  "non-reserved keyword KEY" should "still parse" in {
    parseSelect("SELECT KEY FROM test;")
  }

  "non-reserved keyword LEVEL" should "still parse" in {
    parseSelect("SELECT LEVEL FROM test;")
  }

  "non-reserved keyword LIST" should "still parse" in {
    parseSelect("SELECT LIST FROM test;")
  }

  "non-reserved keyword MAP" should "still parse" in {
    parseSelect("SELECT MAP FROM test;")
  }

  "non-reserved keyword NOSUPERUSER" should "still parse" in {
    parseSelect("SELECT NOSUPERUSER FROM test;")
  }

  "non-reserved keyword PERMISSION" should "still parse" in {
    parseSelect("SELECT PERMISSION FROM test;")
  }

  "non-reserved keyword PERMISSIONS" should "still parse" in {
    parseSelect("SELECT PERMISSIONS FROM test;")
  }

  "non-reserved keyword STATIC" should "still parse" in {
    parseSelect("SELECT STATIC FROM test;")
  }

  "non-reserved keyword STORAGE" should "still parse" in {
    parseSelect("SELECT STORAGE FROM test;")
  }

  "non-reserved keyword SUPERUSER" should "still parse" in {
    parseSelect("SELECT SUPERUSER FROM test;")
  }

  "non-reserved keyword TEXT" should "still parse" in {
    parseSelect("SELECT TEXT FROM test;")
  }

  "non-reserved keyword TIMESTAMP" should "still parse" in {
    parseSelect("SELECT TIMESTAMP FROM test;")
  }

  "non-reserved keyword TIMEUUID" should "still parse" in {
    parseSelect("SELECT TIMEUUID FROM test;")
  }

  "non-reserved keyword TTL" should "still parse" in {
    parseSelect("SELECT TTL FROM test;")
  }

  "non-reserved keyword TUPLE" should "still parse" in {
    parseSelect("SELECT TUPLE FROM test;")
  }

  "non-reserved keyword TYPE" should "still parse" in {
    parseSelect("SELECT TYPE FROM test;")
  }

  "non-reserved keyword USER" should "still parse" in {
    parseSelect("SELECT USER FROM test;")
  }

  "non-reserved keyword USERS" should "still parse" in {
    parseSelect("SELECT USERS FROM test;")
  }

  "non-reserved keyword UUID" should "still parse" in {
    parseSelect("SELECT UUID FROM test;")
  }

  "non-reserved keyword VALUES" should "still parse" in {
    parseSelect("SELECT VALUES FROM test;")
  }

  "non-reserved keyword VARCHAR" should "still parse" in {
    parseSelect("SELECT VARCHAR FROM test;")
  }

  "non-reserved keyword VARINT" should "still parse" in {
    parseSelect("SELECT VARINT FROM test;")
  }

  "non-reserved keyword WRITETIME" should "still parse" in {
    parseSelect("SELECT WRITETIME FROM test;")
  }

}
