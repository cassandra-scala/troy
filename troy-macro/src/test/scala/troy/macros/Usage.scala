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

import com.datastax.driver.core.{ Statement, Row, ResultSet, BoundStatement }

import scala.concurrent.Future

/*
 * Very high level tests, mostly happy path
 * to highlight main usecases of the project
 */
class Usage extends BaseSpec {
  import troy.driver.DSL._
  import troy.dsl._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Post(id: UUID, author_name: String, title: String)
  case class AuthorAndPost(authorId: UUID, postId: UUID, authorName: String, postRating: Int, postTitle: String)
  // FIXME: Select with where is failing
  //  it should "support parsing one row sync" in {
  //    val query = withSchema {()=>
  //      cql"SELECT post_id, author_name, post_title FROM test.posts;".prepared.execute.oneOption.as(Post)
  //    }
  //    val result: Post = query().get
  //    result.title shouldBe "Title"
  //  }
  //
  //  it should "support select * with no params" in {
  //    val query = withSchema {()=>
  //      cql"SELECT * FROM test.posts;".prepared.execute.oneOption
  //    }
  //    val result: Row = query().get
  //    result.getString("post_title") shouldBe "Title"
  //  }
  //
  //  it should "support parsing select * with class/function matching the whole table" in {
  //    val query = withSchema {() =>
  //      cql"SELECT * FROM test.posts;".prepared.execute.oneOption.as(AuthorAndPost)
  //    }
  //    val result: AuthorAndPost = query().get
  //    result.postTitle shouldBe "Title"
  //  }

  it should "support select *" in {
    //    TODO: Limit is not supported yet
    val getByTitle: (String) => Future[Option[Post]] = withSchema { (title: String) =>
      cql"SELECT * FROM test1.posts WHERE title = $title LIMIT 1;".prepared.oneOption.as(Post)
    }
    getByTitle("test"): Future[Option[Post]]
  }

  //  it should "support SET types" in {
  //    val getByTitle2 = withSchema { (title: String) =>
  //      cql"SELECT * FROM test.post_details WHERE title = $title;".prepared.oneOption
  //    }
  //    getByTitle2("test"): Future[Option[Post]]
  //
  //    val filterByTag = withSchema { (tag: String) =>
  //      cql"SELECT * FROM test.post_details WHERE tags CONTAINS $tag;".prepared.all.as(Post)
  //    }
  //    filterByTag("hot"): Future[Seq[Post]]
  //
  //    val getByTitleAndTag = withSchema { (title: String, tag: String) =>
  //      cql"SELECT * FROM test.post_details WHERE title = $title AND tags CONTAINS $tag;".prepared.as(Post)
  //    }
  //    getByTitleAndTag("test", "hot"): Future[Seq[Post]]
  //  }

  //  it should "support INSERT" in {
  //    val createPost = withSchema { (p: Post) =>
  //      cql"""
  //        INSERT INTO test1.posts (id, title, author_name)
  //        VALUES (${p.id}, ${p.title}, ${p.author_name}) IF NOT EXISTS;
  //      """.prepared.executeAsync
  //    }
  //    createPost(Post(UUID.randomUUID(), "Author", "Title")): Future[ResultSet]
  //
  //    val createPost3 = withSchema { case Post(id, title, tags) =>
  //      cql"INSERT INTO test1.posts (id, title) VALUES (uuid());"
  //    } // Should not even compile
  //  }

  //  it should "support Update" in {
  //    val setTitle = withSchema { (oldTitle: String, newTitle: String) =>
  //      cql"""
  //        UPDATE test1.posts
  //        SET title=$oldTitle
  //        If title=$newTitle;
  //      """.prepared.executeAsync
  //    }
  //    setTitle("test", "not test anymore")
  //
  //    val addTag = withSchema { (newTag: String) =>
  //      cql"""
  //        UPDATE test1.posts
  //        SET tags= tags + {$newTag};
  //      """.prepared.executeAsync
  //    }
  //    addTag("")
  //  }
  //
  //  it should "support DELETE" in {
  //    val removeAllTags = withSchema { (id: UUID) =>
  //      cql"DELETE tags FROM test1.posts WHERE id = $id;".prepared.executeAsync
  //    }
  //
  //    val removeFirstTag = withSchema { (id: UUID) =>
  //      cql"DELETE tags[0] FROM test1.posts WHERE id = $id;".prepared.executeAsync
  //    }
  //
  //    val removeTagByIndex = withSchema { (id: UUID, i: Int) =>
  //      cql"DELETE tags[$i] FROM test1.posts WHERE id = $id;".prepared.executeAsync
  //    }
  //
  //    val deletePost = withSchema { (id: UUID) =>
  //      cql"DELETE FROM test1.posts WHERE id = $id;".prepared.executeAsync
  //    }
  //  }
}
