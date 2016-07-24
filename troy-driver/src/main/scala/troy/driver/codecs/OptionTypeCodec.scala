package troy.driver.codecs

import com.datastax.driver.core.TypeCodec

class OptionTypeCodec[T](innerCodec: TypeCodec[T]) extends AdapterTypeCodec[T, Option[T]](innerCodec, classOf[Option[T]]) {
  private var empty: T = _

  override def innerToOuter(inner: T): Option[T] = Option(inner)

  override def outerToInner(outer: Option[T]): T = outer.getOrElse(empty)
}
