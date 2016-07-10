package troy.driver

/**
 * Represents Cassandra Types
 * To be used as Type parameters, not for instantiation
 */
sealed trait CassandraDataType
object CassandraDataType {
  sealed trait Native extends CassandraDataType
  final class Ascii private () extends Native
  final class BigInt private () extends Native
  final class Blob private () extends Native
  final class Boolean private () extends Native
  final class Counter private () extends Native
  final class Date private () extends Native
  final class Decimal private () extends Native
  final class Double private () extends Native
  final class Float private () extends Native
  final class Inet private () extends Native
  final class Int private () extends Native
  final class SmallInt private () extends Native
  final class Text private () extends Native
  final class Time private () extends Native
  final class Timestamp private () extends Native
  final class TimeUuid private () extends Native
  final class TinyInt private () extends Native
  final class Uuid private () extends Native
  final class VarChar private () extends Native
  final class VarInt private () extends Native

  trait Collection extends CassandraDataType
  final class List[T <: Native] private () extends Collection
  final class Set[T <: Native] private () extends Collection
  final class Map[T <: Native, V <: Native] private () extends Collection

  //  final class Tuple(ts: Seq[DataType]) extends DataType
  //  final class Custom(javaClass: String) extends DataType
}
