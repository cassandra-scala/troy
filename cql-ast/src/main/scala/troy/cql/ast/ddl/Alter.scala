package troy
package cql.ast.ddl

import troy.cql.ast.{ DataType, OptionInstruction }
import troy.cql.ast.Identifier

object Alter {
  sealed trait AlterTableInstruction
  final case class AlterType(columnName: Identifier, cqlType: DataType) extends AlterTableInstruction
  final case class AddColumns(instructions: Seq[AddInstruction]) extends AlterTableInstruction
  final case class DropColumn(columnName: Identifier) extends AlterTableInstruction
  final case class With(options: Seq[OptionInstruction]) extends AlterTableInstruction

  final case class AddInstruction(columnName: Identifier, cqlType: DataType, isStatic: Boolean)
}
