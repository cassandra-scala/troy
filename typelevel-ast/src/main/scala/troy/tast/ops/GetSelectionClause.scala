package troy.tast.ops

import troy.tutils.{ TBoolean, TOption, TypeClass }
import troy.tast.{ TableName, _ }

trait GetSelectionClause[Statement] extends TypeClass {
  override type Out <: Select.Selection
}
object GetSelectionClause {
  type Aux[Statement, SelectionClause] = GetSelectionClause[Statement] { type Out = SelectionClause }

  def instance[Statement, O <: Select.Selection]: Aux[Statement, O] = new GetSelectionClause[Statement] {
    override type Out = O
  }

  def apply[S](implicit instance: GetSelectionClause[S]): Aux[S, instance.Out] = instance

  implicit def selectStatement[M <: TOption[Select.Mod], S <: Select.Selection, T <: TableName[_, _], W <: WhereClause.Relations, O <: TOption[Select.OrderBy[_]], P <: TOption[Select.LimitParam], L <: TOption[Select.LimitParam], F <: TBoolean] =
    instance[SelectStatement[M, S, T, W, O, P, L, F], S]

  //  implicit def selectStatement[K <: SelectionClause] = instance[SelectStatement[_, _, TableName[K, _], _, _, _, _, _], K]
}
