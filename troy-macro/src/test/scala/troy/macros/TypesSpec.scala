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

import com.datastax.driver.core.{ ResultSet, Row, Statement }

import scala.concurrent.Future

class TypesSpec extends CassandraSpec {

  import troy.driver.DSL._
  import troy.dsl._

  import scala.concurrent.ExecutionContext.Implicits.global

  override val testDataFixtures =
    """
      INSERT INTO test.post_details (author_id, id , tags , comment_ids, comment_userIds, comment_bodies , comments)
      VALUES ( uuid(), uuid(), {'test1', 'test2'}, {1, 2}, [1, 2], ['test1', 'test2'], {1: 'test1', 2 : 'test2'}) ;
    """

  case class PostDetails(id: UUID, tags: Set[String])
  case class PostCommentIds(id: UUID, commentIds: Set[Int])
  case class PostCommentUserIds(id: UUID, users: Seq[Int])
  case class PostCommentBodies(id: UUID, bodies: Seq[String])
  case class PostComments(id: UUID, comments: Map[Integer, String])

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
      cql"SELECT id, comment_ids FROM test.post_details;".prepared.execute.oneOption.as(PostCommentIds)
    }
    val x = q()
    x.get.commentIds shouldBe Set(1, 2)
  }

  it should "accepted as param" in {
    val filterByTag = withSchema { (tag: String) =>
      cql"SELECT id, tags FROM test.post_details where tags CONTAINS $tag;"
        .prepared
        .executeAsync
        .oneOption
        .as(PostDetails)
    }
    filterByTag("test1").futureValue.get.tags shouldBe Set("test1", "test2")
  }

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

  it should "be queries via contains" in {
    val q = withSchema { (userId: Int) =>
      cql"SELECT id, comment_userIds FROM test.post_details where comment_userIds CONTAINS $userId;".prepared.executeAsync.oneOption.as(PostCommentUserIds)
    }

    q(1).futureValue.get.users shouldBe List(1, 2)
  }

  it should "fail to query using =" in {
    assertTypeError(
      """
        |  withSchema { (userIds: Seq[Int]) =>
        |    cql"SELECT id, comment_userIds FROM test.post_details where comment_userIds = $userIds;".prepared.executeAsync.oneOption.as(PostCommentUserIds)
        |  }
      """.stripMargin
    )
  }

  "MAP column" should "be selected" in {
    val q = withSchema { () =>
      cql"SELECT comments FROM test.post_details;".prepared.execute
    }
    q()
  }

  it should "be parsed" in {
    val q = withSchema { () =>
      cql"SELECT id, comments FROM test.post_details;".prepared.executeAsync.oneOption.as(PostComments)
    }
    q().futureValue.get.comments shouldBe Map(1 -> "test1", 2 -> "test2")
  }

  it should "support CONTAINS KEY" in {
    val q = withSchema { (userId: Int) =>
      cql"SELECT id, comments FROM test.post_details where comments CONTAINS KEY $userId;".prepared.executeAsync.oneOption.as(PostComments)
    }

    q(1).futureValue.get.comments shouldBe Map(1 -> "test1", 2 -> "test2")
  }

  it should "support CONTAINS" in {
    val q = withSchema { (comment: String) =>
      cql"SELECT id, comments FROM test.post_details where comments CONTAINS $comment;".prepared.executeAsync.oneOption.as(PostComments)
    }

    q("test1").futureValue.get.comments shouldBe Map(1 -> "test1", 2 -> "test2")
  }
}
