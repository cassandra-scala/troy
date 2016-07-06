CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
CREATE TABLE test.posts (
  author_id uuid,
  author_name text static,
  post_id uuid,
  post_title text,
  PRIMARY KEY ((author_id), post_id)
);