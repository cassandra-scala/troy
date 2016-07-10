package troy.driver

import com.datastax.driver.core._

import scala.concurrent.{ Future, ExecutionContext }

object InternalDsl {
  import JavaConverters._
  import scala.collection.JavaConverters._

  def column[S](i: Int)(implicit row: Row) = new {
    def as[C <: CassandraDataType](implicit getter: ColumnGetter[S, C]): S =
      getter.get(row, i)
  }

  def param[S](value: S) = new {
    def as[C <: CassandraDataType](implicit setter: VariableSetter[S, C]) =
      Param[S, C](value, setter)
  }

  case class Param[S, C <: CassandraDataType](value: S, setter: VariableSetter[S, C]) {
    def set(bound: BoundStatement, i: Int) = setter.set(bound, i, value)
  }

  def bind(preparedStatement: com.datastax.driver.core.PreparedStatement, params: Param[_, _ <: CassandraDataType]*) =
    params.zipWithIndex.foldLeft(preparedStatement.bind()) {
      case (stmt, (param, i)) => param.set(stmt, i)
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
