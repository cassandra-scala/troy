package troy
package cql.parser.ddl

import troy.cql.ast.CqlParser._
import troy.cql.ast.ddl.Table._
import troy.cql.ast.CreateTable

trait CreateTableParser {
  def createTable: Parser[CreateTable] = {
    def createTable = "create".i ~> ("table".i | "columnfamily".i)

    def columnDefinition: Parser[Column] = identifier ~ dataType ~ staticFlag ~ "PRIMARY KEY".flag ^^^^ Column

    def primaryKeyDefinition: Parser[PrimaryKey] = {
      def simplePartitionKey = identifier.asSeq
      def compositePartitionKey = parenthesis(rep1sep(identifier, ","))
      def partitionKeys: Parser[Seq[String]] = simplePartitionKey | compositePartitionKey
      def clusteringColumns: Parser[Seq[String]] = ("," ~> rep1sep(identifier, ",")) orEmpty

      "PRIMARY KEY".i ~> parenthesis(partitionKeys ~ clusteringColumns) ^^^^ PrimaryKey.apply
    }

    def option: Parser[CreateTableOption] = ??? // <property> | COMPACT STORAGE | CLUSTERING ORDER
    def withOptions: Parser[Seq[CreateTableOption]] = ("WITH".i ~> rep1sep(option, "AND".i)) orEmpty

    createTable ~>
      ifNotExists ~
      tableName ~
      ("(" ~> rep1sep(columnDefinition, ",")) ~
      ("," ~> primaryKeyDefinition).? ~
      (")" ~> withOptions) ^^^^ CreateTable.apply
  }

}
