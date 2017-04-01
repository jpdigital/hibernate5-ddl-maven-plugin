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
import org.reflections.util.ClasspathHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

/**
 * Helper class for finding the entity classes. An instance of this class is
 * created using the {@link #forPackage(MavenProject, Log, String)} method.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
final class EntityFinder {

    private final transient Reflections reflections;

    private EntityFinder(final Reflections reflections) {
        this.reflections = reflections;
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
    @SuppressWarnings("unchecked")
    public static EntityFinder forPackage(final MavenProject project,
                                          final Log log,
                                          final String packageName)
        throws MojoFailureException {
        final Reflections reflections;
        if (project == null) {
            reflections = new Reflections(
                ClasspathHelper.forPackage(packageName));
        } else {
            final List<String> classPathElems;
            try {
                classPathElems = project.getCompileClasspathElements();
            } catch (DependencyResolutionRequiredException ex) {
                throw new MojoFailureException(
                    "Failed to resolve project classpath.", ex);
            }
            final List<URL> classPathUrls = new ArrayList<>();
            for (final String classPathElem : classPathElems) {
                log.info(String.format("Adding classpath elemement '%s'...",
                                       classPathElem));
                classPathUrls.add(classPathElemToUrl(classPathElem));
            }

            log.info("Classpath URLs:");
            for (final URL url : classPathUrls) {
                log.info(String.format("\t%s", url.toString()));
            }

            //Here we have to do some classloader magic to ensure that the 
            //Reflections instance uses the correct class loader. Which is the 
            //one which has access to the compiled classes
            final ClassLoader classLoader = AccessController
                .doPrivileged(new ClassLoaderCreator(classPathUrls));

            reflections = new Reflections(
                ClasspathHelper.forPackage(packageName, classLoader));

        }

        return new EntityFinder(reflections);
    }

    /**
     * Finds all entity classes in the package for which the instance of this
     * class was created.. The entity classes must be annotated with the
     * {@link Entity} annotation. The method uses the
     * <a href="https://code.google.com/p/reflections/">Reflections library</a>
     * for finding the entity classes.
     *
     * @param packageName   The packages in which the entities are found.
     * @param entityClasses A set in which the entity classes are stored.
     *
     * @throws MojoFailureException if something goes wrong in the method.
     */
    public Set<Class<?>> findEntities() {
        final Set<Class<?>> entityClasses = new HashSet<>();

        final Set<Class<?>> classesWithEntity = reflections
            .getTypesAnnotatedWith(Entity.class);
        for (final Class<?> entityClass : classesWithEntity) {
            entityClasses.add(entityClass);
        }

        return entityClasses;
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
                    classPathElem),
                ex);
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
                classPathUrls.toArray(new URL[classPathUrls.size()]),
                Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);

            return classLoader;
        }

    }

}
