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

package troy.cql.ast

sealed trait DataType

// TODO: frozen & UDT
object DataType {
  sealed trait Native extends DataType
  case object ascii extends Native
  case object bigint extends Native
  case object blob extends Native
  case object boolean extends Native
  case object counter extends Native
  case object date extends Native
  case object decimal extends Native
  case object double extends Native
  case object float extends Native
  case object inet extends Native
  case object int extends Native
  case object smallint extends Native
  case object text extends Native
  case object times extends Native
  case object timestamp extends Native
  case object timeuuid extends Native
  case object tinyint extends Native
  case object uuid extends Native
  case object varchar extends Native
  case object varint extends Native

  sealed trait Collection extends DataType
  final case class list(t: Native) extends Collection
  final case class set(t: Native) extends Collection
  final case class map(k: Native, v: Native) extends Collection

  final case class Tuple(ts: Seq[DataType]) extends DataType
  final case class Custom(javaClass: String) extends DataType

  final case class UserDefined(keyspaceName: KeyspaceName, identifier: Identifier) extends DataType
}
