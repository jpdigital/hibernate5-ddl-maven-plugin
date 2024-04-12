hibernate5-ddl-maven-plugin
===========================

The hibernate5-ddl-maven-plugin is a simple Maven plugin for creating SQL DDL
files for JPA entities. The plugin uses Hibernates API for SchemaExport. The
plugin is available from Maven Central.

Please note that this plugin is *not* an official Hibernate tool. It was created
because I needed the functionality provided by this plugin for several projects.

## Compatibility with Java Versions

The different plugin variants embeded the latest version of the branch of 
Hibernate indicated by the artifact name of the plugin. For example the
hibernate52-ddl-maven-plugin uses the latest version of the 5.2 branch.

The plugin requires Java 8 or newer. 

## Building

As usual with Maven:

```
mvn clean package
```

To install the plugin locally run

```
mvn clean package install
```

You will need an GPG key set up with Maven for signing the archive.

For integration tests, the Maven Invoker plugin (see https://maven.apache.org/plugins/maven-invoker-plugin/)
is used. Tu run the integration tests use:

```
mvn clean install integration-test
```

To run only a specific test use:

```
mvn clean package integration-test -Dinvoker.test="ddl-it"
```

In this example the test `ddl-it` is run. Replace the name of test with the test
you want to run.

## Reporting issues

If you have any issues please report them on Github: https://github.com/jpdigital/hibernate5-ddl-maven-plugin/issues 
In most cases it is really helpful if include an example demostrating the issue. Please note that issues
without an example may be closed *without* any comment.

## Supporting the project

If you find this project helpful please you can support the project using 
[Paypal](https://paypal.me/jenspelzetter).

## News

### 2024-04-12: Version 3.0.0

In this version support for Hibernate versions older than 5.6 has been dropped. 
For Hibernate 6 please use the hibernate6-ddl-maven-plugin, which will be 
released soon. This versions also update several dependencies, and replaces 
the Reflections library used in previous versions for scanning the classpath
with the [ClassGraph](https://github.com/classgraph/classgraph) library.

### 2021-11-13: Version 2.5.0 

This release includes some updates for the dependencies, among all others an
update of the Reflections library to the most current version 0.10.2. Version
2.5.0 also adds a variant build against Hibernate 5.6. 

### 2021-09-25: Version 2.4.0

The version updates the dependencies to the latest Hibernate versions, and adds
support for Hibernate 5.5. Also, annotations in `package-info.java` files are
now processed.

### 2018-05-16: Version 2.2.0

Starting with this version only specific properties from the persistence.xml 
file are passed to Hibernate for creating the database schema. The properties
can be customized. More details can be found in the documentation.

### 2017-12-22: Version 2.1.0

Only minor bugfixes and some code cleanup since version 2.1.0-beta.1.

### 2017-09-16: Version 2.1.0-beta.1

This release adds some features requested by users of the plugin. The first
new feature is the option to use custom dialects. Also it is now possible
to customise the name of the output files and the output path (Issue #11).

This release is a beta version which means that there might be some bugs. Also
there are things in the code which need to be addressed before the final release.

### 2017-06-10: Version 2.0.0

Starting with version 2.0.0 the plugin is provided in different variants build
against the most current Hibernate versions. For example, 2.0.0 is available
in three variants: One build against the latest version of Hibernate 5.0
(Hibernate 5.0 is used in Wildfly 10.1) one for the lastest version of the 5.1 branch
one for the lastest version of the 5.2 branch.

## Code Repository

The code is available at
[GitHub](http://github.com/jpdigital/hibernate5-ddl-maven-plugin) at
<http://github.com/jpdigital/hibernate5-ddl-maven-plugin>. The
[projects web site](http://jpdigital.github.com/hibernate5-maven-plugin) is also
available on GitHub at <http://jpdigital.github.io/hibernate5-ddl-maven-plugin>.
