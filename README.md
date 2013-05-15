# Play Java Module for Atlassian Connect

## Description

This is a Play module to help develop Atlassian Connect add-ons. Here are a few of the useful features it brings to
add-on developers:

### Creation of RSA key pair

This module will, in [dev mode][dev], generate an RSA key pair, as pem files to be used by your add-on for OAuth signing
and validation.

Note that the module will automatically add those generated files to `.gitignore` so that you don't accidentally commit
them to your git repository.

### Auto-install

The module will, in dev mode, scan for local instance of Atlassian products and auto-install the add-on you're working on on every
change, thus shortening the dev loop. Consider running your Play app with `~ run` to make it even shorter.

Here is the list of applications the module will scan for:

* http://localhost:1990/confluence
* http://localhost:2990/jira
* http://localhost:5990/refapp

### Add-on descriptor template

This module provides an add-on descriptor template `@ap3.descriptor(){}{}` so that you don't need to worry about the default
descriptor configuration:

* defining the `remote-plugin-container` with the correct base URL and public RSA key. Note that the base URL is computed
by the app and can be further defined using the `BASE_URL` environment variable.
* defining the registration `webhook` so that installation events of the add-on in host application are automatically
handled.

Note that by default the module will serve the _simplest_ descriptor based on this template when installed, which gives
you the minimum working Atlassian Connect add-on.

### Validates incoming OAuth request

Thanks to the `@CheckValidOAuthRequest` annotation, you can ensure incoming requests are valid and coming from a known
trusted host application. This also...

### Enables multi-tenancy

You can in the context of an OAuth request identify the host application the request is coming from `Ap3#getAp3Application()`
and also the user on whose behalf the request is made `Ap3#getUser()`.

For multi-tenancy, the important thing is to identify the `key` of the host application available from the `Ap3Application`
and of course keep track of the current user.

### Make calls back to the host application

Play comes with a [nice library to make HTTP requests][ws] to any host or URL. This module provides a shortcut to make HTTP
requests back to the host application, using that same API. Simply start your calls with `Ap3#url` instead of `WS#url`. This
gives you:

* relative URL handling. Don't put absolute URLs, the helper knows about the current host application you're working with
in the context of the current request.
* default timeout. You never know what might be going on the network, never make an HTTP request without a timeout.
* user identification. The request is going to be made as the current user in the HTTP request context.
* OAuth signing. You're request will be automatically signed, given the key pair you have defined (or we have defined for you
in dev mode).

### Easy integration of [AUI][aui]

Include AUI easily in your HTML pages using the template provided by the module `@ap3.aui.all()`. You can even choose the
version you'd like to use `@ap3.aui.all("5.2-m1")`:

Current supported versions are:

* 5.0
* 5.0.1
* 5.1 (default)
* 5.2-m1

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

#### Define your add-on key (and name)

In `conf/application.conf` define both:

* `ap3.key` with your add-on key
* `ap3.name` with your add-on name, this one is optional and will default to your add-on key.

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
[dev]: http://www.playframework.com/documentation/api/2.1.1/java/play/Play.html#isDev()
[ws]: http://www.playframework.com/documentation/2.1.1/JavaWS
[aui]: https://docs.atlassian.com/aui/latest/
