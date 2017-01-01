package troy.driver.driver

import troy.driver.{CassandraDataType => CDT}
import troy.driver.schema.column.ColumnType
import troy.driver.schema.keyspace.KeyspaceExists
import troy.driver.schema.table.TableExists
import troy.driver.schema.version.VersionExists

object TestSchema {
  implicit val v1Exists = VersionExists.instance[1]
  implicit val keyspaceTestExists = KeyspaceExists.instance[1, "test"]
  implicit val tablePostsInKeyspaceTestExists = TableExists.instance[1, "test", "posts"]

  implicit val column1 = ColumnType.instance[1, "test", "posts", "author_id", CDT.Uuid] // Partition key
  implicit val column2 = ColumnType.instance[1, "test", "posts", "post_id", CDT.TimeUuid] // Clustering column
  implicit val column3 = ColumnType.instance[1, "test", "posts", "author_name", CDT.Ascii] // Static
  implicit val column4 = ColumnType.instance[1, "test", "posts", "post_title", CDT.Ascii]
}
