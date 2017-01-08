package troy
package schema

import org.scalatest.{ FlatSpec, Matchers }

object VTestUtils extends FlatSpec with Matchers {
  implicit class RichV[+W, +E, +S](v: V[W, E, S]) {
    def get: S = v match {
      case V.Success(s, _) => s
      case V.Error(es, ws) => throw new NoSuchElementException(s"V.Error($es, $ws).get")
    }
    def getErrors: Seq[E] = v match {
      case V.Error(es, ws)  => es
      case V.Success(v, ws) => throw new NoSuchElementException(s"V.Success($v, $ws).getErrors")
    }
    def getError: E = getErrors.head
    def isSuccess: Boolean = v.isInstanceOf[V.Success[W, S]]
  }
}
