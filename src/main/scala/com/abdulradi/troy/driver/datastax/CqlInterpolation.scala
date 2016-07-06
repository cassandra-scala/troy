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

package troy.driver

import troy.ast.{ CqlParser, DataType }
import troy.schema.Schema
import com.datastax.driver.core.{ Row, Session }

import scala.collection.JavaConversions._
import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.macros.blackbox.Context

object CqlInterpolation {
  case class Variable(columnName: String, expression: String)
  case class Statement(raw: String, variables: Set[String])

  def cqlImpl(c: Context)(args: c.Expr[Any]*): c.Expr[Statement] = {
    import c.universe._
    val queryParamNames: Seq[String] = args.map(_.tree).map {
      case Ident(x) =>
        x.toTermName.toString
      case _ =>
        c.abort(c.enclosingPosition, "No expressions are supported, only simple identifiers.")
    }
    println(queryParamNames)
    c.prefix.tree match {
      case Apply(x, List(Apply(_, rawParts))) =>
        val parts = rawParts map { case t @ Literal(Constant(const: String)) => const }
        val interleaved = parts.head +: queryParamNames.map(":" + _).zip(parts.tail).flatMap { case (p, q) => Seq(p, q) }
        c.Expr(q"${interleaved.mkString} -> ${queryParamNames.toSet}")
    }
  }

}
