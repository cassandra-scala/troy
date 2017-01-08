package troy
package cql.parser.ddl

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast.ddl.Index
import troy.cql.ast.{ StringConstant, CreateIndex }
import troy.cql.parser.ParserTestUtils.parseSchemaAs

class CreateIndexParserTest extends FlatSpec with Matchers {
  "Create Index Parser" should "parse create index" in {
    val stmt1 = parseSchemaAs[CreateIndex]("CREATE INDEX userIndex ON NerdMovies (user);")
    stmt1.isCustom shouldBe false
    stmt1.ifNotExists shouldBe false
    stmt1.indexName shouldBe Some("userIndex")
    stmt1.tableName.keyspace.isEmpty shouldBe true
    stmt1.tableName.table shouldBe "NerdMovies"
    stmt1.identifier.asInstanceOf[Index.Identifier].value shouldBe "user"
    stmt1.using.isEmpty shouldBe true

    val stmt2 = parseSchemaAs[CreateIndex]("CREATE INDEX ON Mutants (abilityId);")
    stmt2.isCustom shouldBe false
    stmt2.ifNotExists shouldBe false
    stmt2.indexName shouldBe None
    stmt2.tableName.keyspace.isEmpty shouldBe true
    stmt2.tableName.table shouldBe "Mutants"
    stmt2.identifier.asInstanceOf[Index.Identifier].value shouldBe "abilityId"
    stmt2.using.isEmpty shouldBe true

    val stmt3 = parseSchemaAs[CreateIndex]("CREATE INDEX ON users (keys(favs));")
    stmt3.isCustom shouldBe false
    stmt3.ifNotExists shouldBe false
    stmt3.indexName shouldBe None
    stmt3.tableName.keyspace.isEmpty shouldBe true
    stmt3.tableName.table shouldBe "users"
    stmt3.identifier.asInstanceOf[Index.Keys].of shouldBe "favs"
    stmt3.using.isEmpty shouldBe true

    val stmt4 = parseSchemaAs[CreateIndex]("CREATE CUSTOM INDEX ON users (email) USING 'path.to.the.IndexClass';")
    stmt4.isCustom shouldBe true
    stmt4.ifNotExists shouldBe false
    stmt4.indexName shouldBe None
    stmt4.tableName.keyspace.isEmpty shouldBe true
    stmt4.tableName.table shouldBe "users"
    stmt4.identifier.asInstanceOf[Index.Identifier].value shouldBe "email"
    stmt4.using.get.using shouldBe "path.to.the.IndexClass"
    stmt4.using.get.options.isEmpty shouldBe true

    val stmt5 = parseSchemaAs[CreateIndex]("CREATE CUSTOM INDEX ON users (email) USING 'path.to.the.IndexClass' WITH OPTIONS = {'storage': '/mnt/ssd/indexes/'};")
    stmt5.isCustom shouldBe true
    stmt5.ifNotExists shouldBe false
    stmt5.indexName shouldBe None
    stmt5.tableName.keyspace.isEmpty shouldBe true
    stmt5.tableName.table shouldBe "users"
    stmt5.identifier.asInstanceOf[Index.Identifier].value shouldBe "email"
    stmt5.using.get.using shouldBe "path.to.the.IndexClass"
    stmt5.using.get.options.get.pairs.size shouldBe 1
    stmt5.using.get.options.get.pairs(0) shouldBe StringConstant("storage") -> StringConstant("/mnt/ssd/indexes/")
  }
}
