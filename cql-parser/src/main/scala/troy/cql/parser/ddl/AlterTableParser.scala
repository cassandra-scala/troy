package troy
package cql.parser.ddl

import troy.cql.ast.AlterTable
import troy.cql.ast.CqlParser._
import troy.cql.ast.ddl.Alter._

trait AlterTableParser {
  def alterTableStatement: Parser[AlterTable] = {

    def alterTableInstruction: Parser[AlterTableInstruction] = {
      def typeAlterInst = {
        val cqlType = "TYPE".i ~> dataType
        "ALTER".i ~> identifier ~ cqlType ^^^^ AlterType
      }

      def addAlterInst = {
        def addInstruction = identifier ~ dataType ~ staticFlag ^^^^ AddInstruction
        "ADD".i ~> rep1sep(addInstruction, ",") ^^ AddColumns
      }

      def dropAlterInst = "DROP".i ~> identifier ^^ DropColumn // TODO: Check status of https://issues.apache.org/jira/browse/CASSANDRA-12603
      def withAlterInst = "WITH".i ~> rep1sep(optionInstruction, "AND") ^^ With

      typeAlterInst | addAlterInst | dropAlterInst | withAlterInst
    }

    "ALTER TABLE".i ~>
      tableName ~
      alterTableInstruction ^^^^ AlterTable.apply

  }

}
