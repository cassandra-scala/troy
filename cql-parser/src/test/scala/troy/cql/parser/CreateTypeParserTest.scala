package troy
package cql.parser

import org.scalatest._
import troy.cql.ast.ddl.Field
import troy.cql.ast.{ CreateType, DataType }
import troy.cql.parser.ParserTestUtils.parseSchemaAs

class CreateTypeParserTest extends FlatSpec with Matchers {
  "Create Type Parser" should "parse simple create type" in {
    val statement = parseSchemaAs[CreateType]("CREATE TYPE cycling.basic_info (birthday timestamp, nationality text, weight text, height text);")

    statement.ifNotExists shouldBe false
    statement.typeName.keyspace.get.name shouldBe "cycling"
    statement.typeName.name shouldBe "basic_info"

    val fields = statement.fields

    fields.size shouldBe 4
    fields shouldBe Seq(
      Field("birthday", DataType.Timestamp),
      Field("nationality", DataType.Text),
      Field("weight", DataType.Text),
      Field("height", DataType.Text)
    )
  }

  it should "parse create type without keyspace" in {
    val statement = parseSchemaAs[CreateType]("CREATE TYPE address (street text, city text, zip_code int, phones set<text>);")

    statement.ifNotExists shouldBe false
    statement.typeName.keyspace.isEmpty shouldBe true
    statement.typeName.name shouldBe "address"

    val fields = statement.fields

    fields.size shouldBe 4
    fields shouldBe Seq(
      Field("street", DataType.Text),
      Field("city", DataType.Text),
      Field("zip_code", DataType.Int),
      Field("phones", DataType.Set(DataType.Text))
    )
  }

  it should "parse create type with if not exist" in {
    val statement = parseSchemaAs[CreateType]("CREATE TYPE IF NOT EXISTS mykeyspace.address (street text, city text, zip_code int, phones set<text>);")

    statement.ifNotExists shouldBe true
    statement.typeName.keyspace.get.name shouldBe "mykeyspace"
    statement.typeName.name shouldBe "address"

    val fields = statement.fields

    fields.size shouldBe 4
    fields shouldBe Seq(
      Field("street", DataType.Text),
      Field("city", DataType.Text),
      Field("zip_code", DataType.Int),
      Field("phones", DataType.Set(DataType.Text))
    )
  }

  it should "parse create type without keyspace and with if not exist" in {
    val statement = parseSchemaAs[CreateType]("CREATE TYPE IF NOT EXISTS address (street text, city text, zip_code int, phones set<text>);")

    statement.ifNotExists shouldBe true
    statement.typeName.keyspace.isEmpty shouldBe true
    statement.typeName.name shouldBe "address"

    val fields = statement.fields

    fields.size shouldBe 4
    fields shouldBe Seq(
      Field("street", DataType.Text),
      Field("city", DataType.Text),
      Field("zip_code", DataType.Int),
      Field("phones", DataType.Set(DataType.Text))
    )
  }
}
