CREATE KEYSPACE test WITH replication = {'class': 'SimpleStrategy' , 'replication_factor': '1'};

CREATE TABLE test.posts (
  author_id uuid,
  post_id uuid,
  author_name text static,
  post_rating int,
  post_title text,
  PRIMARY KEY ((author_id), post_id)
);

CREATE INDEX ON test.posts (post_title);

CREATE TABLE test.post_details (
  author_id uuid,
  id uuid,
  rating int,
  title text,
  tags set<text>,
  comment_ids set<int>,
  comment_userIds list<int>,
  comment_bodies list<text>,
  comments map<int, text>,
  PRIMARY KEY ((author_id), id)
);

CREATE INDEX ON test.post_details (title);
CREATE INDEX ON test.post_details (tags);
CREATE INDEX ON test.post_details (comment_userIds);
CREATE INDEX ON test.post_details (comments);
CREATE INDEX ON test.post_details (KEYS(comments));