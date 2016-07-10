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

import com.datastax.driver.core.{Row, ResultSet, BoundStatement}

import scala.concurrent.Future

/*
 * Very high level tests, mostly happy path
 * to highlight main usecases of the project
 */
class Usage extends BaseSpec {
  import troy.dsl._

  import scala.concurrent.ExecutionContext.Implicits.global

  case class Post(id: UUID, author_name: String, title: String)
  case class AuthorAndPost(authorId: UUID, postId: UUID, authorName: String, postRating: Int, postTitle: String)

  "The Macro" should "support no params" in {
    val getAll = troy { () =>
      cql"SELECT post_id, author_name, post_title FROM test.posts;".async.all.as(Post)
    }

    val result: Future[Seq[Post]] = getAll()
    result.futureValue.size shouldBe 1
  }

  it should "support single param" in {
    val getByTitle = troy { (title: String) =>
      cql"SELECT post_id, author_name, post_title FROM test.posts WHERE post_title = $title;".async.all.as(Post)
    }
    val result: Future[Seq[Post]] = getByTitle("Title")
    result.futureValue.head.title shouldBe "Title"
  }

  "The Macro" should "support returning the BoundStatement directly with no params" in {
    val createStatement = troy { () =>
      cql"SELECT post_id, author_name, post_title FROM test.posts;"
    }
    val statement: BoundStatement = createStatement()
    val result = session.execute(statement) // Normal Cassandra client code
    result.all().size() shouldBe 1
  }

  "The Macro" should "support returning the BoundStatement directly with params" in {
    val createStatement = troy { (title: String) =>
      cql"SELECT post_id, author_name, post_title FROM test.posts WHERE post_title = $title;"
    }
    val statement: BoundStatement = createStatement("Title")
    val result = session.execute(statement) // Normal Cassandra client code
    result.all().size() shouldBe 1
  }

  it should "support returning the ResultSet" in {
    val query = troy {() =>
      cql"SELECT post_id, author_name, post_title FROM test.posts;".sync
    }
    val result: ResultSet = query()
    result.all().size() shouldBe 1
  }

  it should "support returning the ResultSet asynchronously" in {
    val query = troy { () =>
      cql"SELECT post_id, author_name, post_title FROM test.posts;".async
    }
    val result: ResultSet = query().futureValue
    result.all().size() shouldBe 1
  }

  it should "support returning one element" in {
    val query = troy { () =>
      cql"SELECT post_id, author_name, post_title FROM test.posts;".async.one
    }
    val result: Row = query().futureValue
    result.getString("post_title") shouldBe "Title"
  }

  it should "support parsing one row async" in {
    val query = troy { ()=>
      cql"SELECT post_id, author_name, post_title FROM test.posts;".async.one.as(Post)
    }
    val result: Post = query().futureValue
    result.title shouldBe "Title"
  }

  it should "support parsing one row sync" in {
    val query = troy { ()=>
      cql"SELECT post_id, author_name, post_title FROM test.posts;".sync.one.as(Post)
    }
    val result: Post = query()
    result.title shouldBe "Title"
  }

  it should "support select * with no params" in {
    val query = troy { ()=>
      cql"SELECT * FROM test.posts;".sync.one
    }
    val result: Row = query()
    result.getString("post_title") shouldBe "Title"
  }

  it should "support parsing select * with class/function matching the whole table" in {
    val query = troy { () =>
      cql"SELECT * FROM test.posts;".sync.one.as(AuthorAndPost)
    }
    val result: AuthorAndPost = query()
    result.postTitle shouldBe "Title"
  }


//    val getByTitle0 = query[Post] { (title: String) =>
//      cql"SELECT * FROM test1.posts WHERE title = $title LIMIT 1;"
//    }
//    getByTitle0("test"): Future[Seq[Post]]

//    val getByTitle: (String) => Future[Post] = queryOne[Post] { (title: String) =>
//      cql"SELECT * FROM test1.posts WHERE title = $title LIMIT 1;"
//    }
//    getByTitle("test"): Future[Option[Post]]
//
//    val getByTitle2 = queryOne[Post] { (title: String) =>
//      cql"SELECT * FROM test1.posts WHERE title = $title;"
//    }
//    getByTitle2("test"): Future[Option[Post]]
//
//    val filterByTag = query[Post] { (tag: String) =>
//      cql"SELECT * FROM test1.posts WHERE tags CONTAINS $tag;"
//    }
//    filterByTag("hot"): Future[Seq[Post]]
//
//    import ExecuteParams.Bind
//    val getByTitleAndTag = Troy.execute { (title: String, tag: String) =>
//      ExecuteParams[Future[Seq[Post]]](
//        "SELECT * FROM test1.posts WHERE title = :title AND tags CONTAINS :tag;",
//        Seq(Bind("title", title), Bind("tag", tag))
//      )
//    }
//    getByTitleAndTag("test", "hot"): Future[Seq[Post]] // TODO: Future[Seq[Post]]
//
//    val createPost = execute { (p: Post) =>
//      cql"""
//        INSERT INTO test1.posts (id, title, tags)
//        VALUES (${p.id}, ${p.title}, ${p.tags}) IF NOT EXISTS;
//      """
//    }
//    createPost(testPost): ???
//
//    val createPost2 = cql("""
//        INSERT INTO test1.posts (id, title, tags)
//        VALUES (?, ?, ?) IF NOT EXISTS;
//    """)
//    createPost2(testPost)
//
//    val createPost3 = execute { case Post(id, title, tags) =>
//      cql"INSERT INTO test1.posts (id, title) VALUES (uuid());"
//    } // Should not even compile
//
//    val setTitle = execute { (oldTitle: String, newTitle: String) =>
//      cql"""
//        UPDATE test1.posts
//        SET title=$oldTitle
//        If title=$newTitle;
//      """
//    }
//    setTitle("test", "not test anymore")
//
//    val addTag = execute { (newTag: String) =>
//      cql"""
//        UPDATE test1.posts
//        SET tags= tags + {$newTag};
//      """
//    }
//    add()
//
//    val removeAllTags = execute { (id: UUID) =>
//      cql"DELETE tags FROM test1.posts WHERE id = $id;"
//    }
//
//    val removeFirstTag = execute { (id: UUID) =>
//      cql"DELETE tags[0] FROM test1.posts WHERE id = $id;"
//    }
//
//    val removeTagByIndex = execute { (id: UUID, i: Int) =>
//      cql"DELETE tags[$i] FROM test1.posts WHERE id = $id;"
//    }
//
//    val deletePost = execute { (id: UUID) =>
//      cql"DELETE FROM test1.posts WHERE id = $id;"
//    }
//  }
}
