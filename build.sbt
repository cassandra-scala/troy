import sbt.Keys._

lazy val cqlAst = project
  .in(file("cql-ast"))
  .settings(name := "cql-ast")

lazy val cqlParser = project
  .in(file("cql-parser"))
  .settings(name := "cql-parser")
  .settings(libraryDependencies ++= Vector(
    Library.scalaTest % Test,
    Library.scalaParserCombinators
  ))
  .dependsOn(cqlAst)

lazy val troySchema = project
  .in(file("troy-schema"))
  .settings(name := "troy-schema")
  .dependsOn(cqlParser % "test->test;compile->compile")
  .settings(libraryDependencies ++= Vector(
    Library.scalaTest % Test
  ))

lazy val troyDriver = project
  .in(file("troy-driver"))
  .settings(name := "troy-driver")
  .settings(libraryDependencies ++= Vector(
    Library.scalaReflect,
    Library.scalaTest % Test,
    Library.mockito % Test,
    Library.cassandraDriverCore,
    Library.shapeless
  ))

lazy val troy = project
  .in(file("troy-macro"))
  .settings(libraryDependencies ++= Vector(
    Library.scalaReflect,
    Library.scalaTest % Test,
    Library.cassandraUnit % Test
  ))
  .dependsOn(troyDriver, troySchema % "test->test;compile->compile")
  .settings(Defaults.coreDefaultSettings ++ Seq(
    unmanagedClasspath in Test ++= (unmanagedResources in Test).value,
    parallelExecution in Test := false
  ): _*)

/**
  * Post 1.0 era
  */
lazy val typelevelUtils = project
  .in(file("typelevel-utils"))
  .settings(name := "typelevel-utils")
  .settings(libraryDependencies ++= Vector(
    Library.shapeless
  ))

lazy val tast = project
  .in(file("typelevel-ast"))
  .dependsOn(typelevelUtils % "test->test;compile->compile")
  .settings(name := "typelevel-ast")

lazy val troyMeta = project
  .in(file("troy-meta"))
  .settings(name := "troy-meta")
  .settings(libraryDependencies ++= Vector(
    Library.scalaMeta,
    Library.scalaTest % Test,
    Library.cassandraUnit % Test
  ))
  .dependsOn(troyDriver, troySchema)
  .settings(Defaults.coreDefaultSettings ++ Seq(
    unmanagedClasspath in Test ++= (unmanagedResources in Test).value,
    parallelExecution in Test := false,
    resolvers += Resolver.sonatypeRepo("snapshots"),
    addCompilerPlugin(Library.macroParadise)
  ): _*)

lazy val root = project.in(file("."))
  .settings(name := "troy-root", publishArtifact := false, publish := {}, publishLocal := {})
  .aggregate(troy, troyDriver, troySchema, cqlParser, cqlAst)
