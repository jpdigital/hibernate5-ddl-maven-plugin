# Overview

## The Maven DDL plugin for Hibernate 5

This plugin for Maven provides an wrapper around the SchemaExport class of 
Hibernate 5. Using parameters it is possible to configure the plugin from 
the POM. This plugin requires Java 8 and Maven 3.

Starting with version 2.0.0 the plugin is provided in different variants 
build against the most current Hibernate versions. The version of 
Hibernate is part the artifact name. For example the 
hibernate50-ddl-maven-plugin uses the latest version of the 5.0 branch of 
Hibernate. It is recommended to use the plugin for the same version branch 
as the Hibernate version you project uses. 

The goals of this plugin are described at the Goals page. The Usage page
provides information about how the plugin is used.

Please note that this plugin is *not* an official Hibernate tool. It was 
created because I needed 
the functionality provided by this plugin for several of my own projects.

Please you find this project useful and want to support it you can do this 
using [Paypal](https://paypal.me/jenspelzetter).

## News

### 2021-11-13: Version 2.5.0

This release includes some updates for the dependencies, among all others an
update of the Reflections library to the most current version 0.10.2. Version
2.5.0 also adds a variant build against Hibernate 5.6. 

### 2021-09-25: Version 2.4.0

This relase updates the dependencies to the latest Hibernate versions, and adds
support for Hibernate 5.5. Also, annotations in `package-info.java` files are
now processed.

### 2018-05-16: Version 2.2.0

Starting with this release only specific properties from the persistence.xml 
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
