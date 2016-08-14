/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package troy.cql.ast

import troy.cql.ast.CreateIndex.IndexIdentifier
import troy.cql.ast._
import troy.cql.ast.dml.SelectStatement
import troy.cql.parser.{ Helpers, TermParser }
import troy.cql.parser.dml.SelectStatementParser

import scala.util.parsing.combinator._

// Based on CQLv3.4.3: https://cassandra.apache.org/doc/latest/cql/index.html
object CqlParser extends JavaTokenParsers with Helpers with TermParser with SelectStatementParser {
  def parseSchema(input: String): ParseResult[Seq[DataDefinition]] =
    parse(phrase(rep(dataDefinition <~ semicolon)), input)

  def parseQuery(input: String): ParseResult[SelectStatement] =
    parse(phrase(selectStatement <~ semicolon.?), input)

  ////////////////////////////////////////// Data Definition
  def dataDefinition: Parser[DataDefinition] =
    createKeyspace | createTable | createIndex

  def createKeyspace: Parser[CreateKeyspace] = {
    import CreateKeyspace._
    val mapKey: Parser[String] = "'" ~> identifier <~ "'"
    val mapValue: Parser[String] = "'" ~> identifier <~ "'"
    val mapKeyValue = mapKey ~ (":" ~> mapValue) ^^ { case k ~ v => k -> v }
    val map: Parser[Seq[(String, String)]] = curlyBraces(repsep(mapKeyValue, ","))
    def option: Parser[KeyspaceOption] = ("replication".i ~> "=" ~> map) ^^ Replication
    def withOptions: Parser[Seq[KeyspaceOption]] = ("WITH".i ~> rep1sep(option, "AND".i)) orEmpty

    "CREATE KEYSPACE".i ~>
      ifNotExists ~
      keyspaceName ~
      withOptions ^^^^ CreateKeyspace.apply // TODO: with properties   // <create-keyspace-stmt> ::= CREATE KEYSPACE (IF NOT EXISTS)? <identifier> WITH <properties>
  }

  def use: Parser[UseStatement] = "use".i ~> keyspaceName ^^ UseStatement

  def alterKeyspace: Parser[Cql3Statement] = ??? // <create-keyspace-stmt> ::= ALTER KEYSPACE <identifier> WITH <properties>

  def dropKeyspace: Parser[Cql3Statement] = ??? // <drop-keyspace-stmt> ::= DROP KEYSPACE ( IF EXISTS )? <identifier>

