package troy.driver

import com.datastax.driver.core.{ Session, Row, ResultSet, Statement }

import scala.concurrent.{ ExecutionContext, Future }

object DSL {
  import JavaConverters._
  import scala.collection.JavaConverters._

  implicit class ExternalDSL_RichStatement(val statement: Statement) extends AnyVal {
    def executeAsync(implicit session: Session, executionContext: ExecutionContext): Future[ResultSet] =
      session.executeAsync(statement).asScala

    def execute(implicit session: Session): ResultSet =
      session.execute(statement)

    def all(implicit session: Session, executionContext: ExecutionContext): Future[Seq[Row]] =
      statement.executeAsync.all

    def oneOption(implicit session: Session, executionContext: ExecutionContext): Future[Option[Row]] =
      statement.executeAsync.oneOption
  }

  implicit class ExternalDSL_FutureOfRichStatement(val statement: Future[Statement]) extends AnyVal {
    def executeAsync(implicit session: Session, executionContext: ExecutionContext): Future[ResultSet] =
      statement.flatMap(_.executeAsync)

    def all(implicit session: Session, executionContext: ExecutionContext): Future[Seq[Row]] =
      statement.executeAsync.all

    def oneOption(implicit session: Session, executionContext: ExecutionContext): Future[Option[Row]] =
      statement.executeAsync.oneOption
  }

  implicit class RichResultSet(val resultSet: ResultSet) extends AnyVal {
    def all =
      resultSet.all.asScala

    def oneOption =
      Option(resultSet.one)
  }

  implicit class RichFutureOfResultSet(val resultSet: Future[ResultSet]) extends AnyVal {
    def all(implicit executionContext: ExecutionContext): Future[Seq[Row]] =
      resultSet.map(_.all.asScala)

    def oneOption(implicit executionContext: ExecutionContext): Future[Option[Row]] =
      resultSet.map(r => Option(r.one()))
  }
}
