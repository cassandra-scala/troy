package troy.cql.parser.dml

import troy.cql.ast.CqlParser._
import troy.cql.ast.dml.DeleteStatement

trait DeleteStatementParser {
  def deleteStatement: Parser[DeleteStatement] = {
    import DeleteStatement._
    def from = "FROM" ~> tableName

    def ifCondition: Parser[IfCondition] = {
      def exist = "IF EXISTS".flag ^^ Exist
      def simpleIfCondition = "IF".i ~> rep1sep(condition, "AND".i) ^^ SimpleIfCondition

      simpleIfCondition | exist
    }
    val simpleSelections = rep1sep(simpleSelection, ",")

    "DELETE".i ~>
      simpleSelections.? ~
      from ~
      using.? ~
      where ~
      ifCondition.? ^^^^ DeleteStatement.apply
  }
}