  def createTable: Parser[CreateTable] = {
    import CreateTable._

    def createTable = "create".i ~> ("table".i | "columnfamily".i)

    def columnDefinition: Parser[Column] = identifier ~ dataType ~ "STATIC".flag ~ "PRIMARY KEY".flag ^^^^ Column

    def primaryKeyDefinition: Parser[PrimaryKey] = {
      def simplePartitionKey = identifier.asSeq
      def compositePartitionKey = parenthesis(rep1sep(identifier, ","))
      def partitionKeys: Parser[Seq[String]] = simplePartitionKey | compositePartitionKey
      def clusteringColumns: Parser[Seq[String]] = ("," ~> rep1sep(identifier, ",")) orEmpty

      "PRIMARY KEY".i ~> parenthesis(partitionKeys ~ clusteringColumns) ^^^^ PrimaryKey
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

  def createIndex: Parser[CreateIndex] = {
    import CreateIndex._

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

  ///////////////////////////////////// Queries

  ///////////////////////////////////// Data Manipulation
  def dataChangeStatement: Parser[Cql3Statement] =
    insertStatement | updateStatement | batchStatement | deleteStatement | truncateStatement

  def insertStatement: Parser[Cql3Statement] = ???

  def updateStatement: Parser[Cql3Statement] = ???

  def batchStatement: Parser[Cql3Statement] = ???

  def deleteStatement: Parser[Cql3Statement] = ???

  def truncateStatement: Parser[Cql3Statement] = ???

  //  def schemaChangeStatement: Parser[Cql3Statement] =
  //    createKeyspaceStatement | createColumnFamilyStatement | createIndexStatement | dropKeyspaceStatement |
  //      dropColumnFamilyStatement | dropIndexStatement | alterTableStatement

  def createColumnFamilyStatement: Parser[Cql3Statement] = ???

  def createIndexStatement: Parser[Cql3Statement] = ???

  def dropKeyspaceStatement: Parser[Cql3Statement] = ???

  def dropColumnFamilyStatement: Parser[Cql3Statement] = ???

  def dropIndexStatement: Parser[Cql3Statement] = ???

  def alterTableStatement: Parser[Cql3Statement] = ???

  def usingConsistencyLevelClause: Parser[ConsistencyLevel] = {
    def consistencyLevel = any | one | quorum | all | localQuorum | eachQuorum
    def any = "quorum".i ^^^ ConsistencyLevel.Any
    def one = "quorum".i ^^^ ConsistencyLevel.One
    def quorum = "quorum".i ^^^ ConsistencyLevel.Quorum
    def all = "quorum".i ^^^ ConsistencyLevel.All
    def localQuorum = "quorum".i ^^^ ConsistencyLevel.LocalQuorum
    def eachQuorum = "quorum".i ^^^ ConsistencyLevel.EachQuorum

    "using".i ~> "consistency".i ~> consistencyLevel
  }

  def semicolon: Parser[Unit] = ";".? ^^^ ((): Unit)

  /*
   * <identifier> ::= any quoted or unquoted identifier, excluding reserved keywords
   */
  def identifier: Parser[Identifier] = "[a-zA-Z0-9_]+".r.filter(k => !Keywords.contains(k.toUpperCase))

  object Constants {
    def string = "'".r ~> """[^']*""".r <~ "'"

    def integer = wholeNumber

    def float = floatingPointNumber

    def number = integer | float

    def uuid = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

    def boolean = "true".i | "false".i
  }

  def keyspaceName: Parser[KeyspaceName] = identifier ^^ KeyspaceName

  def positiveNumber = """([1-9]+)""".r

  /*
   * <tablename> ::= (<identifier> '.')? <identifier>
   */
  def tableName: Parser[TableName] = (keyspaceName <~ ".").? ~ identifier ^^^^ TableName

  def ifNotExists: Parser[Boolean] = "if not exists".flag

  def dataType: Parser[DataType] = {
    def ascii = "ascii".i ^^^ DataType.ascii
    def bigint = "bigint".i ^^^ DataType.bigint
    def blob = "blob".i ^^^ DataType.blob
    def boolean = "boolean".i ^^^ DataType.boolean
    def counter = "counter".i ^^^ DataType.counter
    def date = "date".i ^^^ DataType.date
    def decimal = "decimal".i ^^^ DataType.decimal
    def double = "double".i ^^^ DataType.double
    def float = "float".i ^^^ DataType.float
    def inet = "inet".i ^^^ DataType.inet
    def int = "int".i ^^^ DataType.int
    def smallint = "smallint".i ^^^ DataType.smallint
    def text = "text".i ^^^ DataType.text
    def times = "times".i ^^^ DataType.times
    def timestamp = "timestamp".i ^^^ DataType.timestamp
    def timeuuid = "timeuuid".i ^^^ DataType.timeuuid
    def tinyint = "tinyint".i ^^^ DataType.tinyint
    def uuid = "uuid".i ^^^ DataType.uuid
    def varchar = "varchar".i ^^^ DataType.varchar
    def varint = "varint".i ^^^ DataType.varint
    def native: Parser[DataType.Native] =
      ascii | bigint | blob | boolean | counter | date | decimal | double | float | inet | int | smallint | text | times | timestamp | timeuuid | tinyint | uuid | varchar | varint

    def list = "list".i ~> '<' ~> native <~ '>' ^^ DataType.list
    def set = "set".i ~> '<' ~> native <~ '>' ^^ DataType.set
    def map = "map".i ~> '<' ~> native ~ (',' ~> native) <~ '>' ^^ {
      case k ~ v => DataType.map(k, v)
    }
    def collection: Parser[DataType.Collection] = list | set | map

    def tuple: Parser[DataType.Tuple] = "tuple".i ~> '<' ~> rep1sep(native, ",") <~ '>' ^^ DataType.Tuple

    native | collection | tuple // | custom // TODO
  }

  implicit class MyRichString(val str: String) extends AnyVal {
    // Ignore case
    def i: Parser[String] = ("""(?i)\Q""" + str + """\E""").r

    def flag: Parser[Boolean] = (str.i ^^^ true) orElse false
  }

  val Keywords = Set(
    "ADD",
    "ALLOW",
    "ALTER",
    "AND",
    "APPLY",
    "ASC",
    "AUTHORIZE",
    "BATCH",
    "BEGIN",
    "BY",
    "COLUMNFAMILY",
    "CREATE",
    "DELETE",
    "DESC",
    "DESCRIBE",
    "DROP",
    "ENTRIES",
    "EXECUTE",
    "FROM",
    "FULL",
    "GRANT",
    "IF",
    "IN",
    "INDEX",
    "INFINITY",
    "INSERT",
    "INTO",
    "KEYSPACE",
    "LIMIT",
    "MODIFY",
    "NAN",
    "NORECURSIVE",
    "NOT",
    "NULL",
    "OF",
    "ON",
    "OR",
    "ORDER",
    "PRIMARY",
    "RENAME",
    "REPLACE",
    "REVOKE",
    "SCHEMA",
    "SELECT",
    "SET",
    "TABLE",
    "TO",
    //    "TOKEN",
    "TRUNCATE",
    "UNLOGGED",
    "UPDATE",
    "USE",
    "USING",
    "WHERE",
    "WITH"
  )
}

