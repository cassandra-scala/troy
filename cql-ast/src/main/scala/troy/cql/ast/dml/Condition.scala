package troy.cql.ast.dml

import troy.cql.ast.Term

trait Condition
case class SimpleCondition(simpleSelection: SimpleSelection, operator: Operator, term: Term) extends Condition
