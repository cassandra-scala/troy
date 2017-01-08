package troy
package cql.parser.dml

import troy.cql.ast.CqlParser._
import troy.cql.ast.UpdateStatement
import troy.cql.ast.dml.Update._

trait UpdateStatementParser {
  def updateStatement: Parser[UpdateStatement] = {
    val updateOperator: Parser[UpdateOperator] = {
      import UpdateOperator._
      def add = "+".i ^^^ Add
      def subtract = "-".i ^^^ Subtract
      add | subtract
    }

    def assignment: Parser[Assignment] = {
      def simpleSelectionAssignment =
        simpleSelection ~ ("=".i ~> term) ^^^^ SimpleSelectionAssignment

      def termAssignment =
        (identifier <~ "=".i) ~ identifier ~ updateOperator ~ term ^^^^ TermAssignment

      def listLiteralAsLeft = listLiteral.map(Left.apply)
      def bindMarkerAsRight = bindMarker.map(Right.apply)
      def listLiteralAssignment =
        (identifier <~ "=".i) ~ (listLiteralAsLeft | bindMarkerAsRight) ~ ("+".i ~> identifier) ^^^^ ListLiteralAssignment

      termAssignment | listLiteralAssignment | simpleSelectionAssignment
    }

    def set = "SET".i ~> rep1sep(assignment, ",")

    "UPDATE".i ~>
      tableName ~
      using ~
      set ~
      where ~
      ifExistsOrCondition.? ^^^^ UpdateStatement.apply
  }
}