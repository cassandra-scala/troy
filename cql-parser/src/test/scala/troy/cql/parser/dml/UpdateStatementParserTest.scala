package troy.cql.parser.dml

import java.util.UUID

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast._
import troy.cql.ast.dml._
import troy.cql.ast.dml.Update._
import troy.cql.ast.dml.WhereClause.Relation.Simple
import troy.cql.parser.ParserTestUtils.parseQuery

class UpdateStatementParserTest extends FlatSpec with Matchers {
  import SimpleSelection._
  "Update Parser" should "parse simple update statement with simple set" in {
    val statement = parseQuery("UPDATE NerdMovies USING TTL 400 SET director = 'Joss Whedon', main_actor = 'Nathan Fillion',year = 2005 WHERE movie = 'Serenity';").asInstanceOf[UpdateStatement]
    statement.tableName.table shouldBe "NerdMovies"

    statement.using.size shouldBe 1
    val ttl = statement.using(0).asInstanceOf[Ttl]
    ttl.value.asInstanceOf[UpdateValue].value shouldBe "400"

    val assignments = statement.set
    assignments.size shouldBe 3

    val assignment1 = assignments(0).asInstanceOf[SimpleSelectionAssignment]
    assignment1.selection.asInstanceOf[ColumnName].columnName shouldBe "director"
    assignment1.term.asInstanceOf[StringConstant].value shouldBe "Joss Whedon"

    val assignment2 = assignments(1).asInstanceOf[SimpleSelectionAssignment]
    assignment2.selection.asInstanceOf[ColumnName].columnName shouldBe "main_actor"
    assignment2.term.asInstanceOf[StringConstant].value shouldBe "Nathan Fillion"

    val assignment3 = assignments(2).asInstanceOf[SimpleSelectionAssignment]
    assignment3.selection.asInstanceOf[ColumnName].columnName shouldBe "year"
    assignment3.term.asInstanceOf[IntegerConstant].value shouldBe 2005

    val relations = statement.where.relations
    relations.size shouldBe 1

    val simpleRelation = relations(0).asInstanceOf[Simple]
    simpleRelation.columnName shouldBe "movie"
    simpleRelation.operator shouldBe Operator.Equals
    simpleRelation.term.asInstanceOf[StringConstant].value shouldBe "Serenity"
  }

  it should "parse update statement with term assignment" in {
    val statement = parseQuery("UPDATE UserActions SET total = total + 2 WHERE user = B70DE1D0-9908-4AE3-BE34-5573E5B09F14 AND action = 'click';").asInstanceOf[UpdateStatement]
    statement.tableName.table shouldBe "UserActions"
    statement.using.isEmpty shouldBe true
    val assignments = statement.set
    assignments.size shouldBe 1

    val assignment1 = assignments(0).asInstanceOf[TermAssignment]
    assignment1.columnName1 shouldBe "total"
    assignment1.columnName2 shouldBe "total"
    assignment1.updateOperator shouldBe UpdateOperator.Add
    assignment1.term.asInstanceOf[IntegerConstant].value shouldBe 2

    val relations = statement.where.relations
    relations.size shouldBe 2

    val simpleRelation1 = relations(0).asInstanceOf[Simple]
    simpleRelation1.columnName shouldBe "user"
    simpleRelation1.operator shouldBe Operator.Equals
    simpleRelation1.term shouldBe UuidConstant(UUID.fromString("B70DE1D0-9908-4AE3-BE34-5573E5B09F14"))

    val simpleRelation2 = relations(1).asInstanceOf[Simple]
    simpleRelation2.columnName shouldBe "action"
    simpleRelation2.operator shouldBe Operator.Equals
    simpleRelation2.term.asInstanceOf[StringConstant].value shouldBe "click"

  }

