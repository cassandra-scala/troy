package troy.driver.query.select

import scala.concurrent.Future
import troy.driver.JavaConverters.RichListenableFuture
import com.datastax.driver.core.{
  PreparedStatement => DSPreparedStatement,
  Session => DSSession,
  ResultSet => DSResultSet
}

trait PreparationStrategy[Statement] {
  def prepare(raw: String): Statement
}
object PreparationStrategy {
  def instance[S](p: String => S) = new PreparationStrategy[S] {
    override def prepare(raw: String) = p(raw)
  }

  //  val noPreparationStrategy =
  //    instance[com.datastax.driver.core.Statement](s => new DSSimpleStatement(s))

  def eagerSyncPreparationStrategy(implicit session: DSSession) =
    instance[DSPreparedStatement](session.prepare)

  //  def eagerAsyncPreparationStrategy(implicit session: DSSession) =
  //    instance[Future[DSPreparedStatement]](session.prepareAsync(_).asScala)
}

trait ExecutionStrategy[ExecutionMonad[_]] {
  def execute(statement: DSPreparedStatement, binder: StatementBinder[_, _]): ExecutionMonad[DSResultSet]
}
object ExecutionStrategy {
  def instance[ExecutionMonad[_]](e: (DSPreparedStatement, StatementBinder[_, _]) => ExecutionMonad[DSResultSet]) =
    new ExecutionStrategy[ExecutionMonad] {
      def execute(statement: DSPreparedStatement, binder: StatementBinder[_, _]): ExecutionMonad[DSResultSet] =
        e(statement, binder)
    }

  //  def asyncStatementExecutionStrategy[PInfo, Params](implicit session: DSSession) =
  //    instance[Future] { (s, binder) =>
  //      session.executeAsync(binder.bind(s.bind)).asScala
  //    }

  //  def syncStatementExecutionStrategy(implicit session: Session) =
  //    instance[Statement, ResultSet](session.execute)
  //
  //  def asyncStatementExecutionStrategy(implicit session: Session) =
  //    instance[Statement, Future[ResultSet]](session.executeAsync(_).asScala)
  //
  //  def asyncAsyncStatementExecutionStrategy(implicit session: Session) =
  //    instance[Future[Statement], ExecutionContext => Future[ResultSet]](f => implicit ec => f.map(session.executeAsync).flatMap(_.asScala))
}

trait ParsingStrategy[ExecutionMonad[_], OutputMonad[_], Output] {
  def parse(resultSet: ExecutionMonad[DSResultSet]): OutputMonad[Output]
}
