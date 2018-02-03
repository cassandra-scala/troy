import ReleaseTransformations._
import microsites._

inThisBuild(Seq( // TODO: Remove me once https://github.com/scala/scala/pull/5310 is released.
  scalaOrganization := "org.typelevel",
  scalaVersion      := "2.12.4-bin-typelevel-4"
))

addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.4" cross CrossVersion.binary)

// Only run WartRemover on 2.12
def troyWarts(sv: String) =
  CrossVersion.partialVersion(sv) match {
    case Some((2, n)) if n <= 11 => Nil
    case _  =>
      Nil
//      Warts.allBut(
//        Wart.DefaultArguments,   // used for labels in a bunch of places
//      )
  }

lazy val compilerFlags = Seq(
  scalacOptions ++= (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n <= 11 =>
        Seq(
          "-feature",
          "-deprecation",
          "-Yno-adapted-args",
          "-Ywarn-value-discard",
          "-Xlint",
//          "-Xfatal-warnings",
          "-unchecked",
          "-Ywarn-numeric-widen",
          "-Yliteral-types",
        )
      case _ =>
        Seq(
          "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
          "-encoding", "utf-8",                // Specify character encoding used by source files.
          "-explaintypes",                     // Explain type errors in more detail.
          "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
          "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
          "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
          "-language:higherKinds",             // Allow higher-kinded types
          "-language:implicitConversions",     // Allow definition of implicit functions called views
          "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
          "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
//          "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
          "-Xfuture",                          // Turn on future language features.
          "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
          "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
          "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
          "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
          "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
          "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
          "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
          "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
          "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
          "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
          "-Xlint:option-implicit",            // Option.apply used implicit view.
          "-Xlint:package-object-classes",     // Class or object defined in package object.
          "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
          "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
          "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
          "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
          "-Xlint:unsound-match",              // Pattern match may not be typesafe.
          "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
          "-Ypartial-unification",             // Enable partial unification in type constructor inference
          "-Ywarn-dead-code",                  // Warn when dead code is identified.
          "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
          "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
          "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
          "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
          "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
          "-Ywarn-numeric-widen",              // Warn when numerics are widened.
          "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
          "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
          "-Ywarn-unused:locals",              // Warn if a local definition is unused.
          "-Ywarn-unused:params",              // Warn if a value parameter is unused.
          "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
          "-Ywarn-unused:privates",            // Warn if a private member is unused.
          "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
          "-Yliteral-types",
        )
    }
  ),
  scalacOptions in (Test, compile) --= (
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n <= 11 =>
        Seq("-Yno-imports")
      case _ =>
        Seq(
          "-Ywarn-unused:privates",
          "-Ywarn-unused:locals",
          "-Ywarn-unused:imports",
          "-Yno-imports",
        )
    }
  ),
  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports"),
  scalacOptions in (Tut, tut)         --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports")
)

lazy val buildSettings = Seq(
	organization := "com.abdulradi",
	licenses ++= Seq(
		("Apache 2.0", url("https://opensource.org/licenses/Apache-2.0"))
	),
//	scalaVersion := "2.12.4",
//	crossScalaVersions := Seq("2.10.6", "2.11.11", scalaVersion.value)
)

lazy val commonSettings =
	compilerFlags ++ Seq(
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" %% "silencer-plugin" % Versions.silencer),
      "com.github.ghik" %% "silencer-lib" % Versions.silencer
    ),
    wartremoverErrors in (Compile, compile) := troyWarts(scalaVersion.value),
		parallelExecution in Test := false,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    releaseProcess := Nil
	)

lazy val publishSettings = Seq(
  useGpg := false,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  homepage := Some(url("https://github.com/schemasafe/troy")),
  pomIncludeRepository := Function.const(false),
  pomExtra := (
    <developers>
      <developer>
        <id>tabdulradi</id>
        <name>Tamer Abdulradi</name>
        <url>http://abdulradi.com</url>
      </developer>
    </developers>
  ),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value
)

lazy val noPublishSettings = Seq(
  skip in publish := true
)

lazy val troy = project.in(file("."))
  .settings(buildSettings ++ commonSettings)
  .settings(noPublishSettings)
  .dependsOn(coreJVM, liteJVM, schemasafeJVM) // TODO: JS
  .aggregate(coreJVM, liteJVM, schemasafeJVM) // TODO: JS
  .settings(
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      releaseStepCommand("docs/tut"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      releaseStepCommand("sonatypeReleaseAll"),
      releaseStepCommand("docs/publishMicrosite"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

lazy val core =
  crossProject
    .crossType(CrossType.Pure)
    .in(file("modules/core"))
    .settings(buildSettings ++ commonSettings ++ publishSettings)
    .settings(
      name := "troy-core",
      libraryDependencies ++= Seq(
        "com.abdulradi" %% "schemasafe-core" % Versions.schemasafe
      )
    )

lazy val coreJVM = core.jvm
//lazy val coreJS = core.js

lazy val lite =
  crossProject
    .crossType(CrossType.Pure)
    .in(file("modules/lite"))
    .dependsOn(core)
    .settings(buildSettings ++ commonSettings ++ publishSettings)
    .settings(
      name := "troy-lite"
    )

lazy val liteJVM = lite.jvm
//lazy val liteJS = lite.js

lazy val schemasafe =
  crossProject
    .crossType(CrossType.Pure)
    .in(file("modules/schemasafe"))
    .dependsOn(core)
    .settings(buildSettings ++ commonSettings ++ publishSettings)
    .settings(
      name := "troy-schemasafe",
      libraryDependencies += "com.chuusai" %% "shapeless" % Versions.shapeless
    )

lazy val schemasafeJVM = schemasafe.jvm
//lazy val schemasafeJS = schemasafe.js

lazy val docs = project.in(file("modules/docs")).dependsOn(coreJVM)
  .settings(buildSettings ++ commonSettings ++ noPublishSettings)
  .settings(
		name := "troy-docs",
		scalacOptions in Tut --= Seq(
      "-Ywarn-unused:imports",
      "-Yno-imports"
    )
	)
  .enablePlugins(MicrositesPlugin)
  .settings(
    micrositeName             := "troy",
    micrositeDescription      := "The Schemasafe Cassandra Driver",
    micrositeAuthor           := "Tamer Abdulradi",
    micrositeGithubOwner      := "schemasafe",
    micrositeGithubRepo       := "troy",
    micrositeGitterChannel    := false,
    micrositeBaseUrl          := "/troy",
    micrositeDocumentationUrl := "/troy/docs/index.html",
    micrositeHighlightTheme   := "color-brewer",
    micrositeConfigYaml := ConfigYml(
      yamlCustomProperties = Map()
    )
  )
