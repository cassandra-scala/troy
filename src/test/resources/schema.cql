create KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
CREATE TABLE test.posts (
  author_id text,
  author_name text static,
  post_id text,
  post_title text,
  PRIMARY KEY ((author_id), post_id)
);

create KEYSPACE blog WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};
CREATE TABLE blog.posts (
  id uuid PRIMARY KEY,
  title text,
  body text,
  commentsCount int
);