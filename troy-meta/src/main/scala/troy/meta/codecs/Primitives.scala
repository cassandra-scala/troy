package troy.meta.codecs

class PrimitivesConverter[J, S](
  val toScala: J => S,
  val toJava: S => J
)

object Primitives {
  import TroyCodec._
  import troy.driver.{ CassandraDataType => CT }

  implicit val (intAsInt, ointAsInt) = primitive[Int, CT.Int](_.getInt, _.setInt)
  implicit val (doubleAsDouble, odoubleAsDouble) = primitive[Double, CT.Double](_.getDouble, _.setDouble)
  implicit val (booleanAsBoolean, obooleanAsBoolean) = primitive[Boolean, CT.Boolean](_.getBool, _.setBool)

  implicit object IntegerToIntConverter extends PrimitivesConverter[java.lang.Integer, Int](j => j, s => s)
  implicit object LongtoLongConverter extends PrimitivesConverter[java.lang.Long, Long](j => j, s => s)
}
