/*
 * Copyright (C) 2017 Jens Pelzetter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jpdigital.maven.plugins.hibernate5ddl;

import org.apache.maven.plugin.MojoFailureException;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Implementation of the {@link DdlGenerator} interface for Hibernate
 * {@literal 5.1}.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
public class DdlGeneratorHibernate52 implements DdlGenerator {

    @Override
    public void generateDdl(final Dialect dialect,
                            final Set<Class<?>> entityClasses,
                            final GenerateDdlMojo mojo)
        throws MojoFailureException {

        final StandardServiceRegistryBuilder registryBuilder
                                                 = new StandardServiceRegistryBuilder();
        processPersistenceXml(registryBuilder,
                              mojo.getPersistenceXml(),
                              mojo.getLog());

        if (mojo.isCreateDropStatements()) {
            registryBuilder.applySetting("hibernate.hbm2ddl.auto",
                                         "create-drop");
        } else {
            registryBuilder.applySetting("hibernate.hbm2ddl.auto", "create");
        }

        registryBuilder.applySetting("hibernate.dialect",
                                     dialect.getDialectClass());

        final StandardServiceRegistry standardRegistry = registryBuilder.build();

        final MetadataSources metadataSources = new MetadataSources(
            standardRegistry);

        for (final Class<?> entityClass : entityClasses) {
            metadataSources.addAnnotatedClass(entityClass);
        }

        final SchemaExport export = new SchemaExport();
        export.setDelimiter(";");

        final Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory("maven-hibernate5-ddl-plugin");
        } catch (IOException ex) {
            throw new MojoFailureException("Failed to create work dir.", ex);
        }

        final Metadata metadata = metadataSources.buildMetadata();

        export.setOutputFile(String.format(
            "%s/%s.sql",
            tmpDir.toString(),
            dialect.name().toLowerCase(
                Locale.ENGLISH)));
        export.setFormat(true);
        if (mojo.isCreateDropStatements()) {
            export.execute(EnumSet.of(TargetType.SCRIPT),
                           SchemaExport.Action.BOTH,
                           metadata);
        } else {
            export.execute(EnumSet.of(TargetType.SCRIPT),
                           SchemaExport.Action.CREATE,
                           metadata);
        }

        mojo.writeOutputFile(dialect, tmpDir);
    }

    /**
     * Helper method for processing the {@code persistence.xml} file.
     *
     * @param registryBuilder {@link StandardServiceRegistryBuilder} from
     *                        Hibernate.
     * @param persistenceXml  The {@code persistence.xml} file to process
     * @param log             Maven {@link Log} instance to use for printing
     *                        what is done.
     */
    private void processPersistenceXml(
        final StandardServiceRegistryBuilder registryBuilder,
        final File persistenceXml,
        final Log log) {

        if (persistenceXml != null) {

            if (Files.exists(persistenceXml.toPath())) {
                try (final InputStream inputStream = new FileInputStream(
                    persistenceXml)) {
                    log.info("persistence.xml found, "
                                 + "looking for properties...");

                    final SAXParser parser;

                    parser = SAXParserFactory.newInstance().newSAXParser();

                    parser.parse(inputStream,
                                 new PersistenceXmlHandler(registryBuilder,
                                                           log));

                } catch (IOException ex) {
                    log.error("Failed to open persistence.xml. "
                                  + "Not processing properties.",
                              ex);
                } catch (ParserConfigurationException | SAXException ex) {
                    log.error("Error parsing persistence.xml. "
                                  + "Not processing properties",
                              ex);
                }
            } else {
                log.warn(String.format("persistence.xml file '%s' does "
                                           + "not exist. Ignoring.",
                                       persistenceXml.getPath()));
            }
        }
    }

    /**
     * A SAX Handler for processing the {@code persistence.xml} file. Used by
     * {@link #processPersistenceXml(org.hibernate.boot.registry.StandardServiceRegistryBuilder, java.io.File, org.apache.maven.plugin.logging.Log)}.
     */
    private static class PersistenceXmlHandler extends DefaultHandler {

        private final transient StandardServiceRegistryBuilder registryBuilder;

        private final transient Log log;

        public PersistenceXmlHandler(
            final StandardServiceRegistryBuilder registryBuilder,
            final Log log) {

            this.registryBuilder = registryBuilder;
            this.log = log;
        }

        @Override
        public void startElement(final String uri,
                                 final String localName,
                                 final String qName,
                                 final Attributes attributes) {
            log.info(String.format(
                "Found element with uri = '%s', localName = '%s', qName = '%s'...",
                uri,
                localName,
                qName));

            if ("property".equals(qName)) {
                final String propertyName = attributes.getValue("name");
                final String propertyValue = attributes.getValue("value");

                if (propertyName != null && !propertyName.isEmpty()
                        && propertyValue != null && !propertyValue.isEmpty()) {
                    log.info(String.format(
                        "Found property %s = %s in persistence.xml",
                        propertyName,
                        propertyValue));
                    registryBuilder.applySetting(propertyName, propertyValue);
                }
            }
        }

    }

}
