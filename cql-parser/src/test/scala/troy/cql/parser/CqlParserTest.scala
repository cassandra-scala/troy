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
package cql.parser

import org.scalatest._
import troy.cql.ast._
import troy.cql.ast.ddl.{ Keyspace, Table }
import troy.cql.parser.ParserTestUtils.parseSchema

class CqlParserTest extends FlatSpec with Matchers {

  "Cql Parser" should "parse multiple statements " in {
    val statements = parseSchema(
      """
            CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
            CREATE TABLE test.posts (
              author_id text,
              author_name text static,
              author_age int static,
              post_id text,
              post_title text,
              PRIMARY KEY ((author_id), post_id)
            );
          """
    )

    statements.size shouldBe 2
    statements shouldBe Seq(
      CreateKeyspace(false, KeyspaceName("test"), Seq(Keyspace.Replication(Seq(("class", "SimpleStrategy"), ("replication_factor", "1"))))),
      CreateTable(false, TableName(Some(KeyspaceName("test")), "posts"), Seq(
        Table.Column("author_id", DataType.Text, false, false),
        Table.Column("author_name", DataType.Text, true, false),
        Table.Column("author_age", DataType.Int, true, false),
        Table.Column("post_id", DataType.Text, false, false),
        Table.Column("post_title", DataType.Text, false, false)
      ), Some(Table.PrimaryKey(Seq("author_id"), Seq("post_id"))), Nil)
    )

  }
}
