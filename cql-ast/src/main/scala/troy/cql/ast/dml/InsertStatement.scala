package troy.cql.ast.dml

import troy.cql.ast._

case class InsertStatement(
    tableName: TableName,
    insertClause: InsertStatement.InsertClause,
    ifNotExists: Boolean,
    using: Seq[InsertStatement.UpdateParam]
) extends Cql3Statement
object InsertStatement {
  sealed trait InsertClause
  case class NamesValues(columnNames: Seq[Identifier], tupleLiteral: TupleLiteral) extends InsertClause
  case class JsonClause(value: String, default: Option[Default]) extends InsertClause

  sealed trait Default
  case object Null extends Default
  case object UNSET extends Default

  sealed trait UpdateParam
  case class UpdateValue(value: String) extends UpdateParam
  case class UpdateVariable(bindMarker: BindMarker) extends UpdateParam
}