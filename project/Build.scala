import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ac-play-java"
  val appVersion      = "0.6.3-SNAPSHOT"

  val appDependencies = Seq(
    "postgresql" % "postgresql" % "8.4-701.jdbc3",
    "com.atlassian.fugue" % "fugue" % "1.1",
    "commons-codec" % "commons-codec" % "1.8",
    "net.oauth.core" % "oauth" % "20090617",
    "com.google.guava" % "guava" % "14.0.1",
    "org.bouncycastle" % "bcprov-jdk16" % "1.46",
    "com.typesafe.play.extras" % "iteratees-extras_2.10" % "1.0.1",
    "org.hibernate" % "hibernate-entitymanager" % "4.2.1.Final",
    "xml-apis" % "xml-apis" % "1.4.01",
    javaCore,
    javaJdbc,
    javaJpa
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    //we're using JPA so ebean can safely be disabled
    ebeanEnabled := false,
    resolvers += "Typesafe's Repository" at "http://repo.typesafe.com/typesafe/maven-releases",
    resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public",
    organization := "com.atlassian.connect",
    publishTo <<= version { (v: String) =>
      val repo = "https://maven.atlassian.com/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("Atlassian Repositories" at repo + "private-snapshot")
      else
        Some("Atlassian"  at repo + "public")
    },

    publishMavenStyle := true
  )
}
