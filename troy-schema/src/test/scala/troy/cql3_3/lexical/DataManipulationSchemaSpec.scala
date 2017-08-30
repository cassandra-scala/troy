package troy.cql3_3.lexical

import org.scalatest._
import troy.cql.ast.DataType
import troy.schema.SchemaTestUtils._

class DataManipulationSchemaSpec extends FlatSpec with Matchers {
  implicit lazy val schema = buildSchema(
    """
      | CREATE KEYSPACE specs WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
      | CREATE TABLE specs.test (
      |  foo int PRIMARY KEY,
      |  bar TEXT,
      |  barfoo TEXT
      | );
    """.stripMargin
  )

  // TODO: https://github.com/cassandra-scala/troy/issues/136
  "Schema" should "support select *" ignore {
    asteriskOf("SELECT * FROM specs.test;").size shouldBe 3
  }

  it should "support select with DISTINCT partition" in {
    columnsOf("SELECT DISTINCT foo FROM specs.test;").head shouldBe DataType.Int
  }

  it should "support select with JSON formate" in {
    columnsOf("SELECT JSON foo FROM specs.test;").head shouldBe DataType.Int
  }

  //  TODO: https://github.com/cassandra-scala/troy/issues/134
  it should "support select with count aggregate function" ignore {
    columnsOf("SELECT count(foo) FROM specs.test;").head shouldBe DataType.Int
  }

  it should "support select with min aggregate function" ignore {
    columnsOf("SELECT min(foo) FROM specs.test;").head shouldBe DataType.Int
  }

  it should "support select with max aggregate function" ignore {
    columnsOf("SELECT max(foo) FROM specs.test;").head shouldBe DataType.Int
  }

  it should "support select with sum aggregate function" ignore {
    columnsOf("SELECT sum(foo) FROM specs.test;").head shouldBe DataType.Int
  }

  it should "support select with avg aggregate function" ignore {
    columnsOf("SELECT avg(foo) FROM specs.test;").head shouldBe DataType.Int
  }

  //  TODO: https://github.com/cassandra-scala/troy/issues/137
  it should "support select with User-defined aggregate function" ignore {
    columnsOf("SELECT UDAggregate(foo) FROM specs.test;").head shouldBe DataType.Int
  }
}
