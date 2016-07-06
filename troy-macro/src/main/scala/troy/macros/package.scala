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

package troy

import java.io.InputStream

import com.datastax.driver.core.Session
import troy.ast.CqlParser
import troy.cql.ast.DataType
import troy.dsl.MacroParam
import troy.schema.Schema

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.reflect.macros.blackbox.Context
import scala.util.Random

package object macros {

  def log[T](value: T): T = { println(value); value }

  def troyImpl[F: c.WeakTypeTag](c: Context)(code: c.Expr[F])(session: c.Expr[Session]): c.Expr[F] = {
    import c.universe._
    implicit val c_ = c

    val q"(..$params) => $expr" = code.tree
    val (qParts, qParams) = findCqlQuery(c)(expr)
    val rawQuery = qParts.map{case q"${p: String}" => p}.mkString("?")

    val schema = parseSchemaFromFileName("/schema.cql")(c)
    val query = parseQuery(rawQuery)
    val variable = schema.extractVariables(query) match {
      case Right(columns) => columns
      case Left(e)        => c.abort(c.enclosingPosition, e)
    }
    val variableTypes = variable.map(v => translateColumnType(v.dataType)(c))
    val columns = schema(query) match {
      case Right(columns) => columns
      case Left(e)        => c.abort(c.enclosingPosition, e)
    }
    val columnTypes = columns.map(column => translateColumnType(column.dataType)(c))

    val imports = Seq(
      q"import _root_.troy.driver.DriverHelpers._",
      q"import _root_.troy.driver.Types"
    )
    val prepareStatement = q"""
      val prepared = $session.prepare($rawQuery)
    """
    val parser = q"""
      implicit def parser(row: Row): Post = ???
    """

    val bodyParams = qParams.zip(variableTypes).map{ case (p, t) => q"param($p).as[$t]"}
    val body = replaceCqlQuery(c)(expr, q"bind(prepared, ..$bodyParams)")

    val stats = imports ++ Seq(
      prepareStatement,
      parser,
      q"(..$params) => $body"
    )

    c.Expr(log(q"{ ..$stats }"))
  }

  private def findCqlQuery(c: Context)(expr: c.universe.Tree): (List[c.universe.Tree], List[c.universe.Tree]) = {
    import c.universe._
    expr match {
      case q"$_.RichStringContext(scala.StringContext.apply(..$query)).cql(..$params)" =>
        (query, params)
      case q"$y($z)" =>
        findCqlQuery(c)(y)
      case q"$y.$z" =>
        findCqlQuery(c)(y)
      case q"$y[$z]" =>
        findCqlQuery(c)(y)
    }
  }

  private def replaceCqlQuery(c: Context)(expr: c.universe.Tree, replacement: c.universe.Tree): c.universe.Tree = {
    import c.universe._
    expr match {
      case q"$_.RichStringContext(scala.StringContext.apply(..$query)).cql(..$params)" =>
        replacement
      case q"$y($z)" =>
        val replaced = replaceCqlQuery(c)(y, replacement)
        q"$replaced($z)"
      case q"$y.$z" =>
        val replaced = replaceCqlQuery(c)(y, replacement)
        q"$replaced.$z"
      case q"$y[$z]" =>
        val replaced = replaceCqlQuery(c)(y, replacement)
        q"$replaced[$z]"
    }
  }

  private def translateColumnType(typ: DataType)(c: Context): c.universe.Tree = {
    import c.universe._
    typ match {
      case DataType.ascii => tq"Types.Ascii"
      case DataType.bigint => tq"Types.Bigint"
      case DataType.blob => tq"Types.Blob"
      case DataType.boolean => tq"Types.Boolean"
      case DataType.counter => tq"Types.Counter"
      case DataType.date => tq"Types.Date"
      case DataType.decimal => tq"Types.Decimal"
      case DataType.double => tq"Types.Double"
      case DataType.float => tq"Types.Float"
      case DataType.inet => tq"Types.Inet"
      case DataType.int => tq"Types.Int"
      case DataType.smallint => tq"Types.Smallint"
      case DataType.text => tq"Types.Text"
      case DataType.times => tq"Types.Times"
      case DataType.timestamp => tq"Types.Timestamp"
      case DataType.timeuuid => tq"Types.Timeuuid"
      case DataType.tinyint => tq"Types.Tinyint"
      case DataType.uuid => tq"Types.Uuid"
      case DataType.varchar => tq"Types.Varchar"
      case DataType.varint => tq"Types.Varint"
//      case DataType.list(t: Native) => tq"Types."
//      case DataType.set(t: Native) => tq"Types."
//      case DataType.map(k: Native, v: Native) => tq"Types."
//      case DataType.Tuple(ts: Seq[DataType]) => tq"Types."
//      case DataType.Custom(javaClass: String) => tq"Types."

    }
  }


  def parseSchemaFromFileName(path: String)(implicit c: Context) =
    parseSchemaFromInputStream(
      Option(this.getClass.getResourceAsStream(path))
        .getOrElse(c.abort(c.universe.NoPosition, s"Can't find schema file $path"))
    )

  def parseSchemaFromInputStream(schemaFile: InputStream)(implicit c: Context) =
    parseSchemaFromSource(scala.io.Source.fromInputStream(schemaFile))

  def parseSchemaFromSource(schema: Source)(implicit c: Context) = {
    val lines = schema.getLines()
    val str = lines.mkString("\n")
    parseSchemaFromString(str)
  }

  def parseSchemaFromString(schema: String)(implicit c: Context) =
    CqlParser.parseSchema(schema) match {
      case CqlParser.Success(result, _) =>
        Schema(result) match {
          case Right(schema) => schema
          case Left(e)       => c.abort(c.enclosingPosition, e)
        }
      case CqlParser.Failure(msg, next) =>
        c.abort(c.universe.NoPosition, s"Failure during parsing the schema. Error ($msg) near line ${next.pos.line}, column ${next.pos.column}")
    }

  def parseQuery(queryString: String)(implicit c: Context) = CqlParser.parseQuery(queryString) match {
    case CqlParser.Success(result, _) =>
      result
    case CqlParser.Failure(msg, _) =>
      c.abort(c.enclosingPosition, msg)
  }

}
