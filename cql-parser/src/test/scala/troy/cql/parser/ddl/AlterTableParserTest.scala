package troy.cql.parser.ddl

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast._
import troy.cql.ast.ddl.Alter._
import troy.cql.parser.ParserTestUtils.parseSchemaAs

class AlterTableParserTest extends FlatSpec with Matchers {
  "Alter Table Parser" should "parse simple alter table" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ALTER lastKnownLocation TYPE uuid;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[Type]
    alterTableInstruction.columnName shouldBe "lastKnownLocation"
    alterTableInstruction.cqlType shouldBe DataType.uuid
  }

  it should "parse simple alter table with add instruction" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ADD gravesite varchar;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[Add]
    val addInstructions = alterTableInstruction.instructions
    addInstructions.size shouldBe 1

    val addInstruction = addInstructions(0).asInstanceOf[AddInstruction]
    addInstruction.columnName shouldBe "gravesite"
    addInstruction.cqlType shouldBe DataType.varchar
  }

  it should "parse simple alter table with many add instructions" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ADD gravesite varchar, lastKnownLocation uuid;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[Add]
    val addInstructions = alterTableInstruction.instructions
    addInstructions.size shouldBe 2

    val addInstruction1 = addInstructions(0).asInstanceOf[AddInstruction]
    addInstruction1.columnName shouldBe "gravesite"
    addInstruction1.cqlType shouldBe DataType.varchar

    val addInstruction2 = addInstructions(1).asInstanceOf[AddInstruction]
    addInstruction2.columnName shouldBe "lastKnownLocation"
    addInstruction2.cqlType shouldBe DataType.uuid
  }

  it should "parse simple alter table and with instruction" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily WITH comment = 'A most excellent and useful table';")
    statement.tableName.table shouldBe "addamsFamily"

    val alertTableInstruction1 = statement.alterTableInstruction.asInstanceOf[With]
    alertTableInstruction1.options.size shouldBe 1

    val option = alertTableInstruction1.options(0).asInstanceOf[ConstantOption]
    option.basicIdentifier shouldBe "comment"
    option.constant shouldBe Constant("A most excellent and useful table")

  }

  it should "parse alter table with many with instruction" ignore {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily WITH comment = 'A most excellent and useful table' AND read_repair_chance = 0.2;")
    statement.tableName.table shouldBe "addamsFamily"

    val alertTableInstruction1 = statement.alterTableInstruction.asInstanceOf[With]
    alertTableInstruction1.options.size shouldBe 2

    val option1 = alertTableInstruction1.options(0).asInstanceOf[ConstantOption]
    option1.basicIdentifier shouldBe "comment"
    option1.constant shouldBe Constant("A most excellent and useful table")

    val option2 = alertTableInstruction1.options(1).asInstanceOf[ConstantOption]
    option2.basicIdentifier shouldBe "read_repair_chance"
    option2.constant shouldBe Constant("0.2")
  }
}
