package troy.cql.parser.dml

import troy.cql.ast.CqlParser._
import troy.cql.ast.dml.InsertStatement

trait InsertStatementParser {
  def insertStatement: Parser[InsertStatement] = {
    import InsertStatement._

    def into = "INTO" ~> tableName

    def insertClause: Parser[InsertClause] = {
      def names = parenthesis(rep1sep(identifier, ","))

      def namesValues: Parser[NamesValues] = names ~ ("VALUES".i ~> tupleLiteral) ^^^^ NamesValues

      def jsonClause: Parser[JsonClause] = {
        def default: Parser[Default] = {
          def nullValue = "NULL".i ^^^ NullValue
          def unset = "UNSET".i ^^^ Unset

          nullValue | unset
        }

        "JSON" ~> Constants.string ~ ("DEFAULT".i ~> default).? ^^^^ JsonClause
      }

      namesValues | jsonClause
    }

    def ifNotExists = "IF NOT EXISTS".flag

    "INSERT".i ~>
      into ~
      insertClause ~
      ifNotExists ~
      using.? ^^^^ InsertStatement.apply
  }
}
