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
package driver

import java.util.UUID

import com.datastax.driver.core._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import DSL._

/*
 * Playground to test the helpers without macros
 */
object Usage extends App {
  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session: Session = cluster.connect()
  case class Post(id: UUID, authorName: String, title: String)

  val getByTitle = {
    import InternalDsl._
    val prepared = session.prepare("SELECT post_id, author_name, post_title FROM test.posts where post_title = ?;")

    (title: String) => {
      def parser(row: _root_.com.datastax.driver.core.Row) =
        Post(
          column[UUID](0)(row).as[CDT.Uuid],
          column[String](1)(row).as[CDT.Text],
          column[String](2)(row).as[CDT.Text]
        )

      bind(prepared, param(title).as[CDT.Text])
        .executeAsync
        .oneOption
        .parseAs(parser)
    }
  }

  println(Await.result(getByTitle("Title"), Duration(1, "second")))
  session.close()
  cluster.close()
}