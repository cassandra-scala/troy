/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package troy
package driver.schema.column

import troy.driver.codecs.TroyCodec
import troy.driver.schema.TypeBinding
import troy.driver.{ CassandraDataType => CDT }
import troy.tast.{ TableName, Identifier }

sealed trait CodecForColumn[Version, Table <: TableName[_, _], Column <: Identifier] {
  type CassandraType <: CDT
  type ScalaType

  val codec: TroyCodec[CassandraType, ScalaType]
}

object CodecForColumn {
  type Aux[V, T <: TableName[_, _], C <: Identifier, CT <: CDT, ST] = CodecForColumn[V, T, C] {
    type CassandraType = CT
    type ScalaType = ST
  }

  def apply[V, T <: TableName[_, _], C <: Identifier](implicit cf: CodecForColumn[V, T, C]): Aux[V, T, C, cf.CassandraType, cf.ScalaType] = cf

  implicit def instance[V, T <: TableName[_, _], C <: Identifier, CT <: CDT, ST](
    implicit
    columnType: ColumnType.Aux[V, T, C, CT],
    typeBinding: TypeBinding.Aux[CT, ST],
    codec: TroyCodec[CT, ST]
  ): Aux[V, T, C, CT, ST] = new CodecForColumn[V, T, C] {
    type CassandraType = CT
    type ScalaType = ST
    val codec: TroyCodec[CassandraType, ScalaType] = codec
  }
}
