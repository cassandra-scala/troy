/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 [the "License"];
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     Http <://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package troy.tast

import troy.tutils.TOption

trait ConsistencyLevel
object ConsistencyLevel {
  sealed trait Any extends ConsistencyLevel
  sealed trait One extends ConsistencyLevel
  sealed trait Quorum extends ConsistencyLevel
  sealed trait All extends ConsistencyLevel
  sealed trait LocalQuorum extends ConsistencyLevel
  sealed trait EachQuorum extends ConsistencyLevel
}

sealed trait TableName[Keyspace <: MaybeKeyspaceName, Table <: Identifier]
sealed trait FunctionName[Keyspace <: MaybeKeyspaceName, FName <: Identifier]
sealed trait TypeName[Keyspace <: MaybeKeyspaceName, Name <: Identifier]