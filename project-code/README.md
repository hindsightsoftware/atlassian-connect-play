# Atlassian Play Java Module for Remote Plugins

## Description

This is a Play module to help develop Remote Plugins for Atlassian Products.

## Getting started

### Create your Java Play application

You should find everything you need on the [Play! website][play-doc]. Once you have your Play application up and running, go to
the next step:

### Add this module to your Play application.

Your going to need to do three things for that:

#### Add Atlassian's public maven repository

This is called a resolver in [SBT][sbt] world. SBT is the build tool, based on Scala, used by Play.

    val main = play.Project(appName, appVersion, appDependencies).settings(
            resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public",
            resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
    )

Note that I actually also add my local maven repository for good measure and ease of use.

#### Add the module as a dependency

    val appDependencies = Seq(
      javaCore,
      javaEbean,
      "com.atlassian.plugins" % "ap3-java_2.10" % "0.1",
      // your other dependencies go there
    )

#### Add the module's routes to your `routes` configuration

    ->      /                                   ap3.Routes

Now you're ready to reload your application. If you're running the Play console (which I highly recommand) you will need
to run `reload` for the new dependencies, resolvers, etc. to take effect.
Then you can refresh the home page of your application.

[play-doc]: http://www.playframework.com/documentation/2.1.1/Home "Play Documentation"
[sbt]: http://www.scala-sbt.org/ "Simple Build Tool"