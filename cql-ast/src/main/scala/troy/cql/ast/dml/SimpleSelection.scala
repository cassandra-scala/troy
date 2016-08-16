package troy.cql.ast.dml

import troy.cql.ast.{ Term, _ }

sealed trait SimpleSelection
case class ColumnNameSelection(columnName: Identifier) extends SimpleSelection
case class ColumnNameSelectionWithTerm(columnName: Identifier, term: Term) extends SimpleSelection
case class ColumnNameSelectionWithFieldName(columnName: Identifier, fieldName: String) extends SimpleSelection
