import sbt._

object Version {
  final val Scala      = "2.11.7"
  final val ScalaCheck = "1.13.0"
  final val ScalaTest = "2.2.6"
  final val ScalaParserCombinators = "1.0.4"
}

object Library {
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.ScalaCheck
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
  val scalaReflect = "org.scala-lang" % "scala-reflect" % Version.Scala
  val scalaParserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.ScalaParserCombinators
  val cassandraDriverCore = "com.datastax.cassandra"  % "cassandra-driver-core" % "3.0.0"
  val cassandraUnit = "org.cassandraunit" % "cassandra-unit" % "3.0.0.1"
}
