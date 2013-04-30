import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ap3-java"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.atlassian.fugue" % "fugue" % "1.1",
    "net.oauth.core" % "oauth" % "20090617",
    javaCore,
    javaJdbc,
    javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
