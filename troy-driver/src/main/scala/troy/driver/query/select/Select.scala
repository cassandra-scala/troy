package troy.driver.query.select

import shapeless._
import troy.driver.schema.{ FunctionType, KeyspaceExists, TableExists, VersionExists }
import troy.driver.schema.column.ColumnType

import scala.concurrent.Future

trait Select[I, O] {
  type RawType <: HList
  type ParamsType <: HList

  def rawQuery: String

  def apply(i: I): O = {
    println(rawQuery)
    ???
  }
}

object Select {
  //  type Aux[V, K, T, S, O] = Select[V, K, T, S] { type Out = O }
  //  def apply[V, K, T, S](implicit s: Select[V, K, T, S]): Aux[V, K, T, S, s.Out] = s

  // TODO
  // trait Aliased[S, Alias]
  trait Column[Name]
  trait Function[Keyspace, Name, Params <: HList]
  // trait Asterisk
  trait NoKeyspace // Used to mark absence of a specific keyspace

  def select[V, K, T, S, I, O](raw: String)(
    implicit
    versionExists: VersionExists[V],
    keyspaceExists: KeyspaceExists[V, K],
    tableExists: TableExists[V, K, T],
    selection: Selection[V, K, T, S]
  ) = new Select[I, Future[O]] {
    override type RawType = selection.Out
    override type ParamsType = Nothing
    override val rawQuery = raw
  }
}
