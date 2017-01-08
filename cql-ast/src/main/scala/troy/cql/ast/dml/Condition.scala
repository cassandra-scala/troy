package troy
package cql.ast.dml

import troy.cql.ast.Term

final case class Condition(simpleSelection: SimpleSelection, operator: Operator, term: Term)

// Not be confused with "IF NOT EXISTS"
sealed trait IfExistsOrCondition
final case class IfCondition(conditions: Seq[Condition]) extends IfExistsOrCondition
final case object IfExist extends IfExistsOrCondition