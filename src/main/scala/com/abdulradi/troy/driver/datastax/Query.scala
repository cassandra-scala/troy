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

package com.abdulradi.troy.driver.datastax

import com.abdulradi.troy.ast.{ DataType, SelectStatement, CqlParser }
import com.abdulradi.troy.schema.{ Schema, Field }
import com.datastax.driver.core.{ TypeCodec, Row, Session }

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.macros.blackbox.Context
import scala.collection.JavaConversions._

object Query {

  def query[T](cql: String)(implicit session: Session, ec: ExecutionContext): Future[Seq[T]] = macro queryImpl[T]

  def queryExec[T](q: String)(f: Row => T)(implicit session: Session, ec: ExecutionContext): Future[Seq[T]] =
    session.executeAsync(q).toScala.map(_.toStream.map(f))

  def queryImpl[T: c.WeakTypeTag](c: Context)(cql: c.Expr[String])(session: c.Expr[Session], ec: c.Expr[ExecutionContext]): c.Expr[Future[Seq[T]]] = {
    import c.universe._


    implicit val liftFieldType = Liftable[DataType] {
      case DataType.ascii => q"_root_.com.abdulradi.troy.ast.DataType.ascii"
      case DataType.bigint => q"_root_.com.abdulradi.troy.ast.DataType.bigint"
      case DataType.blob => q"_root_.com.abdulradi.troy.ast.DataType.blob"
      case DataType.boolean => q"_root_.com.abdulradi.troy.ast.DataType.boolean"
      case DataType.counter => q"_root_.com.abdulradi.troy.ast.DataType.counter"
      case DataType.date => q"_root_.com.abdulradi.troy.ast.DataType.date"
      case DataType.decimal => q"_root_.com.abdulradi.troy.ast.DataType.decimal"
      case DataType.double => q"_root_.com.abdulradi.troy.ast.DataType.double"
      case DataType.float => q"_root_.com.abdulradi.troy.ast.DataType.float"
      case DataType.inet => q"_root_.com.abdulradi.troy.ast.DataType.inet"
      case DataType.int => q"_root_.com.abdulradi.troy.ast.DataType.int"
      case DataType.smallint => q"_root_.com.abdulradi.troy.ast.DataType.smallint"
      case DataType.text => q"_root_.com.abdulradi.troy.ast.DataType.text"
      case DataType.times => q"_root_.com.abdulradi.troy.ast.DataType.times"
      case DataType.timestamp => q"_root_.com.abdulradi.troy.ast.DataType.timestamp"
      case DataType.timeuuid => q"_root_.com.abdulradi.troy.ast.DataType.timeuuid"
      case DataType.tinyint => q"_root_.com.abdulradi.troy.ast.DataType.tinyint"
      case DataType.uuid => q"_root_.com.abdulradi.troy.ast.DataType.uuid"
      case DataType.varchar => q"_root_.com.abdulradi.troy.ast.DataType.varchar"
      case DataType.varint => q"_root_.com.abdulradi.troy.ast.DataType.varint"
    }

    //1. Query
    val queryString: String = cql.tree match {
      case Literal(Constant(s: String)) => s
      case _                            => c.abort(c.enclosingPosition, "Must provide a string literal.")
    }

    val query = CqlParser.parseQuery(queryString) match {
      case CqlParser.Success(result, _) =>
        result
      case CqlParser.Failure(msg, _) =>
        c.abort(c.enclosingPosition, msg)
    }

    //2. Schema
    val schema = CqlParser.parseSchema(scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/schema.cql")).getLines().mkString("\n")) match {
      case CqlParser.Success(result, _) =>
        Schema(result) match {
          case Right(schema) => schema
          case Left(e)       => c.abort(c.enclosingPosition, e)
        }
      case CqlParser.Failure(msg, next) =>
        c.abort(c.enclosingPosition, s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
    }

    //3. Schema + Query => Typed Query
    val columns = schema(query) match {
      case Right(columns) => columns
      case Left(e)        => c.abort(c.enclosingPosition, e)
    }
    val columnTypes = columns.map(_.dataType)

    //4. Case class
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

    //5. Typed Query + Case class => Codecs
    if (paramTypes.length < columns.length) c.abort(c.enclosingPosition, s"Selected fields more than $t")
    // else is fine, we will hope extra params has defaults

    // Primitives has special treatment
    val intTypeTag = weakTypeOf[Int]
    val optionOfIntTypeTag = weakTypeOf[Option[Int]]

    val params = (paramTypes zip columnTypes).zipWithIndex.map {
      case ((`intTypeTag`, c), i) =>
        q"row.get($i, implicitly[com.abdulradi.troy.driver.datastax.HasCodec[Integer, $c.type]].codec)"
      case ((p, c), i) =>
        q"row.get($i, implicitly[com.abdulradi.troy.driver.datastax.HasCodec[$p, $c.type]].codec)"
    }

    c.Expr[Future[Seq[T]]](
      q"""com.abdulradi.troy.driver.datastax.Query.queryExec($cql){ row =>
         $companionObject.apply(..$params)
      }($session, $ec)"""
    )
  }

}
