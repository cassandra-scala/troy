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

  def randomVariableName = Random.alphanumeric.dropWhile(c => Range.inclusive('0', '9').contains(c)).take(5).mkString

  def troy1[T, R](c: scala.reflect.macros.blackbox.Context)(code: c.Expr[T => troy.dsl.MacroParam[R]])(session: c.Expr[com.datastax.driver.core.Session]) =
    troyImpl[T => R](c)(code.tree)(session)

  private def troyImpl[F](c: Context)(code: c.universe.Tree)(session: c.Expr[Session]): c.Expr[F] = {
    import c.universe._
    implicit val c_ = c

    val q"(..$params) => $expr" = code
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

    val imports = q"import troy.driver.DriverHelpers._"
    val prepareStatement = q"""
      val prepared = $session.prepare($rawQuery)
    """
    val parser = q"""
      implicit def parser(row: Row): Post = ???
    """

    val bodyParams = qParams.zip(variableTypes).map{ case (p, t) => q"param($p).as[$t]"}
    val body = replaceCqlQuery(c)(expr, q"bind(prepared, ..$bodyParams)")

    val stats = Seq(
      imports,
      prepareStatement,
      parser,
      q"(..$params) => $body"
    )

    log(c.Expr[F](q"{ ..$stats }"))
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
      case DataType.ascii => q"troy.driver.Types.Ascii"
      case DataType.bigint => q"troy.driver.Types.Bigint"
      case DataType.blob => q"troy.driver.Types.Blob"
      case DataType.boolean => q"troy.driver.Types.Boolean"
      case DataType.counter => q"troy.driver.Types.Counter"
      case DataType.date => q"troy.driver.Types.Date"
      case DataType.decimal => q"troy.driver.Types.Decimal"
      case DataType.double => q"troy.driver.Types.Double"
      case DataType.float => q"troy.driver.Types.Float"
      case DataType.inet => q"troy.driver.Types.Inet"
      case DataType.int => q"troy.driver.Types.Int"
      case DataType.smallint => q"troy.driver.Types.Smallint"
      case DataType.text => q"troy.driver.Types.Text"
      case DataType.times => q"troy.driver.Types.Times"
      case DataType.timestamp => q"troy.driver.Types.Timestamp"
      case DataType.timeuuid => q"troy.driver.Types.Timeuuid"
      case DataType.tinyint => q"troy.driver.Types.Tinyint"
      case DataType.uuid => q"troy.driver.Types.Uuid"
      case DataType.varchar => q"troy.driver.Types.Varchar"
      case DataType.varint => q"troy.driver.Types.Varint"
//      case DataType.list(t: Native) => q"troy.driver.Types."
//      case DataType.set(t: Native) => q"troy.driver.Types."
//      case DataType.map(k: Native, v: Native) => q"troy.driver.Types."
//      case DataType.Tuple(ts: Seq[DataType]) => q"troy.driver.Types."
//      case DataType.Custom(javaClass: String) => q"troy.driver.Types."

    }
  }

//  private def replaceCqlQuery[T](c: Context)(expr: c.universe.Tree): c.universe.Tree = {
//    import c.universe._
//    expr match {
//      case q"$_.RichStringContext(scala.StringContext.apply(..$query)).cql(..$params)" =>
//        (query, params)
//      case q"$y($z)" =>
//        findCqlQuery(c)(y)
//      case q"$y.$z" =>
//        findCqlQuery(c)(y)
//      case q"$y[$z]" =>
//        findCqlQuery(c)(y)
//    }
//  }

  //  def troy2[T1, T2, R](c: Context)(f: c.Expr[(T1, T2) => ExecuteParams[R]])(session: c.Expr[Session], ec: c.Expr[ExecutionContext]): c.Expr[(T1, T2) => R] =
  //    log(c.Expr(execute(c)(f.tree)(session, ec)))

  //  private def troy(c: Context)(f: c.universe.Tree)(session: c.Expr[Session]): c.universe.Tree = {
  //    import c.universe._
  //    implicit val context = c
  //    implicit val liftDataType = MacroHelpers.liftDataType(c)
  //
  //    val (vparams, executeParams, otherCode) = f match {
  //      case Function(vparams, Block(body, executeParams)) =>
  //        c.warning(c.enclosingPosition, s"$body will be ignored!") // TODO
  //        (vparams, executeParams, body)
  //      case Function(vparams, executeParams) =>
  //        (vparams, executeParams, Nil)
  //    }
  //    println(">>>>>>>>>>>>>>>>>>>>>")
  //    println(showRaw(executeParams))
  //    println(">>>>>>>>>>>>>>>>>>>>>")
  //    println(showCode(executeParams))
  //    println(">>>>>>>>>>>>>>>>>>>>>")
  //    val (rawStatement, boundVariables, outputType) = executeParams match {
  //      case q"""com.abdulradi.troy.driver.ExecuteParams.apply(${ rawStatement: String })""" => (rawStatement, Nil, None)
  //    }
  //
  //    val schema = MacroHelpers.parseSchemaFromFileName("/schema.cql")
  //    val statement = MacroHelpers.parseQuery(rawStatement)
  //    val variables = Variables.extract(statement)
  //
  //    val bindParam = (vparams zip variables) map {
  //      case (q"$mods val $tname: $tpt = $expr", Variable(_, dataType)) =>
  //        q"com.abdulradi.troy.driver.PreparedStatement.ValueCodecPair($tname, implicitly[com.abdulradi.troy.driver.HasCodec[$tpt, $dataType.type]].codec)"
  //    }
  //    val prepareStatement = q"""
  //      val prepared = new com.abdulradi.troy.driver.PreparedStatement($rawStatement, $session)
  //    """
  //    val bindStatement = q"""
  //      prepared.execute(..$bindParam)
  //    """
  //    //    val finalFun = Function(vparams, bindStatement)
  //
  //    val params = vparams.map { case q"$mods val $tname: $tpt = $expr" => q"val $tname: $tpt" }
  //    q"""
  //      {
  //       val prepared = new com.abdulradi.troy.driver.PreparedStatement($rawStatement, $session)
  //        def apply(..$params) = {
  //          ..$otherCode
  //          prepared.execute(..$bindParam)
  //        }
  //
  //       apply _
  //      }
  //     """
  //  }

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
