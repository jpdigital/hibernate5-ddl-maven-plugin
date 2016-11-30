/*
 * Copyright (C) 2014 Jens Pelzetter <jens.pelzetter@googlemail.com>
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Goal which creates DDL SQL files for the JPA entities in the project (using
 * the Hibernate 5 SchemaExport class}.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
@Mojo(name = "gen-ddl",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES,
      requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
      threadSafe = true)
public class GenerateDdlMojo extends AbstractMojo {

    /**
     * Location of the output file.
     */
    @Parameter(defaultValue
                   = "${project.build.directory}/generated-resources/sql/ddl/auto",
               property = "outputDir",
               required = true)
    private File outputDirectory;

    /**
     * Packages containing the entity files for which the SQL DDL scripts shall
     * be generated.
     */
    @Parameter(required = true)
    private String[] packages;

    /**
     * Database dialects for which create scripts shall be generated. For
     * available dialects refer to the documentation the {@link Dialect}
     * enumeration.
     */
    @Parameter(required = true)
    private String[] dialects;

    /**
     * Set this to {@code true} to include drop statements into the generated
     * DDL file.
     */
    @Parameter(required = false)
    @SuppressWarnings("PMD.LongVariable")
    private boolean createDropStatements;

    /**
     * The {@code persistence.xml} file to use to read properties etc. Default
     * value is {@code src/main/resources/META-INF/persistence.xml}. If the file
     * is not present it is ignored. If the file is present all properties set
     * using a {@code <property>} element are set on the Hibernate
     * configuration.
     */
    @Parameter(
        defaultValue = "${basedir}/src/main/resources/META-INF/persistence.xml",
        required = false)
    private File persistenceXml;

    @Parameter(defaultValue = "${project}", readonly = true)
    private transient MavenProject project;

    /**
     * The Mojo's execute method.
     *
     * @throws MojoExecutionException if the Mojo can't be executed.
     * @throws MojoFailureException   if something goes wrong while to Mojo is
     *                                executed.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File outputDir = outputDirectory;

        getLog().info(String.format("Generating DDL SQL files in %s.",
                                    outputDir.getAbsolutePath()));

        //Check if the output directory exists.
        if (!outputDir.exists()) {
            final boolean result = outputDir.mkdirs();
            if (!result) {
                throw new MojoFailureException(
                    "Failed to create output directory for SQL DDL files.");
            }
        }

        //Read the dialects from the parameter and convert them to instances of the dialect enum.
        final Set<Dialect> dialectsList = new HashSet<>();
        for (final String dialect : dialects) {
            convertDialect(dialect, dialectsList);
        }

        //Find the entity classes in the packages.
        final Set<Class<?>> entityClasses = new HashSet<>();
        for (final String packageName : packages) {
            final Set<Class<?>> packageEntities = EntityFinder.forPackage(
                project, getLog(), packageName).findEntities();
            entityClasses.addAll(packageEntities);

            //findEntitiesForPackage(packageName, entityClasses);
        }
        getLog().info(String.format("Found %d entities.",
                                    entityClasses.size()));

        //Generate the SQL scripts
        for (final Dialect dialect : dialectsList) {
            generateDdl(dialect, entityClasses);
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String[] getPackages() {
        return Arrays.copyOf(packages, packages.length);
    }

    public void setPackages(final String... packages) {
        this.packages = Arrays.copyOf(packages, packages.length);
    }

    public String[] getDialects() {
        return Arrays.copyOf(dialects, dialects.length);
    }

    public void setDialects(final String... dialects) {
        this.dialects = Arrays.copyOf(dialects, dialects.length);
    }

    public boolean isCreateDropStatements() {
        return createDropStatements;
    }

    @SuppressWarnings("PMD.LongVariable")
    public void setCreateDropStatements(final boolean createDropStatments) {
        this.createDropStatements = createDropStatments;
    }

    public File getPersistenceXml() {
        return persistenceXml;
    }

    public void setPersistenceXml(final File persistenceXml) {
        this.persistenceXml = persistenceXml;
    }

    /**
     * Helper method for converting the dialects from {@code String} to
     * instances of the {@link Dialect} enumeration.
     *
     * @param dialect      The dialect to convert.
     * @param dialectsList The lists of dialects where the converted dialect is
     *                     stored.
     *
     * @throws MojoFailureException If the dialect string could not be
     *                              converted, for example if it is misspelled.
     *                              This will cause a {@code Build Failure}
     */
    private void convertDialect(final String dialect,
                                final Set<Dialect> dialectsList)
        throws MojoFailureException {

        try {
            dialectsList.add(Dialect
                .valueOf(dialect.toUpperCase(Locale.ENGLISH)));
        } catch (IllegalArgumentException ex) {
            final StringBuffer buffer = new StringBuffer();
            for (final Dialect avilable : Dialect.values()) {
                buffer.append(avilable.toString()).append('\n');
            }

            throw new MojoFailureException(
                String.format(
                    "Can't convert the configured dialect '%s' to a dialect classname. "
                    + "Available dialects are:%n"
                        + "%s",
                    dialect,
                    buffer.toString()),
                ex);
        }
    }

    /**
     * Helper method for generating the DDL classes for a specific dialect. This
     * is place for the real work is done. The method first creates an instance
     * of the {@link Configuration} class from Hibernate an puts the appropriate
     * values into it. It then creates an instance of the {@link SchemaExport}
     * class from the Hibernate API, configured this class, for example by
     * setting {@code format} to {@code true} so that the generated SQL files
     * are formatted nicely. After that it calls the
     * {@link SchemaExport#execute(boolean, boolean, boolean, boolean)} method
     * which will create the SQL script file. The method is called in a way
     * which requires <em>no</em> database connection.
     *
     *
     * @param dialect       The dialect for which the DDL files is generated.
     * @param entityClasses The entity classes for which the DDL file is
     *                      generated.
     *
     * @throws MojoFailureException if something goes wrong.
     */
    private void generateDdl(final Dialect dialect,
                             final Set<Class<?>> entityClasses)
        throws MojoFailureException {

        final StandardServiceRegistryBuilder registryBuilder
                                                 = new StandardServiceRegistryBuilder();
        processPersistenceXml(registryBuilder);

        if (createDropStatements) {
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
//        final SchemaExport export = new SchemaExport(
//            (MetadataImplementor) metadata, true);
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
        if (createDropStatements) {
            export.execute(EnumSet.of(TargetType.SCRIPT),
                           SchemaExport.Action.BOTH,
                           metadata);
        } else {
            export.execute(EnumSet.of(TargetType.SCRIPT),
                           SchemaExport.Action.CREATE,
                           metadata);
        }

        writeOutputFile(dialect, tmpDir);
    }

    private void processPersistenceXml(
        final StandardServiceRegistryBuilder registryBuilder) {
        if (persistenceXml != null) {
            try (InputStream inStream = new FileInputStream(persistenceXml)) {
                getLog()
                  .info("persistence.xml available, looking for properties...");

                final SAXParser parser;

                parser = SAXParserFactory.newInstance().newSAXParser();

                parser.parse(inStream,
                             new PersistenceXmlHandler(registryBuilder));
            } catch (FileNotFoundException ex) {
                getLog().warn("persistence.xml not present; ignoring.");
            } catch (IOException ex) {
                getLog().error(
                    "Failed to open persistence.xml. Not processing properties.",
                    ex);
            } catch (ParserConfigurationException | SAXException ex) {
                getLog().error(
                    "Error parsing persistence.xml. Not processing properties",
                    ex);
            }
        }
    }

    private class PersistenceXmlHandler extends DefaultHandler {

        private final transient StandardServiceRegistryBuilder registryBuilder;

        public PersistenceXmlHandler(
            final StandardServiceRegistryBuilder registryBuilder) {
            this.registryBuilder = registryBuilder;
        }

        @Override
        public void startElement(final String uri,
                                 final String localName,
                                 final String qName,
                                 final Attributes attributes) {
            getLog().info(String.format(
                "Found element with uri = '%s', localName = '%s', qName = '%s'...",
                uri,
                localName,
                qName));

            if ("property".equals(qName)) {
                final String propertyName = attributes.getValue("name");
                final String propertyValue = attributes.getValue("value");

                if (propertyName != null && !propertyName.isEmpty()
                        && propertyValue != null && !propertyValue.isEmpty()) {
                    getLog().info(String.format(
                        "Found property %s = %s in persistence.xml",
                        propertyName,
                        propertyValue));
                    registryBuilder.applySetting(propertyName, propertyValue);
                }
            }
        }

    }

    /**
     * Helper method for writing the output files if necessary. The
     * {@link #generateDdl(Dialect, Set)} method writes the output to temporary
     * files. This method checks of the output files have changed and copies the
     * files if necessary.
     */
    private void writeOutputFile(final Dialect dialect,
                                 final Path tmpDir)
        throws MojoFailureException {
//        final Path outputDir = outputDirectory.toPath();
//        if (Files.exists(outputDir)) {
//            if (!Files.isDirectory(outputDir)) {
//                throw new MojoFailureException("A file with the name of the "
//                                                   + "output directory already "
//                                                   + "exists but is not a "
//                                                   + "directory.");
//            }
//        } else {
//            try {
//                Files.createDirectory(outputDir);
//            } catch (IOException ex) {
//                throw new MojoFailureException(
//                    String.format("Failed to create the output directory: %s",
//                                  ex.getMessage()),
//                    ex);
//            }
//        }

        createOutputDir();

//        final String dirPath;
//        if (outputDirectory.getAbsolutePath().endsWith("/")) {
//            dirPath = outputDirectory.getAbsolutePath().substring(
//                0, outputDirectory.getAbsolutePath().length());
//        } else {
//            dirPath = outputDirectory.getAbsolutePath();
//        }
//
//        final Path outputFilePath = Paths.get(String.format(
//            "%s/%s.sql", dirPath, dialect.name().toLowerCase(Locale.ENGLISH)));
        final Path outputFilePath = createOutputFilePath(dialect);
        final Path tmpFilePath = Paths.get(String.format(
            "%s/%s.sql",
            tmpDir.toString(),
            dialect.name().toLowerCase(
                Locale.ENGLISH)));

        if (Files.exists(outputFilePath)) {

            final String outputFileData;
            final String tmpFileData;
            try {
                outputFileData = new String(
                    Files.readAllBytes(outputFilePath),
                    Charset.forName("UTF-8"));
                tmpFileData = new String(
                    Files.readAllBytes(tmpFilePath),
                    Charset.forName("UTF-8"));
            } catch (IOException ex) {
                throw new MojoFailureException(
                    String.format("Failed to check if DDL file content has "
                                      + "changed: %s",
                                  ex.getMessage()),
                    ex);
            }

            try {
                if (!tmpFileData.equals(outputFileData)) {
                    Files.deleteIfExists(outputFilePath);
                    Files.copy(tmpFilePath, outputFilePath);
                }
            } catch (IOException ex) {
                throw new MojoFailureException(
                    String.format("Failed to copy DDL file content from tmp "
                                      + "file to output file: %s",
                                  ex.getMessage()),
                    ex);
            }
        } else {
            try {
                Files.copy(tmpFilePath, outputFilePath);
            } catch (IOException ex) {
                throw new MojoFailureException(
                    String.format("Failed to copy tmp file to output file: %s",
                                  ex.getMessage()),
                    ex);
            }
        }
    }

    /**
     * Helper for creating the output directory if it does not exist.
     *
     * @return A {@link Path} object describing the output directory.
     *
     * @throws MojoFailureException If The creation of the output directory
     *                              fails.
     */
    private void createOutputDir() throws MojoFailureException {
        final Path outputDir = outputDirectory.toPath();
        if (Files.exists(outputDir)) {
            if (!Files.isDirectory(outputDir)) {
                throw new MojoFailureException("A file with the name of the "
                                                   + "output directory already "
                                                   + "exists but is not a "
                                                   + "directory.");
            }
        } else {
            try {
                Files.createDirectory(outputDir);
            } catch (IOException ex) {
                throw new MojoFailureException(
                    String.format("Failed to create the output directory: %s",
                                  ex.getMessage()),
                    ex);
            }
        }
    }

    private Path createOutputFilePath(final Dialect dialect) {
        final String dirPath;
        if (outputDirectory.getAbsolutePath().endsWith("/")) {
            dirPath = outputDirectory.getAbsolutePath().substring(
                0, outputDirectory.getAbsolutePath().length());
        } else {
            dirPath = outputDirectory.getAbsolutePath();
        }

        return Paths.get(String.format(
            "%s/%s.sql", dirPath, dialect.name().toLowerCase(Locale.ENGLISH)));
    }

}
