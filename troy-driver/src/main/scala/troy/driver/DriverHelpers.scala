package troy.driver

import com.datastax.driver.core._

import scala.concurrent.{ Future, ExecutionContext }

object DriverHelpers {
  import JavaConverters._
  import scala.collection.JavaConverters._

  type Executer[T] = (Session, BoundStatement) => T
  type Parser[T] = Row => T

  def column[S](i: Int)(implicit row: Row) = new {
    def as[C <: Types.CassandraDataType](implicit hasCodec: HasCodec[S, C]): S =
      row.get(i, hasCodec.codec)
  }

  def param[S](value: S) = new {
    def as[C <: Types.CassandraDataType](implicit hasCodec: HasCodec[S, C]) =
      Param[S, C](value)
  }

  case class Param[S, C <: Types.CassandraDataType](value: S)(implicit val hasCodec: HasCodec[S, C]) {
    def withIndex(i: Int) = IndexedParam(i, value)
  }

  case class IndexedParam[S, C <: Types.CassandraDataType](index: Int, value: S)(implicit val hasCodec: HasCodec[S, C])

  def bind(preparedStatement: com.datastax.driver.core.PreparedStatement, params: Param[_, _ <: Types.CassandraDataType]*) =
    params.zipWithIndex.map { case (param, i) => param.withIndex(i) }.foldLeft(preparedStatement.bind()) {
      case (stmt, param) => stmt.set(param.index, param.value, param.hasCodec.codec)
    }

  implicit class RichBoundStatement(val boundStatement: BoundStatement) extends AnyVal {
    def sync(implicit session: Session) = session.execute(boundStatement)
    def async(implicit session: Session) = session.executeAsync(boundStatement).asScala
  }

  implicit class RichResultSet(val resultSet: ResultSet) extends AnyVal {
    def all = resultSet.all.asScala
  }

  implicit class RichFutureOfResultSet(val resultSet: Future[ResultSet]) extends AnyVal {
    def all(implicit executionContext: ExecutionContext) = resultSet.map(_.all.asScala)
    def one(implicit executionContext: ExecutionContext) = resultSet.map(_.one)
  }

  implicit class RichSeqOfRows(val rows: java.util.List[Row]) extends AnyVal {
    def as[T](parser: Row => T): Seq[T] = rows.asScala.map(parser)
  }

  implicit class RichFutureOfSeqOfRows(val rows: Future[Seq[Row]]) extends AnyVal {
    def as[T](parser: Row => T)(implicit executionContext: ExecutionContext): Future[Seq[T]] = rows.map(_.map(parser))
  }

  implicit class RichRow(val row: Row) extends AnyVal {
    def as[T](parser: Row => T): T = parser(row)
  }

  implicit class RichFutureOfRow(val row: Future[Row]) extends AnyVal {
    def as[T](parser: Row => T)(implicit executionContext: ExecutionContext): Future[T] = row.map(parser)
  }

}
