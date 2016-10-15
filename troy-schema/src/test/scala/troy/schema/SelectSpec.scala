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

package troy.schema

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast.CqlParser
import troy.cql.ast.DataType.{ Text, Uuid, Set => CSet, Int => CInt, List => CList, Map => CMap }

class SelectSpec extends FlatSpec with Matchers {
  import VTestUtils._

  val schemaStatements = CqlParser.parseSchema(
    """
     CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
     CREATE TABLE test.posts (
       author_id uuid,
       post_id uuid,
       author_name text static,
       post_rating int,
       post_title text,
       comments map<int, text>,
       PRIMARY KEY ((author_id), post_id)
     );

     CREATE TABLE test.post_details (
       author_id uuid,
       id uuid,
       rating int,
       title text,
       tags set<text>,
       comment_ids set<int>,
       comment_userIds list<uuid>,
       comment_bodies list<text>,
       comments map<int, text>,
       PRIMARY KEY ((author_id), id)
     );
    """
  ).get

  val schema = SchemaEngine(schemaStatements).get

  "Schema" should "support simple SELECT statement with asterisk " ignore {
    val statement = parse("SELECT * FROM test.post_details WHERE author_id = ?;")
    val (rowType, variableTypes) = schema(statement).get

    rowType.asInstanceOf[SchemaEngine.Asterisk].types.size shouldBe 9
    rowType.asInstanceOf[SchemaEngine.Asterisk].types shouldBe Set(
      Uuid, Uuid, CInt, Text, CSet(Text), CSet(CInt), CList(Uuid), CList(Text), CMap(CInt, Text)
    )
    variableTypes shouldBe List(Uuid)
  }

  it should "support SELECT statement with fields" in {
    val statement = parse("SELECT author_id FROM test.post_details WHERE author_id = ?;")
    val (rowType, variableTypes) = schema(statement).get

    rowType.asInstanceOf[SchemaEngine.Columns].types shouldBe Seq(Uuid)
    variableTypes shouldBe List(Uuid)
  }

  def parse(s: String) = CqlParser.parseDML(s) match {
    case CqlParser.Success(result, _) =>
      result
    case CqlParser.Failure(msg, _) =>
      fail(msg)
  }

}
