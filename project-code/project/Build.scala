import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ap3-java"
  val appVersion      = "0.1"

  val appDependencies = Seq(
    "com.atlassian.fugue" % "fugue" % "1.1",
    "net.oauth.core" % "oauth" % "20090617",
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
