package troy
package cql.ast.dml

import troy.cql.ast.{ BindMarker, DataType, Term, _ }

object Select {
  sealed trait Mod
  case object Json extends Mod
  case object Distinct extends Mod

  sealed trait Selection
  case object Asterisk extends Selection
  final case class SelectClause(items: Seq[SelectionClauseItem]) extends Selection
  final case class SelectionClauseItem(selector: Selector, as: Option[Identifier])

  sealed trait Selector
  final case class ColumnName(name: Identifier) extends Selector
  final case class SelectTerm(term: Term) extends Selector
  final case class Cast(selector: Selector, as: DataType) extends Selector
  final case class Function(functionName: FunctionName, params: Seq[Selector]) extends Selector // Non empty
  case object Count extends Selector

  sealed trait LimitParam
  final case class LimitValue(value: String) extends LimitParam
  final case class LimitVariable(bindMarker: BindMarker) extends LimitParam

  final case class OrderBy(orderings: Seq[OrderBy.Ordering])
  object OrderBy {
    trait Direction
    case object Ascending extends Direction
    case object Descending extends Direction

    final case class Ordering(columnName: ColumnName, direction: Option[Direction])
  }
}