package troy
package driver.driver

import troy.driver.{CassandraDataType => CDT}
import troy.driver.schema.column.ColumnType
import troy.driver.schema.KeyspaceExists
import troy.driver.schema.TableExists
import troy.driver.schema.VersionExists

object TestSchema {
  implicit val v1Exists = VersionExists.instance[1]
  implicit val v2Exists = VersionExists.instance[2]
  implicit val keyspacetestExistsInV1 = KeyspaceExists.instance[1, "test"]
  implicit val keyspacetestExistsInV2 = KeyspaceExists.instance[2, "test"]
  implicit val tablepostsExistsInKeyspacetestInV1 = TableExists.instance[1, "test", "posts"]
  implicit val tablepost_detailsExistsInKeyspacetestInV1 = TableExists.instance[1, "test", "post_details"]
  implicit val tablepostsExistsInKeyspacetestInV2 = TableExists.instance[2, "test", "posts"]
  implicit val tablepost_detailsExistsInKeyspacetestInV2 = TableExists.instance[2, "test", "post_details"]
  implicit val columnauthor_idExistsInTablepostsInKeyspacetestInV1 = ColumnType.instance[1, "test", "posts", "author_id", CDT.Uuid]
  implicit val columnpost_idExistsInTablepostsInKeyspacetestInV1 = ColumnType.instance[1, "test", "posts", "post_id", CDT.TimeUuid]
  implicit val columnauthor_nameExistsInTablepostsInKeyspacetestInV1 = ColumnType.instance[1, "test", "posts", "author_name", CDT.Text]
  implicit val columnpost_titleExistsInTablepostsInKeyspacetestInV1 = ColumnType.instance[1, "test", "posts", "post_title", CDT.Text]
  implicit val columntagsExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "tags", CDT.Set[CDT.Text]]
  implicit val columnsomeFieldExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "someField", CDT.Text]
  implicit val columncomment_idsExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "comment_ids", CDT.Set[CDT.Int]]
  implicit val columnidExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "id", CDT.Uuid]
  implicit val columnratingExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "rating", CDT.Int]
  implicit val columnauthor_idExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "author_id", CDT.Uuid]
  implicit val columntitleExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "title", CDT.Text]
  implicit val columncommentsExistsInTablepost_detailsInKeyspacetestInV1 = ColumnType.instance[1, "test", "post_details", "comments", CDT.Map[CDT.Int, CDT.Text]]
  implicit val columnpost_titleExistsInTablepostsInKeyspacetestInV2 = ColumnType.instance[2, "test", "posts", "post_title", CDT.Text]
  implicit val columnpost_idExistsInTablepostsInKeyspacetestInV2 = ColumnType.instance[2, "test", "posts", "post_id", CDT.Uuid]
  implicit val columnauthor_nameExistsInTablepostsInKeyspacetestInV2 = ColumnType.instance[2, "test", "posts", "author_name", CDT.Text]
  implicit val columnauthor_idExistsInTablepostsInKeyspacetestInV2 = ColumnType.instance[2, "test", "posts", "author_id", CDT.Uuid]
  implicit val columnpost_ratingExistsInTablepostsInKeyspacetestInV2 = ColumnType.instance[2, "test", "posts", "post_rating", CDT.Int]
  implicit val columntagsExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "tags", CDT.Set[CDT.Text]]
  implicit val columncomment_bodiesExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "comment_bodies", CDT.List[CDT.Text]]
  implicit val columncomment_idsExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "comment_ids", CDT.Set[CDT.Int]]
  implicit val columnidExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "id", CDT.Uuid]
  implicit val columnratingExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "rating", CDT.Int]
  implicit val columnauthor_idExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "author_id", CDT.Uuid]
  implicit val columntitleExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "title", CDT.Text]
  implicit val columncomment_userIdsExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "comment_userIds", CDT.List[CDT.Int]]
  implicit val columncommentsExistsInTablepost_detailsInKeyspacetestInV2 = ColumnType.instance[2, "test", "post_details", "comments", CDT.Map[CDT.Int, CDT.Text]]
}
