package troy.cql.parser.ddl

import troy.cql.ast.CqlParser._
import troy.cql.ast.CreateType
import troy.cql.ast.ddl.Field

trait CreateTypeParser {
  def createTypeStatement: Parser[CreateType] = {
    def fields = {
      val fieldParser = identifier ~ dataType ^^^^ Field
      parenthesis(rep1sep(fieldParser, ","))
    }

    def optionKeyspaceName = (keyspaceName <~ ".").?

    "CREATE TYPE".i ~>
      ifNotExists ~
      optionKeyspaceName ~
      identifier ~
      fields ^^^^ CreateType.apply
  }
}
