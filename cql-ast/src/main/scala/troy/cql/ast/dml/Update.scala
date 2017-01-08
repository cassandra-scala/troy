package troy
package cql.ast.dml

import troy.cql.ast.{ BindMarker, Identifier, ListLiteral, Term }

object Update {
  sealed trait UpdateOperator
  object UpdateOperator {
    case object Add extends UpdateOperator
    case object Subtract extends UpdateOperator
  }

  sealed trait Assignment
  final case class SimpleSelectionAssignment(selection: SimpleSelection, term: Term) extends Assignment
  final case class TermAssignment(columnName1: Identifier, columnName2: Identifier, updateOperator: UpdateOperator, term: Term) extends Assignment
  final case class ListLiteralAssignment(columnName1: Identifier, listLiteral: Either[ListLiteral, BindMarker], columnName2: Identifier) extends Assignment
}
