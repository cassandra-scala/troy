package troy.cql.ast.dml

import troy.cql.ast.{ DataManipulation, TableName }

case class DeleteStatement(
  simpleSelection: Option[Seq[SimpleSelection]],
  from: TableName,
  using: Option[Seq[UpdateParam]],
  where: WhereClause,
  ifCondition: Option[DeleteStatement.IfCondition]
) extends DataManipulation
object DeleteStatement {
  sealed trait IfCondition
  case class SimpleIfCondition(conditions: Seq[Condition]) extends IfCondition
  case class Exist(value: Boolean) extends IfCondition
}
