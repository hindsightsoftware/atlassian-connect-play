import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play-java-ap3"
  val appVersion      = "0.4"

  val appDependencies = Seq(
    "postgresql" % "postgresql" % "8.4-701.jdbc3",
    "com.atlassian.fugue" % "fugue" % "1.1",
    "net.oauth.core" % "oauth" % "20090617",
    "com.google.guava" % "guava" % "14.0.1",
    "org.bouncycastle" % "bcprov-jdk16" % "1.46",
    javaCore,
    javaJdbc,
    javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    organization := "com.atlassian.plugins",
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
