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
import troy.cql.ast.SelectStatement.WhereClause.{ Operator, Relation }
import troy.cql.ast._

class SelectParserTest extends FlatSpec with Matchers {
  // SELECT name, occupation FROM users
  // SELECT name, occupation FROM test.users
  // SELECT JSON name, occupation FROM users
  // SELECT DISTINCT name, occupation FROM users
  // SELECT * FROM users;
  // SELECT COUNT (*) AS user_count FROM users;
  // SELECT intAsBlob(4) FROM t;
  // SELECT intAsBlob(4) AS four FROM t;
  // SELECT name AS user_name, occupation AS user_occupation FROM users;

  "Cql Parser" should "parse simple select statements" in {
    val statement = parseQuery("SELECT name, occupation FROM users;")
    statement.mod.isEmpty shouldBe true
    statement.from.table shouldBe "users"
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse simple select statements with keyspace" in {
    val statement = parseQuery("SELECT name, occupation FROM test.users;")
    statement.from.keyspace.get.name shouldBe "test"
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse simple select statements with JOSN" in {
    val statement = parseQuery("SELECT JSON name, occupation FROM users;")
    statement.from.table shouldBe "users"
    statement.mod.get shouldBe SelectStatement.Json
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse simple select statements with DISTINCT" in {
    val statement = parseQuery("SELECT DISTINCT name, occupation FROM users;")
    statement.from.table shouldBe "users"
    statement.mod.get shouldBe SelectStatement.Distinct
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true
  }

  it should "parse asterisk select statements" in {
    val statement = parseQuery("SELECT * FROM users;")
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false
    statement.selection shouldBe SelectStatement.Asterisk
  }

  it should "parse select statements with count selector" in {
    val statement = parseQuery("SELECT COUNT (*) AS user_count FROM users;")
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 1

    selection.items(0).selector shouldBe SelectStatement.Count
    selection.items(0).as.isEmpty shouldBe true
  }

  it should "parse select statements with column name as selector" in {
    val statement = parseQuery("SELECT name AS user_name, occupation AS user_occupation FROM users;")
    statement.from.table shouldBe "users"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName
    selection.items(0).as.get shouldBe "user_name"

    selection.items(1).selector shouldBe SelectStatement.ColumnName
    selection.items(1).as.get shouldBe "user_occupation"
  }

  it should "parse select statements with function name selector" in {
    val statement = parseQuery("SELECT intAsBlob(4) FROM t;")
    statement.from.table shouldBe "t"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[SelectStatement.Function]

    selector.functionName shouldBe "intAsBlob"
    val params = selector.params.asInstanceOf[SelectStatement.SelectTerm]
    params shouldBe Term.Constant("4") //TODO: Term Constant need refactor
    selection.items(0).as.isEmpty shouldBe true
  }

  it should "parse select statements with function name selector and as" in {
    val statement = parseQuery("SELECT intAsBlob(4) AS four FROM t;")
    statement.from.table shouldBe "t"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 1

    val selector = selection.items(0).selector.asInstanceOf[SelectStatement.Function]

    selector.functionName shouldBe "intAsBlob"
    val params = selector.params.asInstanceOf[SelectStatement.SelectTerm]
    params shouldBe Term.Constant("4") //TODO: Term Constant need refactor
    selection.items(0).as.get shouldBe "four"
  }

  // SELECT JSON name, occupation FROM users WHERE userid = 199;
  // SELECT name, occupation FROM users WHERE userid IN (199, 200, 207);

  // SELECT time, value FROM events
  //    WHERE event_type = 'myEvent'
  //        AND time > '2011-02-03'
  //        AND time <= '2012-01-01'

  // SELECT entry_title, content FROM posts
  //    WHERE userid = 'john doe'
  //      AND blog_title!='Spam'
  //      AND posted_at >= '2012-01-01' AND posted_at < '2012-01-31'

