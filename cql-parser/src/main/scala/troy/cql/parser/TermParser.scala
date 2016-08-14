package troy.cql.parser

import troy.cql.ast.CqlParser._
import troy.cql.ast._

trait TermParser {
  def term: Parser[Term] = {
    def constant: Parser[Constant] = {
      import Constants._
      (string | number | uuid | boolean) ^^ Constant // | hex // TODO
    }

    def functionCall: Parser[FunctionCall] =
      identifier ~ parenthesis(repsep(term, ",")) ^^^^ FunctionCall

    def typeHint: Parser[TypeHint] =
      parenthesis(dataType) ~ term ^^^^ TypeHint

    constant | literal | functionCall | typeHint | bindMarker
  }

  def mapLiteral: Parser[MapLiteral] = {
    val pair = term ~ (':' ~> term) ^^ { case key ~ value => key -> value }
    val mapBody = repsep(pair, ",") ^^ MapLiteral
    curlyBraces(mapBody)
  }

  def setLiteral: Parser[SetLiteral] =
    curlyBraces(repsep(term, ",")) ^^ SetLiteral

  def listLiteral: Parser[ListLiteral] =
    squareBrackets(repsep(term, ",")) ^^ ListLiteral

  def collectionLiteral: Parser[CollectionLiteral] = mapLiteral | setLiteral | listLiteral

  def udtLiteral: Parser[UdtLiteral] = {
    val member = identifier ~ (':' ~> term) ^^ { case key ~ value => key -> value }
    val udtBody = rep1sep(member, ",") ^^ UdtLiteral
    curlyBraces(udtBody)
  }

  def tupleLiteral: Parser[TupleLiteral] =
    parenthesis(rep1sep(term, ",")) ^^ TupleLiteral

  def literal: Parser[Literal] = collectionLiteral | udtLiteral | tupleLiteral

  def bindMarker: Parser[BindMarker] = {
    import BindMarker._
    def anonymous = """\?""".r ^^^ Anonymous
    def named = ":".r ~> identifier ^^ Named

    anonymous | named
  }
}
