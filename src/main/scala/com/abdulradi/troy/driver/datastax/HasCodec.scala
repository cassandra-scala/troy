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

package com.abdulradi.troy.driver.datastax

import java.nio.ByteBuffer
import java.util.UUID

import com.abdulradi.troy.ast.DataType
import com.datastax.driver.core.{ProtocolVersion, TypeCodec}
import com.datastax.driver.core.{DataType => NativeDataType}

class HasCodec[S, C <: DataType](val codec: TypeCodec[S])



class OptionHasCodec[T, C <: DataType](implicit hasInnerCodec: HasCodec[T, C])
  extends HasCodec[Option[T], C](new OptionCodec[T](hasInnerCodec.codec))


object HasCodec {
  import DataType._
  implicit object stringAsAscii extends HasCodec[String, ascii.type](TypeCodec.ascii())
  implicit object stringAsVarchar extends HasCodec[String, varchar.type](TypeCodec.varchar())

  implicit object stringAsText extends HasCodec[String, text.type](TypeCodec.varchar())
  implicit object optionalStringAsText extends OptionHasCodec[String, text.type]

  implicit object integerAsInt extends HasCodec[Integer, int.type](TypeCodec.cint())
  implicit object optionalIntegerAsInt extends HasCodec[Option[Int], int.type](OptionalIntCodec)

  implicit object uuidAsUuid extends HasCodec[UUID, uuid.type](TypeCodec.uuid())

}
