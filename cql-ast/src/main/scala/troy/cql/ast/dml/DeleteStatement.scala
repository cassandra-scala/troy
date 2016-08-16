package troy.cql.ast.dml

import troy.cql.ast.{ DataManipulation, TableName }

case class DeleteStatement(
  simpleSelection: Seq[SimpleSelection],
  from: TableName,
  using: Option[Seq[UpdateParam]],
  where: Option[WhereClause],
  ifCondition: Option[Condition]
) extends DataManipulation
object DeleteStatement {
  case object Exist extends Condition
}
