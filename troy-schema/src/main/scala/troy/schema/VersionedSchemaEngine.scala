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
package schema

import troy.cql.ast._
import troy.schema.Messages.QueryNotCrossCompatible

object VersionedSchemaEngine {
  type Version = Int

  def apply(allStatements: Seq[Seq[DataDefinition]]): Result[VersionedSchemaEngine] = {
    val initial: Result[VersionedSchemaEngine] = SchemaEngine(allStatements.head).map(wrap)
    allStatements.tail.foldLeft(initial) {
      case (versionedSchema, statements) =>
        statements.foldLeft(versionedSchema.map(_.incrementVersion)) {
          case (schema, statement) =>
            schema.flatMap(_ + statement)
        }
    }
  }

  def wrap(schema: SchemaEngine) =
    VersionedSchemaEngineImpl(Map(1 -> schema))
}

trait VersionedSchemaEngine extends SchemaEngine {
  import SchemaEngine._
  import VersionedSchemaEngine.Version

  /*
   * Check the query against range of versions, starting from `min` version, up to the latest.
   */
  def apply(statement: DataManipulation, min: Version): Result[(RowType, VariableTypes)]

  /*
   * Check the query against range of versions, starting from `min` version, up to the `max`.
   */
  def apply(statement: DataManipulation, min: Version, max: Version): Result[(RowType, VariableTypes)]

  /*
   * Adds a new version, without changing any of previous version.
   */
  def incrementVersion: VersionedSchemaEngine

  def +(statement: DataDefinition): Result[VersionedSchemaEngine]
}

import VersionedSchemaEngine.Version
case class VersionedSchemaEngineImpl(schemas: Map[Version, SchemaEngine]) extends VersionedSchemaEngine {
  import SchemaEngine._

  val latestVersion: Version = schemas.keys.max
  val latestSchema: SchemaEngine = schemas(latestVersion)

  private[this] def setSchema(version: Version, schema: SchemaEngine) =
    copy(schemas + (version -> schema))

  override def incrementVersion: VersionedSchemaEngine = setSchema(latestVersion + 1, latestSchema)

  override def +(statement: DataDefinition): Result[VersionedSchemaEngine] =
    (latestSchema + statement).map(setSchema(latestVersion, _))

  def apply(statement: DataManipulation): Result[(RowType, VariableTypes)] =
    latestSchema(statement)

  def apply(statement: DataManipulation, min: Version): Result[(RowType, VariableTypes)] =
    apply(statement, min, latestVersion)

  def apply(statement: DataManipulation, min: Version, max: Version): Result[(RowType, VariableTypes)] = {
    val versions = min to max
    val results = versions.map(schemas.apply).map(_.apply(statement))
    val result = results.head
    if (results.forall(_ == result)) // Fail if query has different signature
      result
    else
      V.error(QueryNotCrossCompatible((versions zip results).collect {
        case (v, V.Error(es, ws)) => (v, es, ws)
      }))
  }

}
