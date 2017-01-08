package troy
package cql.parser.ddl

import troy.cql.ast.CqlParser._
import troy.cql.ast.CreateIndex
import troy.cql.ast.ddl.Index._

trait CreateIndexParser {
  def createIndex: Parser[CreateIndex] = {
    def indexName = identifier.?
    def onTable = "ON".i ~> tableName
    def indexIdentifier: Parser[IndexIdentifier] = {
      val keys = "KEYS".i ~> parenthesis(identifier) ^^ Keys
      val ident = identifier ^^ Identifier
      parenthesis(((keys | ident)))
    }
    def using = {
      def withOptions =
        "WITH".i ~> "OPTIONS".i ~> "=" ~> mapLiteral

      "using".i ~> Constants.string ~ withOptions.? ^^^^ Using
    }.?

    "CREATE".i ~>
      ("CUSTOM".flag <~ "INDEX".i) ~
      ifNotExists ~
      indexName ~
      onTable ~
      indexIdentifier ~
      using ^^^^ CreateIndex.apply
  }
}
