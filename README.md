# What is Troy?

Scala wrapper for Cassandra Java client, that focuses on
1. Leveraging full power of CQL 
2. Type Safety
3. Ease of use, minimal learning curve.

## Use case
Troy shines with complex schemas, especially with nested entities.
Let's design a simple blog platform, we have the following entities
- Posts
- Comments
One of the possible table schema would be
```
CREATE TABLE comments_by_post (
  post_id text,
  post_title text static,
  post_body text static,
  comment_id text,
  comment_body text,
  PRIMARY KEY ((post_id), comment_id)
```
Here our rows are not flat, `static` columns belong to the partition.
You can imagine comments grouped/nested under posts.

Troy allows you to query such query in a correct nested structure.
```
case class Comment(id: String, body: String)
case class Post(id: String, title: String, body: String, comments: Seq[Comment])

val posts: Future[Seq[Post]] = q"select * from comments_by_post".get.as[Post]
```
## CQL Syntax
Troy currently supports CQL v3.3.1

## Status
Troy is currently is very early stage, most of the features described above 
are not even implemented yet! 
Please watch the repo for updates soon.
Contribution is very welcome.

### TODO
 - [x] Parse simple select statement
 - [x] Parse create keyspace and table statements
 - [ ] Define Schema classes 
 - [ ] Load and parse schema.cql file into Schema instance
 - [ ] Macro to generate compile error if Select statement doesn't match Schema
 - [ ] Define `Query` class that represents a schematized Select statement.
 - [ ] Macro to replace the CQL string to actual query using Cassandra's Java client.

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
