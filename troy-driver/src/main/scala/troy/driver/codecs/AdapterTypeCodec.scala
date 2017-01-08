package troy
package driver.codecs

import java.nio.ByteBuffer

import com.datastax.driver.core.{ ProtocolVersion, TypeCodec }

abstract class AdapterTypeCodec[In, Out](innerCodec: TypeCodec[In], javaClass: Class[Out]) extends TypeCodec[Out](innerCodec.getCqlType, javaClass) {
  def innerToOuter(inner: In): Out

  def outerToInner(outer: Out): In

  override def serialize(value: Out, protocolVersion: ProtocolVersion): ByteBuffer =
    innerCodec.serialize(outerToInner(value), protocolVersion)

  override def parse(value: String): Out =
    innerToOuter(innerCodec.parse(value))

  override def format(value: Out): String =
    innerCodec.format(outerToInner(value))

  override def deserialize(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Out =
    innerToOuter(innerCodec.deserialize(bytes, protocolVersion))
}

class OptionTypeCodec[T <: AnyRef](innerCodec: TypeCodec[T]) extends AdapterTypeCodec[T, Option[T]](innerCodec, classOf[Option[T]]) {
  private var empty: T = _

  override def innerToOuter(inner: T): Option[T] = Option(inner)

  override def outerToInner(outer: Option[T]): T = outer.getOrElse(empty)
}