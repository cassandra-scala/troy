package troy.cql.parser.dml

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast.dml.DeleteStatement.{ Exist, SimpleIfCondition }
import troy.cql.ast.{ Constant, CqlParser, TupleLiteral }
import troy.cql.ast.dml._
import troy.cql.ast.dml.WhereClause.Relation.Simple

class DeleteStatementParserTest extends FlatSpec with Matchers {
  "Delete Parser" should "parse simple delete statement" in {
    val statement = parseQuery("DELETE FROM NerdMovies WHERE movie = 'Serenity';").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isEmpty shouldBe true
    statement.from.table shouldBe "NerdMovies"
    statement.using.isEmpty shouldBe true

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "movie"
    simpleRelation.operator shouldBe Operator.Equals
    simpleRelation.term.asInstanceOf[Constant].raw shouldBe "Serenity"

  }

  it should "parse delete specific column statement" in {
    val statement = parseQuery("DELETE phone FROM Users WHERE userid IN (123, 222);").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isDefined shouldBe true
    statement.simpleSelection.get.size shouldBe 1
    statement.simpleSelection.get(0).asInstanceOf[ColumnNameSelection].columnName shouldBe "phone"

    statement.from.table shouldBe "Users"
    statement.using.isEmpty shouldBe true

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "userid"
    simpleRelation.operator shouldBe Operator.In
    val literal: TupleLiteral = simpleRelation.term.asInstanceOf[TupleLiteral]
    literal.values.size shouldBe 2
    literal.values(0) shouldBe Constant("123")
    literal.values(1) shouldBe Constant("222")

  }

  it should "parse delete specific columns statement " in {
    val statement = parseQuery("DELETE phone, name FROM Users WHERE userid = 123;").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isDefined shouldBe true
    statement.simpleSelection.get.size shouldBe 2
    statement.simpleSelection.get(0).asInstanceOf[ColumnNameSelection].columnName shouldBe "phone"
    statement.simpleSelection.get(1).asInstanceOf[ColumnNameSelection].columnName shouldBe "name"

    statement.from.table shouldBe "Users"
    statement.using.isEmpty shouldBe true

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "userid"
    simpleRelation.operator shouldBe Operator.Equals
    simpleRelation.term.asInstanceOf[Constant].raw shouldBe "123"

  }

  it should "parse delete specific column with field name statement " in {
    val statement = parseQuery("DELETE address.postcode FROM Users WHERE userid = 123;").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isDefined shouldBe true
    statement.simpleSelection.get.size shouldBe 1
    val simpleSelection = statement.simpleSelection.get(0).asInstanceOf[ColumnNameSelectionWithFieldName]
    simpleSelection.columnName shouldBe "address"
    simpleSelection.fieldName shouldBe "postcode"

    statement.from.table shouldBe "Users"
    statement.using.isEmpty shouldBe true

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "userid"
    simpleRelation.operator shouldBe Operator.Equals
    simpleRelation.term.asInstanceOf[Constant].raw shouldBe "123"

  }

  it should "parse delete specific column with term statement " in {
    val statement = parseQuery("DELETE address['postcode'] FROM Users WHERE userid = 123;").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isDefined shouldBe true
    statement.simpleSelection.get.size shouldBe 1
    val simpleSelection = statement.simpleSelection.get(0).asInstanceOf[ColumnNameSelectionWithTerm]
    simpleSelection.columnName shouldBe "address"
    simpleSelection.term.asInstanceOf[Constant].raw shouldBe "postcode"

    statement.from.table shouldBe "Users"
    statement.using.isEmpty shouldBe true

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "userid"
    simpleRelation.operator shouldBe Operator.Equals
    simpleRelation.term.asInstanceOf[Constant].raw shouldBe "123"
  }

  it should "parse delete specific column if exists statement " in {
    val statement = parseQuery("DELETE phone FROM Users WHERE userid = 123 IF EXISTS;").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isDefined shouldBe true
    statement.simpleSelection.get.size shouldBe 1
    statement.simpleSelection.get(0).asInstanceOf[ColumnNameSelection].columnName shouldBe "phone"

    statement.from.table shouldBe "Users"
    statement.using.isEmpty shouldBe true
    statement.ifCondition.isDefined shouldBe true
    statement.ifCondition.get.asInstanceOf[Exist].value shouldBe true

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "userid"
    simpleRelation.operator shouldBe Operator.Equals
    simpleRelation.term.asInstanceOf[Constant].raw shouldBe "123"

  }

  it should "parse delete specific column with simple if condition " in {
    val statement = parseQuery("DELETE phone FROM Users WHERE userid = 123 IF postcode = 'M1' ;").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isDefined shouldBe true
    statement.simpleSelection.get.size shouldBe 1
    statement.simpleSelection.get(0).asInstanceOf[ColumnNameSelection].columnName shouldBe "phone"

    statement.from.table shouldBe "Users"
    statement.using.isEmpty shouldBe true

    statement.ifCondition.isDefined shouldBe true
    val conditions = statement.ifCondition.get.asInstanceOf[SimpleIfCondition].conditions
    conditions.size shouldBe 1
    val condition = conditions(0).asInstanceOf[SimpleCondition]
    condition.simpleSelection.asInstanceOf[ColumnNameSelection].columnName shouldBe "postcode"
    condition.operator shouldBe Operator.Equals
    condition.term.asInstanceOf[Constant].raw shouldBe "M1"

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "userid"
    simpleRelation.operator shouldBe Operator.Equals
    simpleRelation.term.asInstanceOf[Constant].raw shouldBe "123"

  }

  it should "parse IN tuple of UUID" ignore {
    val statement = parseQuery("DELETE phone FROM Users WHERE userid IN (C73DE1D3, B70DE1D0);").asInstanceOf[DeleteStatement]
    statement.simpleSelection.isDefined shouldBe true
    statement.simpleSelection.get.size shouldBe 1
    statement.simpleSelection.get(0).asInstanceOf[ColumnNameSelection].columnName shouldBe "phone"

    statement.from.table shouldBe "Users"
    statement.using.isEmpty shouldBe true

    val relations = statement.where.relations
    relations.size shouldBe 1
    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "userid"
    simpleRelation.operator shouldBe Operator.In
    val literal: TupleLiteral = simpleRelation.term.asInstanceOf[TupleLiteral]
    literal.values.size shouldBe 2
    literal.values(0) shouldBe Constant("C73DE1D3")
    literal.values(1) shouldBe Constant("B70DE1D0")

  }

  def parseQuery(statement: String) =
    CqlParser
      .parseDML(statement) match {
        case CqlParser.Success(res, _)    => res
        case CqlParser.Failure(msg, next) => fail(s"Parse Failure: $msg, line = ${next.pos.line}, column = ${next.pos.column}")
      }
}