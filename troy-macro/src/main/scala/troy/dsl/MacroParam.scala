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
    def one: DslRow = ???
  }

  class DslFutureOfResultSet(val resultSet: Future[ResultSet]) extends MacroParam[Future[ResultSet]] {
    def all(implicit ec: ExecutionContext): DslFutureOfSeqOfRows = ???
    def one(implicit ec: ExecutionContext): DslFutureOfRow = ???
  }

  class DslSeqOfRows(val rows: java.util.List[Row]) extends MacroParam[java.util.List[Row]] {
    def as[R](constructor: () => R): MacroParam[Seq[R]] = ???
    // (1 to 22).map(1 to _).map(_.map(i => s"T$i").mkString(", ")).map(tstr => s"def as[$tstr, R](constructor: ($tstr) => R): MacroParam[Seq[R]] = ???").foreach(println)
    def as[T1, R](constructor: (T1) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, R](constructor: (T1, T2) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, R](constructor: (T1, T2, T3) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, R](constructor: (T1, T2, T3, T4) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, R](constructor: (T1, T2, T3, T4, T5) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, R](constructor: (T1, T2, T3, T4, T5, T6) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, R](constructor: (T1, T2, T3, T4, T5, T6, T7) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): MacroParam[Seq[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): MacroParam[Seq[R]] = ???
  }

  class DslFutureOfSeqOfRows(val rows: Future[Seq[Row]]) extends MacroParam[Future[Seq[Row]]] {
    def as[R](constructor: () => R): MacroParam[Future[Seq[R]]] = ???
    // (1 to 22).map(1 to _).map(_.map(i => s"T$i").mkString(", ")).map(tstr => s"def as[$tstr, R](constructor: ($tstr) => R): MacroParam[Future[Seq[R]]] = ???").foreach(println)
    def as[T1, R](constructor: (T1) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, R](constructor: (T1, T2) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, R](constructor: (T1, T2, T3) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, R](constructor: (T1, T2, T3, T4) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, R](constructor: (T1, T2, T3, T4, T5) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, R](constructor: (T1, T2, T3, T4, T5, T6) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, R](constructor: (T1, T2, T3, T4, T5, T6, T7) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): MacroParam[Future[Seq[R]]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): MacroParam[Future[Seq[R]]] = ???
  }

  class DslRow(val row: Row) extends MacroParam[Row] {
    def as[R](constructor: () => R): MacroParam[R] = ???
    // (1 to 22).map(1 to _).map(_.map(i => s"T$i").mkString(", ")).map(tstr => s"def as[$tstr, R](constructor: ($tstr) => R): MacroParam[R] = ???").foreach(println)
    def as[T1, R](constructor: (T1) => R): MacroParam[R] = ???
    def as[T1, T2, R](constructor: (T1, T2) => R): MacroParam[R] = ???
    def as[T1, T2, T3, R](constructor: (T1, T2, T3) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, R](constructor: (T1, T2, T3, T4) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, R](constructor: (T1, T2, T3, T4, T5) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, R](constructor: (T1, T2, T3, T4, T5, T6) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, R](constructor: (T1, T2, T3, T4, T5, T6, T7) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): MacroParam[R] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): MacroParam[R] = ???
  }

  class DslFutureOfRow(val row: Future[Row]) extends MacroParam[Future[Row]] {
    def as[R](constructor: () => R): MacroParam[Future[R]] = ???
    // (1 to 22).map(1 to _).map(_.map(i => s"T$i").mkString(", ")).map(tstr => s"def as[$tstr, R](constructor: ($tstr) => R): MacroParam[Future[R]] = ???").foreach(println)
    def as[T1, R](constructor: (T1) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, R](constructor: (T1, T2) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, R](constructor: (T1, T2, T3) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, R](constructor: (T1, T2, T3, T4) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, R](constructor: (T1, T2, T3, T4, T5) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, R](constructor: (T1, T2, T3, T4, T5, T6) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, R](constructor: (T1, T2, T3, T4, T5, T6, T7) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): MacroParam[Future[R]] = ???
    def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](constructor: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): MacroParam[Future[R]] = ???
  }
}

