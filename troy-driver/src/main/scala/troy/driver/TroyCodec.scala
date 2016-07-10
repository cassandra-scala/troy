package troy.driver

import com.datastax.driver.core.{ TypeCodec, BoundStatement, Row }
import troy.driver.{ CassandraDataType => CT }

trait ColumnGetter[S, C <: CT] {
  def get(row: Row, i: Int): S
}

trait VariableSetter[S, C <: CT] {
  def set(bound: BoundStatement, i: Int, value: S): BoundStatement
}

trait TroyCodec[S, C <: CT] extends ColumnGetter[S, C] with VariableSetter[S, C]

class TroyPrimitiveCodec[S, C <: CT](getImpl: Row => Int => S, setImpl: BoundStatement => (Int, S) => BoundStatement) extends TroyCodec[S, C] {
  override def set(bound: BoundStatement, i: Int, value: S) =
    setImpl(bound)(i, value)

  override def get(row: Row, i: Int) =
    getImpl(row)(i)
}

class TroyTypeCodecWrapper[S, C <: CT](typeCodec: TypeCodec[S]) extends TroyCodec[S, C] {
  override def set(bound: BoundStatement, i: Int, value: S) =
    bound.set(i, value, typeCodec)

  override def get(row: Row, i: Int) =
    row.get(i, typeCodec)
}

class TroyOptionalPrimitiveTypeCodec[S <: AnyVal, C <: CT](inner: TroyPrimitiveCodec[S, C]) extends TroyCodec[Option[S], C] {
  override def set(bound: BoundStatement, i: Int, value: Option[S]) =
    value
      .map(inner.set(bound, i, _))
      .getOrElse(bound.setToNull(i))

  override def get(row: Row, i: Int) =
    if (row.isNull(i))
      None
    else
      Some(inner.get(row, i))
}

class TroyOptionalTypeCodec[S <: AnyRef, C <: CT](inner: TroyTypeCodecWrapper[S, C]) extends TroyCodec[Option[S], C] {
  private var empty: S = _
  override def set(bound: BoundStatement, i: Int, value: Option[S]) = inner.set(bound, i, value.getOrElse(empty))
  override def get(row: Row, i: Int) = Option(inner.get(row, i))
}

object TroyCodec {
  def primitive[S <: AnyVal, C <: CT](getImpl: Row => Int => S, setImpl: BoundStatement => (Int, S) => BoundStatement) = {
    val codec = new TroyPrimitiveCodec[S, C](getImpl, setImpl)
    (codec, new TroyOptionalPrimitiveTypeCodec[S, C](codec))
  }

  def wrap[S <: AnyRef, C <: CT](typeCodec: TypeCodec[S]) = {
    val codec = new TroyTypeCodecWrapper[S, C](typeCodec)
    (codec, new TroyOptionalTypeCodec[S, C](codec))
  }
}
