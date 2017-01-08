package troy
package cql.parser.ddl

import troy.cql.ast.CqlParser._
import troy.cql.ast.CreateKeyspace
import troy.cql.ast.ddl.Keyspace._

trait CreateKeyspaceParser {
  def createKeyspace: Parser[CreateKeyspace] = {
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

}