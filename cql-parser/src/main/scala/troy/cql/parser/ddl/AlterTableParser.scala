package troy.cql.parser.ddl

import troy.cql.ast.AlterTable
import troy.cql.ast.CqlParser._
import troy.cql.ast.ddl.Alter._

trait AlterTableParser {
  def alterTableStatement: Parser[AlterTable] = {

    def alterTableInstruction: Parser[AlterTableInstruction] = {
      def typeAlterInst = {
        val cqlType = "TYPE".i ~> dataType
        "ALTER".i ~> identifier ~ cqlType ^^^^ Type
      }

      def addAlterInst = {
        def addInstruction = identifier ~ dataType ^^^^ AddInstruction
        "ADD".i ~> rep1sep(addInstruction, ",") ^^ Add
      }

      def dropAlterInst = "DROP".i ~> rep1sep(identifier, "") ^^ Drop
      def withAlterInst = "WITH".i ~> rep1sep(optionInstruction, "AND") ^^ With

      typeAlterInst | addAlterInst | dropAlterInst | withAlterInst
    }

    "ALTER TABLE".i ~>
      tableName ~
      alterTableInstruction ^^^^ AlterTable.apply

  }

}
