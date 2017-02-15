package troy
package driver.query.select

import shapeless._
import troy.driver.CassandraDataType
import troy.driver.schema.FunctionTypeResolver
import troy.driver.schema.column.ColumnType
import troy.tast._
import troy.tast.Select.{ Selection, ColumnName, Function, SelectClause, SelectionClauseItem }
import troy.tutils.{ TLeft, TRight, TEither, TOption }
import scala.reflect.macros.blackbox.Context

trait SelectionTypeResolver[Version, T <: TableName[_, _], S <: Selection] {
  type Out <: TEither[THList[String], THList[CassandraDataType]]
}

object SelectionTypeResolver extends LowPriority {

  type Aux[V, T <: TableName[_, _], S <: Selection, O <: TEither[THList[String], THList[CassandraDataType]]] = SelectionTypeResolver[V, T, S] { type Out = O }

  def apply[V, T <: TableName[_, _], S <: Selection](implicit s: SelectionTypeResolver[V, T, S]): Aux[V, T, S, s.Out] = s

  def instance[V, T <: TableName[_, _], S <: Selection, O <: TEither[THList[String], THList[CassandraDataType]]]: Aux[V, T, S, O] = new SelectionTypeResolver[V, T, S] { type Out = O }

  implicit def hNilInstance[V, T <: TableName[_, _]] = instance[V, T, SelectClause[HNil], TRight[HNil]]

  implicit def hColumnInstance[V, T <: TableName[_, _], CN <: Identifier, CNAlias <: TOption[Identifier], ST <: THList[SelectionClauseItem[_, _]], TailType <: THList[CassandraDataType]](
    implicit
    headType: ColumnType[V, T, CN],
    tailType: SelectionTypeResolver.Aux[V, T, SelectClause[ST], TRight[TailType]]
  ) = instance[V, T, SelectClause[SelectionClauseItem[ColumnName[CN], CNAlias] :: ST], TRight[headType.Out :: TailType]]

  //  implicit def hFunctionInstance[V, K <: MaybeKeyspaceName, T <: Identifier, F <: FunctionName[_, _], FParams <: HList, FParamTypes <: HList, Alias <: TOption[Identifier], ST <: THList[SelectionClauseItem[_, _]]](
  //    implicit
  //    fParamTypes: SelectionTypeResolver.Aux[V, TableName[K, T], FParams, FParamTypes], // FIXME: Is it correct to assume function params can be treated as normal selected fields?
  //    headType: FunctionTypeResolver[V, F, FParamTypes, K],
  //    tailType: SelectionTypeResolver[V, TableName[K, T], SelectClause[ST]]
  //  ) = instance[V, TableName[K, T], SelectClause[SelectionClauseItem[Function[F, FParams], Alias] :: ST], headType.Out :: tailType.Out]
}

trait LowPriority {
  import Select._
  import SelectionTypeResolver.instance

  implicit def hColumnNotFoundInstance[V, K <: MaybeKeyspaceName, T <: Identifier, CN <: Identifier, CNAlias <: TOption[Identifier], ST <: THList[SelectionClauseItem[_, _]]] =
    instance[V, TableName[K, T], SelectClause[SelectionClauseItem[ColumnName[CN], CNAlias] :: ST], TLeft["Column " :: CN :: " does not exist in table " :: T :: HNil]]
}

object ImplicitNotFoundMacro {
  // FIXME: Implicits need to match for failures also to be able to report the exact error
  // The code below falsely report the first column, even if another column doesn't exist
  def columnNotFound[V, T, ColumnName](c: Context)(implicit columnName: c.WeakTypeTag[ColumnName]) = {
    c.abort(c.enclosingPosition, "One of the selected columns doesn't exist") //s"Undefined name ${columnName.tpe} in selection clause.")
  }
}