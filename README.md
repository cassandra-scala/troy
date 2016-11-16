[![Build Status](https://travis-ci.org/cassandra-scala/troy.svg?branch=master)](https://travis-ci.org/cassandra-scala/troy)
[![Gitter](https://badges.gitter.im/cassandra-scala/troy.svg)](https://gitter.im/cassandra-scala/troy?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Coverage Status](https://coveralls.io/repos/github/cassandra-scala/troy/badge.svg?branch=master)](https://coveralls.io/github/cassandra-scala/troy?branch=master)
# What is Troy?

Type-safe & compile-time-checked wrapper around the Cassandra driver. That allows you to write raw CQL queries like:
```
cql"SELECT post_id, post_title FROM test.posts WHERE author_id = $authorId".prepared.as(Post)
```
Validating them against your schema (defined under `resource/schema.cql`), and showing errors at *compile-time* like:
> Main.scala:15: Column 'ops_typo' not found in table 'test.posts'
OR
> Main.scala:15: Incompatible column type Int <--> troy.driver.CassandraDataType.Text

Check our [examples](examples) for more usecases.

## How to use

### 1. Add Troy to your dependencies

```
resolvers += Resolver.bintrayRepo("tabdulradi", "maven")

libraryDependencies += "io.github.cassandra-scala" %% "troy" % "0.4.0"
```

### 2. Add schema.cql files

Troy needs to know the schema at *compile time*, it expects a file called `schema.cql` under `resources` folder.
Schema is represented as plain old CQL data definition statements, the same as you'd write in a `cqlsh`
```cql
CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
CREATE TABLE test.posts (
  author_id text,
  post_id timeuuid,
  post_title text
  PRIMARY KEY ((author_id), post_id)
);
```

### 3. Enjoy schema-safe queries!

Now you can write queries as plain strings, as you are used with the Native Cassandra client.
```scala
import troy.dsl._

val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
implicit val session: Session = cluster.connect()
case class Post(id: UUID, title: String)

val listByAuthor = withSchema {
  (authorId: String) =>
    cql"""
       SELECT post_id, post_title
       FROM test.posts
       WHERE author_id = $authorId
     """.prepared.as(Post)
}

val results: Future[Seq[Post]] = listByAuthor("test")
```
Now Troy will
  1. Validate the Select query against the schema, if you are asking for columns that doesn't exists, *your code won't compile*
  2. Manages parsing Cassandra `Row` into an instance of the class you provided.

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

### Scala Meta
Troy has a proof-of-concept implementation using ScalaMeta, as show below
```
@withSchema def get(authorId: UUID, postId: UUID) =
      cql"SELECT post_id, author_name, post_title, post_rating FROM test.posts where author_id = $authorId AND post_id = $postId;"
        .prepared
        .executeAsync
        .all
        .as[UUID, String, String, Int, Post](Post)
```
The biggest limitation now is that you have to specify the type params for `as` function, since we can't access the
inferred types.

## License ##
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
