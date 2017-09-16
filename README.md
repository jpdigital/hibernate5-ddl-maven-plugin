hibernate5-ddl-maven-plugin
===========================

The hibernate5-ddl-maven-plugin is a simple Maven plugin for creating SQL DDL
files for JPA entities. The plugin uses Hibernates API for SchemaExport. The 
plugin is available from Maven Central.

Please note that this plugin is *not* an official Hibernate tool. It was created 
because I needed the functionality provided by this plugin for several projects.

# News

## 2017-09-16: Version 2.1.0 Beta

This release adds some features requested by users of the plugin. The first
new feature is the option to use custom dialects. Also it is now possible
to customise the name of the output files and the output path (Issue #11).

## 2017-06-10: Version 2.0.0

Starting with version 2.0.0 the plugin is provided in different variants build
against the most current Hibernate versions. For example, 2.0.0 is available
in three variants: One build against the latest version of Hibernate 5.0 
(Hibernate 5.0 is used in Wildfly 10.1) one for the lastest version of the 5.1 branch
one for the lastest version of the 5.2 branch.

# Code Repository

The code is available at 
[GitHub](http://github.com/jpdigital/hibernate5-ddl-maven-plugin) at 
<http://github.com/jpdigital/hibernate5-ddl-maven-plugin>. The 
[projects web site](http://jpdigital.github.com/hibernate5-maven-plugin) is also 
available on GitHub at <http://jpdigital.github.io/hibernate5-ddl-maven-plugin>.





