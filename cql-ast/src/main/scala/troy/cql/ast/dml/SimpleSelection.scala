package troy
package cql.ast.dml

import troy.cql.ast.{ Term, _ }

sealed trait SimpleSelection
object SimpleSelection {
  final case class ColumnName(columnName: Identifier) extends SimpleSelection
  final case class ColumnNameOf(columnName: Identifier, term: Term) extends SimpleSelection
  final case class ColumnNameDot(columnName: Identifier, fieldName: String) extends SimpleSelection
}
