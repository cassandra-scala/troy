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

package com.abdulradi.troy

import com.abdulradi.troy.ast.SelectStatement
import com.abdulradi.troy.schema.{ Field, TypedQuery, Schema }
import org.scalatest._
import CqlInterpolation._

class CqlInterpolationTest extends FlatSpec with Matchers {

  "Cql" should "do something" in {
    //
    //    cql"""INSERT INTO posts (author_id, author_name, post_id, post_title) values ('a1', 'tam', 'p1', 'hello');"""
    //    cql"""INSERT INTO posts (author_id, author_name, post_id, post_title) values ('a1', 'tam', 'p2', 'bye');"""
    //    cql"""INSERT INTO posts (author_id, author_name, post_id, post_title) values ('a2', 'sam', 'p3', 'eshta');"""

    case class Post(id: String, title: String)
    case class AuthorSummary1(authorId: String, authorName: String, lastPost: Post, postsCount: Int)
    case class AuthorSummary2(authorId: String, authorName: String, lastPostId: String, lastPostTitle: String, postsCount: Int)

    //    cql"SELECT author_id, author_name, post_id, post_title FROM test.posts;": TypedQuery[(Field, Field), (Field, Field)]

  }
}
