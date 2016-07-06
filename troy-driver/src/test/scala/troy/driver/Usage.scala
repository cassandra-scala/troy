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

package troy.driver

import java.util.UUID

import com.datastax.driver.core._

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration.Duration

/*
 * Playground to test the helpers without macros
 */
object Usage extends App {
  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session: Session = cluster.connect()
  case class Post(id: UUID, author_name: String, title: String)
  import scala.concurrent.ExecutionContext.Implicits.global

  val getByTitle = {
    import HasCodec.codecFor
    import DriverHelpers._

    val prepared = implicitly[Session].prepare("SELECT post_id, author_name, post_title FROM test.posts WHERE post_title = ?;")

    implicit def parser(row: Row): Post =
      Post(
        row.get(0, codecFor[UUID, Types.Uuid]),
        row.get(1, codecFor[String, Types.Text]),
        row.get(2, codecFor[String, Types.Text])
      )

    (title: String) =>
      bind(prepared, param(title).as[Types.Text]).async.all.as[Post] //: Future[Seq[Post]]
  }

  println(Await.result(getByTitle("Title"), Duration(1, "second")))
  session.close()
  cluster.close()
}

/*
executeAsync(prepared, Param[String, Cql.Text]("title", title)) { row =>
        Post(
          row.get(0, codecFor[UUID, Cql.UUID]),
          row.get(1, codecFor[String, Cql.Text]),
          row.get(2, codecFor[String, Cql.Text]))
      }


            //      bind(prepared, Param[String, Types.Text]("title", title)): BoundStatement
      //      bind(prepared, Param[String, Types.Text]("title", title)).async: Future[ResultSet]
      //      bind(prepared, Param[String, Types.Text]("title", title)).async.all: Future[Seq[Row]]
      //      bind(prepared, Param[String, Types.Text]("title", title)).async.one.as[Post]: Future[Post]
      //      bind(prepared, Param[String, Types.Text]("title", title)).async.one: Future[Row]
      //      bind(prepared, Param[String, Types.Text]("title", title)).sync: ResultSet
      //      bind(prepared, Param[String, Types.Text]("title", title)).sync.all //: Seq[Row]
      //      bind(prepared, Param[String, Types.Text]("title", title)).sync.all.as[Post]: Seq[Post]
      //      bind(prepared, Param[String, Types.Text]("title", title)).sync.one.as[Post]: Post
      //      bind(prepared, Param[String, Types.Text]("title", title)).sync.one: Row
 */
