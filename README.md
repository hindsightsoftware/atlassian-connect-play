# Atlassian Play Java Module for Remote Plugins

## Description

This is a Play module to help develop Remote Plugins for Atlassian Products.

## Getting started

### Create your Java Play application

You should find everything you need on the [Play! website][play-doc]. Once you have your Play application up and running, go to
the next step:

### Add this module to your Play application.

Your going to need to do a few things for that:

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
      "com.atlassian.plugins" % "ap3-java_2.10" % "<version>",
      // your other dependencies go there
    )

Where _<version>_ is the current version of this module.

#### Add the module's routes to your `conf/routes` configuration

    ->      /                                   ap3.Routes

#### Configure the database

If you don't already have configured a database, in your `conf/application.conf` setup the default database. Here is an example
using a local postgres installation:

    db.default.driver=org.postgresql.Driver
    db.default.url="jdbc:postgresql:my-database"
    db.default.user=my-user
    db.default.password=my-password
    db.default.partitionCount=1
    db.default.maxConnectionsPerPartition=5
    db.default.minConnectionsPerPartition=1
    db.default.acquireIncrement=1
    db.default.acquireRetryAttempts=1
    db.default.acquireRetryDelay=5 seconds

Note that the postgres driver is already a dependency of the module, so you don't need to add a dependency for it.
In that same `application.conf` you will need to uncomment the `ebean` configuration line:

    ebean.default="models.*"

And the last thing is to add the evolutions scripts to your project. In the `conf/evolutions/default` copy the evolution
scripts you will find in the source code of this module (named `1.sql`, `2.sql`, etc.), re-order them if necessary to work
with your own evolutions scripts. Note that those scripts are written for Postgres and if you plan to use another
database you might need to tweak them.

You can read more about some of those topics on the Play website:

* [Configuring the JDBC connection][jdbc]
* [Using the Ebean ORM][ebean]
* [Managing database evolutions][evolutions]

You're done with the database configuration.

#### Define your plugin key (and name)

In `conf/application.conf` define both:
* `ap3.key` with your plugin key
* `ap3.name` with your plugin name, this one is optional and will default to your plugin key.

### Reload

Now you're ready to reload your application. If you're running the Play console you will need
to run `reload` for the new dependencies, resolvers, etc. to take effect.
Then you can refresh the home page of your application. You might need to `apply` the database evolutions, before being
able to access the actual application.

[play-doc]: http://www.playframework.com/documentation/2.1.1/Home "Play Documentation"
[sbt]: http://www.scala-sbt.org/ "Simple Build Tool"
[jdbc]: http://www.playframework.com/documentation/2.1.1/SettingsJDBC
[ebean]: http://www.playframework.com/documentation/2.1.1/JavaEbean
[evolutions]: http://www.playframework.com/documentation/2.1.1/Evolutions