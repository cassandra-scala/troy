package troy
package meta

import scala.meta._

class Debug extends scala.annotation.StaticAnnotation {

  inline def apply(defn: Defn): Any = meta {
    println(defn)
    defn
  }
}