package troy.dsl

import com.datastax.driver.core.{ Row, ResultSet, Session, BoundStatement }

import scala.concurrent.{ ExecutionContext, Future }

sealed trait MacroParam[T]
object MacroParam {
  class DslBoundStatement extends MacroParam[BoundStatement] {
    def sync: DslResultSet = ???
    def async: DslFutureOfResultSet = ???
  }

  class DslResultSet(val resultSet: ResultSet) extends MacroParam[ResultSet] {
    def all: DslSeqOfRows = ???
  }

  class DslFutureOfResultSet(val resultSet: Future[ResultSet]) extends MacroParam[Future[ResultSet]] {
    def all(implicit ec: ExecutionContext): DslFutureOfSeqOfRows = ???
    def one(implicit ec: ExecutionContext): DslFutureOfRow = ???
  }

  class DslSeqOfRows(val rows: java.util.List[Row]) extends MacroParam[java.util.List[Row]] {
    def as[T]: MacroParam[Seq[T]] = ???
  }

  class DslFutureOfSeqOfRows(val rows: Future[Seq[Row]]) extends MacroParam[Future[Seq[Row]]] {
    def as[T](implicit ec: ExecutionContext): MacroParam[Future[Seq[T]]] = ???
  }

  class DslRow(val row: Row) extends MacroParam[Row] {
    def as[T]: MacroParam[T] = ???
  }

  class DslFutureOfRow(val row: Future[Row]) extends MacroParam[Future[Row]] {
    def as[T](implicit ec: ExecutionContext): Future[T] = ???
  }
}

