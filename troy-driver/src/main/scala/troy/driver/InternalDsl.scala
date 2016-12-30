package troy.driver

import com.datastax.driver.core._
import troy.driver.codecs.TroyCodec

import scala.concurrent.{ ExecutionContext, Future }

object InternalDsl {
  import DSL._

  import scala.collection.JavaConverters._

  val CDT = CassandraDataType

  def column[S](i: Int)(implicit row: GettableByIndexData) = new {
    def as[C <: CassandraDataType](implicit getter: TroyCodec[C, S]): S =
      getter.get(row, i)
  }

  def param[S](value: S) = new {
    def as[C <: CassandraDataType](implicit setter: TroyCodec[C, S]) =
      Param[S, C](value, setter)
  }

  case class Param[S, C <: CassandraDataType](value: S, setter: TroyCodec[C, S]) {
    def set(bs: BoundStatement, i: Int) = setter.set(bs, i, value)
  }

  def bind(ps: PreparedStatement, params: Param[_, _ <: CassandraDataType]*): BoundStatement =
    bind(ps.bind(), params: _*)

  private[this] def bind(bs: BoundStatement, params: Param[_, _ <: CassandraDataType]*) =
    params.zipWithIndex.foldLeft(bs) {
      case (stmt, (param, i)) => param.set(stmt, i)
    }

  implicit class InternalDSL_RichStatement(val statement: Statement) extends AnyVal {
    def parseAs[T](parser: Row => T)(implicit session: Session, executionContext: ExecutionContext): Future[Seq[T]] =
      statement.executeAsync.parseAs(parser)
  }

  implicit class InternalDSL_RichFutureOfResultSet(val resultSet: Future[ResultSet]) extends AnyVal {
    def parseAs[T](parser: Row => T)(implicit executionContext: ExecutionContext): Future[Seq[T]] =
      resultSet.map(_.parseAs(parser))
  }

  implicit class InternalDSL_RichResultSet(val resultSet: ResultSet) extends AnyVal {
    def parseAs[T](parser: Row => T): Seq[T] =
      resultSet.all.parseAs(parser)
  }

  implicit class InternalDSL_RichSeqOfRows(val rows: java.util.List[Row]) extends AnyVal {
    def parseAs[T](parser: Row => T): Seq[T] =
      rows.asScala.map(parser)
  }

  implicit class InternalDSL_RichFutureOfSeqOfRows(val rows: Future[Seq[Row]]) extends AnyVal {
    def parseAs[T](parser: Row => T)(implicit executionContext: ExecutionContext): Future[Seq[T]] =
      rows.map(_.map(parser))
  }

  implicit class InternalDSL_RichOptionOfRow(val row: Option[Row]) extends AnyVal {
    def parseAs[T](parser: Row => T): Option[T] =
      row.map(parser)
  }

  implicit class InternalDSL_RichFutureOfOptionOfRow(val row: Future[Option[Row]]) extends AnyVal {
    def parseAs[T](parser: Row => T)(implicit executionContext: ExecutionContext): Future[Option[T]] =
      row.map(_.map(parser))
  }
}
