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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 *
 * @author <a href="mailto:jens.pelzetter@googlemail.com">Jens Pelzetter</a>
 */
@SuppressWarnings("PMD.LongVariable")
class OutputFileWriter {

    private File outputDirectory;

    private String outputFileNamePrefix;

    private String outputFileNameSuffix;

    private boolean omitDialectFromFileName;

    public OutputFileWriter() {
        super();
    }
    
    public OutputFileWriter(final File outputDirectory) {
        super();
        this.outputDirectory = outputDirectory;
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

    protected void writeOutputFile(final String dialectClassName,
                                   final Path tmpDir)
        throws MojoFailureException {

        createOutputDir();

        final Path outputFilePath = createOutputFilePath(dialectClassName);
        final Path tmpFilePath = Paths.get(String.format(
            "%s/%s.sql",
            tmpDir.toString(),
            getDialectNameFromClassName(dialectClassName)));

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

    /**
     * Create method for creating the output file path.
     *
     * @param dialectClassName The dialect of the output file.
     *
     * @return The {@link Path} for the output file.
     */
    private Path createOutputFilePath(final String dialectClassName) {

        final String dirPath;
        if (outputDirectory.getAbsolutePath().endsWith("/")) {
            dirPath = outputDirectory
                .getAbsolutePath()
                .substring(0, outputDirectory.getAbsolutePath().length());
        } else {
            dirPath = outputDirectory.getAbsolutePath();
        }

        final StringBuffer fileNameBuffer = new StringBuffer();

        fileNameBuffer.append(outputFileNamePrefix);

        if (!omitDialectFromFileName
                || isFileNamePrefixEmpty() && isFileNameSuffixEmpty()) {
            fileNameBuffer.append(getDialectNameFromClassName(dialectClassName));
        }

        fileNameBuffer.append(outputFileNameSuffix);

        return Paths.get(String.format(
            "%s/%s.sql", dirPath, fileNameBuffer.toString()));
    }

    private String getDialectNameFromClassName(final String dialectClassName) {

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

    private boolean isFileNamePrefixEmpty() {
        return outputFileNamePrefix == null
                   || isBlank(outputFileNamePrefix);
    }

    private boolean isFileNameSuffixEmpty() {
        return outputFileNameSuffix == null
                   || isBlank(outputFileNameSuffix);
    }

    private boolean isBlank(final String str) {

        if (str.isEmpty()) {
            return true;
        }

        final char[] characters = str.toCharArray();

        for (final char character : characters) {
            if (!Character.isWhitespace(character)) {
                return false;
            }
        }

        return true;
    }

}
