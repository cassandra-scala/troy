/*
This is
Multiline
Comments test
*/

CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};

CREATE TABLE test.posts (
  author_id uuid, // Single line comment test with slash
  post_id uuid,
  author_name text static,
  post_title text,
  PRIMARY KEY ((author_id), post_id)
);

CREATE TABLE test.post_details (
  author_id uuid,
  id uuid,
  rating int,
  title text, -- Single line comment test with dash
  someField text,
  tags set<text>,
  comment_ids set<int>,
  comments map<int, text>,
  PRIMARY KEY ((author_id), id)
);

CREATE INDEX ON test.post_details (title);
CREATE INDEX ON test.post_details (comments);