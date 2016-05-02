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

package com.abdulradi.troy.poc

import java.nio.ByteBuffer
import java.util.UUID

import com.abdulradi.troy.driver.datastax.Query
import com.datastax.driver.core._
import com.google.common.util.concurrent.{ ListenableFuture, FutureCallback, Futures }

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Promise, Future }
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import com.abdulradi.troy.driver.datastax._

object Main extends App {
  case class Post(id: UUID, title: String, body: Option[String], commentsCount: Int)

  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  implicit val session: Session = cluster.connect()

  import HasCodec._
  val results = Query.query[Post]("SELECT id, title, body, commentsCount FROM blog.posts")

  println(Await.result(results, Duration.fromNanos(1000000)).toList)
  cluster.close()
}
