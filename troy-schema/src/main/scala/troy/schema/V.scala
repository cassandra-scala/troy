package troy
package schema

/**
 * V for Validation، similar to Either but also includes Warnings
 * Designed to be used with Macros to hold compiler warn/error messages.
 * S = Success
 * E = Error
 * W = Warn
 */
sealed trait V[+W, +E, +S] extends Product with Serializable { self =>
  @inline def flatMap[WW >: W, EE >: E, SS](f: S => V[WW, EE, SS]): V[WW, EE, SS]
  @inline def map[SS](f: S => SS): V[W, E, SS]
  @inline def collect[EE >: E, SS](default: => EE)(pf: PartialFunction[S, SS]): V[W, EE, SS]
  @inline def addWarns[WW >: W](ws2: Iterable[WW]): V[WW, E, S]
  @inline def withDefaultError[EE >: E](error: => EE) = new WithDefaultError(error)
  @inline def |?|[EE >: E](error: => EE) = withDefaultError(error)

  class WithDefaultError[EE >: E](error: => EE) { selfWithError =>
    def filter(p: S => Boolean): V[W, EE, S] =
      flatMap(value => if (p(value)) self else V.error(error))

    def filterNot(p: S => Boolean): V[W, EE, S] =
      filter(s => !p(s))

    def withFilter(p: S => Boolean) = new WithFilter(p)

    class WithFilter(p: S => Boolean) {
      def flatMap[WW >: W, EEE >: EE, SS](f: S => V[WW, EEE, SS]): V[WW, EEE, SS] = selfWithError filter p flatMap f
      def map[SS](f: S => SS): V[W, EE, SS] = selfWithError filter p map f
      def withFilter(q: S => Boolean): WithFilter = new WithFilter(x => p(x) && q(x))
    }
  }
}
object V {
  import Implicits._

  final case class Success[+W, +S](value: S, ws: Seq[W] = Seq.empty) extends V[W, Nothing, S] { self =>
    @inline override def flatMap[WW >: W, EE, SS](f: S => V[WW, EE, SS]): V[WW, EE, SS] =
      f(value).addWarns(ws)

    @inline override def map[SS](f: S => SS): V[W, Nothing, SS] =
      copy(f(value), ws)

    @inline override def collect[EE, SS](default: => EE)(pf: PartialFunction[S, SS]): V[W, EE, SS] =
      pf.lift(value).toV(default)

    @inline override def addWarns[WW >: W](ws2: Iterable[WW]): V[WW, Nothing, S] =
      copy(ws = ws ++ ws2)
  }

  final case class Error[+W, +E](es: Seq[E], ws: Seq[W] = Seq.empty) extends V[W, E, Nothing] {
    @inline override def flatMap[WW >: W, EE >: E, SS](f: Nothing => V[WW, EE, SS]): V[WW, EE, SS] =
      this

    @inline override def map[SS](f: Nothing => SS): V[W, E, SS] =
      this

    @inline override def collect[EE >: E, SS](default: => EE)(pf: PartialFunction[Nothing, SS]): V[W, EE, SS] =
      this

    @inline override def addWarns[WW >: W](ws2: Iterable[WW]): V[WW, E, Nothing] =
      copy(ws = ws ++ ws2)
  }

  def success[W, S](s: S, ws: W*) =
    Success(s, ws)

  def error[W, E](e: E, warnings: Seq[W] = Seq.empty) = Error(Seq(e), warnings)

  def merge[W, E, S](vs: Seq[V[W, E, S]]) = vs.foldLeft[V[W, E, Seq[S]]](Success(Seq.empty)) {
    case (Success(vs, wss), Success(v, ws)) => Success(vs :+ v, wss ++ ws)
    case (Error(ess, wss), Error(es, ws))   => Error(ess ++ es, wss ++ ws)
    case (Success(vs, wss), Error(es, ws))  => Error(es, wss ++ ws)
    case (Error(ess, wss), Success(v, ws))  => Error(ess, wss ++ ws)
  }

  object Implicits {
    implicit class VOptionOps[S](val o: Option[S]) extends AnyVal {
      def toV[E](e: => E) = o.map(s => Success(s)).getOrElse(error(e))
    }
  }
}