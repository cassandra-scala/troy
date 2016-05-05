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
  author_id text,
  author_name text static,
  post_id text,
  post_title text,
  PRIMARY KEY ((author_id), post_id)
```

Now you can write queries as plain strings, as you are used with the Native Cassandra client.
```
val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
implicit val session: Session = cluster.connect()
val results = Troy.query[Post]("SELECT id, title, body, commentsCount FROM blog.posts") // returns Future[Seq[Post]]
```
Given that class Post looks like
```
case class Post(id: UUID, title: String, body: Option[String], commentsCount: Int)
```
Now using Scala `macro`, Troy will 
1. Validate the Select query agains the schema, if you are asking for columns that doesn't exists, *your code won't compile*
2. Rewrite your code something that converts from a Cassandra Row into an instance if your class

## Performance
At runtime, your code is directly using the Datastax Java client, so no performance penalties to pay.
In fact, it may have a slight performance improvement! 

This because when you write something like `row.getString("body")`, the client has to fetch the correct `Codec` instace for you (to deserialize the bytes comming from the wire), but in Cassandra a String can be a VARCHAR or ASCII, each of them have a different Codec, the client doesn't know this information until the query result arrives!
Well, Troy knows the schema at compile time, so your queires will be use a lower level method, that allow specificing the codec, looks like `row.getString(1, theCorrectCodecInstance)`, this should minimize the work to be done at runtime.

## Types and Codecs
TODO: Talk about handling `Option`s and ability to use your custom types as well!

## CQL Syntax
Troy currently supports CQL v3.3.1

## Status
Troy is currently is very early stage, testing, issues and contributions are very welcome.

### TODO
 - [x] Parse simple select statement
 - [x] Parse create keyspace and table statements
 - [x] Load and parse schema.cql file into in memory data structure
 - [x] Macro to generate compile error if Select statement doesn't match Schema
 - [x] Macro to replace the CQL string to actual query using Cassandra's Java client.
 - [ ] Better error reporting in case of selecting wrong fields
 - [ ] Better error reporting if your case classes doesn't match the type of selected columns
 - [ ] Validate where clause of select statement
 - [ ] Support insert, update and delete statement
 - [ ] Support ALTER TABLE statement (for Schema migration purposes)

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
