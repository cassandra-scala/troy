package troy
package macros

import troy.cql.ast.CqlParser
import troy.schema._

object CqlOps {
  import VParseResultImplicits._
  def parseQuery(queryString: String) =
    CqlParser
      .parseDML(queryString)
      .toV(f => Messages.QueryParseFailure(f.msg, f.next.pos.line, f.next.pos.column))
}
