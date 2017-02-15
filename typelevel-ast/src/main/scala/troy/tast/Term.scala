package troy.tast

import java.util.UUID

sealed trait Term

sealed trait Constant extends Term

sealed trait FloatNum extends Constant
sealed trait FloatConstant[Value <: Float] extends FloatNum
sealed trait NaN extends FloatNum
sealed trait Infinity extends FloatNum

sealed trait IntegerConstant[Value <: Int] extends Constant
sealed trait StringConstant[Value <: String] extends Constant
sealed trait BooleanConstant[Value <: Boolean] extends Constant
sealed trait UuidConstant[Value <: UUID] extends Constant
sealed trait BlobConstant[Value <: String] extends Constant
sealed trait NullConstant extends Constant

sealed trait Literal extends Term
sealed trait CollectionLiteral extends Literal
sealed trait MapLiteral[Pairs <: THList[(Term, Term)]] extends CollectionLiteral
sealed trait SetLiteral[Values <: THList[Term]] extends CollectionLiteral
sealed trait ListLiteral[Values <: THList[Term]] extends CollectionLiteral

sealed trait UdtLiteral[Members <: THList[(Identifier, Term)]] extends Literal
sealed trait TupleLiteral[Values <: THList[Term]] extends Literal

sealed trait FunctionCall[FunctionName <: Identifier, params <: THList[Term]] extends Term
sealed trait TypeHint[CqlType <: DataType, term <: Term] extends Term
sealed trait BindMarker extends Term
object BindMarker {
  sealed trait Anonymous extends BindMarker
  sealed trait Named[Name <: Identifier] extends BindMarker
}