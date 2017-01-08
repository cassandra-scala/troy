package troy
package cql.parser.dml
import troy.cql.ast.CqlParser._
import troy.cql.ast.SelectStatement
import troy.cql.ast.dml.Select

trait SelectStatementParser {
  def selectStatement: Parser[SelectStatement] = {
    import Select._

    def mod: Parser[Mod] = {
      def json = "JSON".i ^^^ Select.Json
      def distinct = "DISTINCT".i ^^^ Select.Distinct
      json | distinct
    }
    def select: Parser[Selection] = {
      def asterisk = "*" ^^^ Asterisk
      def select_clause: Parser[SelectClause] = {
        def select_clause_item: Parser[SelectionClauseItem] = {
          def selector: Parser[Selector] = {
            def count = "COUNT".i ~ "(*)" ^^^ Select
              .Count
            def term_selector = term ^^ Select
              .SelectTerm
            def cast = "CAST".i ~> parenthesis(selector ~ ("AS".i ~> dataType)) ^^^^ Select
              .Cast
            def column_name = identifier ^^ ColumnName
            def function = functionName ~ parenthesis(repsep(selector, ",")) ^^^^ Function

            cast | function | term_selector | count | column_name
          }

          selector ~ ("AS".i ~> identifier).? ^^^^ Select
            .SelectionClauseItem
        }

        rep1sep(select_clause_item, ",") ^^ SelectClause
      }

      select_clause | asterisk
    }

    def from = "FROM".i ~> tableName

    def limitParam: Parser[Select.LimitParam] = {
      def limitValue = Constants.integer ^^ LimitValue
      def limitVariable = bindMarker ^^ LimitVariable

      limitValue | limitVariable
    }

    def limit = "LIMIT".i ~> limitParam
    def perPartitionLimit = "PER PARTITION LIMIT".i ~> limitParam

    def allowFiltering = "ALLOW FILTERING".flag

    def orderBy: Parser[OrderBy] = {
      import OrderBy._
      def direction: Parser[Direction] = {
        def asc = "ASC".i ^^^ Ascending
        def des = "DESC".i ^^^ Descending

        asc | des
      }

      def ordering: Parser[Ordering] = {
        def column_name = identifier ^^ ColumnName
        column_name ~ direction.? ^^^^ Ordering
      }
      "ORDER BY".i ~> rep1sep(ordering, ",") ^^ OrderBy.apply
    }

    "SELECT".i ~>
      mod.? ~
      select ~
      from ~
      where.? ~
      orderBy.? ~
      perPartitionLimit.? ~
      limit.? ~
      allowFiltering ^^^^ SelectStatement.apply
  }
}
