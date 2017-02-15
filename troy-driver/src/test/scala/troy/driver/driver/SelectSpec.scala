package troy
package driver.driver

import shapeless._
import troy.driver.query.select.SelectionTypeResolver$
import troy.driver.{CassandraDataType => CDT}
import troy.driver.query.select._

object SelectionSpec {
  import Select._
  import TestSchema._

  def assertSelectionOut[V, K, T, S, O](implicit s: SelectionTypeResolver.Aux[V, K, T, S, O]) = ()

  assertSelectionOut[1, "test", "posts",
    HNil,
    HNil
  ]

  // Columns
  assertSelectionOut[1, "test", "posts",
    Column["author_id"] :: HNil,
    CDT.Uuid :: HNil
  ]

  assertSelectionOut[1, "test", "posts",
    Column["author_id"] :: Column["post_id"] :: HNil,
    CDT.Uuid :: CDT.TimeUuid :: HNil
  ]

  // Functions only
  assertSelectionOut[1, "test", "posts",
    Function[NoKeyspace, "now", HNil] :: HNil,
    CDT.TimeUuid :: HNil
  ]

  // Functions on columns
  assertSelectionOut[1, "test", "posts",
    Function[NoKeyspace, "writetime", Column["author_id"] :: HNil] :: HNil,
    CDT.BigInt :: HNil
  ]

  // Functions AND columns
  assertSelectionOut[1, "test", "posts",
    Column["author_id"] :: Function[NoKeyspace, "writetime", Column["author_id"] :: HNil] :: HNil,
    CDT.Uuid :: CDT.BigInt :: HNil
  ]

  // Functions on Functions
  assertSelectionOut[1, "test", "posts",
    Function[NoKeyspace, "dateof", Function[NoKeyspace, "now", HNil] :: HNil] :: HNil,
    CDT.Timestamp :: HNil
  ]
}
