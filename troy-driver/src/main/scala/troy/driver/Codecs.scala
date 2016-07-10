package troy.driver

import java.util.UUID

import com.datastax.driver.core.TypeCodec

object Codecs {
  import TroyCodec._
  import troy.driver.{ CassandraDataType => CT }

  implicit val (intAsInt, ointAsInt) = primitive[Int, CT.Int](_.getInt, _.setInt)
  implicit val (doubleAsDouble, odoubleAsDouble) = primitive[Double, CT.Double](_.getDouble, _.setDouble)
  implicit val (booleanAsBoolean, obooleanAsBoolean) = primitive[Boolean, CT.Boolean](_.getBool, _.setBool)
  implicit val (stringAsAscii, ostringAsAscii) = wrap[String, CT.Ascii](TypeCodec.ascii)
  implicit val (stringAsVarchar, ostringAsVarchar) = wrap[String, CT.VarChar](TypeCodec.varchar)
  implicit val (stringAsText, ostringAsText) = wrap[String, CT.Text](TypeCodec.varchar)
  implicit val (uuidAsUuid, ouuidAsUuid) = wrap[UUID, CT.Uuid](TypeCodec.uuid)
}
