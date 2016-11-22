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

import java.util.UUID

import troy.cql.ast.dml._
import troy.cql.ast.dml.{ UpdateParam, UpdateParamValue, UpdateVariable }
import troy.cql.parser.{ Helpers, TermParser }
import troy.cql.parser.dml.{ DeleteStatementParser, InsertStatementParser, SelectStatementParser, UpdateStatementParser }
import troy.cql.parser.ddl._

import scala.util.parsing.combinator._

// Based on CQLv3.4.3: https://cassandra.apache.org/doc/latest/cql/index.html
object CqlParser extends JavaTokenParsers
    with Helpers with TermParser
    with CreateKeyspaceParser with CreateTableParser with CreateIndexParser with AlterTableParser with CreateTypeParser
    with SelectStatementParser with InsertStatementParser with DeleteStatementParser with UpdateStatementParser {
  def parseSchema(input: String): ParseResult[Seq[DataDefinition]] =
    parse(phrase(rep(dataDefinition <~ semicolon)), input)

  def parseQuery(input: String): ParseResult[SelectStatement] =
    parse(phrase(selectStatement <~ semicolon.?), input)

  def parseDML(input: String): ParseResult[DataManipulation] =
    parse(phrase(dmlDefinition <~ semicolon.?), input)

  ////////////////////////////////////////// Data Definition
  def dataDefinition: Parser[DataDefinition] =
    createKeyspace | createTable | createIndex | alterTableStatement | createTypeStatement

  ///////////////////////////////////// Data Manipulation
  def dmlDefinition: Parser[DataManipulation] =
    selectStatement | insertStatement | deleteStatement | updateStatement // batchStatement | truncateStatement

  def semicolon: Parser[Unit] = ";".? ^^^ ((): Unit)

  /*
   * <identifier> ::= any quoted or unquoted identifier, excluding reserved keywords
   */
  def constant: Parser[Constant] = {
    import Constants._
    def str = string ^^ StringConstant
    def uuidNum = uuid ^^ { s => new UuidConstant(UUID.fromString(s)) }
    def bool = boolean ^^ { s => new BooleanConstant(s.toBoolean) }
    //def int = integer ^^ { s => new IntegerConstant(s.toInt) }
    //def floatNum = float ^^ { s => new FloatConstant(s.toFloat) }

    def num: Parser[Number] = number ^^ {
      number match {
        case float   => { s => new FloatConstant(s.toFloat) }
        case integer => { s => new IntegerConstant(s.toInt) }
      }
    }

    str | num | uuidNum | bool
  }
  def identifier: Parser[Identifier] = "[a-zA-Z0-9_]+".r.filter(k => !Keywords.contains(k.toUpperCase))

  def optionInstruction: Parser[OptionInstruction] = {
    def identifierOption = identifier ~ ("=".i ~> identifier) ^^^^ IdentifierOption

    def constantOption = identifier ~ ("=".i ~> constant) ^^^^ ConstantOption
    def mapLiteralOption = identifier ~ ("=".i ~> mapLiteral) ^^^^ MapLiteralOption

    constantOption | mapLiteralOption | identifierOption
  }

  object Constants {

    def string = "'".r ~> """([^']|'')*""".r <~ "'" ^^ { _.replace("''", "'") }

    def integer = wholeNumber

    def float = floatingPointNumber

    def number = float | integer

    def uuid = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

    def boolean = "true".i | "false".i
  }

  def keyspaceName: Parser[KeyspaceName] = identifier ^^ KeyspaceName

  /*
   * <tablename> ::= (<identifier> '.')? <identifier>
   */
  def tableName: Parser[TableName] = (keyspaceName <~ ".").? ~ identifier ^^^^ TableName

  def typeName: Parser[TypeName] = (keyspaceName <~ ".").? ~ identifier ^^^^ TypeName

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
    def like = "LIKE".r ^^^ Like

    lte | gte | eq | lt | gt | noteq | in | containsKey | contains | like
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
    def ascii = "ascii".i ^^^ DataType.Ascii
    def bigint = "bigint".i ^^^ DataType.BigInt
    def blob = "blob".i ^^^ DataType.Blob
    def boolean = "boolean".i ^^^ DataType.Boolean
    def counter = "counter".i ^^^ DataType.Counter
    def date = "date".i ^^^ DataType.Date
    def decimal = "decimal".i ^^^ DataType.Decimal
    def double = "double".i ^^^ DataType.Double
    def float = "float".i ^^^ DataType.Float
    def inet = "inet".i ^^^ DataType.Inet
    def int = "int".i ^^^ DataType.Int
    def smallint = "smallint".i ^^^ DataType.Smallint
    def text = "text".i ^^^ DataType.Text
    def time = "time".i ^^^ DataType.Time
    def timestamp = "timestamp".i ^^^ DataType.Timestamp
    def timeuuid = "timeuuid".i ^^^ DataType.Timeuuid
    def tinyint = "tinyint".i ^^^ DataType.Tinyint
    def uuid = "uuid".i ^^^ DataType.Uuid
    def varchar = "varchar".i ^^^ DataType.Varchar
    def varint = "varint".i ^^^ DataType.Varint
    def native: Parser[DataType.Native] =
      ascii | bigint | blob | boolean | counter | date | decimal | double | float | inet | int | smallint | text | timestamp | timeuuid | time | tinyint | uuid | varchar | varint

    def list = "list".i ~> '<' ~> native <~ '>' ^^ DataType.List
    def set = "set".i ~> '<' ~> native <~ '>' ^^ DataType.Set
    def map = "map".i ~> '<' ~> native ~ (',' ~> native) <~ '>' ^^ {
      case k ~ v => DataType.Map(k, v)
    }
    def collection: Parser[DataType.Collection] = list | set | map

    def tuple: Parser[DataType.Tuple] = "tuple".i ~> '<' ~> rep1sep(native, ",") <~ '>' ^^ DataType.Tuple

    native | collection | tuple // | custom // TODO
  }

  def staticFlag = "STATIC".flag

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
