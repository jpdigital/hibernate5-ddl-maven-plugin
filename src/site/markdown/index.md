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


