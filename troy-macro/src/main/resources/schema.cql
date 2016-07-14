CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
CREATE TABLE test.posts (
  author_id uuid,
  post_id uuid,
  author_name text static,
  post_rating int,
  post_title text,
  PRIMARY KEY ((author_id), post_id)
);

CREATE TABLE test.post_details (
  author_id uuid,
  id uuid,
  rating int,
  title text,
  tags set<text>,
  PRIMARY KEY ((author_id), id)
);