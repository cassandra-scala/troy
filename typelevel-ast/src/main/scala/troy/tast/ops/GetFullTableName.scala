package troy.tast.ops

import troy.tutils.{ TBoolean, TOption, TypeClass }
import troy.tast.{ TableName, _ }

trait GetFullTableName[Statement] extends TypeClass {
  override type Out <: TableName[_, _]
}
object GetFullTableName {
  type Aux[Statement, FullTableName] = GetFullTableName[Statement] { type Out = FullTableName }

  def instance[Statement, T <: TableName[_, _]]: Aux[Statement, T] = new GetFullTableName[Statement] {
    override type Out = T
  }

  def apply[S](implicit instance: GetFullTableName[S]): Aux[S, instance.Out] = instance

  implicit def selectStatement[T <: TableName[_, _], Mod <: TOption[Select.Mod], Selection <: Select.Selection, Where <: WhereClause.Relations, OrderBy <: TOption[Select.OrderBy[_]], PerPartitionLimit <: TOption[Select.LimitParam], Limit <: TOption[Select.LimitParam], AllowFiltering <: TBoolean] =
    instance[SelectStatement[Mod, Selection, T, Where, OrderBy, PerPartitionLimit, Limit, AllowFiltering], T]

  //  implicit def selectStatement[K <: FullTableName] = instance[SelectStatement[_, _, TableName[K, _], _, _, _, _, _], K]
}
