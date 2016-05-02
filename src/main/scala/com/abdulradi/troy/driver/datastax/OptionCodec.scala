package com.abdulradi.troy.driver.datastax

import java.nio.ByteBuffer

import com.datastax.driver.core.{ProtocolVersion, TypeCodec}

abstract class OptionAdapterCodec[T, U](innerCodec: TypeCodec[T]) extends AdapterCodec[T, Option[U]](innerCodec, classOf[Option[U]]) {
  private var empty: T = _

  def box: T => U
  def unbox: U => T

  override def outMap(value: T): Option[U] =
    Option(box(value))

  override def inMap(maybeValue: Option[U]): T =
    maybeValue.map(unbox).getOrElse(empty)
}

class OptionCodec[T](innerCodec: TypeCodec[T]) extends OptionAdapterCodec[T, T](innerCodec) {
  override val box = identity[T] _
  override val unbox = identity[T] _
}

abstract class OptionalPrimitiveCodec[T, U](innerCodec: TypeCodec[T]) extends OptionAdapterCodec[T, U](innerCodec) {
  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Option[U] =
    if (bytes == null || bytes.remaining == 0)  // Avoid NullPointerExceptions
      None
    else
      super.deserialize(bytes, protocolVersion)
}

object OptionalIntCodec extends OptionalPrimitiveCodec[Integer, Int](TypeCodec.cint()) {
  override val box = (_: Integer).intValue()
  override val unbox = Integer.valueOf(_: Int)
}