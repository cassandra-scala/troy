package troy
package cql.parser.ddl

import troy.cql.ast.CqlParser._
import troy.cql.ast.CreateType
import troy.cql.ast.ddl.Field

trait CreateTypeParser {
  def createTypeStatement: Parser[CreateType] = {
    def fields = {
      val fieldParser = identifier ~ dataType ^^^^ Field
      parenthesis(rep1sep(fieldParser, ","))
    }

    "CREATE TYPE".i ~>
      ifNotExists ~
      typeName ~
      fields ^^^^ CreateType.apply
  }
}
