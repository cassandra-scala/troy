package troy.driver.query.select

import shapeless._
import troy.driver.schema.keyspace.KeyspaceExists
import troy.driver.schema.table.TableExists
import troy.driver.schema.version.VersionExists
import troy.driver.schema.column.ColumnType
import troy.driver.schema.FunctionType

trait Select[Version, Keyspace, Table, Selection] {
  type Out <: HList
}

object Select {
  type Aux[V, K, T, S, O] = Select[V, K, T, S] { type Out = O }
  def apply[V, K, T, S](implicit s: Select[V, K, T, S]): Aux[V, K, T, S, s.Out] = s

  // TODO
  // trait Aliased[S, Alias]
  trait Column[Name]
  trait Function[Keyspace, Name, Params <: HList]
  // trait Asterisk
  trait NoKeyspace // Used to mark absence of a specific keyspace

  implicit def hNilInstance[V, K, T](
    implicit
    tableExists: TableExists[V, K, T]
  ): Aux[V, K, T, HNil, HNil] =
    new Select[V, K, T, HNil] { type Out = HNil }

  implicit def hColumnInstance[V, K, T, ColumnName, ST <: HList](
    implicit
    tableExists: TableExists[V, K, T],
    headType: ColumnType[V, K, T, ColumnName],
    tailType: Select[V, K, T, ST]
  ): Aux[V, K, T, Column[ColumnName] :: ST, headType.Out :: tailType.Out] =
    new Select[V, K, T, Column[ColumnName] :: ST] { type Out = headType.Out :: tailType.Out }

  implicit def hFunctionInstance[V, K, T, FKeyspace, FName, FParams <: HList, FParamTypes <: HList, ST <: HList](
    implicit
    tableExists: TableExists[V, K, T],
    fParamTypes: Select.Aux[V, K, T, FParams, FParamTypes], // FIXME: I don't like resuing the same typeclass to query param types, maybe rename the whole Select typeclass?
    headType: FunctionType[V, FKeyspace, FName, FParamTypes],
    tailType: Select[V, K, T, ST]
  ): Aux[V, K, T, Function[FKeyspace, FName, FParams] :: ST, headType.Out :: tailType.Out] =
    new Select[V, K, T, Function[FKeyspace, FName, FParams] :: ST] { type Out = headType.Out :: tailType.Out }
}
