import sbt._
import Keys._
import PlayKeys._

name    := "ac-play-java"

version := "0.12.0"

scalaVersion := "2.10.4"

val atlassianJwtVersion = "1.0.1"

libraryDependencies ++= Seq(
  "postgresql" % "postgresql" % "8.4-701.jdbc3",
  "com.atlassian.fugue" % "fugue" % "1.1",
  "commons-codec" % "commons-codec" % "1.8",
  "net.oauth.core" % "oauth" % "20090617",
  "com.google.guava" % "guava" % "18.0",
  "org.bouncycastle" % "bcprov-jdk16" % "1.46",
  "org.hibernate" % "hibernate-entitymanager" % "4.2.1.Final",
  "xml-apis" % "xml-apis" % "1.4.01",
  "com.atlassian.jwt" % "jwt-api" % atlassianJwtVersion,
  "com.atlassian.jwt" % "jwt-core" % atlassianJwtVersion,
  "commons-lang" % "commons-lang" % "2.6",
  "org.hamcrest" % "hamcrest-all" % "1.3" % "test",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "com.typesafe.play" %% "play" % "2.3.10" % "provided",
  "com.typesafe.play" %% "play-java-ws" % "2.3.10" % "provided",
  "com.typesafe.play" %% "play-java-jdbc" % "2.3.10" % "provided",
  "com.typesafe.play" %% "play-java-jpa" % "2.3.10" % "provided",
  "com.typesafe.play" %% "play-cache" % "2.3.10" % "provided",
  "com.typesafe.play" %% "filters-helpers" % "2.3.10" % "provided"
)  

lazy val root = (project in file(".")).enablePlugins(PlayJava)

ebeanEnabled := false

resolvers += "Typesafe's Repository" at "http://repo.typesafe.com/typesafe/maven-releases"

resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public"

organization := "com.atlassian.connect"
    
publishTo <<= version { (v: String) =>
      val repo = "https://maven.atlassian.com/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("Atlassian Repositories" at repo + "private-snapshot")
      else
        Some("Atlassian"  at repo + "public")
}

publishMavenStyle := true
