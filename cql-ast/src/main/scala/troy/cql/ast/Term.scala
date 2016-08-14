package troy.cql.ast

sealed trait Term

object Term {
  case class Constant(raw: String) extends Term
  sealed trait Literal extends Term
  sealed trait CollectionLiteral extends Literal
  case class MapLiteral(pairs: Seq[(Term, Term)]) extends CollectionLiteral
  case class SetLiteral(values: Seq[Term]) extends CollectionLiteral
  case class ListLiteral(values: Seq[Term]) extends CollectionLiteral

  case class UdtLiteral(members: Seq[(Identifier, Term)]) extends Literal
  case class TupleLiteral(values: Seq[Term]) extends Literal

  case class FunctionCall(functionName: Identifier, params: Seq[Term]) extends Term
  case class TypeHint(cqlType: DataType, term: Term) extends Term
  sealed trait BindMarker extends Term
  object BindMarker {
    case object Anonymous extends BindMarker
    case class Named(name: Identifier) extends BindMarker
  }
}