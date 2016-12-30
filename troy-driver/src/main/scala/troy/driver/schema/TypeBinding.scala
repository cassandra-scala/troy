package troy.driver.schema

import troy.driver.{ CassandraDataType => CDT }

/*
 * Maps a Cassandra type to equivalent Scala type  
 * This type-class is meant to be instantiated by Troy, yet still can be overridden in Call site
 */
trait TypeBinding[CassandraType <: CDT] {
  type ScalaType
}

object TypeBinding {
  type Aux[CT <: CDT, ST] = TypeBinding[CT] { type ScalaType = ST }

  def instance[CT <: CDT, ST]: Aux[CT, ST] =
    new TypeBinding[CT] { type ScalaType = ST }

  implicit val defaultIntMapping = instance[CDT.Int, Int]
  implicit val defaultAsciiMapping = instance[CDT.Ascii, String]
  implicit val defaultTextMapping = instance[CDT.Text, String]
  // TODO
  implicit def defaultListMapping[T <: CDT.Native](implicit tb: TypeBinding[T]) = instance[CDT.List[T], Seq[tb.ScalaType]]
  implicit def defaultSetMapping[T <: CDT.Native](implicit tb: TypeBinding[T]) = instance[CDT.Set[T], Set[tb.ScalaType]]
  implicit def defaultMapMapping[K <: CDT.Native, V <: CDT.Native](implicit kTb: TypeBinding[K], vTb: TypeBinding[V]) =
    instance[CDT.Map[K, V], Map[kTb.ScalaType, vTb.ScalaType]]
  // TODO
}

