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

  // Columns
  assertSelectOut[1, "test", "posts",
    Column["author_id"] :: HNil,
    CDT.Uuid :: HNil
  ]

  assertSelectOut[1, "test", "posts",
    Column["author_id"] :: Column["post_id"] :: HNil,
    CDT.Uuid :: CDT.TimeUuid :: HNil
  ]

  // Functions only
  assertSelectOut[1, "test", "posts",
    Function[NoKeyspace, "now", HNil] :: HNil,
    CDT.TimeUuid :: HNil
  ]

  // Functions on columns
  assertSelectOut[1, "test", "posts",
    Function[NoKeyspace, "writetime", Column["author_id"] :: HNil] :: HNil,
    CDT.BigInt :: HNil
  ]

  // Functions AND columns
  assertSelectOut[1, "test", "posts",
    Column["author_id"] :: Function[NoKeyspace, "writetime", Column["author_id"] :: HNil] :: HNil,
    CDT.Uuid :: CDT.BigInt :: HNil
  ]

  // Functions on Functions
  assertSelectOut[1, "test", "posts",
    Function[NoKeyspace, "dateof", Function[NoKeyspace, "now", HNil] :: HNil] :: HNil,
    CDT.Timestamp :: HNil
  ]
}
