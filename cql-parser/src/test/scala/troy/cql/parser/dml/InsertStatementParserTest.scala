package troy.cql.parser.dml

import org.scalatest.{ FlatSpec, Matchers }
import troy.cql.ast.dml.{ InsertStatement, Timestamp, Ttl, UpdateValue }
import troy.cql.ast._
import org.scalatest._

class InsertStatementParserTest extends FlatSpec with Matchers {

  "Insert Parser" should "parse simple insert statement" in {
    val statement = parseQuery("INSERT INTO NerdMovies (movie, director, main_actor, year) VALUES ('Serenity', 'Joss Whedon', 'Nathan Fillion', 2005);").asInstanceOf[InsertStatement]
    statement.into.table shouldBe "NerdMovies"

    val insertClause = statement.insertClause.asInstanceOf[InsertStatement.NamesValues]
    val names: Seq[Identifier] = insertClause.columnNames
    names.size shouldBe 4
    names(0) shouldBe "movie"
    names(1) shouldBe "director"
    names(2) shouldBe "main_actor"
    names(3) shouldBe "year"

    val values = insertClause.values.asInstanceOf[TupleLiteral].values
    values.size shouldBe 4
    values(0).asInstanceOf[Constant].raw shouldBe "Serenity"
    values(1).asInstanceOf[Constant].raw shouldBe "Joss Whedon"
    values(2).asInstanceOf[Constant].raw shouldBe "Nathan Fillion"
    values(3).asInstanceOf[Constant].raw shouldBe "2005"

    statement.ifNotExists shouldBe false
    statement.using.isEmpty shouldBe true
  }

  it should "parse simple insert statement with if not exists" in {
    val statement = parseQuery("INSERT INTO NerdMovies (movie, director, main_actor, year) VALUES ('Serenity', 'Joss Whedon', 'Nathan Fillion', 2005) IF NOT EXISTS ;").asInstanceOf[InsertStatement]
    statement.into.table shouldBe "NerdMovies"

    val insertClause = statement.insertClause.asInstanceOf[InsertStatement.NamesValues]
    val names: Seq[Identifier] = insertClause.columnNames
    names.size shouldBe 4
    names(0) shouldBe "movie"
    names(1) shouldBe "director"
    names(2) shouldBe "main_actor"
    names(3) shouldBe "year"

    val values = insertClause.values.asInstanceOf[TupleLiteral].values
    values.size shouldBe 4
    values(0).asInstanceOf[Constant].raw shouldBe "Serenity"
    values(1).asInstanceOf[Constant].raw shouldBe "Joss Whedon"
    values(2).asInstanceOf[Constant].raw shouldBe "Nathan Fillion"
    values(3).asInstanceOf[Constant].raw shouldBe "2005"

    statement.ifNotExists shouldBe true
    statement.using.isEmpty shouldBe true
  }

  it should "parse simple insert statement with using TTL update paramter" in {
    val statement = parseQuery("INSERT INTO NerdMovies (movie, director, main_actor, year) VALUES ('Serenity', 'Joss Whedon', 'Nathan Fillion', 2005) USING TTL 86400;").asInstanceOf[InsertStatement]
    statement.into.table shouldBe "NerdMovies"

    val insertClause = statement.insertClause.asInstanceOf[InsertStatement.NamesValues]
    val names: Seq[Identifier] = insertClause.columnNames
    names.size shouldBe 4
    names(0) shouldBe "movie"
    names(1) shouldBe "director"
    names(2) shouldBe "main_actor"
    names(3) shouldBe "year"

    val values = insertClause.values.asInstanceOf[TupleLiteral].values
    values.size shouldBe 4
    values(0).asInstanceOf[Constant].raw shouldBe "Serenity"
    values(1).asInstanceOf[Constant].raw shouldBe "Joss Whedon"
    values(2).asInstanceOf[Constant].raw shouldBe "Nathan Fillion"
    values(3).asInstanceOf[Constant].raw shouldBe "2005"

    statement.ifNotExists shouldBe false
    statement.using.isDefined shouldBe true

    statement.using.size shouldBe 1
    val ttl = statement.using.get(0).asInstanceOf[Ttl]
    ttl.value.asInstanceOf[UpdateValue].value shouldBe "86400"
  }

