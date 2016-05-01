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

import com.abdulradi.troy.ast._
import com.abdulradi.troy.schema.{ FieldLevel, Field, Schema }
import org.scalatest._

class SchemaValidationTest extends FlatSpec with Matchers {

  "Schema" should "fetch fields" in {
//    val schema = Schema(Seq(
//      CreateKeyspace(false, KeyspaceName("test"), Seq(CreateKeyspace.Replication(Seq(("class", "SimpleStrategy"), ("replication_factor", "1"))))),
//      CreateTable(false, TableName(Some(KeyspaceName("test")), "posts"), Seq(
//        CreateTable.Column("author_id", DataType.text, false, false),
//        CreateTable.Column("author_name", DataType.text, true, false),
//        CreateTable.Column("author_age", DataType.int, true, false),
//        CreateTable.Column("post_id", DataType.text, false, false),
//        CreateTable.Column("post_title", DataType.text, false, false)
//      ), Some(CreateTable.PrimaryKey(Seq("author_id"), Seq("post_id"))), Nil)
//    ))
//    schema.getField("test", "posts", "author_id").get shouldBe Field("author_id", DataType.text, FieldLevel.Partition)
//    schema.getField("test", "posts", "author_name").get shouldBe Field("author_name", DataType.text, FieldLevel.Partition)
//    schema.getField("test", "posts", "author_age").get shouldBe Field("author_age", DataType.int, FieldLevel.Partition)
//    schema.getField("test", "posts", "post_id").get shouldBe Field("post_id", DataType.text, FieldLevel.Row)
//    schema.getField("test", "posts", "post_title").get shouldBe Field("post_title", DataType.text, FieldLevel.Row)
  }
}
