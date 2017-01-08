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
package cql.ast

sealed trait DataType

// TODO: frozen & UDT
object DataType {
  sealed trait Native extends DataType
  case object Ascii extends Native
  case object BigInt extends Native
  case object Blob extends Native
  case object Boolean extends Native
  case object Counter extends Native
  case object Date extends Native
  case object Decimal extends Native
  case object Double extends Native
  case object Float extends Native
  case object Inet extends Native
  case object Int extends Native
  case object Smallint extends Native
  case object Text extends Native
  case object Time extends Native
  case object Timestamp extends Native
  case object Timeuuid extends Native
  case object Tinyint extends Native
  case object Uuid extends Native
  case object Varchar extends Native
  case object Varint extends Native

  sealed trait Collection extends DataType
  final case class List(t: Native) extends Collection
  final case class Set(t: Native) extends Collection
  final case class Map(k: Native, v: Native) extends Collection

  final case class Tuple(ts: Seq[DataType]) extends DataType
  final case class Custom(javaClass: String) extends DataType

  final case class UserDefined(keyspaceName: KeyspaceName, identifier: Identifier) extends DataType
}
