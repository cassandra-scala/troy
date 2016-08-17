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
  .dependsOn(cqlParser)
  .settings(libraryDependencies ++= Vector(
    Library.scalaTest % Test
  ))

lazy val troyDriver = project
  .in(file("troy-driver"))
  .settings(name := "troy-driver")
  .settings(libraryDependencies ++= Vector(
    Library.scalaTest % Test,
    Library.cassandraDriverCore
  ))

lazy val troy = project
  .in(file("troy-macro"))
  .settings(libraryDependencies ++= Vector(
    Library.scalaReflect,
    Library.scalaTest % Test,
    Library.cassandraUnit % Test
  ))
  .dependsOn(troyDriver, troySchema)
  .settings(Defaults.coreDefaultSettings ++ Seq(
    unmanagedClasspath in Test ++= (unmanagedResources in Test).value,
    parallelExecution in Test := false
  ): _*)

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

initialCommands := """import java.util.UUID
                     |import troy.Troy
                     |import com.datastax.driver.core._
                     |import scala.concurrent.duration.Duration
                     |import scala.concurrent.Await
                     |import scala.concurrent.ExecutionContext.Implicits.global
                     |
                     |val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
                     |implicit val session: Session = cluster.connect()
                     |
                     |import Troy._
                     |
                     |""".stripMargin