package troy.driver.query.select

import shapeless._
import troy.driver.schema.keyspace.KeyspaceExists
import troy.driver.schema.table.TableExists
import troy.driver.schema.version.VersionExists
import troy.driver.schema.column.ColumnType

trait Select[Version, Keyspace, Table, Selection] {
  type Out <: HList
}

object Select {
  type Aux[V, K, T, S, O] = Select[V, K, T, S] { type Out = O }
  def apply[V, K, T, S](implicit s: Select[V, K, T, S]): Aux[V, K, T, S, s.Out] = s

  // TODO
  // trait Aliased[S, Alias]
  trait Column[Name]
  // trait Function[Name, Params <: HList]
  // trait Asterisk

  implicit def hNilInstance[V, K, T](
    implicit
    tableExists: TableExists[V, K, T]
  ): Aux[V, K, T, HNil, HNil] =
    new Select[V, K, T, HNil] { type Out = HNil }

  implicit def hColumnInstance[V, K, T, ColumnName, ST <: HList](
    implicit
    tableExists: TableExists[V, K, T],
    columnType: ColumnType[V, K, T, ColumnName],
    tailType: Select[V, K, T, ST]
  ): Aux[V, K, T, Column[ColumnName] :: ST, columnType.Out :: tailType.Out] =
    new Select[V, K, T, Column[ColumnName] :: ST] { type Out = columnType.Out :: tailType.Out }
}
