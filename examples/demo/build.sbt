scalaVersion := "2.11.8"

resolvers += Resolver.bintrayRepo("tabdulradi", "maven")

libraryDependencies += "io.github.cassandra-scala" %% "troy" % "0.6.0-SNAPSHOT"

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value
