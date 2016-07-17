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

package troy.macros

import java.util.UUID

import com.datastax.driver.core.{ResultSet, Row, Statement}

import scala.concurrent.Future

class TypesSpec extends BaseSpec {

  import troy.driver.DSL._
  import troy.dsl._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class PostDetails(id: UUID, tags: Set[String])

  // INSERT INTO test.post_details (author_id, id , tags ) VALUES ( uuid(), uuid(), {'test1', 'test2'}) ;

  "SET column" should "be selected" in {
    val q = withSchema { () =>
      cql"SELECT tags FROM test.post_details;".prepared.execute
    }
    q()
  }

  it should "be parsed" in {
    val q = withSchema { () =>
      cql"SELECT id, tags FROM test.post_details;".prepared.executeAsync.oneOption.as(PostDetails)
    }
    q().futureValue.get.tags shouldBe Set("test1", "test2")
  }

  it should "accepted as param" in {
    val q = withSchema { (tags: Set[String]) =>
      cql"SELECT id, tags FROM test.post_details where tags = $tags;".prepared.executeAsync.oneOption.as(PostDetails)
    }
    q(Set("test1", "test2")).futureValue.get.tags shouldBe Set("test1", "test2")
  }

//  it should "accepted as param" in {
//    val q = withSchema { (tag: String) =>
//      cql"SELECT id, tags FROM test.post_details where tags CONTAINS $tag;".prepared.executeAsync.oneOption.as(PostDetails)
//    }
//    q("test1").futureValue.get.tags shouldBe Set("test1", "test2")
//  }
}
