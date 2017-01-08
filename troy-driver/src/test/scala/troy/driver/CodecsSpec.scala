package troy
package driver

import java.math.{ BigInteger, BigDecimal }
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{ UUID, Date }

import scala.collection.JavaConverters._

import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core._

import org.mockito.Matchers
import org.mockito.Mockito._
import org.mockito.Matchers._

import org.scalatest.{ MustMatchers, FreeSpec }
import org.scalatest.mock.MockitoSugar

import troy.driver.codecs.PrimitivesCodecs._
import InternalDsl._

class CodecsSpec extends FreeSpec with MockitoSugar with MustMatchers {

  "InternalDSL Codecs for type pair" - {

    "Int <--> int" - {
      type SType = Int
      val value: SType = 55
      type CType = CDT.Int
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.getInt(0)).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).setInt(0, value)
      }
    }

    "Long <-->" - {
      type SType = Long
      val value: SType = 55555L

      "BigInt" - {
        type CType = CDT.BigInt
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getLong(0)).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).setLong(0, value)
        }
      }
      "Counter" - {
        type CType = CDT.Counter
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getLong(0)).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).setLong(0, value)
        }
      }
      "Time" - {
        type CType = CDT.Time
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getLong(0)).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).setLong(0, value)
        }
      }
    }

    "Short <--> smallint" - {
      type SType = Short
      val value: SType = 5.toShort
      type CType = CDT.SmallInt

      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.getShort(0)).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).setShort(0, value)
      }
    }
    "Byte <--> tinyint" - {
      type SType = Byte
      val value: SType = 5.toByte
      type CType = CDT.TinyInt

      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.getByte(0)).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).setByte(0, value)
      }
    }
    "Double <--> double" - {
      type SType = Double
      val value: SType = 5.5D
      type CType = CDT.Double

      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.getDouble(0)).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).setDouble(0, value)
      }
    }
    "Float <--> float" - {
      type SType = Float
      val value: SType = 5.5F
      type CType = CDT.Float
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.getFloat(0)).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).setFloat(0, value)
      }
    }
    "Boolean <--> boolean" - {
      type SType = Boolean
      val value: SType = true
      type CType = CDT.Boolean
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.getBool(0)).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).setBool(0, value)
      }
    }
    "String <-->" - {
      type SType = String
      val value: SType = ""
      type TCodec = TypeCodec[SType]
      "ascii" - {
        type CType = CDT.Ascii
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
        }
      }
      "text" - {
        type CType = CDT.Text
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
        }
      }
      "varchar" - {
        type CType = CDT.VarChar
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
        }
      }
    }
    "Date <--> timestamp" - {
      type SType = Date
      val value: SType = new Date
      type CType = CDT.Timestamp
      type TCodec = TypeCodec[SType]
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
      }
    }
    "BigDecimal <--> decimal" - {
      type SType = BigDecimal
      val value: SType = BigDecimal.ZERO
      type CType = CDT.Decimal
      type TCodec = TypeCodec[SType]
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
      }
    }
    "InetAddress <--> inet" - {
      type SType = InetAddress
      val value: SType = InetAddress.getLocalHost
      type CType = CDT.Inet
      type TCodec = TypeCodec[SType]
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
      }
    }
    "BigInteger <--> varint" - {
      type SType = BigInteger
      val value: SType = BigInteger.ZERO
      type CType = CDT.VarInt
      type TCodec = TypeCodec[SType]
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
      }
    }
    "ByteBuffer <--> blob" - {
      type SType = ByteBuffer
      val value: SType = ByteBuffer.allocate(1)
      type CType = CDT.Blob
      type TCodec = TypeCodec[SType]
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
      }
    }
    "UUID <-->" - {
      type SType = Option[UUID]
      val value: SType = Some(UUIDs.timeBased)
      type TCodec = TypeCodec[SType]
      "timeuuid" - {
        type CType = CDT.TimeUuid
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
        }
      }
      "uuid" - {
        type CType = CDT.Uuid
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
          column[SType](0).as[CType] mustBe value
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(value).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
        }
      }
    }
    "LocalDate <--> .date" - {
      type SType = LocalDate
      val value: SType = LocalDate.fromMillisSinceEpoch(0)
      type CType = CDT.Date
      type TCodec = TypeCodec[SType]
      "get from Row" in {
        implicit val gettable = mock[Row]
        when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(value)
        column[SType](0).as[CType] mustBe value
      }
      "set into BoundStatement" in {
        val settable = mock[BoundStatement]
        param(value).as[CType].set(settable, 0)
        verify(settable).set(Matchers.eq(0), Matchers.eq(value), any[TCodec])
      }
    }

    "Option" - {
      "[Int] <--> int" - {
        type JType = Int
        type SType = Option[JType]
        val jValue: JType = 55
        val sValue: SType = Some(jValue)
        type CType = CDT.Int

        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.isNull(0)).thenReturn(false)
          when(gettable.getInt(0)).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).setInt(0, jValue)
        }
        "get from Row (nullable)" in {
          implicit val gettable = mock[Row]
          when(gettable.isNull(0)).thenReturn(true)
          column[SType](0).as[CType] mustBe None
        }
        "set into BoundStatement (nullable)" in {
          val settable = mock[BoundStatement]
          param(None: SType).as[CType].set(settable, 0)
          verify(settable).setToNull(0)
        }
      }
      "[Long] <--> list" - {
        type JType = Long
        type SType = Option[JType]
        val jValue: JType = 55555L
        val sValue: SType = Some(jValue)

        "<bigint>" - {
          type CType = CDT.BigInt

          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.getLong(0)).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).setLong(0, jValue)
          }
          "get from Row (nullable)" in {
            implicit val gettable = mock[Row]
            when(gettable.isNull(0)).thenReturn(true)
            column[SType](0).as[CType] mustBe None
          }
          "set into BoundStatement (nullable)" in {
            val settable = mock[BoundStatement]
            param(None: SType).as[CType].set(settable, 0)
            verify(settable).setToNull(0)
          }
        }
        "<counter>" - {
          type CType = CDT.Counter

          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.getLong(0)).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).setLong(0, jValue)
          }
          "get from Row (nullable)" in {
            implicit val gettable = mock[Row]
            when(gettable.isNull(0)).thenReturn(true)
            column[SType](0).as[CType] mustBe None
          }
          "set into BoundStatement (nullable)" in {
            val settable = mock[BoundStatement]
            param(None: SType).as[CType].set(settable, 0)
            verify(settable).setToNull(0)
          }
        }
        "<time>" - {
          type CType = CDT.Time
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.getLong(0)).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).setLong(0, jValue)
          }
          "get from Row (nullable)" in {
            implicit val gettable = mock[Row]
            when(gettable.isNull(0)).thenReturn(true)
            column[SType](0).as[CType] mustBe None
          }
          "set into BoundStatement (nullable)" in {
            val settable = mock[BoundStatement]
            param(None: SType).as[CType].set(settable, 0)
            verify(settable).setToNull(0)
          }
        }
      }
      "[Short] <--> smallint" - {
        type JType = Short
        type SType = Option[JType]
        val jValue: JType = 5.toShort
        val sValue: SType = Some(jValue)
        type CType = CDT.SmallInt

        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getShort(0)).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).setShort(0, jValue)
        }
        "get from Row (nullable)" in {
          implicit val gettable = mock[Row]
          when(gettable.isNull(0)).thenReturn(true)
          column[SType](0).as[CType] mustBe None
        }
        "set into BoundStatement (nullable)" in {
          val settable = mock[BoundStatement]
          param(None: SType).as[CType].set(settable, 0)
          verify(settable).setToNull(0)
        }
      }
      "[Byte] <--> tinyint" - {
        type JType = Byte
        type SType = Option[JType]
        val jValue: JType = 5.toByte
        val sValue: SType = Some(jValue)
        type CType = CDT.TinyInt

        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getByte(0)).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).setByte(0, jValue)
        }
        "get from Row (nullable)" in {
          implicit val gettable = mock[Row]
          when(gettable.isNull(0)).thenReturn(true)
          column[SType](0).as[CType] mustBe None
        }
        "set into BoundStatement (nullable)" in {
          val settable = mock[BoundStatement]
          param(None: SType).as[CType].set(settable, 0)
          verify(settable).setToNull(0)
        }
      }
      "[Double] <--> double" - {
        type JType = Double
        type SType = Option[JType]
        val jValue: JType = 5.5D
        val sValue: SType = Some(jValue)
        type CType = CDT.Double

        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getDouble(0)).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).setDouble(0, jValue)
        }
        "get from Row (nullable)" in {
          implicit val gettable = mock[Row]
          when(gettable.isNull(0)).thenReturn(true)
          column[SType](0).as[CType] mustBe None
        }
        "set into BoundStatement (nullable)" in {
          val settable = mock[BoundStatement]
          param(None: SType).as[CType].set(settable, 0)
          verify(settable).setToNull(0)
        }
      }
      "[Float] <--> float" - {
        type JType = Float
        type SType = Option[JType]
        val jValue: JType = 5.5F
        val sValue: SType = Some(jValue)
        type CType = CDT.Float

        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getFloat(0)).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).setFloat(0, jValue)
        }
        "get from Row (nullable)" in {
          implicit val gettable = mock[Row]
          when(gettable.isNull(0)).thenReturn(true)
          column[SType](0).as[CType] mustBe None
        }
        "set into BoundStatement (nullable)" in {
          val settable = mock[BoundStatement]
          param(None: SType).as[CType].set(settable, 0)
          verify(settable).setToNull(0)
        }
      }
      "[Boolean] <--> boolean" - {
        type JType = Boolean
        type SType = Option[JType]
        val jValue: JType = true
        val sValue: SType = Some(jValue)
        type CType = CDT.Boolean

        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.getBool(0)).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).setBool(0, jValue)
        }
        "get from Row (nullable)" in {
          implicit val gettable = mock[Row]
          when(gettable.isNull(0)).thenReturn(true)
          column[SType](0).as[CType] mustBe None
        }
        "set into BoundStatement (nullable)" in {
          val settable = mock[BoundStatement]
          param(None: SType).as[CType].set(settable, 0)
          verify(settable).setToNull(0)
        }
      }
      "[String] <-->" - {
        type JType = String
        type SType = Option[JType]
        val jValue: JType = ""
        val sValue: SType = Some(jValue)
        type TCodec = TypeCodec[SType]
        "ascii" - {
          type CType = CDT.Ascii
          "get from Row" in {
            implicit val gettable = FakeAbstractGettableByIndexData.ascii(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
          }
          "get from Row (nullable)" in {
            implicit val gettable = FakeAbstractGettableByIndexData.ascii(null)
            column[SType](0).as[CType] mustBe None
          }
          "set into BoundStatement (nullable)" in {
            val settable = mock[BoundStatement]
            param(None: SType).as[CType].set(settable, 0)
          }
        }
      }
      "[Date] <--> timestamp" - {
        type JType = Date
        type SType = Option[JType]
        val jValue: JType = new Date
        val sValue: SType = Some(jValue)
        type TCodec = TypeCodec[SType]
        type CType = CDT.Timestamp
        val settable = mock[BoundStatement]

        "get from Row" in {
          implicit val gettable = FakeAbstractGettableByIndexData.timestamp(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          param(sValue).as[CType].set(settable, 0)
        }
        "get from Row (nullable)" in {
          implicit val gettable = FakeAbstractGettableByIndexData.timestamp(null)
          column[SType](0).as[CType] mustBe None
        }
        "set into BoundStatement (nullable)" in {
          param(None: SType).as[CType].set(settable, 0)
        }
      }
    }

    "Seq" - {
      "[Int] <--> list<int>" - {
        type SType = Seq[Int]
        type JType = java.util.List[Int]
        val sValue: SType = Seq(55)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Int]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Long] <--> list" - {
        type SType = Seq[Long]
        type JType = java.util.List[Long]
        val sValue: SType = Seq(55555L)
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]

        "<bigint>" - {
          type CType = CDT.List[CDT.BigInt]

          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<counter>" - {
          type CType = CDT.List[CDT.Counter]

          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<time>" - {
          type CType = CDT.List[CDT.Time]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[Short] <--> list<smallint>" - {
        type SType = Seq[Short]
        type JType = java.util.List[Short]
        val sValue: SType = Seq(5.toShort)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.SmallInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Byte] <--> list<tinyint>" - {
        type SType = Seq[Byte]
        type JType = java.util.List[Byte]
        val sValue: SType = Seq(5.toByte)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.TinyInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Double] <--> list<double>" - {
        type SType = Seq[Double]
        type JType = java.util.List[Double]
        val sValue: SType = Seq(5.5D)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Double]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Float] <--> list<float>" - {
        type SType = Seq[Float]
        type JType = java.util.List[Float]
        val sValue: SType = Seq(5.5F)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Float]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Boolean] <--> list<boolean>" - {
        type SType = Seq[Boolean]
        type JType = java.util.List[Boolean]
        val sValue: SType = Seq(true)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Boolean]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[String] <--> list" - {
        type SType = Seq[String]
        type JType = java.util.List[String]
        val sValue: SType = Seq("")
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<ascii>" - {
          type CType = CDT.List[CDT.Ascii]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<text>" - {
          type CType = CDT.List[CDT.Text]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<varchar>" - {
          type CType = CDT.List[CDT.VarChar]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[Date] <--> list<timestamp>" - {
        type SType = Seq[Date]
        type JType = java.util.List[Date]
        val sValue: SType = Seq(new Date)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Timestamp]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[BigDecimal] <--> list<decimal>" - {
        type SType = Seq[BigDecimal]
        type JType = java.util.List[BigDecimal]
        val sValue: SType = Seq(BigDecimal.ZERO)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Decimal]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[InetAddress] <--> list<inet>" - {
        type SType = Seq[InetAddress]
        type JType = java.util.List[InetAddress]
        val sValue: SType = Seq(InetAddress.getLocalHost)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Inet]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[BigInteger] <--> list<varint>" - {
        type SType = Seq[BigInteger]
        type JType = java.util.List[BigInteger]
        val sValue: SType = Seq(BigInteger.ZERO)
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.VarInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[ByteBuffer] <--> list<blob>" - {
        type SType = Seq[ByteBuffer]
        type JType = java.util.List[ByteBuffer]
        val sValue: SType = Seq(ByteBuffer.allocate(1))
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Blob]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[UUID] <--> list" - {
        type SType = Seq[UUID]
        type JType = java.util.List[UUID]
        val sValue: SType = Seq(UUIDs.timeBased)
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<timeuuid>" - {
          type CType = CDT.List[CDT.TimeUuid]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<uuid>" - {
          type CType = CDT.List[CDT.Uuid]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }

      "LocalDate <--> list<date>" - {
        type SType = Seq[LocalDate]
        type JType = java.util.List[LocalDate]
        val sValue: SType = Seq(LocalDate.fromMillisSinceEpoch(0))
        val jValue: JType = sValue.asJava
        type CType = CDT.List[CDT.Date]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
    }
    "Set" - {
      "[Int] <--> set<int>" - {
        type SType = Set[Int]
        type JType = java.util.Set[Int]
        val sValue: SType = Set(55)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Int]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Long] <--> set" - {
        type SType = Set[Long]
        type JType = java.util.Set[Long]
        val sValue: SType = Set(55555L)
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<bigint>" - {
          type CType = CDT.Set[CDT.BigInt]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<counter>" - {
          type CType = CDT.Set[CDT.Counter]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<time>" - {
          type CType = CDT.Set[CDT.Time]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[Short] <--> set<smallint>" - {
        type SType = Set[Short]
        type JType = java.util.Set[Short]
        val sValue: SType = Set(5.toShort)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.SmallInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Byte] <--> set<tinyint>" - {
        type SType = Set[Byte]
        type JType = java.util.Set[Byte]
        val sValue: SType = Set(5.toByte)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.TinyInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Double] <--> set<double>" - {
        type SType = Set[Double]
        type JType = java.util.Set[Double]
        val sValue: SType = Set(5.5D)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Double]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Float] <--> set<float>" - {
        type SType = Set[Float]
        type JType = java.util.Set[Float]
        val sValue: SType = Set(5.5F)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Float]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Boolean] <--> set<boolean>" - {
        type SType = Set[Boolean]
        type JType = java.util.Set[Boolean]
        val sValue: SType = Set(true)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Boolean]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[String] <--> set" - {
        type SType = Set[String]
        type JType = java.util.Set[String]
        val sValue: SType = Set("")
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<ascii>" - {
          type CType = CDT.Set[CDT.Ascii]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<text>" - {
          type CType = CDT.Set[CDT.Text]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<varchar>" - {
          type CType = CDT.Set[CDT.VarChar]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[Date] <--> set<timestamp>" - {
        type SType = Set[Date]
        type JType = java.util.Set[Date]
        val sValue: SType = Set(new Date)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Timestamp]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[BigDecimal] <--> set<decimal>" - {
        type SType = Set[BigDecimal]
        type JType = java.util.Set[BigDecimal]
        val sValue: SType = Set(BigDecimal.ZERO)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Decimal]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[InetAddress] <--> set<inet>" - {
        type SType = Set[InetAddress]
        type JType = java.util.Set[InetAddress]
        val sValue: SType = Set(InetAddress.getLocalHost)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Inet]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[BigInteger] <--> set<varint>" - {
        type SType = Set[BigInteger]
        type JType = java.util.Set[BigInteger]
        val sValue: SType = Set(BigInteger.ZERO)
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.VarInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[ByteBuffer] <--> set<blob>" - {
        type SType = Set[ByteBuffer]
        type JType = java.util.Set[ByteBuffer]
        val sValue: SType = Set(ByteBuffer.allocate(1))
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Blob]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[UUID] <--> set" - {
        type SType = Set[UUID]
        type JType = java.util.Set[UUID]
        val sValue: SType = Set(UUIDs.timeBased)
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<timeuuid>" - {
          type CType = CDT.Set[CDT.TimeUuid]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<uuid>" - {
          type CType = CDT.Set[CDT.Uuid]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[LocalDate] <--> set<date>" - {
        type SType = Set[LocalDate]
        type JType = java.util.Set[LocalDate]
        val sValue: SType = Set(LocalDate.fromMillisSinceEpoch(0))
        val jValue: JType = sValue.asJava
        type CType = CDT.Set[CDT.Date]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
    }
    "Map" - {
      "[Int, Int] <--> map<int, int>" - {
        type SType = Map[Int, Int]
        type JType = java.util.Map[Int, Int]
        val sValue: SType = Map(55 -> 55)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Int, CDT.Int]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Long, Long] <--> map" - {
        type SType = Map[Long, Long]
        type JType = java.util.Map[Long, Long]
        val sValue: SType = Map(55555L -> 55555L)
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<bigint, bigint>" - {
          type CType = CDT.Map[CDT.BigInt, CDT.BigInt]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<counter, counter>" - {
          type CType = CDT.Map[CDT.Counter, CDT.Counter]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "map<time, time>" - {
          type CType = CDT.Map[CDT.Time, CDT.Time]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[Short, Short] <--> map<smallint, smallint>" - {
        type SType = Map[Short, Short]
        type JType = java.util.Map[Short, Short]
        val sValue: SType = Map(5.toShort -> 5.toShort)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.SmallInt, CDT.SmallInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Byte, Byte] <--> map<tinyint, tinyint>" - {
        type SType = Map[Byte, Byte]
        type JType = java.util.Map[Byte, Byte]
        val sValue: SType = Map(5.toByte -> 5.toByte)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.TinyInt, CDT.TinyInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Double, Double] <--> map<double, double>" - {
        type SType = Map[Double, Double]
        type JType = java.util.Map[Double, Double]
        val sValue: SType = Map(5.5D -> 5.5D)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Double, CDT.Double]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Float, Float] <--> map<float, float>" - {
        type SType = Map[Float, Float]
        type JType = java.util.Map[Float, Float]
        val sValue: SType = Map(5.5F -> 5.5F)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Float, CDT.Float]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Boolean, Boolean] <--> map<boolean, boolean>" - {
        type SType = Map[Boolean, Boolean]
        type JType = java.util.Map[Boolean, Boolean]
        val sValue: SType = Map(true -> true)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Boolean, CDT.Boolean]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[String, String] <--> map" - {
        type SType = Map[String, String]
        type JType = java.util.Map[String, String]
        val sValue: SType = Map("" -> "")
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<ascii, ascii>" - {
          type CType = CDT.Map[CDT.Ascii, CDT.Ascii]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "map<text, text>" - {
          type CType = CDT.Map[CDT.Text, CDT.Text]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "[String, String] <--> map<varchar, varchar>" - {
          type CType = CDT.Map[CDT.VarChar, CDT.VarChar]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[Date, Date] <--> map<timestamp, timestamp>" - {
        type SType = Map[Date, Date]
        type JType = java.util.Map[Date, Date]
        val sValue: SType = Map(new Date -> new Date)
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        type CType = CDT.Map[CDT.Timestamp, CDT.Timestamp]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[BigDecimal, BigDecimal] <--> map<decimal, decimal>" - {
        type SType = Map[BigDecimal, BigDecimal]
        type JType = java.util.Map[BigDecimal, BigDecimal]
        val sValue: SType = Map(BigDecimal.ZERO -> BigDecimal.ZERO)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Decimal, CDT.Decimal]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[InetAddress, InetAddress] <--> map<inet, inet>" - {
        type SType = Map[InetAddress, InetAddress]
        type JType = java.util.Map[InetAddress, InetAddress]
        val sValue: SType = Map(InetAddress.getLocalHost -> InetAddress.getLocalHost)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Inet, CDT.Inet]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[BigInteger, BigInteger] <--> map<varint, varint>" - {
        type SType = Map[BigInteger, BigInteger]
        type JType = java.util.Map[BigInteger, BigInteger]
        val sValue: SType = Map(BigInteger.ZERO -> BigInteger.ZERO)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.VarInt, CDT.VarInt]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[ByteBuffer, ByteBuffer] <--> map<blob, blob>" - {
        type SType = Map[ByteBuffer, ByteBuffer]
        type JType = java.util.Map[ByteBuffer, ByteBuffer]
        val sValue: SType = Map(ByteBuffer.allocate(1) -> ByteBuffer.allocate(1))
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Blob, CDT.Blob]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[UUID, UUID] <--> map" - {
        type SType = Map[UUID, UUID]
        type JType = java.util.Map[UUID, UUID]
        val sValue: SType = Map(UUIDs.timeBased -> UUIDs.timeBased)
        val jValue: JType = sValue.asJava
        type TCodec = TypeCodec[JType]
        "<timeuuid, timeuuid>" - {
          type CType = CDT.Map[CDT.TimeUuid, CDT.TimeUuid]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
        "<uuid, uuid>" - {
          type CType = CDT.Map[CDT.Uuid, CDT.Uuid]
          "get from Row" in {
            implicit val gettable = mock[Row]
            when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
            column[SType](0).as[CType] mustBe sValue
          }
          "set into BoundStatement" in {
            val settable = mock[BoundStatement]
            param(sValue).as[CType].set(settable, 0)
            verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
          }
        }
      }
      "[LocalDate, LocalDate] <--> map<date, date>" - {
        type SType = Map[LocalDate, LocalDate]
        type JType = java.util.Map[LocalDate, LocalDate]
        val sValue: SType = Map(LocalDate.fromMillisSinceEpoch(0) -> LocalDate.fromMillisSinceEpoch(0))
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Date, CDT.Date]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[Int, String] <--> map<int, text>" - {
        type SType = Map[Int, String]
        type JType = java.util.Map[Int, String]
        val sValue: SType = Map(55 -> "")
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Int, CDT.Text]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
      "[String, Int] <--> map<text, int>" - {
        type SType = Map[String, Int]
        type JType = java.util.Map[String, Int]
        val sValue: SType = Map("" -> 55)
        val jValue: JType = sValue.asJava
        type CType = CDT.Map[CDT.Text, CDT.Int]
        type TCodec = TypeCodec[JType]
        "get from Row" in {
          implicit val gettable = mock[Row]
          when(gettable.get(Matchers.eq(0), any[TCodec])).thenReturn(jValue)
          column[SType](0).as[CType] mustBe sValue
        }
        "set into BoundStatement" in {
          val settable = mock[BoundStatement]
          param(sValue).as[CType].set(settable, 0)
          verify(settable).set(Matchers.eq(0), Matchers.eq(jValue), any[TCodec])
        }
      }
    }
    // TODO: Tuple & UDT
  }
}
