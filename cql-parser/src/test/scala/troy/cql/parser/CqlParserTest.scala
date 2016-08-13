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

package troy.cql.parser

import org.scalatest._
import troy.cql.ast._
import troy.cql.ast.SelectStatement.WhereClause.{ Operator, Relation }

class CqlParserTest extends FlatSpec with Matchers {

  "Cql Parser" should "parse simple select statements" in {
    val statement = parseQuery("SELECT author_id, author_name, post_id, post_title FROM test.posts;")
    statement.isJson shouldBe false
    statement.from.keyspace.get.name shouldBe "test"
    statement.from.table shouldBe "posts"
    statement.where shouldBe None
    statement.orderBy shouldBe None
    statement.limit shouldBe None
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.Selection]
    selection.isDistinct shouldBe false

    val selectionList = selection.selectionList.asInstanceOf[SelectStatement.SelectionItems].items
    selectionList.size shouldBe 4
    selectionList(0).selector shouldBe SelectStatement.ColumnName("author_id")
    selectionList(1).selector shouldBe SelectStatement.ColumnName("author_name")
    selectionList(2).selector shouldBe SelectStatement.ColumnName("post_id")
    selectionList(3).selector shouldBe SelectStatement.ColumnName("post_title")
    selectionList.map(_.as).forall(_.isEmpty) shouldBe true
  }

  it should "select statements with simple where clause" in {
    val statement = parseQuery("SELECT author_id FROM test.posts WHERE author_name = 'test';")

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].identifier shouldBe "author_name"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "test"
  }

  it should "select statements with where clause containing anonymous variables" in {
    val statement = parseQuery("SELECT author_id FROM test.posts WHERE author_name = ?;")

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].identifier shouldBe "author_name"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe Term.BindMarker.Anonymous
  }

  it should "select statements with where clause containing named variables" in {
    val statement = parseQuery("SELECT author_id FROM test.posts WHERE author_name = :x;")

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].identifier shouldBe "author_name"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.BindMarker.Named].name shouldBe "x"
  }

  it should "parse create keyspace" in {
    val statement = parseSchemaAs[CreateKeyspace]("create KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'}; ")

    statement.ifNotExists shouldBe false
    statement.keyspaceName.name shouldBe "test"
    statement.properties shouldBe Seq(CreateKeyspace.Replication(Seq(("class", "SimpleStrategy"), ("replication_factor", "1"))))
  }

  it should "parse simple create table" in {
    val statement = parseSchemaAs[CreateTable]("CREATE TABLE test.posts (author_id text PRIMARY KEY);")
    statement.ifNotExists shouldBe false
    statement.tableName.keyspace.get.name shouldBe "test"
    statement.tableName.table shouldBe "posts"
    statement.columns.size shouldBe 1
    statement.columns.head.dataType shouldBe DataType.text
    statement.columns.head.isPrimaryKey shouldBe true
    statement.columns.head.isStatic shouldBe false
    statement.columns.head.name shouldBe "author_id"
  }

  it should "parse create index" in {
    val stmt1 = parseSchemaAs[CreateIndex]("CREATE INDEX userIndex ON NerdMovies (user);")
    stmt1.isCustom shouldBe false
    stmt1.ifNotExists shouldBe false
    stmt1.indexName shouldBe Some("userIndex")
    stmt1.tableName.keyspace.isEmpty shouldBe true
    stmt1.tableName.table shouldBe "NerdMovies"
    stmt1.identifier.asInstanceOf[CreateIndex.Identifier].value shouldBe "user"
    stmt1.using.isEmpty shouldBe true

    val stmt2 = parseSchemaAs[CreateIndex]("CREATE INDEX ON Mutants (abilityId);")
    stmt2.isCustom shouldBe false
    stmt2.ifNotExists shouldBe false
    stmt2.indexName shouldBe None
    stmt2.tableName.keyspace.isEmpty shouldBe true
    stmt2.tableName.table shouldBe "Mutants"
    stmt2.identifier.asInstanceOf[CreateIndex.Identifier].value shouldBe "abilityId"
    stmt2.using.isEmpty shouldBe true

    val stmt3 = parseSchemaAs[CreateIndex]("CREATE INDEX ON users (keys(favs));")
    stmt3.isCustom shouldBe false
    stmt3.ifNotExists shouldBe false
    stmt3.indexName shouldBe None
    stmt3.tableName.keyspace.isEmpty shouldBe true
    stmt3.tableName.table shouldBe "users"
    stmt3.identifier.asInstanceOf[CreateIndex.Keys].of shouldBe "favs"
    stmt3.using.isEmpty shouldBe true

    val stmt4 = parseSchemaAs[CreateIndex]("CREATE CUSTOM INDEX ON users (email) USING 'path.to.the.IndexClass';")
    stmt4.isCustom shouldBe true
    stmt4.ifNotExists shouldBe false
    stmt4.indexName shouldBe None
    stmt4.tableName.keyspace.isEmpty shouldBe true
    stmt4.tableName.table shouldBe "users"
    stmt4.identifier.asInstanceOf[CreateIndex.Identifier].value shouldBe "email"
    stmt4.using.get.using shouldBe "path.to.the.IndexClass"
    stmt4.using.get.options.isEmpty shouldBe true

    val stmt5 = parseSchemaAs[CreateIndex]("CREATE CUSTOM INDEX ON users (email) USING 'path.to.the.IndexClass' WITH OPTIONS = {'storage': '/mnt/ssd/indexes/'};")
    stmt5.isCustom shouldBe true
    stmt5.ifNotExists shouldBe false
    stmt5.indexName shouldBe None
    stmt5.tableName.keyspace.isEmpty shouldBe true
    stmt5.tableName.table shouldBe "users"
    stmt5.identifier.asInstanceOf[CreateIndex.Identifier].value shouldBe "email"
    stmt5.using.get.using shouldBe "path.to.the.IndexClass"
    stmt5.using.get.options.get.size shouldBe 1
    // TODO: Map-Literal
  }

  it should "parse multiline create table" in {
    val statement = parseSchemaAs[CreateTable](
      """
        CREATE TABLE test.posts (
          author_id text PRIMARY KEY
        );
      """
    )
    statement.ifNotExists shouldBe false
    statement.columns.size shouldBe 1
    statement.columns.head.dataType shouldBe DataType.text
    statement.columns.head.isPrimaryKey shouldBe true
    statement.columns.head.isStatic shouldBe false
    statement.columns.head.name shouldBe "author_id"
  }

  it should "parse multi-fields create table" in {
    val statement = parseSchemaAs[CreateTable](
      """
        CREATE TABLE test.posts (
          author_id text PRIMARY KEY,
          author_name text static,
          author_age int static,
          post_id text,
          post_title text
        );
      """
    )
    statement.ifNotExists shouldBe false
    statement.columns shouldBe Seq(
      CreateTable.Column("author_id", DataType.text, false, true),
      CreateTable.Column("author_name", DataType.text, true, false),
      CreateTable.Column("author_age", DataType.int, true, false),
      CreateTable.Column("post_id", DataType.text, false, false),
      CreateTable.Column("post_title", DataType.text, false, false)
    )
    statement.primaryKey.isEmpty shouldBe true // Primary is defined inline instead
    statement.options.isEmpty shouldBe true
  }

  it should "parse create table with clustering columns" in {
    val statement = parseSchemaAs[CreateTable](
      """
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
    statement.ifNotExists shouldBe false
    statement.columns shouldBe Seq(
      CreateTable.Column("author_id", DataType.text, false, false), // Yes, it is not THE primary key, only a partition key
      CreateTable.Column("author_name", DataType.text, true, false),
      CreateTable.Column("author_age", DataType.int, true, false),
      CreateTable.Column("post_id", DataType.text, false, false),
      CreateTable.Column("post_title", DataType.text, false, false)
    )
    statement.primaryKey.get.partitionKeys shouldBe Seq("author_id")
    statement.primaryKey.get.clusteringColumns shouldBe Seq("post_id")
  }

  it should "parse different primary key styles" in {
    def pk(pk: String) =
      parseSchemaAs[CreateTable](s"CREATE TABLE test.posts ( author_id text, author_name text, post_id text, post_title text, PRIMARY KEY $pk);").primaryKey.get

    pk("((author_id), post_id)") shouldBe CreateTable.PrimaryKey(Seq("author_id"), Seq("post_id"))
    pk("(author_id, post_id)") shouldBe CreateTable.PrimaryKey(Seq("author_id"), Seq("post_id"))
    pk("((author_id, author_name), post_id)") shouldBe CreateTable.PrimaryKey(Seq("author_id", "author_name"), Seq("post_id"))
    pk("((author_id, author_name), post_id, post_title)") shouldBe CreateTable.PrimaryKey(Seq("author_id", "author_name"), Seq("post_id", "post_title"))
    pk("(author_id, post_id, post_title)") shouldBe CreateTable.PrimaryKey(Seq("author_id"), Seq("post_id", "post_title"))
  }

  it should "parse multiple statements " in {
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
      CreateKeyspace(false, KeyspaceName("test"), Seq(CreateKeyspace.Replication(Seq(("class", "SimpleStrategy"), ("replication_factor", "1"))))),
      CreateTable(false, TableName(Some(KeyspaceName("test")), "posts"), Seq(
        CreateTable.Column("author_id", DataType.text, false, false),
        CreateTable.Column("author_name", DataType.text, true, false),
        CreateTable.Column("author_age", DataType.int, true, false),
        CreateTable.Column("post_id", DataType.text, false, false),
        CreateTable.Column("post_title", DataType.text, false, false)
      ), Some(CreateTable.PrimaryKey(Seq("author_id"), Seq("post_id"))), Nil)
    )

  }

  def parseQuery(statement: String) =
    CqlParser
      .parseQuery(statement)
      .get

  def parseSchema(statement: String) =
    CqlParser.parseSchema(statement) match {
      case CqlParser.Success(res, _)    => res
      case CqlParser.Failure(msg, next) => fail(s"Parse Failure: $msg, line = ${next.pos.line}, column = ${next.pos.column}")
    }

  def parseSchemaAs[T](statement: String) =
    parseSchema(statement)
      .head
      .asInstanceOf[T]
}
