package troy.tast

object Update {
  sealed trait UpdateOperator
  object UpdateOperator {
    sealed trait Add extends UpdateOperator
    sealed trait Subtract extends UpdateOperator
  }

  sealed trait Assignment
  sealed trait SimpleSelectionAssignment[selection <: SimpleSelection, term <: Term] extends Assignment
  sealed trait TermAssignment[columnName1 <: Identifier, columnName2 <: Identifier, updateOperator <: UpdateOperator, term <: Term] extends Assignment
  sealed trait ListLiteralAssignment[columnName1 <: Identifier, listLiteral <: Either[ListLiteral[_], BindMarker], columnName2 <: Identifier] extends Assignment
}
