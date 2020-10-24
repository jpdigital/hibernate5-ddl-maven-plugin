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

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Base class the the Mojo class providing the {@code gen-ddl} goal. In the
 * plugins it should be enough to create an empty class which extends this class
 * and is annotated with the {@link Mojo} annotation.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
@Mojo(name = "gen-ddl",
      defaultPhase = LifecyclePhase.PROCESS_CLASSES,
      requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
      threadSafe = true)
@SuppressWarnings({"PMD.LongVariable", "PMD.DataClass"})
public class GenerateDdlMojo extends AbstractMojo {

    private final static String[] DEFAULT_PROPERTIES_TO_USE = new String[]{
        "format_sql",
        "use_sql_comments",
        "hibernate.id.new_generator_mappings",
        "org.hibernate.envers.audit_strategy",};

    /**
     * Location of the output file.
     */
    @Parameter(defaultValue
        = "${project.build.directory}/generated-resources/sql/ddl/auto",
               property = "outputDir",
               required = true)
    private File outputDirectory;

    /**
     * If set each name of an output file will be prefixed with the value of
     * this parameter.
     */
    @Parameter(required = false, defaultValue = "")
    private String outputFileNamePrefix = "";

    /**
     * If set the value of this parameter will be appended to the name of each
     * output file.
     */
    @Parameter(required = false, defaultValue = "")
    private String outputFileNameSuffix = "";

    /**
     * If set to true <strong>and</strong> if only one dialect is configured
     * <strong>and</strong> either {@link #outputFileNamePrefix} or
     * {@link #outputFileNameSuffix} are set the dialect name will be omitted
     * from the name of the DDL file.
     */
    @Parameter(required = false)
    private boolean omitDialectFromFileName;

    /**
     * Packages containing the entity files for which the SQL DDL scripts shall
     * be generated.
     */
    @Parameter(required = true)
    private String[] packages;

    /**
     * Set to {@code true} to include classes in {@code src/test}.
     */
    @Parameter(required = false)
    private boolean includeTestClasses;

    /**
     * Database dialects for which create scripts shall be generated. For
     * available dialects refer to the documentation the {@link Dialect}
     * enumeration.
     */
    @Parameter(required = false)
    private String[] dialects;

    @Parameter(required = false)
    private String[] customDialects;

