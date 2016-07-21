# What is Troy?

Scala wrapper for Cassandra Java client, that focuses on
  1. Leveraging full power of CQL, while retaining full type safety.
  2. Ease of use, less biolerplate, minimizing learning curve.

## How to use
Troy needs to know the schema at *compile time*, it expects a file called `schema.cql` under `resources` folder.
Schema is represented as plain old CQL data definition statements, the same as you'd write in a `cqlsh`
```
CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
CREATE TABLE test.posts (
  id uuid PRIMARY KEY,
  title text,
  comments list<text>,
  comments_count int
);
```

Now you can write queries as plain strings, as you are used with the Native Cassandra client.
```
import troy.dsl._
val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
implicit val session: Session = cluster.connect()

case class Post(id: UUID, title: String, comments: Seq[String], commentsCount: Option[Int])

val getPost = withSchema { (id: UUID) =>
  cql"SELECT id, title, comments, comments_count FROM test.posts WHERE id = id;"
    .prepared
    .executeAsync
    .all
    .as(Post)
}

val results = getPost(UUID.randomUUID)
```

Using Scala `macro`, Troy will 
1. Validate the Select query agains the schema, if you are asking for columns that doesn't exists, *your code won't compile*
2. Rewrite your code something that converts from a Cassandra Row into an instance if your class

## Compile-time Codec registery
Since Troy knows the schema at compile time, so your queires will be use lower level methods, that allow specificing the codec, looks like `row.getString(1, theCorrectCodecInstance)`, this should minimize the work to be done at runtime.

Resolving of the correct codec has been done at Compile time rather that Runtime. This is also plugable, using Type Classes, you can define you own `HasCodec` instance that maps any Cassandra type to your custom classes, and the compile will pick your codec instead.

### Optional columns
Since Cassandra every column is optional, even if it is part of the primary key! 
We have built-in handling for `Option`, saving you from `null`s and `NPE`.

## CQL Syntax
Troy currently supports CQL v3.3.1

## Status
Troy is currently is very early stage, testing, issues and contributions are very welcome.

## License ##
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
