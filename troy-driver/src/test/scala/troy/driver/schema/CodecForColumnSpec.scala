package troy
package driver.schema

import org.scalatest.{FlatSpec, Matchers}
import shapeless.test.illTyped
import troy.driver.{CassandraDataType => CDT}
import troy.driver.codecs.TroyCodec
import troy.driver.codecs.PrimitivesCodecs.intAsInt
import troy.driver.schema.column.{CodecForColumn, ColumnType}
import troy.driver.schema.KeyspaceExists
import troy.driver.schema.TableExists
import troy.driver.schema.VersionExists

class CodecForColumnSpec extends FlatSpec with Matchers {
  implicit val v1Exists = VersionExists.instance[1]
  implicit val keyspaceTestExists = KeyspaceExists.instance[1, "my_keyspace"]
  implicit val tablePostsInKeyspaceTestExists = TableExists.instance[1, "my_keyspace", "my_table"]

  implicit val column1 = ColumnType.instance[1, "my_keyspace", "my_table", "my_column_1", CDT.Ascii]
  implicit val column2 = ColumnType.instance[1, "my_keyspace", "my_table", "my_column_2", CDT.Int]
  implicit val column3 = ColumnType.instance[1, "my_keyspace", "my_table", "my_column_3", CDT.List[CDT.Int]]
  implicit val column4 = ColumnType.instance[1, "my_keyspace", "my_table", "my_column_4", CDT.Map[CDT.Int, CDT.Ascii]]

  CodecForColumn[1, "my_keyspace", "my_table", "my_column_1"].codec: TroyCodec[CDT.Ascii, String]
  CodecForColumn[1, "my_keyspace", "my_table", "my_column_2"].codec: TroyCodec[CDT.Int, Int]
  CodecForColumn[1, "my_keyspace", "my_table", "my_column_3"].codec: TroyCodec[CDT.List[CDT.Int], Seq[Int]]
  CodecForColumn[1, "my_keyspace", "my_table", "my_column_4"].codec: TroyCodec[CDT.Map[CDT.Int, CDT.Ascii], Map[Int, String]]

  "Fetching codec for non existing column" should "fail" in {
    illTyped(
      """
        CodecForColumn[1, "my_keyspace", "my_table", "my_column_99"].codec
      """)
  }
}
