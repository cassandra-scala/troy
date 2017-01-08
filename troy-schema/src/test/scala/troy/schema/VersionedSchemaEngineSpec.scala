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
package schema

import org.scalatest._
import troy.cql.ast.{ DataType, CqlParser }
import troy.cql.parser.ParserTestUtils
import troy.schema.Messages.QueryNotCrossCompatible

class VersionedSchemaEngineSpec extends WordSpec with Matchers {
  import VTestUtils._
  import ParserTestUtils._
  import SchemaEngine._

  val v1 = CqlParser.parseSchema("""
     CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
     CREATE TABLE test.posts (
       author_id uuid,
       post_id uuid,
       author_name text static,
       post_title text,
       comments map<int, text>,
       PRIMARY KEY ((author_id), post_id)
     );
  """).get

  val v2 = CqlParser.parseSchema("""
     ALTER TABLE test.posts ADD post_rating int;
     CREATE INDEX ON test.posts (post_title);
  """).get

  val v3 = CqlParser.parseSchema("""
     ALTER TABLE test.posts DROP comments ;
  """).get

  val schema = VersionedSchemaEngine(Seq(v1, v2, v3)).get

  "Versioned Schema" when {
    "querying only latest schema" should {
      "behave like non-versioned schema" in {
        val nonVersionedSchema: SchemaEngine = schema
        val (Asterisk(types), _) = nonVersionedSchema(parseQuery("SELECT * from test.posts")).get
        types shouldBe Set(DataType.Uuid, DataType.Uuid, DataType.Text, DataType.Text, DataType.Int)
      }
    }

    "query on range of versioned schemas, upto current version" should {
      "work if query matches all versions" in {
        val (Columns(types), _) = schema(parseQuery("SELECT author_id, post_id, post_rating from test.posts"), 2).get
        types shouldBe Seq(DataType.Uuid, DataType.Uuid, DataType.Int)
      }

      "fail if query doesn't match one or more of the versions" in {
        val error = schema(parseQuery("SELECT author_id, post_id, post_rating from test.posts"), 1).getError
        error.asInstanceOf[QueryNotCrossCompatible].message // shouldBe "" // TODO
      }
    }

    "query on an old range of versioned schemas" should {
      "work if query matches all versions" in {
        val (Columns(types), _) = schema(parseQuery("SELECT author_id, post_id from test.posts"), 1, 2).get
        types shouldBe Seq(DataType.Uuid, DataType.Uuid)
      }

      "fail if query doesn't match one or more of the versions" in {
        val error = schema(parseQuery("SELECT author_id, post_id, post_rating from test.posts"), 1, 2).getError
        error.asInstanceOf[QueryNotCrossCompatible].message // shouldBe "" // TODO
      }
    }
  }

}
