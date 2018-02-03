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
package troy.api.schemasafe

import troy.api.schemasafe._
import singleton.ops.XString
import _root_.schemasafe.core._

object Example extends App {
  final case class ByFoo(foo: Int)
  final case class FooBar(foo: Int, bar: String)

  private val query0 = 
    q("select foo, bar from table x where foo = ?")
      .typed[ByFoo => FooBar]
      .materialize
  //     .prepareSync

  // def execQuery0(input: ByFoo): Future[Seq[FooBar]] =
  //   query0.executeAsync(session).all
}
