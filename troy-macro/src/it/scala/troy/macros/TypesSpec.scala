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
  case class PostCommentIds(id: UUID, commentIds: Set[Int])
  case class PostCommentUserIds(id: UUID, users: Seq[Int])
  case class PostCommentBodies(id: UUID, bodies: Seq[String])

  // INSERT INTO test.post_details (author_id, id , tags , comment_ids, comment_userIds, comment_bodies ) VALUES ( uuid(), uuid(), {'test1', 'test2'}, {1, 2}, [1, 2], ['test1', 'test2']) ;

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

  it should "be parsed (primitive)" in {
    val q = withSchema { () =>
      cql"SELECT id,  comment_ids FROM test.post_details;".prepared.execute.oneOption.as(PostCommentIds)
    }
    val x = q()
    x.get.commentIds shouldBe Set(1, 2)
  }

// TODO: Contains restriction
//  it should "accepted as param" in {
//    val q = withSchema { (tag: String) =>
//      cql"SELECT id, tags FROM test.post_details where tags CONTAINS $tag;".prepared.executeAsync.oneOption.as(PostDetails)
//    }
//    q("test1").futureValue.get.tags shouldBe Set("test1", "test2")
//  }

  "LIST column" should "be selected" in {
    val q = withSchema { () =>
      cql"SELECT comment_bodies FROM test.post_details;".prepared.execute
    }
    q()
  }

  it should "be parsed" in {
    val q = withSchema { () =>
      cql"SELECT id, comment_bodies FROM test.post_details;".prepared.executeAsync.oneOption.as(PostCommentBodies)
    }
    q().futureValue.get.bodies shouldBe List("test1", "test2")
  }

  it should "be parsed (primitive)" in {
    val q = withSchema { () =>
      cql"SELECT id, comment_userIds FROM test.post_details;".prepared.execute.oneOption.as(PostCommentUserIds)
    }
    q().get.users shouldBe List(1, 2)
  }

  // TODO: Contains restriction
  //  it should "accepted as param" in {
  //    val q = withSchema { (userId: Int) =>
  //      cql"SELECT id, comment_userIds FROM test.post_details where comment_userIds CONTAINS $tag;".prepared.executeAsync.oneOption.as(PostCommentUserIds)
  //    }
  //    q("test1").futureValue.get.tags shouldBe Set("test1", "test2")
  //  }
}
