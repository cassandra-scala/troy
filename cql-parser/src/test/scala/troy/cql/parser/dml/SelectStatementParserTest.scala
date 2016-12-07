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

package troy.cql.parser.dml

import java.util.UUID

import org.scalatest._
import troy.cql.ast
import troy.cql.ast._
import troy.cql.ast.dml.Select.OrderBy
import troy.cql.ast.dml.Select.OrderBy.Ordering
import troy.cql.ast.dml.SimpleSelection.ColumnName
import troy.cql.ast.dml.{ Operator, Select }
import troy.cql.ast.dml.WhereClause.Relation.{ Simple, Token, Tupled }
import troy.cql.ast.dml.WhereClause.Relation
import troy.cql.parser.ParserTestUtils.parseQuery

class SelectStatementParserTest extends FlatSpec with Matchers {
  "Select Parser" should "parse simple select statements" in {
    val statement = parseQuery("SELECT name, occupation FROM users;").asInstanceOf[SelectStatement]
    statement.mod.isEmpty shouldBe true
    statement.from.table shouldBe "users"
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse simple select statements with keyspace" in {
    val statement = parseQuery("SELECT name, occupation FROM test.users;").asInstanceOf[SelectStatement]
    statement.from.keyspace.get.name shouldBe "test"
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse simple select statements with order by" in {
    val statement = parseQuery("SELECT name, occupation FROM test.users ORDER BY name DESC;").asInstanceOf[SelectStatement]
    statement.from.keyspace.get.name shouldBe "test"
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isDefined shouldBe true
    statement.orderBy.get.orderings.size shouldBe 1

    val orderings = statement.orderBy.get.orderings(0)
    orderings.columnName.name shouldBe "name"
    orderings.direction.isDefined shouldBe true
    orderings.direction.get shouldBe OrderBy.Descending

    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse simple select statements with JSON" in {
    val statement = parseQuery("SELECT JSON name, occupation FROM users;").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "users"
    statement.mod.get shouldBe Select.Json
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse simple select statements with DISTINCT" in {
    val statement = parseQuery("SELECT DISTINCT name, occupation FROM users;").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "users"
    statement.mod.get shouldBe Select.Distinct
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse asterisk select statements" in {
    val statement = parseQuery("SELECT * FROM users;").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false
    statement.selection shouldBe Select.Asterisk
  }

  it should "parse select statements with count selector" in {
    val statement = parseQuery("SELECT COUNT (*) AS user_count FROM users;").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 1

    selection.items(0).selector shouldBe Select.Count
    selection.items(0).as.get shouldBe "user_count"
  }

  it should "parse select statements with column name as selector" in {
    val statement = parseQuery("SELECT name AS user_name, occupation AS user_occupation FROM users;").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector.asInstanceOf[Select.ColumnName].name shouldBe "name"
    selection.items(0).as.get shouldBe "user_name"

    selection.items(1).selector.asInstanceOf[Select.ColumnName].name shouldBe "occupation"
    selection.items(1).as.get shouldBe "user_occupation"
  }

  it should "parse select statements with function name selector" ignore {
    val statement = parseQuery("SELECT intAsBlob(4) FROM t;").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "t"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[Select.Function]

    selector.functionName shouldBe "intAsBlob"
    val params = selector.params.asInstanceOf[Select.SelectTerm]
    params shouldBe BlobConstant("4") //TODO: Term Constant need refactor
    selection.items(0).as.isEmpty shouldBe true
  }

  it should "parse select statements with function name selector and as" ignore {
    val statement = parseQuery("SELECT intAsBlob(4) AS four FROM t;").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "t"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[Select.Function]

    selector.functionName shouldBe "intAsBlob"
    val params = selector.params.asInstanceOf[Select.SelectTerm]
    params shouldBe BlobConstant("4") //TODO: Term Constant need refactor
    selection.items(0).as.get shouldBe "four"
  }

  it should "parse simple LIMIT clause" in {
    val statement = parseQuery("SELECT name, occupation FROM users LIMIT 1;").asInstanceOf[SelectStatement]
    statement.limit.get shouldBe Select.LimitValue("1")
    statement.allowFiltering shouldBe false
    statement.selection.asInstanceOf[Select.SelectClause].items.size shouldBe 2
  }

