package troy.tast

import troy.tutils.TOption

object Select {
  sealed trait Mod
  sealed trait Json extends Mod
  sealed trait Distinct extends Mod

  sealed trait Selection
  sealed trait Asterisk extends Selection
  sealed trait SelectClause[Items <: THList[SelectionClauseItem[_, _]]] extends Selection
  sealed trait SelectionClauseItem[S <: Selector, As <: TOption[Identifier]]

  sealed trait Selector
  sealed trait ColumnName[Name <: Identifier] extends Selector
  sealed trait SelectTerm[T <: Term] extends Selector
  sealed trait Cast[S <: Selector, As <: DataType] extends Selector
  sealed trait Function[F <: FunctionName[_, _], Params <: THList[Selector]] extends Selector // Non empty
  sealed trait Count extends Selector

  sealed trait LimitParam
  sealed trait LimitValue[Value <: String] extends LimitParam
  sealed trait LimitVariable[B <: BindMarker] extends LimitParam

  sealed trait OrderBy[Orderings <: THList[OrderBy.Ordering[_, _]]]
  object OrderBy {
    trait Direction
    sealed trait Ascending extends Direction
    sealed trait Descending extends Direction

    sealed trait Ordering[C <: ColumnName[_], D <: TOption[Direction]]
  }
}