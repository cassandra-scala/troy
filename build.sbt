lazy val cqlAst = project
  .in(file("cql-ast"))

lazy val cqlParser = project
  .in(file("cql-parser"))
  .settings(libraryDependencies ++= Vector(
    Library.scalaTest % "test",
    Library.scalaParserCombinators
  ))
  .dependsOn(cqlAst)

lazy val troySchema = project
  .in(file("troy-schema"))
  .dependsOn(cqlParser)
  .settings(libraryDependencies ++= Vector(
    Library.scalaTest % "test"
  ))

lazy val troyDriver = project
  .in(file("troy-driver"))
  .settings(libraryDependencies ++= Vector(
    Library.scalaTest % "test",
    Library.cassandraDriverCore
  ))

lazy val troyMacro = project
  .in(file("troy-macro"))
  .settings(libraryDependencies ++= Vector(
    Library.scalaReflect,
    Library.scalaTest % "test"
  ))
  .dependsOn(troyDriver, troySchema)
  .configs( IntegrationTest )
  .settings((Defaults.coreDefaultSettings ++ Seq(
    unmanagedClasspath in Test ++= (unmanagedResources in Test).value
  )) : _*)

scalacOptions ++= Seq("-unchecked", "-deprecation")

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
