package troy.tast

sealed trait Condition[simpleSelection <: SimpleSelection, operator <: Operator, term <: Term]

// Not be confused with "IF NOT EXISTS"
sealed trait IfExistsOrCondition
sealed trait IfCondition[conditions <: THList[Condition[_, _, _]]] extends IfExistsOrCondition
sealed trait IfExist extends IfExistsOrCondition