  it should "parse simple limit (lowercase) clause" in {
    val statement = parseQuery("SELECT name, occupation FROM users limit 1;").asInstanceOf[SelectStatement]
    statement.limit.get shouldBe Select.LimitValue("1")
    statement.allowFiltering shouldBe false
    statement.selection.asInstanceOf[Select.SelectClause].items.size shouldBe 2
  }

  it should "parse select statements with simple where clause" in {
    val statement = parseQuery("SELECT JSON name, occupation FROM users WHERE userid = 199;").asInstanceOf[SelectStatement]

    statement.from.table shouldBe "users"
    statement.mod.get shouldBe Select.Json
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "userid"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[IntegerConstant].value shouldBe 199
  }

  it should "parse select statements with simple where clause with UUID term" in {
    val statement = parseQuery(
      "SELECT JSON name, occupation FROM users WHERE userid = 01234567-0123-0123-0123-0123456789ab;"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "users"
    statement.mod.get shouldBe Select.Json
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "userid"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe UuidConstant(UUID.fromString("01234567-0123-0123-0123-0123456789ab"))
  }

  it should "parse select statements with simple where clause with float term" in {
    val statement = parseQuery(
      "SELECT JSON name, occupation FROM users WHERE weight = 50.4;"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "users"
    statement.mod.get shouldBe Select.Json
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "weight"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[FloatConstant].value shouldBe 50.4f
  }

  it should "parse select statements with simple where clause with escaped quote" in {
    val statement = parseQuery(
      "SELECT entry_title, content FROM posts WHERE blog_title='John''s Blog';"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "posts"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "blog_title"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe StringConstant("John's Blog")
  }

  it should "parse select statements with simple where clause with multiple escaped quote" in {
    val statement = parseQuery(
      "SELECT entry_title, content FROM posts WHERE blog_title='This isn''t John''s Blog';"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "posts"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "blog_title"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe StringConstant("This isn't John's Blog")
  }

  it should "parse select statements with where IN clause" in {
    val statement = parseQuery("SELECT name, occupation FROM users WHERE userid IN (199, 200, 207);").asInstanceOf[SelectStatement]

    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector.asInstanceOf[Select.ColumnName].name shouldBe "name"
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector.asInstanceOf[Select.ColumnName].name shouldBe "occupation"
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    val relation = relations(0).asInstanceOf[Simple]
    relation.columnName shouldBe "userid"
    relation.operator shouldBe Operator.In
    val tupleLiteral = relation.term.asInstanceOf[TupleLiteral]
    tupleLiteral.values.size shouldBe 3
    tupleLiteral.values(0) shouldBe IntegerConstant(199)
    tupleLiteral.values(1) shouldBe IntegerConstant(200)
    tupleLiteral.values(2) shouldBe IntegerConstant(207)
  }

  it should "parse select statements with where clause and = > <= operators" in {
    val statement = parseQuery("SELECT time, value FROM events WHERE event_type = 'myEvent' AND time > '2011-02-03' AND time <= '2012-01-01';").asInstanceOf[SelectStatement]

    statement.from.table shouldBe "events"
    statement.mod.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("time")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("value")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 3
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "event_type"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "myEvent"

    relations(1).asInstanceOf[Relation.Simple].columnName shouldBe "time"
    relations(1).asInstanceOf[Relation.Simple].operator shouldBe Operator.GreaterThan
    relations(1).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "2011-02-03"

    relations(2).asInstanceOf[Relation.Simple].columnName shouldBe "time"
    relations(2).asInstanceOf[Relation.Simple].operator shouldBe Operator.LessThanOrEqual
    relations(2).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "2012-01-01"

  }

  it should "parse select statements with where clause and != >= < operators" in {
    val statement = parseQuery("SELECT entry_title, content FROM posts WHERE userid = 'john doe' AND blog_title!='Spam' AND posted_at >= '2012-01-01' AND posted_at < '2012-01-31';").asInstanceOf[SelectStatement]

    statement.from.table shouldBe "posts"
    statement.mod.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 4
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "userid"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "john doe"

    relations(1).asInstanceOf[Relation.Simple].columnName shouldBe "blog_title"
    relations(1).asInstanceOf[Relation.Simple].operator shouldBe Operator.NotEquals
    relations(1).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "Spam"

    relations(2).asInstanceOf[Relation.Simple].columnName shouldBe "posted_at"
    relations(2).asInstanceOf[Relation.Simple].operator shouldBe Operator.GreaterThanOrEqual
    relations(2).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "2012-01-01"

    relations(3).asInstanceOf[Relation.Simple].columnName shouldBe "posted_at"
    relations(3).asInstanceOf[Relation.Simple].operator shouldBe Operator.LessThan
    relations(3).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "2012-01-31"
  }

  it should "parse asterisk select statements with where clause token" in {
    val statement = parseQuery("SELECT * FROM posts WHERE token(userid) > token('tom') AND token(userid) < token('bob');").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "posts"
    statement.mod.isEmpty shouldBe true
    statement.where.isDefined shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false
    statement.selection shouldBe Select.Asterisk

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 2
    val relation1 = relations(0).asInstanceOf[Token]
    relation1.columnNames.size shouldBe 1
    relation1.columnNames(0) shouldBe "userid"
    relation1.operator shouldBe Operator.GreaterThan
    relation1.term.asInstanceOf[FunctionCall].functionName shouldBe "token"
    relation1.term.asInstanceOf[FunctionCall].params.size shouldBe 1
    relation1.term.asInstanceOf[FunctionCall].params(0) shouldBe StringConstant("tom")

    val relation2 = relations(1).asInstanceOf[Token]
    relation2.columnNames.size shouldBe 1
    relation2.columnNames(0) shouldBe "userid"
    relation2.operator shouldBe Operator.LessThan
    relation2.term.asInstanceOf[FunctionCall].functionName shouldBe "token"
    relation2.term.asInstanceOf[FunctionCall].params.size shouldBe 1
    relation2.term.asInstanceOf[FunctionCall].params(0) shouldBe StringConstant("bob")

  }

  it should "parse asterisk select statements with where clause tupled literal424" in {
    val statement = parseQuery("SELECT * FROM posts WHERE userid = 'john doe' AND (blog_title, posted_at) > ('Johns Blog', '2012-01-01');").asInstanceOf[SelectStatement]
    statement.from.table shouldBe "posts"
    statement.mod.isEmpty shouldBe true
    statement.where.isDefined shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false
    statement.selection shouldBe Select.Asterisk

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 2

    val relation1 = relations(0).asInstanceOf[Simple]
    relation1.columnName shouldBe "userid"
    relation1.operator shouldBe Operator.Equals
    relation1.term.asInstanceOf[StringConstant].value shouldBe "john doe"

    val relation2 = relations(1).asInstanceOf[Tupled]
    relation2.columnNames.size shouldBe 2
    relation2.columnNames(0) shouldBe "blog_title"
    relation2.columnNames(1) shouldBe "posted_at"
    relation2.operator shouldBe Operator.GreaterThan
    val tupleLiteral: TupleLiteral = relation2.term.asInstanceOf[TupleLiteral]
    tupleLiteral.values.size shouldBe 2
    tupleLiteral.values(0) shouldBe StringConstant("Johns Blog")
    tupleLiteral.values(1) shouldBe StringConstant("2012-01-01")

  }

  it should "select statements with where clause containing anonymous variables" in {
    val statement = parseQuery("SELECT author_id FROM test.posts WHERE author_name = ?;").asInstanceOf[SelectStatement]

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "author_name"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe BindMarker.Anonymous
  }

  it should "select statements with where clause containing named variables" in {
    val statement = parseQuery("SELECT author_id FROM test.posts WHERE author_name = :x;").asInstanceOf[SelectStatement]

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "author_name"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[BindMarker.Named].name shouldBe "x"
  }

  it should "parse select statements with where clause and allow filtering" in {
    val statement = parseQuery("SELECT time, value FROM events WHERE event_type = 'myEvent' AND time > '2011-02-03' AND time <= '2012-01-01' ALLOW FILTERING;").asInstanceOf[SelectStatement]

    statement.from.table shouldBe "events"
    statement.mod.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe true

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("time")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("value")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 3
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "event_type"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "myEvent"

    relations(1).asInstanceOf[Relation.Simple].columnName shouldBe "time"
    relations(1).asInstanceOf[Relation.Simple].operator shouldBe Operator.GreaterThan
    relations(1).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "2011-02-03"

    relations(2).asInstanceOf[Relation.Simple].columnName shouldBe "time"
    relations(2).asInstanceOf[Relation.Simple].operator shouldBe Operator.LessThanOrEqual
    relations(2).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "2012-01-01"

  }

  it should "parse select statements with where clause and LIKE operator include term" in {
    val statement = parseQuery(
      "SELECT entry_title, content FROM posts WHERE blog_title LIKE '%John%';"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "posts"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "blog_title"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Like
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "%John%"
  }

  it should "parse select statements with where clause and LIKE operator start with term" in {
    val statement = parseQuery(
      "SELECT entry_title, content FROM posts WHERE blog_title LIKE 'John%';"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "posts"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "blog_title"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Like
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "John%"
  }

  it should "parse select statements with where clause and LIKE operator end with term" in {
    val statement = parseQuery(
      "SELECT entry_title, content FROM posts WHERE blog_title LIKE '%n';"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "posts"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "blog_title"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Like
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[StringConstant].value shouldBe "%n"
  }
  // Null is case-insensitive
  it should "parse select statements with where clause with NULL term" in {
    val statement = parseQuery("SELECT name, occupation FROM users WHERE occupation = NULL;").asInstanceOf[SelectStatement]

    statement.from.table shouldBe "users"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "occupation"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe NullConstant
  }

  it should "parse select statements with where clause and Boolean term" in {
    val statement = parseQuery(
      "SELECT entry_title, content FROM posts WHERE published = true;"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "posts"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "published"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[BooleanConstant].value shouldBe true
  }

  // NaN is case-sensitive
  it should "parse select statements with simple where clause with float term and NaN value" in {
    val statement = parseQuery(
      "SELECT name, occupation FROM users WHERE weight = NaN;"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "users"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "weight"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe NaN
  }

  // Infinity is case-sensitive
  it should "parse select statements with simple where clause with float term and Infinity value" in {
    val statement = parseQuery(
      "SELECT name, occupation FROM users WHERE weight = Infinity;"
    ).asInstanceOf[SelectStatement]

    statement.from.table shouldBe "users"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe Select.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe Select.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName shouldBe "weight"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe Infinity
  }

  // NaN and Infinity parse test
  it should "parse select statements with simple where clause with float term and NaN" in {
    CqlParser.parseDML(
      "SELECT name, occupation FROM users WHERE weight = nan;"
    ).asInstanceOf[CqlParser.Failure]
  }

  it should "parse select statements with simple where clause with float term and Infinity" in {
    CqlParser.parseDML(
      "SELECT name, occupation FROM users WHERE weight = infinity;"
    ).asInstanceOf[CqlParser.Failure]
  }
  //
  it should "parse select statements with function name selector and column name" in {
    val statement = parseQuery(
      "SELECT dateOf(x) FROM system.local;"
    ).asInstanceOf[SelectStatement]

    statement.from.keyspace.get.name shouldBe "system"
    statement.from.table shouldBe "local"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[Select.Function]

    selector.functionName.keyspace shouldBe None
    selector.functionName.table shouldBe "dateOf"
    selector.params.seq(0) shouldBe Select.ColumnName("x")
  }

  it should "parse select statements with function name selector, cast and as" in {
    val statement = parseQuery(
      "SELECT dateOf(cast(now() AS timeuuid)) from system.local;"
    ).asInstanceOf[SelectStatement]

    statement.from.keyspace.get.name shouldBe "system"
    statement.from.table shouldBe "local"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[Select.Function]

    selector.functionName.keyspace shouldBe None
    selector.functionName.table shouldBe "dateOf"
    val params = selector.params.seq(0).asInstanceOf[Select.Cast]

    val fn = params.selector.asInstanceOf[Select.Function]
    fn.functionName.keyspace shouldBe None
    fn.functionName.table shouldBe "now"
    params.as shouldBe DataType.Timeuuid
  }

  it should "parse select statements with function name selector and asterisk " in {
    val statement = parseQuery(
      "SELECT bigintAsBlob(count(*)) from system.local;"
    ).asInstanceOf[SelectStatement]

    statement.from.keyspace.get.name shouldBe "system"
    statement.from.table shouldBe "local"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[Select.Function]

    selector.functionName.keyspace shouldBe None
    selector.functionName.table shouldBe "bigintAsBlob"
    selector.params.seq(0) shouldBe Select.Count
    selection.items(0).as.isEmpty shouldBe true
  }

  it should "parse select statements with function name selector and cast" in {
    val statement = parseQuery(
      "SELECT cast(count(*) AS int) from system.local;"
    ).asInstanceOf[SelectStatement]

    statement.from.keyspace.get.name shouldBe "system"
    statement.from.table shouldBe "local"
    statement.mod.isDefined shouldBe false
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[Select.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[Select.Cast]

    selector.selector shouldBe Select.Count
    selector.as shouldBe DataType.Int
  }
}
