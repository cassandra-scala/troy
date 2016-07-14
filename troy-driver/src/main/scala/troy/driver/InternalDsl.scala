package troy.driver

import com.datastax.driver.core._

import scala.concurrent.{ Future, ExecutionContext }

object InternalDsl {
  import JavaConverters._
  import scala.collection.JavaConverters._
  import DSL._

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

  implicit class InternalDSL_RichStatement(val statement: Statement) extends AnyVal {
    def parseAs[T](parser: Row => T)(implicit session: Session, executionContext: ExecutionContext): Future[Seq[T]] =
      statement.executeAsync.parseAs(parser)

    //    def preparedAync = statement
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
