package troy
package cql.parser

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast._
import troy.cql.ast.dml.Insert.NamesValues

object ParserTestUtils extends FlatSpec with Matchers {
  def parseSchema(statement: String) =
    handleParseFailure(CqlParser.parseSchema(statement))

  def parseSchemaAs[T](statement: String) =
    parseSchema(statement)
      .head
      .asInstanceOf[T]

  def parseCreateTable(statement: String) =
    parseSchemaAs[CreateTable](statement)

  def schemaError(statement: String) =
    handleUnexpectedParseSuccess(CqlParser.parseSchema(statement))

  def parseQuery(statement: String) =
    handleParseFailure(CqlParser.parseDML(statement))

  def queryError(statement: String) =
    handleUnexpectedParseSuccess(CqlParser.parseDML(statement))

  private[this] def handleParseFailure[T](parsed: => CqlParser.ParseResult[T]): T =
    parsed match {
      case CqlParser.Success(res, _)    => res
      case CqlParser.Failure(msg, next) => fail(s"Parse Failure: $msg, line = ${next.pos.line}, column = ${next.pos.column}")
      case CqlParser.Error(msg, _)      => throw new Exception(msg)
    }

  private[this] def handleUnexpectedParseSuccess(parsed: => CqlParser.ParseResult[_]): String =
    parsed match {
      case CqlParser.Success(res, _)    => fail(s"Parse unexpectedly succeeded with: $res")
      case CqlParser.Failure(msg, next) => msg
      case CqlParser.Error(msg, _)      => throw new Exception(msg)
    }

  def parseSelect(statement: String) =
    parseQuery(statement).asInstanceOf[SelectStatement]

  def parseInsert(statement: String) =
    parseQuery(statement).asInstanceOf[InsertStatement]

  object InsertUtils {
    val parse = parseInsert _

    implicit class InsertStatementHelpers(val statement: InsertStatement) extends AnyVal {
      def values = statement.insertClause.asInstanceOf[NamesValues].values.values
    }
  }
}
