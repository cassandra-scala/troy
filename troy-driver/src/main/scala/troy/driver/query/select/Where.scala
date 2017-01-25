package troy.driver.query.select

import shapeless._
import troy.driver.schema.FunctionType
import troy.driver.schema.column.ColumnType

import scala.reflect.macros.blackbox.Context

trait Where[Version, Keyspace, Table, Relations] {
  type BindMarkerTypes <: HList
}

object Where {
  import Select._

  type Aux[V, K, T, Rs, BMs] = Where[V, K, T, Rs] { type BindMarkerTypes = BMs }

  def apply[V, K, T, Rs](implicit w: Where[V, K, T, Rs]): Aux[V, K, T, Rs, w.BindMarkerTypes] = w

  def instance[V, K, T, Rs, BMs <: HList]: Aux[V, K, T, Rs, BMs] = new Where[V, K, T, Rs] { type BindMarkerTypes = BMs }

  implicit def hNilInstance[V, K, T]: Aux[V, K, T, HNil, HNil] = instance[V, K, T, HNil, HNil]

  /**
   * For equality operators like ==, <, >, etc ..
   * the type of the term on the right, is the same as the left
   */
  implicit def simpleEqualityInstance[V, K, T, C, RT <: HList](
    implicit
    headType: ColumnType[V, K, T, C],
    tailType: Where[V, K, T, RT]
  ) = instance[V, K, T, Relation[Column[C] :: HNil, Equality, AnonymousBindMarker :: HNil] :: RT, headType.Out :: tailType.BindMarkerTypes]

  //  implicit def implicitNotFoundMacro[V, K, T, C, CT]: Aux[V, K, T, C, CT] = macro implicitNotFoundMacroImpl[V, K, T, C]
  //
  //  def implicitNotFoundMacroImpl[V, K, T, Rs](c: Context) = c.abort(c.enclosingPosition, s"a7a000hh")
}
