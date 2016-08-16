package troy.cql.ast.dml

import troy.cql.ast._

case class InsertStatement(
  into: TableName,
  insertClause: InsertStatement.InsertClause,
  ifNotExists: Boolean,
  using: Option[Seq[UpdateParam]]
) extends DataManipulation
object InsertStatement {
  sealed trait InsertClause
  case class NamesValues(columnNames: Seq[Identifier], values: TupleLiteral) extends InsertClause
  case class JsonClause(value: String, default: Option[Default]) extends InsertClause

  sealed trait Default
  case object NullValue extends Default
  case object Unset extends Default
}