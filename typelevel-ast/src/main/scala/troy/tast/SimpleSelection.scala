package troy.tast

sealed trait SimpleSelection
object SimpleSelection {
  sealed trait ColumnName[columnName <: Identifier] extends SimpleSelection
  sealed trait ColumnNameOf[columnName <: Identifier, term <: Term] extends SimpleSelection
  sealed trait ColumnNameDot[columnName <: Identifier, fieldName <: String] extends SimpleSelection
}
