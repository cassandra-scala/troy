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

import java.util.UUID

import com.datastax.driver.core.{Row, Cluster, Session}
import org.scalatest._
import troy.driver.DriverHelpers

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/*
 * Very high level tests, mostly happy path
 * to highlight main usecases of the project
 */
class Usage extends App {// FlatSpec with Matchers {
  import troy.dsl._
  import DriverHelpers._

  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session: Session = cluster.connect()
  case class Post(id: UUID, author_name: String, title: String)
  import scala.concurrent.ExecutionContext.Implicits.global
  val prefix = "zew"

  val getByTitle = troy { (title: String) =>
    cql"SELECT post_id, author_name, post_title FROM test.posts WHERE post_title = $title AND post_title = $title;".async.all.as[Post]
  }(session)

  println(Await.result(getByTitle("Title"), Duration(1, "second")))
  session.close()
  cluster.close()

//  "Schema" should "fetch fields" in {

    //  @troy def getByTitle(name: String) =
    //    cql"SELECT author_id FROM test.posts WHERE author_name = $name;".as[Post]


//
//    val getByTitle = Troy.execute { shawshank: String =>
//      println("")
//      cql("SELECT author_id FROM test.posts WHERE author_name = ?;")
//    }
//    getByTitle("test"): Future[ResultSet]

//    val getAll = query[Post]("SELECT * FROM test1.posts;")
//    getAll(): Future[Seq[Post]]
//
//    val getAll2 = query[Post](cql"SELECT * FROM test1.posts;")
//    getAll2(): Future[Seq[Post]]

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
