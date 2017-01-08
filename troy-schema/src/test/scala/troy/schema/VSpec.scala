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
package schema

import org.scalatest._

class VSpec extends FlatSpec with Matchers {

  "V.merge" should "reserve order" in {
    V.merge(Seq(V.Success(1), V.Success(2), V.Success(3))) shouldBe V.Success(Seq(1, 2, 3))
    V.merge(Seq(V.error(1), V.error(2), V.error(3))) shouldBe V.Error(Seq(1, 2, 3))
  }

  it should "collect all errors" in {
    V.merge(Seq(V.error(1), V.error(2), V.error(3), V.Success(4), V.error(5))) shouldBe V.Error(Seq(1, 2, 3, 5))
  }
}
