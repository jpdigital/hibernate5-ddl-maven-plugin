hibernate5-ddl-maven-plugin
===========================

The hibernate5-ddl-maven-plugin is a simple Maven plugin for creating SQL DDL
files for JPA entities. The plugin uses Hibernates API for SchemaExport. The plugin is available from Maven Central.

Starting with version 1.0.1 the plugin is provided in different variants build
against the most current Hibernate versions. For example, 1.0.1 is available
in two variants: One build against Hibernate 5.1.2 (latest version of the 
5.1.x branch of Hibernate at that time) and 5.2.4 (latest version of the 
5.2.x branch). The Hibernate team also still maintains the 5.0.x branch. 
Unfortunately the parts of the Hibernate API used by this plugin were changed 
between Hibernate 5.0.x and Hibernate 5.1.x. You can still use this plugin
even if you are using 5.0.x in your application.

The code is available at [GitHub](http://github.com/jpdigital/hibernate5-ddl-maven-plugin) at 
<http://github.com/jpdigital/hibernate5-ddl-maven-plugin>. The 
[projects web site](http://jpdigital.github.com/hibernate5-maven-plugin) is also available on GitHub 
at <http://jpdigital.github.io/hibernate5-ddl-maven-plugin>.

Please note that this plugin is *not* an official Hibernate tool. It was created because I needed 
the functionality provided by this plugin for several projects.




