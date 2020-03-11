JSON datatype contributor for Hibernate ORM 5.2+
========

If you need to use `javax.json.JsonArray`, `javax.json.JsonObject` or
`javax.json.JsonStructure` objects in your JPA entities and already use
Hibernate as your JPA provider, this is the plugin for you, so long as
Hibernate, your JSR-353 provider and this plugin are loaded by the same
ClassLoader, which is the typical use-case.

Changelog:
--------

Version 1.1: 

* Updated Hibernate version to 5.2, requiring Java 8.
* Removed test dependency log4j, using JBoss logger, available from Hibernate, instead.
* Add database types to case-sensitive registration keys. No effect, unless you manage to somehow load the contributor with the hibernate-mapping tool for class generation from a live database.
* `build.gradle` no longer mixes tabs and spaces for indentation. Now it's tabs only.

Version 1.2:

* When used in conjunction with `hibernate-array-contributor`, it will automatically register the array types.

Set-up for testing environment:
--------

* PostgreSQL 9.2 server or newer, running on port 5432
* Username, password and database `hibernate_orm_test` with full privileges that database.

Compiling:
--------

Assuming you have your JAVA_HOME environment variable pointing to JDK7

    ./gradlew clean build

If you want to build without running the tests, use `assemble` instead of `build`.

To make the final jar available for your Maven or compatible project type
that uses the Maven repository, run:

    ./gradlew publishToMavenLocal

Now you just need to add the dependency to your project's `pom.xml`.

    <dependency>
        <groupId>com.mopano</groupId>
        <artifactId>hibernate-json-contributor</artifactId>
        <version>1.1</version>
    </dependency>

Or if you're using Gradle:

    dependencies {
        compile group: 'com.mopano', name: 'hibernate-json-contributor', version: '1.1'
    }


Potential pitfall:
--------

If your application is loaded after Hibernate, instead of alongside it,
it might not load the type contributor. This can happen if you use Hibernate
on a Glassfish server. In this case you would need to add it to Glassfish's
classpath the same way you added Hibernate, as it is not the default provider.

You cannot use `JsonArray` objects as parameters in native queries before
Hibernate version 5.2.10.