  it should "parse simple insert statement with using TIMESTAMP update paramter" in {
    val statement = parseQuery("INSERT INTO NerdMovies (movie, director, main_actor, year) VALUES ('Serenity', 'Joss Whedon', 'Nathan Fillion', 2005) USING TIMESTAMP 86400;").asInstanceOf[InsertStatement]
    statement.into.table shouldBe "NerdMovies"

    val insertClause = statement.insertClause.asInstanceOf[InsertStatement.NamesValues]
    val names: Seq[Identifier] = insertClause.columnNames
    names.size shouldBe 4
    names(0) shouldBe "movie"
    names(1) shouldBe "director"
    names(2) shouldBe "main_actor"
    names(3) shouldBe "year"

    val values = insertClause.values.asInstanceOf[TupleLiteral].values
    values.size shouldBe 4
    values(0).asInstanceOf[Constant].raw shouldBe "Serenity"
    values(1).asInstanceOf[Constant].raw shouldBe "Joss Whedon"
    values(2).asInstanceOf[Constant].raw shouldBe "Nathan Fillion"
    values(3).asInstanceOf[Constant].raw shouldBe "2005"

    statement.ifNotExists shouldBe false
    statement.using.isDefined shouldBe true

    statement.using.size shouldBe 1
    val ttl = statement.using.get(0).asInstanceOf[Timestamp]
    ttl.value.asInstanceOf[UpdateValue].value shouldBe "86400"
  }

  it should "parse simple insert statement with json clause" in {
    val statement = parseQuery("INSERT INTO NerdMovies JSON '{\"movie\": \"Serenity\", \"director\": \"Joss Whedon\", \"year\": 2005}';").asInstanceOf[InsertStatement]
    statement.into.table shouldBe "NerdMovies"

    val insertClause = statement.insertClause.asInstanceOf[InsertStatement.JsonClause]
    insertClause.value shouldBe "{\"movie\": \"Serenity\", \"director\": \"Joss Whedon\", \"year\": 2005}"
    insertClause.default.isEmpty shouldBe true

    statement.ifNotExists shouldBe false
    statement.using.isEmpty shouldBe true
  }

  it should "parse simple insert statement with json clause with default null" in {
    val statement = parseQuery("INSERT INTO NerdMovies JSON '{\"movie\": \"Serenity\", \"director\": \"Joss Whedon\", \"year\": 2005}' DEFAULT NULL;").asInstanceOf[InsertStatement]
    statement.into.table shouldBe "NerdMovies"

    val insertClause = statement.insertClause.asInstanceOf[InsertStatement.JsonClause]
    insertClause.value shouldBe "{\"movie\": \"Serenity\", \"director\": \"Joss Whedon\", \"year\": 2005}"
    insertClause.default.isDefined shouldBe true
    insertClause.default.get shouldBe InsertStatement.NullValue
    statement.ifNotExists shouldBe false
    statement.using.isEmpty shouldBe true
  }

  it should "parse simple insert statement with json clause with default unset" in {
    val statement = parseQuery("INSERT INTO NerdMovies JSON '{\"movie\": \"Serenity\", \"director\": \"Joss Whedon\", \"year\": 2005}' DEFAULT UNSET;").asInstanceOf[InsertStatement]
    statement.into.table shouldBe "NerdMovies"

    val insertClause = statement.insertClause.asInstanceOf[InsertStatement.JsonClause]
    insertClause.value shouldBe "{\"movie\": \"Serenity\", \"director\": \"Joss Whedon\", \"year\": 2005}"
    insertClause.default.isDefined shouldBe true
    insertClause.default.get shouldBe InsertStatement.Unset
    statement.ifNotExists shouldBe false
    statement.using.isEmpty shouldBe true
  }
  def parseQuery(statement: String) =
    CqlParser
      .parseDML(statement) match {
        case CqlParser.Success(res, _)    => res
        case CqlParser.Failure(msg, next) => fail(s"Parse Failure: $msg, line = ${next.pos.line}, column = ${next.pos.column}")
      }
}
