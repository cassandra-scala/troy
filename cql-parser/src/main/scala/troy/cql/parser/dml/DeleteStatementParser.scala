package troy
package cql.parser.dml

import troy.cql.ast.CqlParser._
import troy.cql.ast.DeleteStatement

trait DeleteStatementParser {
  def deleteStatement: Parser[DeleteStatement] = {
    def from = "FROM" ~> tableName
    val simpleSelections = repsep(simpleSelection, ",")

    "DELETE".i ~>
      simpleSelections ~
      from ~
      using ~
      where ~
      ifExistsOrCondition.? ^^^^ DeleteStatement.apply
  }
}