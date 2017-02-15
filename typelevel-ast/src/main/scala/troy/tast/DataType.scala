/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 [The "License"];
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

package troy.tast

sealed trait DataType

// TODO <: frozen & UDT
object DataType {
  sealed trait Native extends DataType
  sealed trait Ascii extends Native
  sealed trait BigInt extends Native
  sealed trait Blob extends Native
  sealed trait Boolean extends Native
  sealed trait Counter extends Native
  sealed trait Date extends Native
  sealed trait Decimal extends Native
  sealed trait Double extends Native
  sealed trait Float extends Native
  sealed trait Inet extends Native
  sealed trait Int extends Native
  sealed trait Smallint extends Native
  sealed trait Text extends Native
  sealed trait Time extends Native
  sealed trait Timestamp extends Native
  sealed trait Timeuuid extends Native
  sealed trait Tinyint extends Native
  sealed trait Uuid extends Native
  sealed trait Varchar extends Native
  sealed trait Varint extends Native

  sealed trait Collection extends DataType
  sealed trait List[T <: Native] extends Collection
  sealed trait Set[T <: Native] extends Collection
  sealed trait Map[K <: Native, V <: Native] extends Collection

  sealed trait Tuple[Ts <: THList[DataType]] extends DataType
  sealed trait Custom[JavaClass <: String] extends DataType

  sealed trait UserDefined[K <: MaybeKeyspaceName, I <: Identifier] extends DataType
}
