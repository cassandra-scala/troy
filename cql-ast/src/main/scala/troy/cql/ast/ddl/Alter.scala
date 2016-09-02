package troy.cql.ast.ddl

import troy.cql.ast.{ DataType, OptionInstruction }
import troy.cql.ast.Identifier

object Alter {
  sealed trait AlterTableInstruction

  final case class Type(columnName: Identifier, cqlType: DataType) extends AlterTableInstruction

  final case class Add(instructions: Seq[AddInstruction]) extends AlterTableInstruction
  final case class AddInstruction(columnName: Identifier, cqlType: DataType)

  final case class Drop(columnName: Seq[Identifier]) extends AlterTableInstruction
  final case class With(options: Seq[OptionInstruction]) extends AlterTableInstruction

}
