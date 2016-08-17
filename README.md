# What is Troy?

Scala wrapper for Cassandra Java client, that focuses on
  1. Leveraging full power of CQL, while retaining full type safety.
  2. Ease of use, less biolerplate, minimizing learning curve.

## How to use

### 1. Add Troy to your dependencies

```
resolvers += Resolver.bintrayRepo("tabdulradi", "maven")

libraryDependencies += "io.github.cassandra-scala" %% "troy" % "0.0.2"
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
Check under "examples" directory for more use-cases.

Using Scala `macro`, Troy will

  1. Validate the Select query against the schema, if you are asking for columns that doesn't exists, *your code won't compile*
  2. Rewrite your code something that converts from a Cassandra Row into an instance if your class

## Compile-time Codec registery
Since Troy knows the schema at compile time, so your queires will be use lower level methods, that allow specificing the codec, looks like `row.getString(1, theCorrectCodecInstance)`, this should minimize the work to be done at runtime.

Resolving of the correct codec has been done at Compile time rather that Runtime. This is also plugable, using Type Classes, you can define you own `HasCodec` instance that maps any Cassandra type to your custom classes, and the compile will pick your codec instead.

### Optional columns
Since Cassandra every column is optional, even if it is part of the primary key!
We have built-in handling for `Option`, saving you from `null`s and `NPE`.

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
