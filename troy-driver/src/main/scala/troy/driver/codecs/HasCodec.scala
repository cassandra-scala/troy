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

package troy.driver

import java.util.UUID
import com.datastax.driver.core.TypeCodec

class HasCodec[S, C <: Types.CassandraDataType](val codec: TypeCodec[S])

class OptionHasCodec[T, C <: Types.CassandraDataType](implicit hasInnerCodec: HasCodec[T, C])
  extends HasCodec[Option[T], C](new OptionCodec[T](hasInnerCodec.codec))

object HasCodec {

  implicit object stringAsAscii extends HasCodec[String, Types.Ascii](TypeCodec.ascii())
  implicit object stringAsVarchar extends HasCodec[String, Types.VarChar](TypeCodec.varchar())

  implicit object stringAsText extends HasCodec[String, Types.Text](TypeCodec.varchar())
  implicit object optionalStringAsText extends OptionHasCodec[String, Types.Text]

  implicit object integerAsInt extends HasCodec[Integer, Types.Int](TypeCodec.cint())
  implicit object optionalIntegerAsInt extends HasCodec[Option[Int], Types.Int](OptionalIntCodec)

  implicit object uuidAsUuid extends HasCodec[UUID, Types.Uuid](TypeCodec.uuid())

  def codecFor[S, C <: Types.CassandraDataType](implicit hasCodec: HasCodec[S, C]) = hasCodec.codec
}
