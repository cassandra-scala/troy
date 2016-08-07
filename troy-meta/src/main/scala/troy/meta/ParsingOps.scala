package troy.meta

import scala.annotation.compileTimeOnly

trait ParsingOps {
  type ParseAs[R]

  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[R](mapper: () => R): ParseAs[R] = ???
  // (1 to 22).map(1 to _).map(_.map(i => s"T$i").mkString(", ")).map(tstr => s"def as[$tstr, R](mapper: ($tstr) => R): MacroDSL[M[R]]").foreach(println)
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, R](mapper: (T1) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, R](mapper: (T1, T2) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, R](mapper: (T1, T2, T3) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, R](mapper: (T1, T2, T3, T4) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, R](mapper: (T1, T2, T3, T4, T5) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, R](mapper: (T1, T2, T3, T4, T5, T6) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, R](mapper: (T1, T2, T3, T4, T5, T6, T7) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R): ParseAs[R] = ???
  @compileTimeOnly("as can be called only inside troy.dsl.withSchema block")
  def as[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](mapper: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R): ParseAs[R] = ???
}
