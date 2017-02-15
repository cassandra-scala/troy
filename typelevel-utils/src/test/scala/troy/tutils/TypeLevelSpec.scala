package troy
package tutils

object TypeLevelMatchers {
  def t[T]: T = null.asInstanceOf[T]

  implicit class ShouldBeChecker[A](val a: A) extends AnyVal {
    def shouldBe[B](b: B)(implicit witness: A <:< B) = null
  }

  implicit class TypeClassMatchers[A](val a: TypeClass.Aux[A]) extends AnyVal {
    def outShouldBe[B](b: B)(implicit witness: A <:< B) = null
    def outShouldBe[B](implicit witness: A <:< B) = null
  }
}