  // SELECT * FROM posts
  //    WHERE token(userid) > token('tom') AND token(userid) < token('bob')

  // SELECT * FROM posts
  //    WHERE userid = 'john doe'
  //      AND (blog_title, posted_at) > ('John''s Blog', '2012-01-01')

  // SELECT time, value FROM events
  //    WHERE event_type = 'myEvent'
  //      AND time > '2011-02-03'
  //      AND time <= '2012-01-01'
  //    ALLOW FILTERING
  it should "parse select statements with simple where clause" in {
    val statement = parseQuery("SELECT JSON name, occupation FROM users WHERE userid = 199;")

    statement.from.table shouldBe "users"
    statement.mod.get shouldBe SelectStatement.Json
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "userid"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "199"
  }

  it should "parse select statements with where IN clause" in {
    val statement = parseQuery("SELECT name, occupation FROM users WHERE userid IN (199, 200, 207);")

    statement.from.table shouldBe "users"
    statement.mod.get shouldBe SelectStatement.Json
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("name")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("occupation")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "userid"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.In
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "(199, 200, 207)" //FIXME
  }

  it should "parse select statements with where clause and = > <= operators" in {
    val statement = parseQuery("SELECT time, value FROM events WHERE event_type = 'myEvent' AND time > '2011-02-03' AND time <= '2012-01-01';")

    statement.from.table shouldBe "events"
    statement.mod.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("time")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("value")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 3
    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "event_type"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "myEvent"

    relations(1).asInstanceOf[Relation.Simple].columnName.name shouldBe "time"
    relations(1).asInstanceOf[Relation.Simple].operator shouldBe Operator.GreaterThan
    relations(1).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "2011-02-03"

    relations(2).asInstanceOf[Relation.Simple].columnName.name shouldBe "time"
    relations(2).asInstanceOf[Relation.Simple].operator shouldBe Operator.LessThanOrEqual
    relations(2).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "2012-01-01"

  }

  it should "parse select statements with where clause and != >= < operators" in {
    val statement = parseQuery("SELECT entry_title, content FROM posts WHERE userid = 'john doe' AND blog_title!='Spam' AND posted_at >= '2012-01-01' AND posted_at < '2012-01-31';")

    statement.from.table shouldBe "posts"
    statement.mod.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("entry_title")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("content")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 4
    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "userid"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "john doe"

    relations(1).asInstanceOf[Relation.Simple].columnName.name shouldBe "blog_title"
    relations(1).asInstanceOf[Relation.Simple].operator shouldBe Operator.NotEquals
    relations(1).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "Spam"

    relations(2).asInstanceOf[Relation.Simple].columnName.name shouldBe "posted_at"
    relations(2).asInstanceOf[Relation.Simple].operator shouldBe Operator.GreaterThanOrEqual
    relations(2).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "2012-01-01"

    relations(3).asInstanceOf[Relation.Simple].columnName.name shouldBe "posted_at"
    relations(3).asInstanceOf[Relation.Simple].operator shouldBe Operator.LessThan
    relations(3).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "2012-01-31"
  }

  it should "parse asterisk select statements with where clause token" in {
    val statement = parseQuery("SELECT * FROM posts WHERE token(userid) > token('tom') AND token(userid) < token('bob');")
    statement.from.table shouldBe "posts"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false
    statement.selection shouldBe SelectStatement.Asterisk

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 2
    relations(0).asInstanceOf[Relation.Token].columnNames.size shouldBe 1
    relations(0).asInstanceOf[Relation.Token].columnNames(0).name shouldBe "userid"
    relations(0).asInstanceOf[Relation.Token].operator shouldBe Operator.GreaterThan
    relations(0).asInstanceOf[Relation.Token].term.asInstanceOf[Term.Constant].raw shouldBe "token('tom')"

    relations(1).asInstanceOf[Relation.Token].columnNames.size shouldBe 1
    relations(1).asInstanceOf[Relation.Token].columnNames(0).name shouldBe "userid"
    relations(1).asInstanceOf[Relation.Token].operator shouldBe Operator.LessThan
    relations(1).asInstanceOf[Relation.Token].term.asInstanceOf[Term.Constant].raw shouldBe "token('bob')"

  }

