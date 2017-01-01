package troy.driver.schema.column

import troy.driver.codecs.TroyCodec
import troy.driver.schema.TypeBinding
import troy.driver.{ CassandraDataType => CDT }

sealed trait CodecForColumn[Version, Keyspace, Table, Column] {
  type CassandraType <: CDT
  type ScalaType

  val codec: TroyCodec[CassandraType, ScalaType]
}

object CodecForColumn {
  type Aux[V, K, T, C, CT, ST] = CodecForColumn[V, K, T, C] {
    type CassandraType = CT
    type ScalaType = ST
  }

  def apply[V, K, T, C](implicit cf: CodecForColumn[V, K, T, C]): Aux[V, K, T, C, cf.CassandraType, cf.ScalaType] = cf

  implicit def instance[V, K, T, C, CT <: CDT, ST](
    implicit
    columnType: ColumnType.Aux[V, K, T, C, CT],
    typeBinding: TypeBinding.Aux[CT, ST],
    codec: TroyCodec[CT, ST]
  ): Aux[V, K, T, C, CT, ST] = new CodecForColumn[V, K, T, C] {
    type CassandraType = CT
    type ScalaType = ST
    val codec: TroyCodec[CassandraType, ScalaType] = codec
  }
}
