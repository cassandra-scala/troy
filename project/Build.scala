import com.typesafe.sbt.{ GitPlugin, SbtScalariform }
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.license.Apache2_0
import sbt._
import sbt.plugins.JvmPlugin
import sbt.Keys._
import scalariform.formatter.preferences.{ AlignSingleLineCaseStatements, DoubleIndentClassDeclaration }
import bintray.BintrayKeys._

object Build extends AutoPlugin {

  override def requires = JvmPlugin && HeaderPlugin && GitPlugin && SbtScalariform

  override def trigger = allRequirements

  def compileSettings = Vector(
    // Core settings
    organization := "com.abdulradi",
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := Version.Scala,
    crossScalaVersions := Vector(scalaVersion.value),
    scalacOptions ++= Vector(
      "-encoding", "UTF-8",
      "-target:jvm-1.8",
      "-language:_",
      "-unchecked",
      "-feature",
      "-deprecation",
      "-Xlint",
      "-Xfuture",
      "-Ywarn-dead-code",
      "-Ywarn-unused-import",
      "-Ywarn-unused",
      "-Ywarn-nullary-unit"
    ),
    unmanagedSourceDirectories.in(Compile) := Vector(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Vector(scalaSource.in(Test).value)
  )

  def testSettings = Vector(
    parallelExecution in Test := false,
    publishArtifact in Test := false
  )

  def stablePublishSettings = Vector(
    publishArtifact in Test := false,

    pomExtra := (
      <modules>
        <module>cqlAst</module>
        <module>cqlParser</module>
        <module>troySchema</module>
        <module>troyDriver</module>
        <module>troyMacro</module>
      </modules>
        <scm>
          <url>git@github.com:tabdulradi/troy.git</url>
          <connection>scm:git:git@github.com:tabdulradi/troy.git</connection>
        </scm>
        <developers>
          <developer>
            <id>tabdulradi</id>
            <name>Tamer Abdulradi</name>
            <url>http://abdulradi.com</url>
          </developer>
        </developers>),

    publishMavenStyle := false,
    licenses := ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil
  )

  def snapshotPublishSettings = Seq() //TODO

  def publishSettings = stablePublishSettings

  def pluginsSettings = Vector(
    // Scalariform settings
    SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
      .setPreference(DoubleIndentClassDeclaration, true),

    // Git settings
    GitPlugin.autoImport.git.useGitDescribe := true,

    // Header settings
    HeaderPlugin.autoImport.headers := Map("scala" -> Apache2_0("2016", "Tamer AbdulRadi"))
  )

  override def projectSettings =
    Defaults.coreDefaultSettings ++
      compileSettings ++
      testSettings ++
      publishSettings ++
      pluginsSettings
}
