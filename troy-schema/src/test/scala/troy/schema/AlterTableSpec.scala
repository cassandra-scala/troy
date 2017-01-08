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
import troy.cql.ast._
import troy.cql.parser.ParserTestUtils

class AlterTableSpec extends WordSpec with Matchers {
  import ParserTestUtils._
  import VTestUtils._
  val tableName = TableName(Some(KeyspaceName("test")), "posts")
  val schemaStatements = CqlParser.parseSchema("""
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
   """).get

  val schema = SchemaEngine(schemaStatements).get

  def alter(s: String) = schema + parseSchemaAs[AlterTable](s)

  "Schema" when {
    "adding column" should {
      "support adding one column" in {
        val (row, _) = alter("ALTER TABLE test.posts ADD brandNewColumn decimal static ;")
          .get(parseQuery("SELECT brandNewColumn FROM test.posts;")).get
        row.asInstanceOf[SchemaEngine.Columns].types shouldBe Seq(DataType.Decimal)
      }

      "support multiple columns" in {
        val (row, _) = alter("ALTER TABLE test.posts ADD brandNewColumn decimal, anotherBrandNewColumn text;")
          .get(parseQuery("SELECT brandNewColumn, anotherBrandNewColumn FROM test.posts;")).get
        row.asInstanceOf[SchemaEngine.Columns].types shouldBe Seq(DataType.Decimal, DataType.Text)
      }

      "support adding static columns" in {
        val updatedSchema = alter("ALTER TABLE test.posts ADD brandNewColumn decimal, anotherBrandNewColumn text static ;").get
        updatedSchema(parseQuery("SELECT DISTINCT anotherBrandNewColumn FROM test.posts;")).get
        updatedSchema(parseQuery("SELECT DISTINCT brandNewColumn FROM test.posts;")).getError shouldBe Messages.SelectedDistinctNonStaticColumn("brandNewColumn")
      }
    }

    "dropping a column" should {
      "work if column is not part of Primary key" in {
        alter("ALTER TABLE test.posts DROP post_rating;").get(parseQuery("SELECT post_rating FROM test.posts;")).getErrors //shouldBe Messages.ColumnNotFound("post_rating")
      }

      "refuse removing a non-primary-key-part column" in {
        alter("ALTER TABLE test.posts DROP author_id;").getError shouldBe Messages.CannotDropPrimaryKeyPart("author_id")
      }

      "refuse removing non-existing column" in {
        val e = alter("ALTER TABLE test.posts DROP nonExisting;").getError
        e shouldBe Messages.ColumnNotFound("nonExisting", tableName)
        e.message should include("test.posts")
      }
    }

    "altering column type" should {
      "accept compatible types" in {
        val (row, _) = alter("ALTER TABLE test.posts ALTER post_title TYPE blob;")
          .get(parseQuery("SELECT post_title from test.posts;")).get
        row.asInstanceOf[SchemaEngine.Columns].types shouldBe Seq(DataType.Blob)
      }

      "refuse incompatible types" in {
        alter("ALTER TABLE test.posts ALTER author_name TYPE date;").getError shouldBe Messages.IncompatibleAlterType("author_name", DataType.Text, DataType.Date)
      }
    }

    "adding options" should {
      "ignore them" in {
        alter("ALTER TABLE test.posts WITH comment = 'A most excellent and useful table';").get shouldBe schema
      }
    }
  }
}
