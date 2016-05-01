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

package com.abdulradi.troy.driver.native

import com.abdulradi.troy.schema.{ Field, TypedQuery }
import com.datastax.driver.core.{ TypeCodec, Row, Session }

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.macros.blackbox.Context
import scala.collection.JavaConversions._

object Query {

  def query[T](cql: String)(implicit session: Session, ec: ExecutionContext): Future[Seq[T]] = macro queryImpl[T]

  def queryExec[T](q: String)(f: Row => T)(implicit session: Session, ec: ExecutionContext): Future[Seq[T]] =
    session.executeAsync(q).toScala.map(_.toStream.map(f))

  def getValue[T: TypeCodec](row: Row, i: Int): T =
    row.get(i, implicitly[TypeCodec[T]])

  def queryImpl[T: c.WeakTypeTag](c: Context)(cql: c.Expr[String])(session: c.Expr[Session], ec: c.Expr[ExecutionContext]): c.Expr[Future[Seq[T]]] = {
    import c.universe._

    val query: String = cql.tree match {
      case Literal(Constant(s: String)) => s
      case _                            => c.abort(c.enclosingPosition, "Must provide a string literal.")
    }

    val t = weakTypeOf[T]
    val companioned = t.typeSymbol
    val companionObject = companioned.companion
    val companionType = companionObject.typeSignature

    val applies =
      companionType.decl(TermName("apply")) match {
        case NoSymbol => c.abort(c.enclosingPosition, "No apply function found")
        case s        => s.asTerm.alternatives
      }

    val apply = applies.head // TODO: How to pick the correct apply method?
    val paramList = apply.typeSignature.paramLists.head // TODO: What if there is multiple paramlist?
    val paramTypes = paramList.map(_.typeSignature)

    val params = paramTypes.zipWithIndex.map { case (u, i) => q"com.abdulradi.troy.driver.native.Query.getValue[$u](row, $i)" }

    c.Expr[Future[Seq[T]]](
      q"""com.abdulradi.troy.driver.native.Query.queryExec($cql){ row =>
         $companionObject.apply(..$params)
      }($session, $ec)"""
    )
  }

}