    /**
     * Set this to {@code true} to include drop statements into the generated
     * DDL file.
     */
    @Parameter(required = false)
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
        required = false
    )
    private File persistenceXml;

    @Parameter(required = false)
    private String[] persistencePropertiesToUse;

    @Parameter(required = false)
    private Map<String, String> persistenceProperties;

    @Parameter(defaultValue = "${project}", readonly = true)
    private transient MavenProject project;

    public GenerateDdlMojo() {
        this.persistenceProperties = new HashMap<>();
    }

    /**
     * The Mojo's execute method.
     *
     * @throws MojoExecutionException if the Mojo can't be executed.
     * @throws MojoFailureException   if something goes wrong while to Mojo is
     *                                executed.
     */
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void execute() throws MojoExecutionException, MojoFailureException {
        final File outputDir = outputDirectory;

        getLog().info(String.format(
            "Generating DDL SQL files in %s.", outputDir.getAbsolutePath())
        );

        //Check if the output directory exists.
        if (!outputDir.exists()) {
            final boolean result = outputDir.mkdirs();
            if (!result) {
                throw new MojoFailureException(
                    "Failed to create output directory for SQL DDL files."
                );
            }
        }

        //Find the entity classes in the packages.
//        final Set<Class<?>> entityClasses = new HashSet<>();
//        for (final String packageName : packages) {
//            final Set<Class<?>> packageEntities = EntityFinder
//                .forPackage(project, getLog(), packageName, includeTestClasses)
//                .findEntities();
//            entityClasses.addAll(packageEntities);
//        }
        final Set<Class<?>> entityClasses =  EntityFinder
            .forClassPath(project, getLog(), includeTestClasses)
            .findEntities();
        
        getLog().info(
            String.format(
                "Found %d entities.", entityClasses.size()
            )
        );

        if (getPersistenceProperties().isEmpty()) {
            getLog().info("No persistence properties set in POM.");
        } else {
            getLog().info("Persistence properties set in POM:");
            getPersistenceProperties()
                .entrySet()
                .stream()
                .forEach(
                    property -> getLog().info(
                        String.format(
                            "\t%s = %s",
                            property.getKey(),
                            property.getValue()
                        )
                    )
                );
        }

        // Find the DDL generator implementation to use.
        final ServiceLoader<DdlGenerator> serviceLoader = ServiceLoader
            .load(DdlGenerator.class);
        final DdlGenerator ddlGenerator;
        if (serviceLoader.iterator().hasNext()) {
            ddlGenerator = serviceLoader.iterator().next();
        } else {
            throw new MojoFailureException(
                String.format(
                    "No implementation of '%s' is available.",
                    DdlGenerator.class.getName()
                )
            );
        }

        for (final Dialect dialect : convertDialects()) {
            ddlGenerator.generateDdl(dialect, entityClasses, this);
        }

        if (customDialects != null) {
            for (final String customDialect : customDialects) {
                ddlGenerator.generateDdl(customDialect, entityClasses, this);
            }
        }
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getOutputFileNamePrefix() {
        return outputFileNamePrefix;
    }

    public void setOutputFileNamePrefix(final String outputFileNamePrefix) {
        this.outputFileNamePrefix = outputFileNamePrefix;
    }

    public String getOutputFileNameSuffix() {
        return outputFileNameSuffix;
    }

    public void setOutputFileNameSuffix(final String outputFileNameSuffix) {
        this.outputFileNameSuffix = outputFileNameSuffix;
    }

    public boolean isOmitDialectFromFileName() {
        return omitDialectFromFileName;
    }

    public void setOmitDialectFromFileName(final boolean omitDialectFromFileName) {
        this.omitDialectFromFileName = omitDialectFromFileName;
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

    public String[] getCustomDialects() {
        return Arrays.copyOf(customDialects, customDialects.length);
    }

    public void setCustomDialects(final String... customDialects) {
        this.customDialects = Arrays.copyOf(customDialects,
                                            customDialects.length);
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

    public String[] getPersistencePropertiesToUse() {
        if (persistencePropertiesToUse == null
                || persistencePropertiesToUse.length == 0) {

            return Arrays.copyOf(DEFAULT_PROPERTIES_TO_USE,
                                 DEFAULT_PROPERTIES_TO_USE.length);
        } else {
            return Arrays.copyOf(persistencePropertiesToUse,
                                 persistencePropertiesToUse.length);
        }
    }

    public void setPersistencePropertiesToUse(
        final String... persistencePropertiesToUse) {

        if (persistencePropertiesToUse == null) {
            this.persistencePropertiesToUse = null;
        } else {
            this.persistencePropertiesToUse = Arrays
                .copyOf(persistencePropertiesToUse,
                        persistencePropertiesToUse.length);
        }
    }

    public Map<String, String> getPersistenceProperties() {
        return new HashMap<>(persistenceProperties);
    }

    public void setPersistenceProperties(
        final Map<String, String> persistenceProperties
    ) {
        this.persistenceProperties = new HashMap<>(persistenceProperties);
    }
    
    protected MavenProject getProject() {
        return project;
    }
    
    protected void setProject(final MavenProject project) {
        this.project = project;
    }

    public boolean isIncludeTestClasses() {
        return includeTestClasses;
    }

    public void setIncludeTestClasses(final boolean includeTestClasses) {
        this.includeTestClasses = includeTestClasses;
    }

    /**
     * Helper method which reads the dialects from the parameter and converts
     * them into instances of the {@link Dialect} enumeration.
     *
     * @return A list of all dialects to use
     *
     * @throws MojoFailureException If an error occurs.
     */
    private Set<Dialect> convertDialects() throws MojoFailureException {

        final Set<Dialect> dialectsList = new HashSet<>();
        if (dialects != null) {
            for (final String dialect : dialects) {
                convertDialect(dialect, dialectsList);
            }
        }
        return dialectsList;
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

    protected void writeOutputFile(final String dialectClassName,
                                   final Path tmpDir)
        throws MojoFailureException {

        final OutputFileWriter writer = new OutputFileWriter(outputDirectory);
        writer.setOmitDialectFromFileName(omitDialectFromFileName
                                              && dialects.length == 1);
        writer.setOutputFileNamePrefix(outputFileNamePrefix);
        writer.setOutputFileNameSuffix(outputFileNameSuffix);

        writer.writeOutputFile(dialectClassName, tmpDir);

    }

    public String getDialectNameFromClassName(final String dialectClassName) {

        final int pos = dialectClassName.lastIndexOf('.');

        if (dialectClassName.toLowerCase(Locale.ROOT).endsWith("dialect")) {
            return dialectClassName
                .substring(pos + 1,
                           dialectClassName.length() - "dialect".length())
                .toLowerCase(Locale.ROOT);
        } else {
            return dialectClassName.substring(pos + 1)
                .toLowerCase(Locale.ROOT);
        }
    }

}
