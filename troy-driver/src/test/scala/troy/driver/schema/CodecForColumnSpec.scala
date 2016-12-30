package troy.driver.schema

import org.scalatest.{FlatSpec, Matchers}
import shapeless.test.illTyped
import troy.driver.{CassandraDataType => CDT}
import troy.driver.codecs.TroyCodec
import troy.driver.codecs.PrimitivesCodecs.intAsInt

class CodecForColumnSpec extends FlatSpec with Matchers {
  implicit val column1 = ColumnType.instance[1, "my_keyspace", "my_table", "my_column_1", CDT.Ascii]
  implicit val column2 = ColumnType.instance[1, "my_keyspace", "my_table", "my_column_2", CDT.Int]

  CodecForColumn[1, "my_keyspace", "my_table", "my_column_1"].codec: TroyCodec[CDT.Ascii, String]
  CodecForColumn[1, "my_keyspace", "my_table", "my_column_2"].codec: TroyCodec[CDT.Int, Int]

  "Fetching codec for non existing column" should "fail" in {
    illTyped(
      """
        CodecForColumn[1, "my_keyspace", "my_table", "my_column_3"].codec
      """)
  }
}
