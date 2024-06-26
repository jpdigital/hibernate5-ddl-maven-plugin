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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.PackageInfo;
import io.github.classgraph.ScanResult;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

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
            new HashSet<>(
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

    private final ClassLoader classLoader;

    private final ScanResult scanResult;

    private EntityFinder(
        final ScanResult scanResult,
        final ClassLoader classLoader
    ) {
        this.scanResult = scanResult;
        this.classLoader = classLoader;
    }

    public static EntityFinder forClassPath(
        final MavenProject project,
        final Log log,
        final boolean includeTestClasses
    ) throws MojoFailureException {
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
        //the correct class loader is used to find the entities. Which is the 
        //one that has access to the compiled classes
        final ClassLoader classLoader = AccessController.doPrivileged(
            new ClassLoaderCreator(classPathUrls)
        );

        final ScanResult scanResult = new ClassGraph()
            .enableAllInfo()
            .addClassLoader(classLoader)
            .scan();

        return new EntityFinder(
            scanResult,
            classLoader
        );
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
     * @throws MojoFailureException If the {@link ClassGraph} instance needed
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
        final ScanResult scanResult;

        final ClassLoader classLoader;
        if (project == null) {
            scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(packageName)
                .scan();

            classLoader = scanResult.getClass().getClassLoader();
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
            //the correct class loader is used to find the entity classes. 
            //Which is the one which has access to the compiled classes
            classLoader = AccessController.doPrivileged(
                new ClassLoaderCreator(classPathUrls)
            );

            scanResult = new ClassGraph()
                .enableAllInfo()
                .addClassLoader(classLoader)
                .acceptPackages(packageName)
                .scan();
        }

        return new EntityFinder(
            scanResult,
            classLoader
        );
    }

    /**
     * Finds all entity classes and all converter classes in the package for
     * which the instance of this class was created. The entity classes must be
     * annotated with the {@link Entity} annotation, the converter classes must
     * be annotated with the {@link Converter} annotation. The method uses the
     * <a href="https://github.com/classgraph/classgraph">ClassGraph library</a>
     * for finding the entity classes.
     *
     * @return An {@link Set} with all entity classes.
     */
    @SuppressWarnings({"PMD.LongVariable"})
    public Set<Class<?>> findEntities() {
        final Set<Class<?>> entityClasses = new HashSet<>();

        entityClasses.addAll(
            scanResult
                .getClassesWithAnnotation(Entity.class)
                .stream()
                .map(ClassInfo::loadClass)
                .collect(Collectors.toSet())
        );

        entityClasses.addAll(
            scanResult
                .getClassesWithAnnotation(Converter.class)
                .stream()
                .map(ClassInfo::loadClass)
                .collect(Collectors.toSet())
        );

        return entityClasses;
    }

    /**
     * Finds all packages with Hibernate annotations on the package level.
     *
     * @return A {@link Set} with all packages annoated with package level
     *         Hibernate annotations.
     */
    public Set<Package> findPackages() {
        return scanResult
            .getPackageInfo()
            .filter(this::acceptPackagesWithPackageLevelAnnotations)
            .stream()
            .map(PackageInfo::getName)
            .map(name -> classLoader.getDefinedPackage(name))
            .collect(Collectors.toSet());
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
                classPathUrls.toArray(URL[]::new),
                Thread.currentThread().getContextClassLoader()
            );
            Thread.currentThread().setContextClassLoader(classLoader);

            return classLoader;
        }

    }

    private boolean acceptPackagesWithPackageLevelAnnotations(
        final PackageInfo packageInfo
    ) {
        boolean hasPackageLevelAnnotation = false;

        for (String packageLevelAnnotation : PACKAGE_LEVEL_ANNOTATIONS) {
            hasPackageLevelAnnotation = hasPackageLevelAnnotation || packageInfo
                .hasAnnotation(packageLevelAnnotation);
        }

        return hasPackageLevelAnnotation;
    }

}
