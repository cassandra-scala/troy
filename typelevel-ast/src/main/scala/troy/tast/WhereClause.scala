package troy.tast

object WhereClause {
  type Relations = THList[Relation]
  sealed trait Relation
  object Relation {
    sealed trait Simple[columnName <: Identifier, operator <: Operator, term <: Term] extends Relation
    sealed trait Tupled[columnNames <: THList[Identifier], operator <: Operator, term <: TupleLiteral[_]] extends Relation
    sealed trait Token[columnNames <: THList[Identifier], operator <: Operator, term <: Term] extends Relation
  }
}

sealed trait Operator
object Operator {
  sealed trait Equality extends Operator
  sealed trait Equals extends Equality
  sealed trait LessThan extends Equality
  sealed trait GreaterThan extends Equality
  sealed trait LessThanOrEqual extends Equality
  sealed trait GreaterThanOrEqual extends Equality
  sealed trait NotEquals extends Equality

  sealed trait In extends Operator
  sealed trait Contains extends Operator
  sealed trait ContainsKey extends Operator
  sealed trait Like extends Operator
}