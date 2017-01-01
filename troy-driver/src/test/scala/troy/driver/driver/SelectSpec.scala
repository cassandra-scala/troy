package troy.driver.driver

import org.scalatest.{FlatSpec, Matchers}
import shapeless._
import shapeless.test.illTyped
import troy.driver.codecs.TroyCodec
import troy.driver.query.select.Select
import troy.driver.schema.column.{CodecForColumn, ColumnType}
import troy.driver.schema.version.VersionExists
import troy.driver.{CassandraDataType => CDT}
import troy.driver.query.select._

object SelectSpec {
  import Select._
  import TestSchema._

  def assertSelectOut[V, K, T, S, O](implicit s: Select.Aux[V, K, T, S, O]) = ()

  assertSelectOut[1, "test", "posts",
    HNil,
    HNil
  ]

  assertSelectOut[1, "test", "posts",
    Column["author_id"] :: HNil,
    CDT.Uuid :: HNil
  ]

  assertSelectOut[1, "test", "posts",
    Column["author_id"] :: Column["post_id"] :: HNil,
    CDT.Uuid :: CDT.TimeUuid :: HNil
  ]
}
