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
package troy.meta

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import troy.driver.DSL._

class DslSpec extends BaseSpec {
  val authorId1 = UUID.randomUUID()
  val postId1 = UUID.randomUUID()

  override val testDataFixtures =
    s"""
      INSERT INTO test.posts (author_id, post_id , author_name , post_rating, post_title)
      VALUES ( $authorId1, $postId1, 'test author', 5, 'test post') ;
    """

  case class Post(id: UUID, author_name: String, title: String, rating: Int)

  "The Macro" should "work!" in {
    @withSchema def get(authorId: UUID, postId: UUID) =
      cql"SELECT post_id, author_name, post_title, post_rating FROM test.posts where author_id = $authorId AND post_id = $postId;"
        .prepared
        .executeAsync
        .all
        .as[UUID, String, String, Int, Post](Post)

    val res: Future[Seq[Post]] = get(authorId1, postId1)
    res.futureValue.head shouldBe Post(postId1, "test author", "test post", 5)
  }
}
