/*
 * Copyright (C) 2015 Jens Pelzetter
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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Converter;
import javax.persistence.Entity;

/**
 * Helper class for finding the entity classes. An instance of this class is
 * created using the
 * {@link EntityFinder#forPackage(org.apache.maven.project.MavenProject, org.apache.maven.plugin.logging.Log, java.lang.String, boolean)}
 * method.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
final class EntityFinder {

    /**
     * Hibernate Annotations that only be applied on package level (in a
     * {@code package-info.java} file).
     */
    private static final Set<String> PACKAGE_LEVEL_ANNOTATIONS = Collections
        .unmodifiableSet(
            new HashSet<String>(
                Arrays.asList(
                    new String[]{
                        "org.hibernate.annotations.AnyMetaDef",
                        "org.hibernate.annotations.AnyMetaDefs",
                        "org.hibernate.annotations.FetchProfile",
                        "org.hibernate.annotations.FetchProfile.FetchOverride",
                        "org.hibernate.annotations.FetchProfiles",
                        "org.hibernate.annotations.FilterDef",
                        "org.hibernate.annotations.FilterDefs",
                        "org.hibernate.annotations.GenericGenerator",
                        "org.hibernate.annotations.GenericGenerators",
                        "org.hibernate.annotations.NamedNativeQueries",
                        "org.hibernate.annotations.NamedNativeQuery",
                        "org.hibernate.annotations.NamedQueries",
                        "org.hibernate.annotations.NamedQuery",
                        "org.hibernate.annotations.TypeDef",
                        "org.hibernate.annotations.TypeDefs"}
                )
            )
        );

    private final transient Reflections reflections;

    private final ClassLoader classLoader;

    private EntityFinder(
        final Reflections reflections, final ClassLoader classLoader
    ) {
        this.reflections = reflections;
        this.classLoader = classLoader;
    }

    public static EntityFinder forClassPath(
        final MavenProject project,
        final Log log,
        final boolean includeTestClasses
    ) throws MojoFailureException {
        final Reflections reflections;

        Objects.requireNonNull(project, "Parameter project is null");

        final List<String> classPathElements = new ArrayList<>();
        try {
            classPathElements.addAll(project.getCompileClasspathElements());
            if (includeTestClasses) {
                classPathElements.addAll(project.getTestClasspathElements());
            }
        } catch (DependencyResolutionRequiredException ex) {
            throw new MojoFailureException(
                "Failed to resolve project classpath.", ex
            );
        }

        final List<URL> classPathUrls = new ArrayList<>();
        for (final String classPathElem : classPathElements) {
            log.info(
                String.format(
                    "Adding classpath elemement '%s'...", classPathElem
                )
            );
            classPathUrls.add(classPathElemToUrl(classPathElem));
        }

        log.info("Classpath URLs:");
        for (final URL url : classPathUrls) {
            log.info(String.format("\t%s", url.toString()));
        }

        //Here we have to do some classloader magic to ensure that the 
        //Reflections instance uses the correct class loader. Which is the 
        //one that has access to the compiled classes
        final ClassLoader classLoader = AccessController.doPrivileged(
            new ClassLoaderCreator(classPathUrls)
        );

        reflections = new Reflections(
            new ConfigurationBuilder()
                .setUrls(
                    ClasspathHelper.forClassLoader(classLoader)
                )
                .setScanners(
                    new SubTypesScanner(),
                    new TypeAnnotationsScanner()
                )
        );
        return new EntityFinder(reflections, classLoader);
    }

    /**
     * Creates an {@code EntityFinder} for the provided package.
     *
     * @param project     The Maven project in which the calling Mojo is
     *                    executed. Can be {@code null}.
     * @param log         An Maven log object for creating output.
     * @param packageName The name of the package in the class should look for
     *                    entities.
     *
     * @return An {@code EntityFinder} instance.
     *
     * @throws MojoFailureException If the {@link Reflections} instance needed
     *                              by the {@code EntityFinder} can't be
     *                              created.
     */
    @SuppressWarnings({"unchecked", "PMD.LongVariable"})
    public static EntityFinder forPackage(
        final MavenProject project,
        final Log log,
        final String packageName,
        final boolean includeTestClasses
    ) throws MojoFailureException {

        final Reflections reflections;
        final ClassLoader classLoader;
        if (project == null) {
            reflections = new Reflections(
                new ConfigurationBuilder()
                    .setUrls(
                        ClasspathHelper.forPackage(packageName)
                    )
                    .filterInputsBy(
                        new FilterBuilder().includePackage(packageName)
                    )
                    .setScanners(
                        new SubTypesScanner(),
                        new TypeAnnotationsScanner()
                    )
            );
            classLoader = reflections.getClass().getClassLoader();
        } else {
            final List<String> classPathElements = new ArrayList<>();
            try {
                classPathElements.addAll(project.getCompileClasspathElements());
                if (includeTestClasses) {
                    classPathElements.addAll(project.getTestClasspathElements());
                }
            } catch (DependencyResolutionRequiredException ex) {
                throw new MojoFailureException(
                    "Failed to resolve project classpath.", ex
                );
            }
            final List<URL> classPathUrls = new ArrayList<>();
            for (final String classPathElement : classPathElements) {
                log.info(
                    String.format(
                        "Adding classpath elemement '%s'...", classPathElement
                    )
                );
                classPathUrls.add(classPathElemToUrl(classPathElement));
            }

            log.info("Classpath URLs:");
            for (final URL url : classPathUrls) {
                log.info(String.format("\t%s", url.toString()));
            }

            //Here we have to do some classloader magic to ensure that the 
            //Reflections instance uses the correct class loader. Which is the 
            //one which has access to the compiled classes
            classLoader = AccessController.doPrivileged(
                new ClassLoaderCreator(classPathUrls)
            );

            reflections = new Reflections(
                new ConfigurationBuilder()
                    .setUrls(
                        ClasspathHelper.forPackage(
                            packageName, classLoader
                        )
                    )
                    .filterInputsBy(
                        new FilterBuilder().includePackage(packageName)
                    )
                    .setScanners(
                        new SubTypesScanner(),
                        new TypeAnnotationsScanner()
                    )
            );

        }

        return new EntityFinder(reflections, classLoader);
    }

    /**
     * Finds all entity classes and all converter classes in the package for
     * which the instance of this class was created. The entity classes must be
     * annotated with the {@link Entity} annotation, the converter classes must
     * be annotated with the {@link Converter} annotation. The method uses the
     * <a href="https://code.google.com/p/reflections/">Reflections library</a>
     * for finding the entity classes.
     *
     * @return An {@link Set} with all entity classes.
     */
    @SuppressWarnings(
        {"PMD.LongVariable"})
    public Set<Class<?>> findEntities() {
        final Set<Class<?>> entityClasses = new HashSet<>();

        final Set<Class<?>> classesWithEntity = reflections
            .getTypesAnnotatedWith(Entity.class);
        for (final Class<?> entityClass : classesWithEntity) {
            entityClasses.add(entityClass);
        }
        final Set<Class<?>> classesWithConverter = reflections
            .getTypesAnnotatedWith(Converter.class);
        for (final Class<?> entityClass : classesWithConverter) {
            entityClasses.add(entityClass);
        }
        return entityClasses;
    }

    /**
     * Finds all packages with Hibernate annotations on the package level.
     *
     * @return A {@link Set} with all packages annoated with package level
     *         Hiberannotations.
     */
    public Set<Package> findPackages() {
//        final Configuration conf = Objects
//            .requireNonNull(reflections)
//            .getConfiguration();
//        final ClassLoader[] classLoaders = Objects
//            .requireNonNull(conf)
//            .getClassLoaders();
//        final Set<Package> allPackages = Arrays
//            .stream(Objects.requireNonNull(classLoaders))
//            .map(ClassLoader::getDefinedPackages)
//            .flatMap(packages -> Arrays.stream(packages))
//            .collect(Collectors.toSet());

//        return allPackages
//            .stream()
        return Arrays
            .stream(classLoader.getDefinedPackages())
            .filter(
                aPackage -> PACKAGE_LEVEL_ANNOTATIONS.contains(
                    aPackage.getClass().getName()
                )
            ).collect(Collectors.toSet());
    }

    /**
     * Helper method for converting a fully qualified package name from the
     * string representation to a a URL.
     *
     * @param classPathElem The class path to convert.
     *
     * @return A URL for the package.
     *
     * @throws MojoFailureException If something goes wrong.
     */
    private static URL classPathElemToUrl(final String classPathElem) throws
        MojoFailureException {
        final File file = new File(classPathElem);
        final URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new MojoFailureException(
                String.format(
                    "Failed to convert classpath element '%s' to an URL.",
                    classPathElem
                ),
                ex
            );
        }

        return url;
    }

    private static class ClassLoaderCreator implements
        PrivilegedAction<ClassLoader> {

        private final transient List<URL> classPathUrls;

        public ClassLoaderCreator(final List<URL> classPathUrls) {
            this.classPathUrls = classPathUrls;
        }

        @Override
        public ClassLoader run() {

            final URLClassLoader classLoader = new URLClassLoader(
                classPathUrls.toArray(new URL[0]),
                Thread.currentThread().getContextClassLoader()
            );
            Thread.currentThread().setContextClassLoader(classLoader);

            return classLoader;
        }

    }

}