  it should "parse update statement with bindmarks in term assignment" in {
    val statement = parseQuery("UPDATE UserActions SET total = total + ? WHERE user = B70DE1D0-9908-4AE3-BE34-5573E5B09F14 AND action = 'click';").asInstanceOf[UpdateStatement]
    statement.tableName.table shouldBe "UserActions"
    statement.using.isEmpty shouldBe true
    val assignments = statement.set
    assignments.size shouldBe 1

    val assignment1 = assignments(0).asInstanceOf[TermAssignment]
    assignment1.columnName1 shouldBe "total"
    assignment1.columnName2 shouldBe "total"
    assignment1.updateOperator shouldBe UpdateOperator.Add
    assignment1.term shouldBe BindMarker.Anonymous

    val relations = statement.where.relations
    relations.size shouldBe 2

    val simpleRelation1 = relations(0).asInstanceOf[Simple]
    simpleRelation1.columnName shouldBe "user"
    simpleRelation1.operator shouldBe Operator.Equals
    simpleRelation1.term shouldBe UuidConstant(UUID.fromString("B70DE1D0-9908-4AE3-BE34-5573E5B09F14"))

    val simpleRelation2 = relations(1).asInstanceOf[Simple]
    simpleRelation2.columnName shouldBe "action"
    simpleRelation2.operator shouldBe Operator.Equals
    simpleRelation2.term.asInstanceOf[StringConstant].value shouldBe "click"

  }

  it should "parse update statement with list literal assignment" in {
    val statement = parseQuery("UPDATE UserActions SET users = [2] + users WHERE user = B70DE1D0-9908-4AE3-BE34-5573E5B09F14 AND action = 'click';").asInstanceOf[UpdateStatement]
    statement.tableName.table shouldBe "UserActions"
    statement.using.isEmpty shouldBe true
    val assignments = statement.set
    assignments.size shouldBe 1

    val assignment1 = assignments(0).asInstanceOf[ListLiteralAssignment]

    assignment1.columnName1 shouldBe "users"
    assignment1.columnName2 shouldBe "users"
    assignment1.listLiteral.left.get.values(0).asInstanceOf[IntegerConstant].value shouldBe 2

    val relations = statement.where.relations
    relations.size shouldBe 2

    val simpleRelation1 = relations(0).asInstanceOf[Simple]
    simpleRelation1.columnName shouldBe "user"
    simpleRelation1.operator shouldBe Operator.Equals
    simpleRelation1.term shouldBe UuidConstant(UUID.fromString("B70DE1D0-9908-4AE3-BE34-5573E5B09F14"))

    val simpleRelation2 = relations(1).asInstanceOf[Simple]
    simpleRelation2.columnName shouldBe "action"
    simpleRelation2.operator shouldBe Operator.Equals
    simpleRelation2.term.asInstanceOf[StringConstant].value shouldBe "click"
  }

  it should "parse update statement with bindmark in list literal assignment" in {
    val statement = parseQuery("UPDATE UserActions SET users = ? + users WHERE user = B70DE1D0-9908-4AE3-BE34-5573E5B09F14 AND action = 'click';").asInstanceOf[UpdateStatement]
    statement.tableName.table shouldBe "UserActions"
    statement.using.isEmpty shouldBe true
    val assignments = statement.set
    assignments.size shouldBe 1

    val assignment1 = assignments(0).asInstanceOf[ListLiteralAssignment]

    assignment1.columnName1 shouldBe "users"
    assignment1.columnName2 shouldBe "users"
    assignment1.listLiteral.right.get shouldBe BindMarker.Anonymous

    val relations = statement.where.relations
    relations.size shouldBe 2

    val simpleRelation1 = relations(0).asInstanceOf[Simple]
    simpleRelation1.columnName shouldBe "user"
    simpleRelation1.operator shouldBe Operator.Equals
    simpleRelation1.term shouldBe UuidConstant(UUID.fromString("B70DE1D0-9908-4AE3-BE34-5573E5B09F14"))

    val simpleRelation2 = relations(1).asInstanceOf[Simple]
    simpleRelation2.columnName shouldBe "action"
    simpleRelation2.operator shouldBe Operator.Equals
    simpleRelation2.term.asInstanceOf[StringConstant].value shouldBe "click"
  }
}
