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
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Implementation of the {@link DdlGenerator} interface for Hibernate
 * {@literal 5.1}.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
public class DdlGeneratorHibernate54 implements DdlGenerator {

    @Override
    public void generateDdl(
        final String dialectClassName,
        final Set<Package> packages,
        final Set<Class<?>> entityClasses,
        final GenerateDdlMojo mojo
    ) throws MojoFailureException {
        final StandardServiceRegistryBuilder registryBuilder
            = new StandardServiceRegistryBuilder();
        processPersistenceXml(registryBuilder, mojo);

        if (mojo.isCreateDropStatements()) {
            registryBuilder.applySetting(
                "hibernate.hbm2ddl.auto", "create-drop"
            );
        } else {
            registryBuilder.applySetting("hibernate.hbm2ddl.auto", "create");
        }

        registryBuilder.applySetting("hibernate.dialect", dialectClassName);

        if (!mojo.getPersistenceProperties().isEmpty()) {
            mojo.getLog().info("Applying persistence properties set in POM...");
            final Map<String, String> properties = mojo
                .getPersistenceProperties()
                .entrySet()
                .stream()
                .filter(
                    property -> !property.getKey().equals(
                        "hibernate.hbm2ddl.auto"
                    )
                )
                .filter(
                    property -> !property.getKey().equals(
                        "hibernate.dialect"
                    )
                )
                .collect(
                    Collectors.toMap(
                        property -> property.getKey(),
                        property -> property.getValue()
                    )
                );

            for (final Map.Entry<String, String> property : properties
                .entrySet()) {
                mojo.getLog().info(
                    String.format(
                        "Setting peristence property %s = %s",
                        property.getKey(),
                        property.getValue()
                    )
                );
            }

            registryBuilder.applySettings(properties);
        }

        final StandardServiceRegistry standardRegistry = registryBuilder.build();

        final MetadataSources metadataSources = new MetadataSources(
            standardRegistry
        );

        if (packages.isEmpty()) {
            System.err.println("No packages to process.");
        }
        for (final Package aPackage : packages) {
            System.err.printf("will process package %s%n", aPackage.getName());
            metadataSources.addPackage(aPackage);
        }
        for (final Class<?> entityClass : entityClasses) {
            metadataSources.addAnnotatedClass(entityClass);
        }

        final SchemaExport export = new SchemaExport();
        export.setDelimiter(";");

        final Path tmpDir;
        try {
            tmpDir = Files.createTempDirectory("hibernate5-ddl-maven-plugin");
        } catch (IOException ex) {
            throw new MojoFailureException("Failed to create work dir.", ex);
        }

        final Metadata metadata = metadataSources.buildMetadata();

        export.setManageNamespaces(true);
        export.setOutputFile(
            String.format(
                "%s/%s.sql",
                tmpDir.toString(),
                mojo.getDialectNameFromClassName(dialectClassName)
            )
        );
        export.setFormat(true);
        if (mojo.isCreateDropStatements()) {
            export.execute(
                EnumSet.of(TargetType.SCRIPT),
                SchemaExport.Action.BOTH,
                metadata
            );
        } else {
            export.execute(
                EnumSet.of(TargetType.SCRIPT),
                SchemaExport.Action.CREATE,
                metadata
            );
        }

        mojo.writeOutputFile(dialectClassName, tmpDir);

        try {
            Files
                .walk(tmpDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException ex) {
            throw new MojoFailureException("Failed to clean up temporary files.",
                                           ex);
        }
    }

    @Override
    public void generateDdl(
        final Dialect dialect,
        final Set<Package> packages,
        final Set<Class<?>> entityClasses,
        final GenerateDdlMojo mojo
    )
        throws MojoFailureException {

        generateDdl(
            dialect.getDialectClassName(), packages, entityClasses, mojo
        );
    }

    /**
     * Helper method for processing the {@code persistence.xml} file.
     *
     * @param registryBuilder {@link StandardServiceRegistryBuilder} from
     *                        Hibernate.
     * @param mojo            Provides access to the Maven {@link Log} and the
     *                        properties provided to the Mojo.
     */
    private void processPersistenceXml(
        final StandardServiceRegistryBuilder registryBuilder,
        final GenerateDdlMojo mojo
    ) {
        final Log log = mojo.getLog();
        final File persistenceXml = mojo.getPersistenceXml();

        if (persistenceXml != null) {
            if (Files.exists(persistenceXml.toPath())) {
                try (InputStream inputStream = new FileInputStream(
                    mojo.getPersistenceXml()
                )) {
                    log.info(
                        "persistence.xml found, looking for properties..."
                    );

                    final SAXParser parser = SAXParserFactory
                        .newInstance()
                        .newSAXParser();

                    parser.parse(
                        inputStream,
                        new PersistenceXmlHandler(
                            registryBuilder,
                            mojo.getLog(),
                            new HashSet<>(
                                Arrays.asList(
                                    mojo.getPersistencePropertiesToUse()
                                )
                            )
                        )
                    );

                } catch (IOException ex) {
                    log.error(
                        "Failed to open persistence.xml. "
                            + "Not processing properties.",
                        ex
                    );
                } catch (ParserConfigurationException | SAXException ex) {
                    log.error(
                        "Error parsing persistence.xml. "
                            + "Not processing properties",
                        ex
                    );
                }
            } else {
                log.warn(
                    String.format(
                        "persistence.xml file '%s' does not exist. Ignoring.",
                        persistenceXml.getPath()
                    )
                );
            }
        }
    }

    /**
     * A SAX Handler for processing the {@code persistence.xml} file. Used by
     * {@link #processPersistenceXml(org.hibernate.boot.registry.StandardServiceRegistryBuilder, de.jpdigital.maven.plugins.hibernate5ddl.GenerateDdlMojo) }.
     */
    private static class PersistenceXmlHandler extends DefaultHandler {

        private final transient StandardServiceRegistryBuilder registryBuilder;

        private final transient Set<String> propertiesToUse;

        private final transient Log log;

        public PersistenceXmlHandler(
            final StandardServiceRegistryBuilder registryBuilder,
            final Log log,
            final Set<String> propertiesToUse
        ) {
            this.registryBuilder = registryBuilder;
            this.log = log;
            this.propertiesToUse = propertiesToUse;
        }

        @Override
        public void startElement(
            final String uri,
            final String localName,
            final String qName,
            final Attributes attributes
        ) {
            log.info(
                String.format(
                    "Found element with uri = '%s', localName = '%s', qName = '%s'...",
                    uri,
                    localName,
                    qName
                )
            );

            if ("property".equals(qName)
                    && propertiesToUse.contains(attributes.getValue("name"))) {

                final String propertyName = attributes.getValue("name");
                final String propertyValue = attributes.getValue("value");

                if (propertyName != null && !propertyName.isEmpty()
                        && propertyValue != null && !propertyValue.isEmpty()) {
                    log.info(
                        String.format(
                            "Found property %s = %s in persistence.xml",
                            propertyName,
                            propertyValue
                        )
                    );
                    registryBuilder.applySetting(propertyName, propertyValue);
                }
            }
        }

    }

}
