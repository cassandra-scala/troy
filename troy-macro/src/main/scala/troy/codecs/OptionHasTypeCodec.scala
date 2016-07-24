package troy.codecs

import troy.driver.codecs.OptionTypeCodec
import troy.driver.{ CassandraDataType => CT }

object OptionHasTypeCodec {
  def apply[S, C <: CT](implicit inner: HasTypeCodec[S, C]) =
    HasTypeCodec[Option[S], C](new OptionTypeCodec(inner.typeCodec))
}