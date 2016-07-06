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

///*
// * Copyright 2016 Tamer AbdulRadi
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package troy.driver
//
//import troy.ast.{ DataType, Term, SelectStatement, CqlParser }
//import troy.schema.Schema
//import com.datastax.driver.core.{ Session, ResultSet }
//
//import scala.concurrent.{ ExecutionContext, Future }
//import scala.reflect.macros.blackbox.Context
//
//object Cql {
//  type Statement = (String, Set[String])
//
//  // def execute[I](cql: I => Statement)(implicit session: Session, ec: ExecutionContext): I => Future[ResultSet]
//  def cql(c: Context)(raw: c.Expr[String]): c.Expr[ExecuteParams] = {
//    import c.universe._
//    //    implicit val context = c
//    //    implicit val liftDataType = MacroHelpers.liftDataType(c)
//    //
//    //    //1. Schema
//    //    val schema = MacroHelpers.parseSchemaFromFileName("/schema.cql")
//    //
//    //    //2. Query
//    //    val queryString: String = raw.tree match {
//    //      case Literal(Constant(s: String)) => s
//    //      case _                            => c.abort(c.enclosingPosition, "Must provide a string literal.")
//    //    }
//    //
//    //    val query = CqlParser.parseQuery(queryString) match {
//    //      case CqlParser.Success(result, _) =>
//    //        result
//    //      case CqlParser.Failure(msg, _) =>
//    //        c.abort(c.enclosingPosition, msg)
//    //    }
//    //
//    //    //3. Where variables
//    //    val variables = query.where.map { where =>
//    //      import SelectStatement.WhereClause.Relation._
//    //      schema.apply(query.from, where.relations.flatMap {
//    //        case Simple(identifier, _, Term.Variable.Anonymous) => Seq(identifier)
//    //        case _                                              => Seq()
//    //      }) match {
//    //        case Right(columns) => columns
//    //        case Left(e)        => c.abort(c.enclosingPosition, e)
//    //      }
//    //    }.getOrElse(Seq.empty)
//
//    //    val query: String = ???
//    //    val values: Map[String, Any] = ???
//    // V1: HasCodec[V1, text]] // def apply[V1: HasCodec[V1, text]](...v1: V1) = {
//    //    val tparams = variables.map(_.dataType).zipWithIndex.map{ case (t, i) => q"type T$i: HasCodec[T$i, $t]"}
//    //    val paramss = variables.map(_.name).zipWithIndex.map {
//    //      case (v, i) =>
//    //        val _type = s"T$i"
//    //        q"val $v: T$i"
//    //    }
//
//    //    val code = q"""new com.abdulradi.troy.driver.PreparedStatement1[${variables.head.dataType}]($raw)($session)"""
//
//    //    val code = q"""new com.abdulradi.troy.driver.PreparedStatement($raw, $session) {
//    //      import troy.driver.HasCodec
//    //      def apply[T](value: T)(implicit ev: HasCodec[T, ${variables.head.dataType}]) = {
//    //        execute(Seq(value -> ev.codec))
//    //      }
//    //    }"""
//    //
//    //    c.Expr(code)
//    c.Expr(q"com.abdulradi.troy.driver.ExecuteParams($raw)")
//  }
//
//}