  it should "parse asterisk select statements with where clause tupled" in {
    val statement = parseQuery("SELECT * FROM posts WHERE userid = 'john doe' AND (blog_title, posted_at) > ('John''s Blog', '2012-01-01');")
    statement.from.table shouldBe "posts"
    statement.mod.isEmpty shouldBe true
    statement.where.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe false
    statement.selection shouldBe SelectStatement.Asterisk

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 2

    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "userid"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "john doe"

    relations(1).asInstanceOf[Relation.Tupled].columnNames.size shouldBe 2
    relations(1).asInstanceOf[Relation.Tupled].columnNames(0).name shouldBe "blog_title"
    relations(1).asInstanceOf[Relation.Tupled].columnNames(1).name shouldBe "posted_at"
    relations(1).asInstanceOf[Relation.Tupled].operator shouldBe Operator.GreaterThan
    relations(1).asInstanceOf[Relation.Tupled].term.asInstanceOf[Term.Constant].raw shouldBe "('John''s Blog', '2012-01-01')"
  }

  it should "select statements with where clause containing anonymous variables" in {
    val statement = parseQuery("SELECT author_id FROM test.posts WHERE author_name = ?;")

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "author_name"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term shouldBe Term.BindMarker.Anonymous
  }

  it should "select statements with where clause containing named variables" in {
    val statement = parseQuery("SELECT author_id FROM test.posts WHERE author_name = :x;")

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 1
    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "author_name"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.BindMarker.Named].name shouldBe "x"
  }

  it should "parse select statements with where clause and allow filtering" in {
    val statement = parseQuery("SELECT time, value FROM events WHERE event_type = 'myEvent' AND time > '2011-02-03' AND time <= '2012-01-01' ALLOW FILTERING;")

    statement.from.table shouldBe "events"
    statement.mod.isEmpty shouldBe true
    statement.orderBy.isEmpty shouldBe true
    statement.perPartitionLimit.isEmpty shouldBe true
    statement.limit.isEmpty shouldBe true
    statement.allowFiltering shouldBe true

    val selection = statement.selection.asInstanceOf[SelectStatement.SelectClause]
    selection.items.size shouldBe 2

    selection.items(0).selector shouldBe SelectStatement.ColumnName("time")
    selection.items(0).as.isEmpty shouldBe true

    selection.items(1).selector shouldBe SelectStatement.ColumnName("value")
    selection.items(1).as.isEmpty shouldBe true

    statement.where.isDefined shouldBe true
    val relations = statement.where.get.relations
    relations.size shouldBe 3
    relations(0).asInstanceOf[Relation.Simple].columnName.name shouldBe "event_type"
    relations(0).asInstanceOf[Relation.Simple].operator shouldBe Operator.Equals
    relations(0).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "myEvent"

    relations(1).asInstanceOf[Relation.Simple].columnName.name shouldBe "time"
    relations(1).asInstanceOf[Relation.Simple].operator shouldBe Operator.GreaterThan
    relations(1).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "2011-02-03"

    relations(2).asInstanceOf[Relation.Simple].columnName.name shouldBe "time"
    relations(2).asInstanceOf[Relation.Simple].operator shouldBe Operator.LessThanOrEqual
    relations(2).asInstanceOf[Relation.Simple].term.asInstanceOf[Term.Constant].raw shouldBe "2012-01-01"

  }

  def parseQuery(statement: String) =
    CqlParser
      .parseQuery(statement) match {
        case CqlParser.Success(res, _)    => res
        case CqlParser.Failure(msg, next) => fail(s"Parse Failure: $msg, line = ${next.pos.line}, column = ${next.pos.column}")
      }
}
