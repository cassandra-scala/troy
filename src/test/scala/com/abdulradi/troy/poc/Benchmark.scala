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

package troy.poc

import java.util.UUID

import troy.Troy
import com.datastax.driver.core.{ Cluster, Session }

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object Benchmark extends App {
  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session: Session = cluster.connect()

  // Setup, using plain old Cassandra client, not Troy specific code ///////////////////////////
  //  session.execute("CREATE KEYSPACE IF NOT EXISTS blog WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};");
  //    session.execute(
  //      """
  //        |CREATE TABLE IF NOT EXISTS blog.posts (
  //        |      id uuid PRIMARY KEY,
  //        |      title text,
  //        |      body text,
  //        |      commentsCount int);
  //      """.stripMargin
  //    )
  //    def randomString(size: Int = 5) = Random.alphanumeric.take(size).mkString
  //
  //    def createRow(id: UUID = UUID.randomUUID(), title: String = randomString(), body: String = randomString(), commentsCount: Int = Random.nextInt()) =
  //        s"""INSERT INTO blog.posts (id, title, body, commentsCount) VALUES ($id, '$title', '$body', $commentsCount);""".stripMargin
  //
  //    def createRows(count: Int = 820) =
  //      (1 to count).map(_ => createRow())
  //
  //  def createRowsBatch() =
  //    s"""
  //      |BEGIN BATCH
  //      |  ${createRows().mkString("\n")}
  //      |APPLY BATCH;
  //    """.stripMargin
  //
  //  (1 to 10000).foreach(_ => session.execute(createRowsBatch()))
  //

  case class Post(id: UUID, title: String, body: Option[String], commentsCount: Int)

  // Troy Test ////////////////////////////////////////
  import Troy._
  val t1 = System.nanoTime()
  val postsF = query[Post]("SELECT id, title, body, commentsCount FROM blog.posts")
  val posts = Await.result(postsF, Duration.Inf)
  val max = posts.map(_.commentsCount).max
  val t2 = System.nanoTime()
  println(s"${t2 - t1} ns max commentsCount = $max rows = ${posts.size}")

  // Plain old Cassandra client test ////////////////////
  //  import troy.driver.RichListenableFuture
  //  import scala.collection.JavaConversions._
  //  val t1 = System.nanoTime()
  //  val postsF = session.executeAsync("SELECT id, title, body, commentsCount FROM blog.posts")
  //    .toScala.map(_.map { row =>
  //    Post(row.getUUID("id"), row.getString("title"), Option(row.getString("body")), row.getInt("commentsCount"))
  //  })
  //  val posts = Await.result(postsF, Duration.Inf)
  //  val max = posts.map(_.commentsCount).max

  // Results
  //  val t2 = System.nanoTime()
  //  println(s"${t2 - t1} ns max commentsCount = $max")

  cluster.close()
}
