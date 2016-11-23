package troy.cql.ast

import java.util.UUID

sealed trait Term

sealed trait Constant extends Term
final case class IntegerConstant(value: Int) extends Constant
final case class FloatConstant(value: Float) extends Constant
final case class StringConstant(value: String) extends Constant
final case class BooleanConstant(value: Boolean) extends Constant
final case class UuidConstant(value: UUID) extends Constant
final case class BlobConstant(value: String) extends Constant
case object NullConstant extends Constant

sealed trait Literal extends Term
sealed trait CollectionLiteral extends Literal
final case class MapLiteral(pairs: Seq[(Term, Term)]) extends CollectionLiteral
final case class SetLiteral(values: Seq[Term]) extends CollectionLiteral
final case class ListLiteral(values: Seq[Term]) extends CollectionLiteral

final case class UdtLiteral(members: Seq[(Identifier, Term)]) extends Literal
final case class TupleLiteral(values: Seq[Term]) extends Literal

final case class FunctionCall(functionName: Identifier, params: Seq[Term]) extends Term
final case class TypeHint(cqlType: DataType, term: Term) extends Term
sealed trait BindMarker extends Term
object BindMarker {
  case object Anonymous extends BindMarker
  final case class Named(name: Identifier) extends BindMarker
}