package troy.driver.query.select

import shapeless._
import troy.driver.schema.column.ColumnType
import troy.tast._

trait BindMarkerTypesOfWhereClause[Version, Table <: TableName[_, _], Relations <: WhereClause.Relations] {
  type Out <: HList
}

object BindMarkerTypesOfWhereClause {
  type Aux[V, T <: TableName[_, _], Rs <: WhereClause.Relations, O] = BindMarkerTypesOfWhereClause[V, T, Rs] { type Out = O }

  def apply[V, T <: TableName[_, _], Rs <: WhereClause.Relations, O](implicit w: BindMarkerTypesOfWhereClause[V, T, Rs]): Aux[V, T, Rs, w.Out] = w

  def instance[V, T <: TableName[_, _], Rs <: WhereClause.Relations, O <: HList]: Aux[V, T, Rs, O] =
    new BindMarkerTypesOfWhereClause[V, T, Rs] { override type Out = O }

  implicit def hNilInstance[V, T <: TableName[_, _]]: Aux[V, T, HNil, HNil] = instance[V, T, HNil, HNil]

  /**
   * For equality operators like ==, <, >, etc ..
   * the type of the term on the right, is the same as the left
   */
  implicit def simpleEqualityInstance[V, T <: TableName[_, _], C <: Identifier, RT <: HList](
    implicit
    headType: ColumnType[V, T, C],
    tailType: BindMarkerTypesOfWhereClause[V, T, RT]
  ) = instance[V, T, WhereClause.Relation.Simple[C, Operator.Equality, BindMarker.Anonymous] :: RT, headType.Out :: tailType.Out]

  // sealed trait Simple[columnName <: Identifier, operator <: Operator, term <: Term] extends Relation

  //  implicit def implicitNotFoundMacro[V, K, T, C, CT]: Aux[V, K, T, C, CT] = macro implicitNotFoundMacroImpl[V, K, T, C]
  //
  //  def implicitNotFoundMacroImpl[V, K, T, Rs](c: Context) = c.abort(c.enclosingPosition, s"a7a000hh")
}
