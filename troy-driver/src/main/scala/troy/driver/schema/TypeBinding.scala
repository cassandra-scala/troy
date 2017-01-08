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

