package troy.cql.ast.dml

import troy.cql.ast.{ BindMarker, DataType, Term, TupleLiteral, _ }

case class SelectStatement(
  mod: Option[SelectStatement.Mod],
  selection: SelectStatement.Selection,
  from: TableName,
  where: Option[SelectStatement.WhereClause],
  orderBy: Option[SelectStatement.OrderBy],
  perPartitionLimit: Option[SelectStatement.LimitParam],
  limit: Option[SelectStatement.LimitParam],
  allowFiltering: Boolean
) extends Cql3Statement
object SelectStatement {
  sealed trait Mod
  case object Json extends Mod
  case object Distinct extends Mod

  sealed trait Selection
  case object Asterisk extends Selection
  case class SelectClause(items: Seq[SelectionClauseItem]) extends Selection
  case class SelectionClauseItem(selector: Selector, as: Option[Identifier])

  sealed trait Selector
  case class ColumnName(name: Identifier) extends Selector
  case class SelectTerm(term: Term) extends Selector
  case class Cast(selector: Selector, as: DataType) extends Selector
  case class Function(functionName: FunctionName, params: Seq[Selector]) extends Selector // Non empty
  case object Count extends Selector

  case class WhereClause(relations: Seq[WhereClause.Relation])
  object WhereClause {

    trait Operator
    object Operator {
      case object Equals extends Operator
      case object LessThan extends Operator
      case object GreaterThan extends Operator
      case object LessThanOrEqual extends Operator
      case object GreaterThanOrEqual extends Operator
      case object NotEquals extends Operator
      case object In extends Operator
      case object Contains extends Operator
      case object ContainsKey extends Operator
    }

    trait Relation
    object Relation {
      case class Simple(columnName: ColumnName, operator: Operator, term: Term) extends Relation
      case class Tupled(columnNames: Seq[ColumnName], operator: Operator, term: TupleLiteral) extends Relation
      case class Token(columnNames: Seq[ColumnName], operator: Operator, term: Term) extends Relation
    }

  }

  sealed trait LimitParam
  case class LimitValue(value: String) extends LimitParam
  case class LimitVariable(bindMarker: BindMarker) extends LimitParam

  case class OrderBy(orderings: Seq[OrderBy.Ordering])
  object OrderBy {
    trait Direction
    case object Ascending extends Direction
    case object Descending extends Direction

    case class Ordering(columnName: ColumnName, direction: Option[Direction])
  }
}