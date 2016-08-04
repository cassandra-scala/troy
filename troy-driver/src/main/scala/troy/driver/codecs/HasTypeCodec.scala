package troy.driver.codecs

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{ Date, UUID }

import com.datastax.driver.core.{ LocalDate, TypeCodec }
import troy.driver.{ CassandraDataType => CT }

case class HasTypeCodec[S, C <: CT](typeCodec: TypeCodec[S]) extends AnyVal

object HasTypeCodec {
  import TypeCodec._

  implicit val BooleanHasCodecAsBoolean = HasTypeCodec[java.lang.Boolean, CT.Boolean](cboolean)
  implicit val TinyIntHasCodecAsByte = HasTypeCodec[java.lang.Byte, CT.TinyInt](tinyInt)
  implicit val SmallIntHasCodecAsShort = HasTypeCodec[java.lang.Short, CT.SmallInt](smallInt)
  implicit val IntHasCodecAsInteger = HasTypeCodec[java.lang.Integer, CT.Int](cint)
  implicit val BigIntHasCodecAsLong = HasTypeCodec[java.lang.Long, CT.BigInt](bigint)
  implicit val CounterHasCodecAsLong = HasTypeCodec[java.lang.Long, CT.Counter](counter)
  implicit val FloatHasCodecAsFloat = HasTypeCodec[java.lang.Float, CT.Float](cfloat)
  implicit val DoubleHasCodecAsDouble = HasTypeCodec[java.lang.Double, CT.Double](cdouble)
  implicit val VarIntHasCodecAsBigInteger = HasTypeCodec[java.math.BigInteger, CT.VarInt](varint)
  implicit val DecimalHasCodecAsBigDecimal = HasTypeCodec[java.math.BigDecimal, CT.Decimal](decimal)
  implicit val AsciiHasCodecAsString = HasTypeCodec[String, CT.Ascii](ascii)
  implicit val VarCharHasCodecAsString = HasTypeCodec[String, CT.VarChar](varchar)
  implicit val TextHasCodecAsString = HasTypeCodec[String, CT.Text](varchar)
  implicit val BlobHasCodecAsByteBuffer = HasTypeCodec[ByteBuffer, CT.Blob](blob)
  implicit val DateHasCodecAsLocalDate = HasTypeCodec[LocalDate, CT.Date](date)
  implicit val TimeHasCodecAsLong = HasTypeCodec[java.lang.Long, CT.Time](time)
  implicit val TimestampHasCodecAsDate = HasTypeCodec[Date, CT.Timestamp](timestamp)
  implicit val UuidHasCodecAsUUID = HasTypeCodec[UUID, CT.Uuid](uuid)
  implicit val TimeUuidHasCodecAsUUID = HasTypeCodec[UUID, CT.TimeUuid](timeUUID)
  implicit val InetHasCodecAsInetAddress = HasTypeCodec[InetAddress, CT.Inet](inet)
}
