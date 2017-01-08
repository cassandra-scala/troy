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

import org.scalatest.FreeSpec
import troy.schema.Messages._

class SchemaLoaderSpec extends FreeSpec with VMatchers {
  import VTestUtils._

  "String Schema Loader should" - {
    "load schema from a valid string" in {
      new StringSchemaLoader("""
          CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
          CREATE TABLE test.posts (author_id uuid, post_id uuid, PRIMARY KEY ((author_id), post_id) );
      """).load should beSuccess
    }

    "fail on non-valid schema string" in {
      new StringSchemaLoader("""
          CREATE TABLE test.posts (author_id uuid, PRIMARY KEY ((author_id), post_id) );
      """).load should failWith[KeyspaceNotFound]
    }
  }

  "Resource File Schema Loader should" - {
    "load valid path" in {
      new ResourceFileSchemaLoader("/test.cql").load should beSuccess
    }

    "fail with SchemaNotFound on wrong path" in {
      new ResourceFileSchemaLoader("/non_existent.cql").load should failWith[SchemaNotFound]
    }
  }

  "Resource Folder Schema Loader should" - {
    "load valid path" in {
      new ResourceFolderSchemaLoader("/test/").load should beSuccess
    }

    "fail with SchemaNotFound on wrong path" in {
      new ResourceFolderSchemaLoader("/non_existent").load should failWith[SchemaNotFound]
    }
  }

  "Resource File or Folder Schema Loader should" - {
    "load file if existing, ignoring the folder path" in {
      new ResourceFileOrFolderSchemaLoader("/test.cql", "/non_existent").load should beSuccess
    }

    "load folder, if file didn't exist" in {
      new ResourceFileOrFolderSchemaLoader("/non_existent.cql", "/test/").load should beSuccess
    }

    "fail if both didn't exist" in {
      new ResourceFileOrFolderSchemaLoader("/non_existent.cql", "/non_existent").load should failWith[SchemaNotFound]
    }
  }
}
