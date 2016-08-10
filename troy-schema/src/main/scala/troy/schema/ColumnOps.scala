package troy.schema

import troy.cql.ast.{ CreateTable, DataType }
import troy.cql.ast.SelectStatement.WhereClause.Operator

object ColumnOps {
  implicit class Operations(val column: CreateTable.Column) extends AnyVal {
    import Operator._
    import DataType._

    def operandType(operator: Operator): Option[DataType] = operator match {
      case Equals | LessThan | GreaterThan | LessThanOrEqual | GreaterThanOrEqual =>
        Some(column.dataType).collect {
          case n: Native => n
        }
      case Contains =>
        Some(column.dataType).collect {
          case list(t)   => t
          case set(t)    => t
          case map(_, v) => v
        }
      case ContainsKey =>
        Some(column.dataType).collect {
          case map(k, _) => k
        }
    }
  }
}

