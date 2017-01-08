package troy
package meta

import com.datastax.driver.core._

import scala.concurrent.Future

object MacroDSL {
  trait TroyCql {
    def prepared: Statement
    //    def preparedAsync: Future[Statement]
    //    def unprepared: Statement
  }

  trait DslBoundStatement extends ParsingOps {
    type ParseAs[R] = Future[Seq[R]]
  }

  trait DslFutureBoundStatement extends ParsingOps {
    type ParseAs[R] = Future[Seq[R]]
  }

  trait DslResultSet extends ParsingOps {
    type ParseAs[R] = Seq[R]
  }

  trait DslFutureOfResultSet extends ParsingOps {
    type ParseAs[R] = Future[Seq[R]]
  }

  trait DslFutureOfSeqOfRow extends ParsingOps {
    type ParseAs[R] = Future[Seq[R]]
  }

  trait DslFutureOfOptionOfRow extends ParsingOps {
    type ParseAs[R] = Future[Option[R]]
  }

  trait DslSeqOfRow extends ParsingOps {
    type ParseAs[R] = Seq[R]
  }

  trait DslOptionOfRow extends ParsingOps {
    type ParseAs[R] = Option[R]
  }
}

