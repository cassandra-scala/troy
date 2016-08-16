package troy.cql.parser.dml
import troy.cql.ast.CqlParser._
import troy.cql.ast.dml.{ Operator, SelectStatement, WhereClause }
import troy.cql.ast.dml.SelectStatement.OrderBy.{ Direction, Ordering }
import troy.cql.ast.dml.WhereClause.Relation
import troy.cql.ast.dml.SelectStatement.{ OrderBy, _ }

trait SelectStatementParser {
  def selectStatement: Parser[SelectStatement] = {
    import SelectStatement._

    def mod: Parser[Mod] = {
      def json = "JSON".i ^^^ SelectStatement.Json
      def distinct = "DISTINCT".i ^^^ SelectStatement.Distinct
      json | distinct
    }
    def select: Parser[Selection] = {
      def asterisk = "*" ^^^ Asterisk
      def select_clause: Parser[SelectClause] = {
        def select_clause_item: Parser[SelectionClauseItem] = {
          def selector: Parser[Selector] = {
            def count = "COUNT".i ~ "(*)" ^^^ SelectStatement.Count
            def term_selector = term ^^ SelectStatement.SelectTerm
            def cast = "CAST".i ~> parenthesis(selector ~ ("AS".i ~> dataType)) ^^^^ SelectStatement.Cast
            def column_name = identifier ^^ ColumnName
            term_selector | cast | count | column_name
          }

          selector ~ ("AS".i ~> identifier).? ^^^^ SelectStatement.SelectionClauseItem
        }

        rep1sep(select_clause_item, ",") ^^ SelectClause
      }

      select_clause | asterisk
    }

    def from = "FROM" ~> tableName

    def limitParam: Parser[SelectStatement.LimitParam] = {
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
      rep1sep(ordering, ",") ^^ OrderBy.apply
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
