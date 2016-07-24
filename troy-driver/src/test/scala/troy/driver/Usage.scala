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
import troy.dsl.{ Codecs, InternalDsl }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/*
 * Playground to test the helpers without macros
 */
object Usage extends App {
  import DSL._

  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session: Session = cluster.connect()
  case class Post(id: UUID, author_name: String, title: String)
  import scala.concurrent.ExecutionContext.Implicits.global

  val getByTitle = {
    import InternalDsl._
    import Codecs._

    val prepared =
      session.prepare("SELECT post_id, author_name, post_title FROM test.posts;")

    def parser(row: Row) = Post(
      column[java.util.UUID](0)(row).as[CassandraDataType.Uuid],
      column[String](1)(row).as[CassandraDataType.Text],
      column[String](2)(row).as[CassandraDataType.Text]
    )

    (title: String) =>
      bind(prepared, param(title).as[CassandraDataType.Text])
        .executeAsync
        .all
        .parseAs(parser)
  }

  println(Await.result(getByTitle("Title"), Duration(1, "second")))
  session.close()
  cluster.close()
}