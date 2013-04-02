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

/**
 * Abstracts Darcs version.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public enum DarcsVersion {

    /**
     * Version 2.5.
     */
    V2_5("2.5", "2.5 (release)\n", "exact_version_2.5"),
    /**
     * Version 2.8.
     */
    V2_8("2.8", "2.8.0 (release)\n", "exact_version_2.8");

    /**
     * Short version with major and minor.
     */
    private final String shortVersion;
    /**
     * Version string captured from `darcs --version`.
     */
    private final String normalVersion;
    /**
     * File base name with exact version string captured from `darcs --exact-version`.
     */
    private final String exactVersionFile;

    /**
     * Default constructor.
     *
     * @param shortVersion short version with major and minor
     * @param normalVersion version string captured from `darcs --version`.
     * @param exactVersionFile file base name with exact version string captured from `darcs --exact-version`
     */
    private DarcsVersion(final String shortVersion, final String normalVersion, final String exactVersionFile) {
        this.shortVersion = shortVersion;
        this.normalVersion = normalVersion;
        this.exactVersionFile = exactVersionFile;
    }

    /**
     * Get short version with major and minor.
     *
     * @return short version string
     */
    public String getShortVersion() {
        return shortVersion;
    }

    /**
     * Get version string captured from `darcs --version`.
     *
     * @return version string
     */
    public String getNormalVersion() {
        return normalVersion;
    }

    /**
     * Get file base name with exact version string captured from `darcs --exact-version`.
     *
     * @return file name
     */
    public String getExactVersionFile() {
        return exactVersionFile;
    }

    @Override
    public String toString() {
        return getShortVersion();
    }

}
