package troy.cql.parser.ddl

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast._
import troy.cql.ast.ddl.Alter._
import troy.cql.parser.ParserTestUtils.parseSchemaAs

class AlterTableParserTest extends FlatSpec with Matchers {
  "Alter Table Parser" should "parse simple alter table" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ALTER lastKnownLocation TYPE uuid;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[AlterType]
    alterTableInstruction.columnName shouldBe "lastKnownLocation"
    alterTableInstruction.cqlType shouldBe DataType.Uuid
  }

  it should "parse simple alter table with add instruction" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ADD gravesite varchar;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[AddColumns]
    val addInstructions = alterTableInstruction.instructions
    addInstructions.size shouldBe 1

    val addInstruction = addInstructions(0)
    addInstruction.columnName shouldBe "gravesite"
    addInstruction.cqlType shouldBe DataType.Varchar
    addInstruction.isStatic shouldBe false
  }

  it should "parse alter table with static flag" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ADD gravesite varchar static;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[AddColumns]
    val addInstructions = alterTableInstruction.instructions
    addInstructions.size shouldBe 1

    val addInstruction = addInstructions(0)
    addInstruction.columnName shouldBe "gravesite"
    addInstruction.cqlType shouldBe DataType.Varchar
    addInstruction.isStatic shouldBe true
  }

  it should "parse simple alter table with drop instruction" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily DROP gravesite;")
    statement.tableName.table shouldBe "addamsFamily"
    statement.alterTableInstruction.asInstanceOf[DropColumn].columnName shouldBe "gravesite"
  }

  it should "parse simple alter table with multiple add instructions" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ADD gravesite varchar, lastKnownLocation uuid;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[AddColumns]
    val addInstructions = alterTableInstruction.instructions
    addInstructions.size shouldBe 2

    val addInstruction1 = addInstructions(0)
    addInstruction1.columnName shouldBe "gravesite"
    addInstruction1.cqlType shouldBe DataType.Varchar
    addInstruction1.isStatic shouldBe false

    val addInstruction2 = addInstructions(1)
    addInstruction2.columnName shouldBe "lastKnownLocation"
    addInstruction2.cqlType shouldBe DataType.Uuid
    addInstruction2.isStatic shouldBe false
  }
  it should "parse simple alter table with multiple add instructions 2" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE test.posts ADD brandNewColumn decimal, anotherBrandNewColumn text;")
    val instructions = statement.alterTableInstruction.asInstanceOf[AddColumns].instructions
    instructions.size shouldBe 2
  }

  it should "parse simple alter table with multiple add instructions with mixed static flags" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily ADD gravesite varchar static, lastKnownLocation uuid;")
    statement.tableName.table shouldBe "addamsFamily"
    val alterTableInstruction = statement.alterTableInstruction.asInstanceOf[AddColumns]
    val addInstructions = alterTableInstruction.instructions
    addInstructions.size shouldBe 2

    val addInstruction1 = addInstructions(0)
    addInstruction1.columnName shouldBe "gravesite"
    addInstruction1.cqlType shouldBe DataType.Varchar
    addInstruction1.isStatic shouldBe true

    val addInstruction2 = addInstructions(1)
    addInstruction2.columnName shouldBe "lastKnownLocation"
    addInstruction2.cqlType shouldBe DataType.Uuid
    addInstruction2.isStatic shouldBe false
  }

  it should "parse simple alter table and with instruction" in {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily WITH comment = 'A most excellent and useful table';")
    statement.tableName.table shouldBe "addamsFamily"

    val alertTableInstruction1 = statement.alterTableInstruction.asInstanceOf[With]
    alertTableInstruction1.options.size shouldBe 1

    val option = alertTableInstruction1.options(0).asInstanceOf[ConstantOption]
    option.basicIdentifier shouldBe "comment"
    option.constant shouldBe StringConstant("A most excellent and useful table")

  }

  it should "parse alter table with many with instruction" ignore {
    val statement = parseSchemaAs[AlterTable]("ALTER TABLE addamsFamily WITH comment = 'A most excellent and useful table' AND read_repair_chance = 0.2;")
    statement.tableName.table shouldBe "addamsFamily"

    val alertTableInstruction1 = statement.alterTableInstruction.asInstanceOf[With]
    alertTableInstruction1.options.size shouldBe 2

    val option1 = alertTableInstruction1.options(0).asInstanceOf[ConstantOption]
    option1.basicIdentifier shouldBe "comment"
    option1.constant shouldBe StringConstant("A most excellent and useful table")

    val option2 = alertTableInstruction1.options(1).asInstanceOf[ConstantOption]
    option2.basicIdentifier shouldBe "read_repair_chance"
    option2.constant shouldBe FloatConstant(0.2f)
  }
}
