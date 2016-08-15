/*
 * Copyright 2016 Tamer AbdulRadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package troy.macros

import java.util.UUID
import com.datastax.driver.core.Session
import org.scalatest.FreeSpec

class SelectValidationSpec extends FreeSpec {

  import troy.driver.DSL._
  import troy.dsl._

  case class Post(id: UUID, author_name: String, title: String)

  implicit def session: Session = ???

  "The Macro should" - {

    "allow query by partition key" in {
      assertCompiles("""
        withSchema { (authorId: UUID) =>
          cql"SELECT post_id FROM test.posts WHERE author_id = $authorId;".prepared
        }
      """)
    }

    "refuse query by clustering column only" in {
      assertTypeError("""
        withSchema { (postId: UUID) =>
          cql"SELECT post_id FROM test.posts WHERE post_id = $postId;".prepared
        }
      """)
    }

    "allow query by clustering column only if allow filtering was enabled" in {
      assertCompiles("""
        withSchema { (postId: UUID) =>
          cql"SELECT post_id FROM test.posts WHERE post_id = $postId ALLOW FILTERING;".prepared
        }
      """)
    }

    "allow query by whole primary key" in {
      assertCompiles("""
        withSchema { (authorId: UUID, postId: UUID) =>
          cql"SELECT post_id FROM test.posts WHERE author_id = $authorId AND post_id = $postId;".prepared
        }
      """)
    }

    "refuse query by normal unindexed column" in {
      assertTypeError("""
        withSchema { (authorName: String) =>
          cql"SELECT post_id FROM test.posts WHERE author_name = $authorName;".prepared
        }
      """)

      assertTypeError("""
        withSchema { (rating: Int) =>
          cql"SELECT post_id FROM test.posts WHERE post_rating = $rating;".prepared
        }
      """)
    }

    "allow query by normal unindexed column if allow filtering was enabled" in {
      assertCompiles("""
        withSchema { (authorName: String) =>
          cql"SELECT post_id FROM test.posts WHERE author_name = $authorName ALLOW FILTERING;".prepared
        }
      """)

      assertTypeError("""
        withSchema { (rating: Int) =>
          cql"SELECT post_id FROM test.posts WHERE post_rating = $rating ALLOW FILTERING;".prepared
        }
      """)
    }

    "refuse query by normal unindexed column, even if whole primary key is specified" in {
      assertTypeError("""
        withSchema { (authorId: UUID, postId: UUID, authorName: String) =>
          cql"SELECT post_id FROM test.posts WHERE author_id = $authorId AND post_id = $postId AND author_name = $authorName;".prepared
        }
      """)
    }

    "allow query by unindexed column + whole primary key if allow filtering was enabled" in {
      assertCompiles("""
        withSchema { (authorId: UUID, postId: UUID, authorName: String) =>
          cql"SELECT post_id FROM test.posts WHERE author_id = $authorId AND post_id = $postId AND author_name = $authorName ALLOW FILTERING;".prepared
        }
      """)
    }

    "allow query on indexed columns" in {
      assertCompiles("""
        withSchema { (title: String) =>
          cql"SELECT post_id FROM test.posts WHERE post_title = $title;".prepared
        }
      """)
    }

    "allow query on indexed columns, combined with partition key" in {
      assertCompiles("""
        withSchema { (title: String, authorId: UUID) =>
          cql"SELECT post_id FROM test.posts WHERE post_title = $title AND author_id = $authorId;".prepared
        }
      """)
    }

    "allow query by indexed column + whole primary key" in {
      assertCompiles("""
        withSchema { (authorId: UUID, postId: UUID, title: String) =>
          cql"SELECT post_id FROM test.posts WHERE author_id = $authorId AND post_id = $postId AND post_title = $title;".prepared
        }
      """)
    }
  }
}
