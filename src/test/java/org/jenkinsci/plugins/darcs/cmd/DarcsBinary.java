/*
 *  LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 43):
 * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a non alcohol-free beer in return.
 *
 * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
 */
package org.jenkinsci.plugins.darcs.cmd;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;

/**
 * Abstracts Darcs binary as fixture for various platforms.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public enum DarcsBinary {

    /**
     * Binary for Mac OS X in version 2.5.
     */
    MACOS_BIN("darcs_bin_macos_2.5", DarcsVersion.V2_5),
    /**
     * Binary for GNU/Linux in version 2.8.
     */
    LINUX_BIN("darcs_bin_linux_2.8", DarcsVersion.V2_8);
    /**
     * Base directory for fixture files.
     */
    private static final String FIXTURE_BASE = "/org/jenkinsci/plugins/darcs/cmd";
    /**
     * Binary file name.
     */
    private final String filename;
    /**
     * Version of Darcs.
     */
    private final DarcsVersion version;

    /**
     * Default constructor.
     *
     * @param filename base name of Darcs binary
     * @param version version of Darcs
     */
    private DarcsBinary(final String filename, final DarcsVersion version) {
        this.filename = filename;
        this.version = version;
    }

    /**
     * Reads file into a string.
     *
     * @param file to read
     * @return file content
     * @throws URISyntaxException if resource URI is malformed
     * @throws IOException if I/O error happened
     */
    private String readResource(final String file) throws URISyntaxException, IOException {
        return FileUtils.readFileToString(new File(getClass().getResource(file).toURI()), "utf-8");
    }

    /**
     * Get the binary file.
     *
     * @return always new instance
     * @throws URISyntaxException if resource URI is malformed
     */
    public File getBin() throws URISyntaxException {
        final File bin = new File(getClass().getResource(FIXTURE_BASE + "/" + filename).toURI());
        bin.setExecutable(true);
        return bin;
    }

    /**
     * Get the Darcs version.
     *
     * @return the version enum
     */
    public DarcsVersion getVersion() {
        return version;
    }

    /**
     * Return the exact version string.
     *
     * TODO Move into {@link DarcsVersion}.
     *
     * @return exact version string
     * @throws URISyntaxException if resource URI is malformed
     * @throws IOException if I/O error happened
     */
    public String getExactVersion() throws URISyntaxException, IOException {
        return readResource(FIXTURE_BASE + "/" + version.getExactVersionFile());
    }

    /**
     * Determine appropriate binary for the platform OS.
     *
     * @return a binary enum
     * CHECKSTYLE:OFF
     * @throws IllegalArgumentException if platform OS is not supported
     * CHECKSTYLE:ON
     */
    public static DarcsBinary determine() {
        final String os = System.getProperty("os.name", "unknown").toLowerCase();

        if (os.indexOf("linux") >= 0) {
            return LINUX_BIN;
        } else if (os.indexOf("mac os x") >= 0) {
            return MACOS_BIN;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported os '%s'!", os));
        }
    }
}
