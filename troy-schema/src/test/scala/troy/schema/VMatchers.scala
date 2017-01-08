package troy
package schema

import org.scalatest._
import matchers._

trait VMatchers {
  import VTestUtils._

  def beSuccess = new Matcher[V[_, _, _]] {
    def apply(left: V[_, _, _]) =
      MatchResult(
        left.isSuccess,
        s"""Value $left was not success. Errors: ${left}"""",
        s"""Value $left was success""""
      )
  }

  def beFailure = beSuccess.andThen(_.negated)

  //  def failWith[E](e: E) = new Matcher[V[_, E, _]] {
  //    def apply(left: V[_, E, _]) =
  //      MatchResult(
  //        left.getErrors.contains(e),
  //        s"""Value $left didn't fail. Value was: ${left.get}"""",
  //        s"""Value $left did fail as expected""""
  //      )
  //  }

  def failWith[E] = new Matcher[V[_, _ >: E, _]] {
    def apply(left: V[_, _ >: E, _]) =
      MatchResult(
        left.getErrors.exists(_.isInstanceOf[E]),
        s"""Value $left didn't fail. Value was: ${left}"""",
        s"""Value $left did fail as expected""""
      )
  }

}

object VMatchers extends VMatchers