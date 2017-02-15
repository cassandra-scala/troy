package troy.tast.ops

import troy.tutils.{ TBoolean, TOption, TypeClass }
import troy.tast.{ TableName, _ }

trait GetKeyspaceName[Statement] extends TypeClass {
  override type Out <: MaybeKeyspaceName
}
object GetKeyspaceName {
  type Aux[Statement, KeyspaceName] = GetKeyspaceName[Statement] { type Out = KeyspaceName }

  def instance[Statement, K <: MaybeKeyspaceName]: Aux[Statement, K] = new GetKeyspaceName[Statement] {
    override type Out = K
  }

  def apply[S](implicit instance: GetKeyspaceName[S]): Aux[S, instance.Out] = instance

  implicit def selectStatment[K <: MaybeKeyspaceName, T <: Identifier, Mod <: TOption[Select.Mod], Selection <: Select.Selection, Where <: WhereClause.Relations, OrderBy <: TOption[Select.OrderBy[_]], PerPartitionLimit <: TOption[Select.LimitParam], Limit <: TOption[Select.LimitParam], AllowFiltering <: TBoolean] =
    instance[SelectStatement[Mod, Selection, TableName[K, T], Where, OrderBy, PerPartitionLimit, Limit, AllowFiltering], K]

  //  implicit def selectStatement[K <: KeyspaceName] = instance[SelectStatement[_, _, TableName[K, _], _, _, _, _, _], K]
}
