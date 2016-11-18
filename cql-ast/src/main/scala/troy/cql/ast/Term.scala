package troy.cql.ast

import java.sql.Blob
import java.util.UUID

sealed trait Term


sealed trait Constant extends Term
final case class StringConstant (raw: String) extends Constant
final case class IntegerConstant (raw: Int) extends Constant
final case class FloatConstant (raw: Float) extends Constant
final case class BooleanConstant (raw: Boolean) extends Constant
final case class UuidConstant (raw: UUID) extends Constant
final case class BlobConstant (raw: Blob) extends Constant
final case class NullConstant (raw: Null) extends Constant


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