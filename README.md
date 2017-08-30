[![Build Status](https://travis-ci.org/cassandra-scala/troy.svg?branch=master)](https://travis-ci.org/cassandra-scala/troy)
[![Gitter](https://badges.gitter.im/cassandra-scala/troy.svg)](https://gitter.im/cassandra-scala/troy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Coverage Status](https://coveralls.io/repos/github/cassandra-scala/troy/badge.svg?branch=master)](https://coveralls.io/github/cassandra-scala/troy?branch=master)

# Warning

Last stable code [(as preseneted in ScalaExchange 2016)](https://skillsmatter.com/skillscasts/9039-introducing-troy-the-schema-safe-cassandra-toolkit) under "v0.5" tag.

The new approach (as presented in ScalaSwarm 2017, and Scala.World 2017) is here (in master branch), however is highly unstable at the moment. If you want something to play with, I recommend checking our [POC](https://github.com/schemasafe/poc) instead.


# What is Troy?

Type-safe & compile-time-checked wrapper around the Cassandra driver. That allows you to write raw CQL queries like:
```
@schemasafe val q = 
  query[Search, Result]("""
    SELECT x, y, z
    FROM test
    WHERE x = ? AND y = ?;
  """);
```

Validating them against your schema, defined as below: 
```
@schema object Schema extends SchemaFromString(
  """     
    CREATE TABLE test (
      x text PRIMARY KEY,
      y int,
      z list<text>
    );
  """
)
```
and showing errors at *compile-time* like:
> Main.scala:15: Column 'ops_typo' not found in table 'test.posts'
OR
> Main.scala:15: Incompatible column type Int <--> troy.driver.CassandraDataType.Text

Check our [examples](examples) for more usecases.


## Compile-time Codec registery
Troy wraps Cassandra's codecs in Typeclasses, to allow picking the correct codec at compile-time, rather than runtime.
This is also extensible, by defining an implicit `HasTypeCodec[YourType, CassandraType]`.

### Optional columns
Troy handles optional values automatically, by wrapping Cassandra's codec with `null` checking.
All you need to do is define your classes to contain `Option[T]` like.
```
case class Post(id: UUID, title: Option[String])
```

## CQL Syntax
Troy targets (but not fully implements) CQL v3.4.3

## Status
Troy is currently is very early stage, testing, issues and contributions are very welcome.

## License ##
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
