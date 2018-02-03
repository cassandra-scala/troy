/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package troy.schema

import schemasafe.core.Materializer
import schemasafe.core.Query
import schemasafe.core.utils.XResult
import troy.schema.tladt.DBType
import singleton.ops.{XLong, XString}
import scala.reflect.macros.whitebox._
import shapeless._

//val universe: scala.reflect.runtime.universe.type = scala.reflect.runtime.universe
//scala> import universe._
trait QuerySignature[DBI <: HList, DBO <: HList, S <: Option[XLong]]

trait CassandraQueryAnalyser[LQ <: XString] {
  type Out <: XResult[QuerySignature[_, _, _]]
}

object CassandraQueryAnalyser {

  type Aux[LQ <: XString, O] =
    CassandraQueryAnalyser[LQ] {
      type Out = O
    }

  def instance[LQ <: XString, O <: XResult[QuerySignature[_, _, _]]] =
    new CassandraQueryAnalyser[LQ] {
      override type Out = O
    }

  implicit val dummy = instance[
    "select foo, bar from table x where foo = ?",
    Right[Nothing, QuerySignature[
      DBType.INT :: HNil,
      DBType.INT :: DBType.VARCHAR :: HNil,
      None.type
    ]]
  ]


//  def materialize[LQ <: XString](c: Context): c.Expr[(HList, HList, Option[XLong])] = {
//    import c.universe._
//    println("a7aaaaa")
//
//
//    tq"???"
//  }
}