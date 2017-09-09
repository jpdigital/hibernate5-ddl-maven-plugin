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

import java.util.ServiceLoader;
import java.util.Set;

/**
 * Interface for the {@code DdlGenerator} which generates the SQL DDL files.
 *
 * The {@link GenerateDdlMojo} will use the {@link ServiceLoader} from the Java
 * Standard API to find the implementation to use. Therefore an implementation
 * of this interface must be accompanied by a file called
 * {@code de.jpdigital.maven.plugins.hibernate5ddl.DdlGenerator} in the
 * {@code META-INF/services} directory.
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
public interface DdlGenerator {

    /**
     * Generates a SQL DDL file for a specific SQL dialect.
     *
     * @param dialect       The SQL dialect to use.
     * @param entityClasses The entity classes for which SQL DDL statements will
     *                      be created.
     * @param mojo          The {@link GenerateDdlMojo} which calls the method.
     *
     * @throws MojoFailureException If an error occurs while creating the DDL
     *                              file.
     *
     * @see Dialect
     */
    void generateDdl(Dialect dialect,
                     Set<Class<?>> entityClasses,
                     GenerateDdlMojo mojo)
        throws MojoFailureException;

    /**
     * Generates a SQL DDL file for a specific SQL dialect. This method accepts
     * a string a is called by {@link GenerateDdlMojo} for processing custom
     * dialects.
     *
     * @param dialect       The SQL dialect to use.
     * @param entityClasses The entity classes for which SQL DDL statements will
     *                      be created.
     * @param mojo          The {@link GenerateDdlMojo} which calls the method.
     *
     * @throws MojoFailureException If an error occurs while creating the DDL
     *                              file.
     *
     */
    void generateDdl(String dialect,
                     Set<Class<?>> entityClasses,
                     GenerateDdlMojo mojo)
        throws MojoFailureException;

}
