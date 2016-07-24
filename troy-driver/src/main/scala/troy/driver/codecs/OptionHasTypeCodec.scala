package troy.driver.codecs

import troy.driver.CassandraDataType

object OptionHasTypeCodec {
  def apply[S, C <: CassandraDataType](implicit inner: HasTypeCodec[S, C]) =
    HasTypeCodec[Option[S], C](new OptionTypeCodec(inner.typeCodec))
}