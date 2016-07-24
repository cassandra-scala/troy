scalaVersion := "2.11.8"
libraryDependencies += "com.abdulradi" %% "troymacro" % "0.1-SNAPSHOT"
unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value
