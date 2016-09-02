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

import troy.cql.ast.dml._
import troy.cql.ast.dml.{ UpdateParam, UpdateParamValue, UpdateVariable }
import troy.cql.parser.{ Helpers, TermParser }
import troy.cql.parser.dml.{ DeleteStatementParser, InsertStatementParser, SelectStatementParser, UpdateStatementParser }
import troy.cql.parser.ddl.{ AlterTableParser, CreateIndexParser, CreateKeyspaceParser, CreateTableParser }

import scala.util.parsing.combinator._

// Based on CQLv3.4.3: https://cassandra.apache.org/doc/latest/cql/index.html
object CqlParser extends JavaTokenParsers
    with Helpers with TermParser
    with CreateKeyspaceParser with CreateTableParser with CreateIndexParser with AlterTableParser
    with SelectStatementParser with InsertStatementParser with DeleteStatementParser with UpdateStatementParser {
  def parseSchema(input: String): ParseResult[Seq[DataDefinition]] =
    parse(phrase(rep(dataDefinition <~ semicolon)), input)

  def parseQuery(input: String): ParseResult[SelectStatement] =
    parse(phrase(selectStatement <~ semicolon.?), input)

  def parseDML(input: String): ParseResult[DataManipulation] =
    parse(phrase(dmlDefinition <~ semicolon.?), input)

  ////////////////////////////////////////// Data Definition
  def dataDefinition: Parser[DataDefinition] =
    createKeyspace | createTable | createIndex | alterTableStatement

  def alterKeyspace: Parser[Cql3Statement] = ??? // <create-keyspace-stmt> ::= ALTER KEYSPACE <identifier> WITH <properties>

  def dropKeyspace: Parser[Cql3Statement] = ??? // <drop-keyspace-stmt> ::= DROP KEYSPACE ( IF EXISTS )? <identifier>

  ///////////////////////////////////// Data Manipulation
  def dmlDefinition: Parser[DataManipulation] =
    selectStatement | insertStatement | deleteStatement | updateStatement // batchStatement | truncateStatement

  def batchStatement: Parser[Cql3Statement] = ???

  def truncateStatement: Parser[Cql3Statement] = ???

  //  def schemaChangeStatement: Parser[Cql3Statement] =
  //    createKeyspaceStatement | createColumnFamilyStatement | createIndexStatement | dropKeyspaceStatement |
  //      dropColumnFamilyStatement | dropIndexStatement | alterTableStatement

  def createColumnFamilyStatement: Parser[Cql3Statement] = ???

  def createIndexStatement: Parser[Cql3Statement] = ???

  def dropKeyspaceStatement: Parser[Cql3Statement] = ???

  def dropColumnFamilyStatement: Parser[Cql3Statement] = ???

  def dropIndexStatement: Parser[Cql3Statement] = ???

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
  def constant: Parser[Constant] = {
    import Constants._
    (string | number | uuid | boolean) ^^ Constant // | hex // TODO
  }
  def identifier: Parser[Identifier] = "[a-zA-Z0-9_]+".r.filter(k => !Keywords.contains(k.toUpperCase))

  def optionInstruction: Parser[OptionInstruction] = {
    def identifierOption = identifier ~ ("=".i ~> identifier) ^^^^ IdentifierOption

    def constantOption = identifier ~ ("=".i ~> constant) ^^^^ ConstantOption
    def mapLiteralOption = identifier ~ ("=".i ~> mapLiteral) ^^^^ MapLiteralOption

    constantOption | mapLiteralOption | identifierOption
  }

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

  def UpdateParamValue: Parser[UpdateParamValue] = {
    def updateValue = Constants.integer ^^ UpdateValue
    def updateVariable = bindMarker ^^ UpdateVariable

    updateValue | updateVariable
  }

  def updateParam: Parser[UpdateParam] = {
    def timestamp = "TIMESTAMP".i ~> UpdateParamValue ^^ Timestamp
    def ttl = "TTL".i ~> UpdateParamValue ^^ Ttl

    timestamp | ttl
  }

  def using = getOrElse("USING".i ~> rep1sep(updateParam, "AND".i), Nil)

  def simpleSelection: Parser[SimpleSelection] = {
    import SimpleSelection._
    def columnNameSelection = identifier ^^ ColumnName
    def columnNameSelectionWithTerm = identifier ~ squareBrackets(term) ^^^^ ColumnNameOf
    def columnNameSelectionWithFieldName = (identifier <~ ".") ~ "[a-zA-Z0-9_]+".r ^^^^ ColumnNameDot

    columnNameSelectionWithTerm | columnNameSelectionWithFieldName | columnNameSelection
  }

  def ifExistsOrCondition: Parser[IfExistsOrCondition] = {
    def exist = "IF EXISTS".r ^^^ IfExist
    def ifCondition = "IF".i ~> rep1sep(condition, "AND".i) ^^ IfCondition

    ifCondition | exist
  }

  def operator: Parser[Operator] = {
    import Operator._
    def eq = "=".r ^^^ Equals
    def lt = "<".r ^^^ LessThan
    def gt = ">".r ^^^ GreaterThan
    def lte = "<=".r ^^^ LessThanOrEqual
    def gte = ">=".r ^^^ GreaterThanOrEqual
    def noteq = "!=".r ^^^ NotEquals
    def in = "IN".r ^^^ In
    def contains = "CONTAINS".i ^^^ Contains
    def containsKey = "CONTAINS KEY".i ^^^ ContainsKey

    lte | gte | eq | lt | gt | noteq | in | containsKey | contains
  }

  def where: Parser[WhereClause] = {
    import WhereClause._
    def relation: Parser[Relation] = {
      import Relation._

      def columnNames = parenthesis(rep1sep(identifier, ","))

      def simple = identifier ~ operator ~ term ^^^^ Simple
      def tupled = columnNames ~ operator ~ tupleLiteral ^^^^ Tupled
      def token = "TOKEN".i ~> columnNames ~ operator ~ term ^^^^ Token

      simple | tupled | token
    }

    "WHERE".i ~> rep1sep(relation, "AND".i) ^^ WhereClause.apply
  }

  def condition = simpleSelection ~ operator ~ term ^^^^ Condition

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

