package troy
package cql.parser.ddl

import org.scalatest._
import troy.cql.ast.CreateKeyspace
import troy.cql.ast.ddl.Keyspace
import troy.cql.parser.ParserTestUtils.parseSchemaAs

class CreateKeyspaceParserTest extends FlatSpec with Matchers {

  "Create Keyspace Parser" should "parse simple create keyspace" in {
    val statement = parseSchemaAs[CreateKeyspace]("create KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'}; ")

    statement.ifNotExists shouldBe false
    statement.keyspaceName.name shouldBe "test"
    statement.properties shouldBe Seq(Keyspace.Replication(Seq(("class", "SimpleStrategy"), ("replication_factor", "1"))))
  }
}
