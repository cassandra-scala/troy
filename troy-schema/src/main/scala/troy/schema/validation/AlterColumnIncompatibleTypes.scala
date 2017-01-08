package troy
package schema.validation

import troy.cql.ast._
import troy.cql.ast.ddl.Alter.AlterType
import troy.schema.{ V, Messages, Schema }

class AlterColumnIncompatibleTypes(schema: Schema) extends Validation[DataDefinition] {
  import DataType._
  override def rules = {
    case AlterTable(tableName, AlterType(columnName, newCqlType)) =>
      schema.getColumns(tableName, Seq(columnName)).map(_.head.dataType).flatMap { oldCqlType =>
        if (isCompatible(oldCqlType, newCqlType))
          noMessages
        else
          V.error(Messages.IncompatibleAlterType(columnName, oldCqlType, newCqlType))
      }
  }

  def isCompatible(oldDT: DataType, newDT: DataType) = oldDT -> newDT match {
    case (_, Blob) => true
    // TODO
    case _         => oldDT == newDT
  }
}
