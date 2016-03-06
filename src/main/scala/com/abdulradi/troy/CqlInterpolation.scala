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

package com.abdulradi.troy

import com.abdulradi.troy.schema.{ FieldLevel, TypedQuery, Schema, Field }
import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros

object CqlInterpolation {

  implicit class FilePathHelper(val sc: StringContext) extends AnyVal {
    def cql(args: Any*): TypedQuery[(Field, Field), (Field, Field)] = macro CqlImpl
  }

  def CqlImpl(c: Context)(args: c.Expr[Any]*): c.Expr[TypedQuery[(Field, Field), (Field, Field)]] = {
    import c.universe._

    //    implicit val liftField = Liftable[SelectStatement.Field] {
    //      case SelectStatement.Field(name) =>
    //        q"_root_.com.abdulradi.agamemnon.ast.SelectStatement.Field($name)"
    //    }
    //
    //    implicit val liftProjectedField = Liftable[SelectStatement.ProjectedField] {
    //      case SelectStatement.ProjectedField(projection, field) =>
    //        q"_root_.com.abdulradi.agamemnon.ast.SelectStatement.ProjectedField($projection, $field)"
    //    }
    //
    //    implicit val liftSelectedField = Liftable[SelectStatement.SelectedField] {
    //      case f: SelectStatement.Field           => q"$f"
    //      case pf: SelectStatement.ProjectedField => q"$pf"
    //    }
    //
    //    implicit val liftSelector = Liftable[SelectStatement.Selector] {
    //      case SelectStatement.Fields(fields) =>
    //        q"_root_.com.abdulradi.agamemnon.ast.SelectStatement.Fields(Seq(..$fields))"
    //    }
    //
    //    implicit val liftKeyspaceName = Liftable[KeyspaceName] {
    //      case KeyspaceName(name) =>
    //        q"_root_.com.abdulradi.agamemnon.ast.KeyspaceName($name)"
    //    }
    //
    //    implicit val liftTableName = Liftable[TableName] {
    //      case TableName(keyspace, table) =>
    //        q"_root_.com.abdulradi.agamemnon.ast.TableName($keyspace, $table)"
    //    }

    //    implicit val liftFieldType = Liftable[schema.FieldType] {
    //      case FieldType.ascii => q"_root_.com.abdulradi.agamemnon.schema.FieldType.ascii"
    //      case FieldType.text  => q"_root_.com.abdulradi.agamemnon.schema.FieldType.text"
    //      case _               => ???
    //    }
    //
    //    implicit val liftFieldLevel = Liftable[schema.FieldLevel] {
    //      case FieldLevel.Partition => q"_root_.com.abdulradi.agamemnon.schema.FieldLevel.Partition"
    //      case FieldLevel.Row       => q"_root_.com.abdulradi.agamemnon.schema.FieldLevel.Row"
    //    }
    //
    //    implicit val liftSchemaField = Liftable[Field] {
    //      case Field(name, ftype, fLevel) =>
    //        q"_root_.com.abdulradi.agamemnon.schema.Field($name, $ftype, $fLevel)"
    //    }

    c.prefix.tree match {
      case Apply(_, List(Apply(_, rawParts))) =>
        //        val parts = rawParts map { case t @ Literal(Constant(const: String)) => const }
        //        val input = parts.mkString("?")
        //        CqlParser.parse(input) match {
        //          case Parser.Success(result, _) =>
        //            result match {
        //              case SelectStatement(Fields(fields), table) =>
        //                val lines = scala.io.Source.fromInputStream(this.getClass.getResourceAsStream("/schema.cql")).getLines()
        //                val schema: Schema = Schema.fromString(lines.mkString("\n"))
        //                val fieldsByLevel = fields.map {
        //                  case ASTField(fname) =>
        //                    schema.getField(table.keyspace.get.name, table.table, fname).get
        //                }.groupBy(_.fLevel)
        //                val Seq(pk, sc) = fieldsByLevel.getOrElse(FieldLevel.Partition, Seq.empty)
        //                val Seq(cc, nc) = fieldsByLevel.getOrElse(FieldLevel.Row, Seq.empty)
        //                c.Expr[TypedQuery[(Field, Field), (Field, Field)]](q"com.abdulradi.agamemnon.schema.TypedQuery(($pk, $sc), ($cc, $nc), ${schema.keyspace}, ${schema.table})")
        //            }
        //          case Parser.Failure(msg, _) =>
        //            c.abort(c.enclosingPosition, msg)
        //        }
        ???
      case _ =>
        c.abort(c.enclosingPosition, "invalid")
    }
  }
}
