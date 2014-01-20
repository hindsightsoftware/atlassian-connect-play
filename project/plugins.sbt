// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Community repository
resolvers += "Community repository" at "http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.1")

//addSbtPlugin("org.clapper" % "sbt-editsource" % "0.6.5")
//addSbtPlugin("org.clapper" % "sbt-editsource" % "0.6.5", sbtVersion = "0.12", scalaVersion = "2.9.2")
// look into source generators http://www.scala-sbt.org/release/docs/Howto/generatefiles.html
// or use http://software.clapper.org/grizzled-scala/
// libraryDependencies += "org.clapper" % "grizzled-scala_2.10" % "1.1.4"
