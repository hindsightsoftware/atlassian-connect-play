# Play Java Module for Atlassian Connect

## Description

This is a Play module to help develop Atlassian Connect add-ons. Here are a few of the useful features it brings to
add-on developers:

* [Auto-install of Addon](#markdown-header-auto-install)
* [atlassian-connect.json](#markdown-header-add-on-descriptor-template)
* [Validates incoming JWT request](#markdown-header-validates-incoming-jwt-request)
* [Enables multi-tenancy](#markdown-header-enables-multi-tenancy)
* [Make calls back to the host application](#markdown-header-make-calls-back-to-the-host-application)
* [Easy integration of AUI](#markdown-header-easy-integration-of-aui)
* [Soy and experimental AUI features](#markdown-header-soy-and-experimental-aui-features)

More details can be found in the [AC Play Java Benefits](#markdown-header-ac-play-java-benefits) section.

## Release Notes

## 0.10.0

* Added support for pluggable persistence for AcHost data. Thanks to Alan Parkinson for the contribution
* Use core Ac logger for JWT. Thanks to Alan Parkinson for the contribution

0.9.0

* Upgraded AUI to version 5.6.12 (see [AUI release notes](https://developer.atlassian.com/display/AUI/AUI+5.x) for details). Previous version was 5.4.3.


See changelog.md for more details

_Note: The module requires Java 7 in order for your project to compile._

## Getting started

### Create your Java Play application

You should find everything you need on the [Play! website][play-doc].

_Note: When you run `play new` make sure you choose the **Create a simple Java application** option when prompted. AC Play Java supports Java only. There is an open source [Scala version of AC Play][ac-play-scala] in Atlassian Labs if you prefer, but note that it is not officially supported by Atlassian._

Once you have your Play application up and running, go to
the next step:

### Add this module to your Play application.

Your going to need to do a few things for that:

#### Add Atlassian's public maven repository in your `build.sbt` file

This is called a resolver in the [SBT][sbt] world. SBT is the build tool, based on Scala, used by Play. *Note the newline is important.*

    resolvers += "Atlassian's Maven Public Repository" at "https://maven.atlassian.com/content/groups/public"

	resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository"


Note that I actually also add my local maven repository for good measure and ease of use.

#### Add the module as a dependency in your `build.sbt` file

    libraryDependencies ++= Seq(
    	javaCore,
    	javaJpa,
    	"com.atlassian.connect" % "ac-play-java_2.10" % "<version>" withSources()
        // your other dependencies go there
	)

Where _<version>_ is the latest version of this module. The latest published version of the Atlassian Connect Play module can be found in the [Atlassian Maven repository][atlassian-maven-repo]. The current latest version is 0.7.0-BETA10.
Note _withSources()_ is optional. It will download the source which can help with debugging.

#### Add the module's routes to your `conf/routes` configuration

Comment the default application index and add the module's routes:

    # Home page
    # GET     /                           controllers.Application.index()
    ->        /                           ac.Routes

This will ensure that any routes that are not handled by your application are delegated to this helper module.

#### Setup application key and name
Configure yor plugin key and name in `conf/application.conf`. 
These are used as substitution variables in [atlassian-connect.json](#markdown-header-add-on-descriptor-template).

    ac.key = PLUGIN-KEY
    ac.name = PLUGIN-NAME

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

    db.default.jndiName=DefaultDS
    jpa.default=defaultPersistenceUnit

Note that the postgres driver is already a dependency of the module, so you don't need to add a dependency for it.

You can [get postgres from the official website](http://www.postgresql.org/) in case you don't have it installed. Follow the instructions to create and start a new server. You'll need to create a new user and database. Make sure that you marry up the values in `conf/application.conf` with the values you use in postgres.

The play library uses JPA for persistence so you'll have to create a persistence.xml file in conf/META-INF:

    <persistence xmlns="http://java.sun.com/xml/ns/persistence"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
                 version="2.0">
        <persistence-unit name="defaultPersistenceUnit" transaction-type="RESOURCE_LOCAL">
            <provider>org.hibernate.ejb.HibernatePersistence</provider>
            <non-jta-data-source>DefaultDS</non-jta-data-source>
            <class>com.atlassian.connect.play.java.AcHost</class>
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

![The Atlassian Connect Play Module home page](https://bitbucket.org/atlassian/atlassian-connect-play-java/raw/master/public/img/ac-home-page.png "The Atlassian Connect Play Module home page")

Follow the instructions on that page on defining your own descriptor.

## A note on Security

Most requests to the remote Play application will be properly signed with JWT headers coming from the Atlassian application.
This Play module will take care of authenticating these requests.  However any subsequent requests within the original page will require more work to authenticate remotely.

For example a remote admin page may include a horizontal navbar including links to various other remote admin pages. When a user clicks
on any of these links they will load within the iframe without any additional JWT headers being sent to the remote server.  To overcome
this, this module provides a secure token mechanism.  If you use `@ac.page` to decorate your pages all links, forms and ajax requests will
automatically be decorated with this secure token.  If `@ac.page` is not used any requests will have to be decorated manually. This can be
done by adding the following request parameters:

    ?acpt=<SECURE_TOKEN>

For ajax requests one can also add the following header to the request:

    X-acpt:<SECURE_TOKEN>

The secure token can be obtained via a call to `AC.getToken().getOrNull()`.

On the server side to verify that an action in your Play controller is being called with a valid token, you can simply add the `@CheckValidToken`
annotation.  Tokens contain a timestamp and will time out once they are older than 15 minutes (configurable via `ac.token.expiry.secs` in application.conf).
Any response from an action annotated with @CheckValidToken will contain a fresh token in the 'X-acpt' response header.  If `@ac.page` is used, this will
trigger tokens to be refreshed client-side automatically, however if `@ac.page` is not used this may have to be done manually.

## AC Play Java Benefits
[benefits]:

### Auto-install
[autoInstall]:

The module will, in dev mode, scan for local instance of Atlassian products and auto-install the add-on you're working on on every
change, thus shortening the dev loop. Consider running your Play app with `~ run` to make it even shorter.

Here is the list of applications the module will scan for:

* http://localhost:1990/confluence
* http://localhost:2990/jira
* http://localhost:5990/refapp

### atlassian-connect.json
[addOnDescriptorTemplate]:

The `atlassian-connect.json` describes what your add-on will do. There are three main parts to the descriptor: meta information that describes your add-on (i.e., name, description, key, etc.), permissions and authentication information, and a list of the components your add-on will extend. This descriptor is sent to the host (i.e., JIRA or Confluence) when your add-on is installed.

AC Play supports variable substitution for `atlassian-connect.json`. Currently it supports the following variables:

* `${localBaseUrl}` which maps to the base url defined for the application. Note that the base URL is computed
by the app and can be further defined using the `BASE_URL` environment variable.
* `${addonKey}`. This variable is populated based on the configuration variable `ac.key` (in application.conf etc)
* `${addonName}`. This variable is populated based on the configuration variable `ac.name` (in application.conf etc)

If you follow the instuctions in Getting Started then you can view the descriptor json by navigating to `/atlassian-connect.json` (e.g If running as localhost [http://localhost:9000/atlassian-connect.json](http://localhost:9000/atlassian-connect.json)). Or alternatively use

```
curl -H "Accept: application/json"  http://localhost:9000/
```


To see all of the available settings in the `atlassian-connect.json`, visit the module sections of the [atlassian-connect documentation](https://developer.atlassian.com/static/connect/docs/)


### Validates incoming JWT request
[requestValidation]:

Thanks to the `@AuthenticateJwtRequest` annotation, you can ensure incoming requests are valid and coming from a known
trusted host application. This also...

### Enables multi-tenancy
[multiTenancy]:

You can in the context of an JWT request identify the host application the request is coming from `AC#getAcHost()`
and also the user on whose behalf the request is made `AC#getUser()`.

For multi-tenancy, the important thing is to identify the `key` of the host application available from the `AcHost`
and of course keep track of the current user.

### Make calls back to the host application
[hostRequests]:

Play comes with a [nice library to make HTTP requests][ws] to any host or URL. This module provides a shortcut to make HTTP
requests back to the host application, using that same API. Simply start your calls with `AC#url` instead of `WS#url`. This
gives you:

* relative URL handling. Don't put absolute URLs, the helper knows about the current host application you're working with
in the context of the current request.
* default timeout. You never know what might be going on the network, never make an HTTP request without a timeout.
* user identification. The request is going to be made as the current user in the HTTP request context.
* JWT signing. Your request will be automatically signed with the shared secret that was stored in the database when your addon was installed on the host.

#### Using the product REST API

Certain REST URLs may require additional permissions that should be added to your atlassian-plugin.xml file.

[Jira Permissions][jira-permissions]

[Confluence Permissions][confluence-permissions]

For example, to view details of a specific jira issue.

    AC.url("/rest/api/2/issue/ISSUE-KEY").get();

You also need to add the required scopes to your atlassian-connect.json file:
````
    "scopes": ["READ"]
````

### Easy integration of [AUI][aui]
[auiIntegration]:

Include AUI easily in your HTML pages using the template provided by the modules `@ac.aui.styles()` and `@ac.aui.scripts()`. Presently [AUI][aui] 5.2 is the
only AUI version provided, but future versions can be used when published by including the AUI version and jQuery versions (Scripts only) as parameters
`@ac.aui.styles("5.2")` and `@ac.aui.scripts("5.2", "1.8.3")` (and make sure to use the same version in each).  For the best results, put `@ac.aui.styles()`
in the head of your HTML and `@ac.aui.scripts()` at the end of the body (but before your own scripts).

Previous versions of the Play Module supported older AUI versions, these have been removed as AUI Styles and Scripts are now sourced from a CDN for
performance reasons.

#### [Soy][soy] and experimental AUI features
[soyTemplates]:

Support for JavaScript [Soy][soy] templates and experimental AUI features can be enabled by passing additional parameters to the `@ac.aui.styles()` and
`@ac.aui.scripts()` templates.

* Enable experimental AUI features `@ac.aui.styles("5.2", true)` and `@ac.aui.scripts("5.2", "1.8.3", true)`
* Enable soy templates support `@ac.aui.scripts("5.2", "1.8.3", false, true)`

## How to deploy to Heroku
Before you start, install Git and the [Heroku Toolbelt](https://toolbelt.heroku.com/).

If you aren't using git to track your add-on, now is a good time to do so as it is required for Heroku. Ensure you are in the root directory of your Play project and run the following commands:

	git config --global user.name "John Doe"
	git config --global user.email johndoe@example.com
	ssh-keygen -t rsa
	git init
	git add .
	git commit . -m "some message"
	heroku keys:add

Next, create the app on Heroku:

    heroku apps:create <add-on-name>

A good practice is also to externalize your Play application secret as an environment variable in Heroku.

In your Play application's `conf/application.conf`, replace

	application.secret=abc123...

with 

	application.secret=${APP_SECRET}

Then set the application environment variable in Heroku:

	heroku config:add APP_SECRET=abc123...

Next, let's store our registration information in a Postgres database. In development, you were likely using the memory store. In production, you'll want to use a real database.

    heroku addons:add heroku-postgresql:dev --app <add-on-name>

Lastly, let's add the project files to Heroku and deploy! 

If you aren't already there, switch to your project home directory. From there, run these commands:

    git remote add heroku git@heroku.com:<add-on-name>.git
    git push heroku master

It will take a minute or two for Heroku to spin up your add-on. When it's done, you'll be given the URL where your add-on is deployed, however, you'll still need to register it on your Atlassian instance.

If you're running an OnDemand instance of JIRA or Confluence locally, you can install it from the add-on administration console. See complete [instructions in the Atlassian Connect doc](https://developer.atlassian.com/display/AC/Hello+World#HelloWorld-Registertheadd-on) for more information.

For further detail, we recommend reading [Heroku Play Framework Support documentation](https://devcenter.heroku.com/articles/play-support).

[play-doc]: http://www.playframework.com/documentation/2.2.x/Home "Play Documentation"
[sbt]: http://www.scala-sbt.org/ "Simple Build Tool"
[jdbc]: http://www.playframework.com/documentation/2.2.x/SettingsJDBC
[ebean]: http://www.playframework.com/documentation/2.2.x/JavaEbean
[evolutions]: http://www.playframework.com/documentation/2.2.x/Evolutions
[dev]: http://www.playframework.com/documentation/api/2.2.x/java/play/Play.html#isDev()
[ws]: http://www.playframework.com/documentation/2.2.x/JavaWS
[aui]: https://docs.atlassian.com/aui/latest/
[soy]: https://docs.atlassian.com/aui/latest/docs/soy.html
[jira-permissions]: https://developer.atlassian.com/static/connect/index-plugin.html?lic=none&xdm_e=https%3A%2F%2Fdeveloper.atlassian.com&xdm_c=channel-interactive-guide-0&xdm_p=1#jira/permissions "Jira Permissions"
[confluence-permissions]: https://developer.atlassian.com/static/connect/index-plugin.html?lic=none&xdm_e=https%3A%2F%2Fdeveloper.atlassian.com&xdm_c=channel-interactive-guide-0&xdm_p=1#confluence/permissions "Confluence Permissions"
[ac-play-scala]: https://bitbucket.org/atlassianlabs/atlassian-connect-play-scala "Scala version of AC Play"
[atlassian-maven-repo]: https://maven.atlassian.com/content/groups/public/com/atlassian/connect/ac-play-java_2.10 "Atlassian Maven repository"