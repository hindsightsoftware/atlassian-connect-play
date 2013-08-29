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

This module provides an add-on descriptor template `@ac.descriptor(){}{}` so that you don't need to worry about the default
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

You can in the context of an OAuth request identify the host application the request is coming from `AC#getAcHost()`
and also the user on whose behalf the request is made `AC#getUser()`.

For multi-tenancy, the important thing is to identify the `key` of the host application available from the `AcHost`
and of course keep track of the current user.

### Make calls back to the host application

Play comes with a [nice library to make HTTP requests][ws] to any host or URL. This module provides a shortcut to make HTTP
requests back to the host application, using that same API. Simply start your calls with `AC#url` instead of `WS#url`. This
gives you:

* relative URL handling. Don't put absolute URLs, the helper knows about the current host application you're working with
in the context of the current request.
* default timeout. You never know what might be going on the network, never make an HTTP request without a timeout.
* user identification. The request is going to be made as the current user in the HTTP request context.
* OAuth signing. Your request will be automatically signed, given the key pair you have defined (or we have defined for you
in dev mode).

### Easy integration of [AUI][aui]

Include AUI easily in your HTML pages using the template provided by the modules `@ac.aui.styles()` and `@ac.aui.scripts()`. You can even choose the
version you'd like to use `@ac.aui.styles("5.2-m1")` and `@ac.aui.scripts("5.2-m1")` (and make sure to use the same version in each).  For the best
results, put `@ac.aui.styles()` in the head of your HTML and `@ac.aui.scripts()` at the end of the body (but before your own scripts).

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

#### Add Atlassian's public maven repository in your `project/Build.scala` file

This is called a resolver in the [SBT][sbt] world. SBT is the build tool, based on Scala, used by Play.

    val main = play.Project(appName, appVersion, appDependencies).settings(
            resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public",
            resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"
    )

Note that I actually also add my local maven repository for good measure and ease of use.

#### Add the module as a dependency in your `project/Build.scala` file

    val appDependencies = Seq(
      javaCore,
      javaJpa,
      "com.atlassian.connect" % "ac-play-java_2.10" % "<version>",
      // your other dependencies go there
    )

Where _<version>_ is the current version of this module.

#### Add the module's routes to your `conf/routes` configuration

Comment the default application index and add the module's routes:

    # Home page
    # GET     /                           controllers.Application.index()
    ->      /                                   ac.Routes

This will ensure that any routes that are not handled by your application are delegated to this helper module.

#### Configure the database

If you haven't already configured a database, in your `conf/application.conf` setup the default database. Here is an example
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

The play library uses JPA for persistance so you'll have to create a persistence.xml file in conf/META-INF:

    <persistence xmlns="http://java.sun.com/xml/ns/persistence"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
                 version="2.0">
        <persistence-unit name="defaultPersistenceUnit" transaction-type="RESOURCE_LOCAL">
            <provider>org.hibernate.ejb.HibernatePersistence</provider>
            <non-jta-data-source>DefaultDS</non-jta-data-source>
            <class>com.atlassian.connect.play.java.model.AcHostModel</class>
            <properties>
                <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQL82Dialect"/>

                <!-- Not recommended for PRODUCTION! This will re-create all tables on restart. -->
                <property name="hibernate.hbm2ddl.auto" value="create"/>
                <!--<property name="hibernate.show_sql" value="true"/>-->
                <!--<property name="hibernate.format_sql" value="true"/>-->
            </properties>
        </persistence-unit>
    </persistence>

You can read more about some of those topics on the Play website:

* [Configuring the JDBC connection][jdbc]
* [Managing database evolutions][evolutions]

You're done with the database configuration.

### Reload

Now you're ready to reload your application. If you're running the Play console you will need
to run `reload` for the new dependencies, resolvers, etc. to take effect.
Then you can refresh the home page of your application. You might need to `apply` the database evolutions, before being
able to access the actual application.

If all went well, you should now see the welcome page of the Atlassian Connect Play Module:

![The Atlassian Connect Play Module home page](raw/master/public/img/ac-home-page.png "The Atlassian Connect Play Module home page")

Follow the instructions on that page on defining your own descriptor.

## A note on Security

Most requests to the remote Play application will be properly signed with OAuth headers coming from the Atlassian application.
This Play module will take care of authenticating these requests.  However any subsequent requests within the original page will require
more work to authenticate remotely.

For example a remote admin page may include a horizontal navbar including links to various other remote admin pages. When a user clicks
on any of these links they will load within the iframe without any additional OAuth headers being sent to the remote server.  To overcome
this, this module provides a secure token mechanism.  If you use `@ac.page` to decorate your pages all links, forms and ajax requests will
automatically be decorated with this secure token.  If `@ac.page` is not used any requests will have to be decorated manually. This can be
done by adding the following request parameters:

    ?acpt=<SECURE_TOKEN>&acck=<CONSUMER_KEY>&acuid=<USER_ID>

For ajax requests one can also add the following headers to the request:

    X-acpt:<SECURE_TOKEN>
    X-acck:<CONSUMER_KEY>
    X-acuid:<USER_ID>

The secure token can be obtained via a call to `AC.tokenStore.get(new TokenKey(AC.getAcHost().getKey(), AC.getUser()), System.currentTimeMillis())`.

On the server side to verify that an action in your Play controller is being called with a valid token, you can simply add the `@CheckValidPageToken`
annotation.

By default this module provides an in memory token store which may not be sufficient for large scale production deployments that may require persistence
to allow for horizontal scaling as well as restarts.  An in memory store could also increase memory usage significantly on a large scale deployment.

A custom token store implementation can be configured via the `ac.token.store` configuration property.  This token store has to implement and follow the
contract defined in the `TokenStore` interface defined in this module.

[play-doc]: http://www.playframework.com/documentation/2.1.1/Home "Play Documentation"
[sbt]: http://www.scala-sbt.org/ "Simple Build Tool"
[jdbc]: http://www.playframework.com/documentation/2.1.1/SettingsJDBC
[ebean]: http://www.playframework.com/documentation/2.1.1/JavaEbean
[evolutions]: http://www.playframework.com/documentation/2.1.1/Evolutions
[dev]: http://www.playframework.com/documentation/api/2.1.1/java/play/Play.html#isDev()
[ws]: http://www.playframework.com/documentation/2.1.1/JavaWS
[aui]: https://docs.atlassian.com/aui/latest/
