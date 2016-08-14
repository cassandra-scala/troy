package troy.cql.parser

import scala.util.parsing.combinator.JavaTokenParsers

/**
 * Created by ohelmi on 14/08/2016.
 */
trait Helpers {
  this: JavaTokenParsers =>

  def parenthesis[A](p: Parser[A]) =
    around(p, "(", ")")

  def curlyBraces[A](p: Parser[A]) =
    around(p, "{", "}")

  def squareBrackets[A](p: Parser[A]) =
    around(p, "[", "]")

  def around[A](p: Parser[A], prefix: Parser[_], postfix: Parser[_]) =
    prefix ~> p <~ postfix

  implicit class orElse[A](p: Parser[A]) {
    def orElse[B >: A](default: => B): Parser[B] = p.? ^^ (_ getOrElse default)
  }

  implicit class orEmpty[A](p: Parser[Seq[A]]) {
    def orEmpty: Parser[Seq[A]] = p orElse Seq.empty
  }

  implicit class asSeq[A](p: Parser[A]) {
    def asSeq: Parser[Seq[A]] = p ^^ { case x => Seq(x) }
  }

  implicit class as2[A, B](t: Parser[A ~ B]) {
    def ^^^^[T](co: (A, B) => T) = t map { tt => val (a ~ b) = tt; co(a, b) }
  }

  implicit class as3[A, B, C](t: Parser[A ~ B ~ C]) {
    def ^^^^[T](co: (A, B, C) => T) = t map { tt => val (a ~ b ~ c) = tt; co(a, b, c) }
  }

  implicit class as4[A, B, C, D](t: Parser[A ~ B ~ C ~ D]) {
    def ^^^^[T](co: (A, B, C, D) => T) = t map { tt => val (a ~ b ~ c ~ d) = tt; co(a, b, c, d) }
  }

  implicit class as5[A, B, C, D, E](t: Parser[A ~ B ~ C ~ D ~ E]) {
    def ^^^^[T](co: (A, B, C, D, E) => T) = t map { tt => val (a ~ b ~ c ~ d ~ e) = tt; co(a, b, c, d, e) }
  }

  implicit class as6[A, B, C, D, E, F](t: Parser[A ~ B ~ C ~ D ~ E ~ F]) {
    def ^^^^[T](co: (A, B, C, D, E, F) => T) = t map { tt => val (a ~ b ~ c ~ d ~ e ~ f) = tt; co(a, b, c, d, e, f) }
  }

  implicit class as7[A, B, C, D, E, F, G](t: Parser[A ~ B ~ C ~ D ~ E ~ F ~ G]) {
    def ^^^^[T](co: (A, B, C, D, E, F, G) => T) = t map { tt => val (a ~ b ~ c ~ d ~ e ~ f ~ g) = tt; co(a, b, c, d, e, f, g) }
  }

  implicit class as8[A, B, C, D, E, F, G, H](t: Parser[A ~ B ~ C ~ D ~ E ~ F ~ G ~ H]) {
    def ^^^^[T](co: (A, B, C, D, E, F, G, H) => T) = t map { tt => val (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h) = tt; co(a, b, c, d, e, f, g, h) }
  }

}
