package troy
package cql.parser.ddl

import troy.cql.ast.CqlParser._
import troy.cql.ast.UseStatement

trait UseStatementParser {
  def use: Parser[UseStatement] = "use".i ~> keyspaceName ^^ UseStatement

}
