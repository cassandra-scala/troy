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

import java.io.InputStream

import troy.ast._
import troy.schema.Schema
import scala.io.Source
import scala.reflect.macros.blackbox.Context

object MacroHelpers {

  def liftDataType(implicit c: Context) = {
    import c.universe._
    //    val dataType = q"_root_.com.abdulradi.troy.ast.DataType"
    Liftable[DataType] {
      case DataType.ascii     => q"_root_.com.abdulradi.troy.ast.DataType.ascii"
      case DataType.bigint    => q"_root_.com.abdulradi.troy.ast.DataType.bigint"
      case DataType.blob      => q"_root_.com.abdulradi.troy.ast.DataType.blob"
      case DataType.boolean   => q"_root_.com.abdulradi.troy.ast.DataType.boolean"
      case DataType.counter   => q"_root_.com.abdulradi.troy.ast.DataType.counter"
      case DataType.date      => q"_root_.com.abdulradi.troy.ast.DataType.date"
      case DataType.decimal   => q"_root_.com.abdulradi.troy.ast.DataType.decimal"
      case DataType.double    => q"_root_.com.abdulradi.troy.ast.DataType.double"
      case DataType.float     => q"_root_.com.abdulradi.troy.ast.DataType.float"
      case DataType.inet      => q"_root_.com.abdulradi.troy.ast.DataType.inet"
      case DataType.int       => q"_root_.com.abdulradi.troy.ast.DataType.int"
      case DataType.smallint  => q"_root_.com.abdulradi.troy.ast.DataType.smallint"
      case DataType.text      => q"_root_.com.abdulradi.troy.ast.DataType.text"
      case DataType.times     => q"_root_.com.abdulradi.troy.ast.DataType.times"
      case DataType.timestamp => q"_root_.com.abdulradi.troy.ast.DataType.timestamp"
      case DataType.timeuuid  => q"_root_.com.abdulradi.troy.ast.DataType.timeuuid"
      case DataType.tinyint   => q"_root_.com.abdulradi.troy.ast.DataType.tinyint"
      case DataType.uuid      => q"_root_.com.abdulradi.troy.ast.DataType.uuid"
      case DataType.varchar   => q"_root_.com.abdulradi.troy.ast.DataType.varchar"
      case DataType.varint    => q"_root_.com.abdulradi.troy.ast.DataType.varint"
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
