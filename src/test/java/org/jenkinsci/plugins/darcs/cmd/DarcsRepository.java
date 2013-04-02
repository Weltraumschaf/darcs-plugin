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
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import static org.jenkinsci.plugins.darcs.cmd.DarcsCommandFacadeTest.readResource;
import static org.mockito.Mockito.mock;

/**
 * Abstracts various Darcs repositories as fixture.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public enum DarcsRepository {

    /**
     * Empty repository.
     */
    EMPTY("empty_repo"),
    /**
     * Repository with mixed character encodings in commit messages.
     */
    DIRTY_REPO("dirty_repo"),
    /**
     * Normal repository.
     */
    REPO("repo");

    /**
     * Base directory for fixture files.
     */
    private static final String FIXTURE_BASE = "/org/jenkinsci/plugins/darcs/cmd";
    /**
     * File name format for associated change log XML files.
     *
     * Format is FIXTURE_BASE/file_reponame_version.xml
     */
    private static final String FIXTURE_FORMAT = "%s/%s_%s_%s.xml";
    /**
     * Base file name for last summarized changes.
     */
    private static final String LAST_SUMMARIZED_CHANGES = "last_summarized_changes";
    /**
     * Base file name for all summarized changes.
     */
    private static final String ALL_SUMMARIZED_CHANGES = "all_summarized_changes";
    /**
     * Base file name for all changes.
     */
    private static final String ALL_CHANGES = "all_changes";
    /**
     * Base name of repository.
     */
    private final String reponame;

    /**
     * Default constructor.
     *
     * @param reponame base name of repository
     */
    private DarcsRepository(final String reponame) {
        this.reponame = reponame;
    }

    /**
     * Generates the repositories fixture file.
     *
     * @return file object referencing the tarred and gzipped repository
     * @throws URISyntaxException if file name is not a valid URI
     */
    public File getArchive() throws URISyntaxException {
        return new File(getClass().getResource(FIXTURE_BASE + "/" + reponame + ".tar.gz").toURI());
    }

    /**
     * Get the repositories base name.
     *
     * @return string w/o path directories and file extension
     */
    public String getReponame() {
        return reponame;
    }

    /**
     * Extracts the repository into the given destination.
     *
     * @param destination where to extract
     * @return object referencing the extracted repository
     * @throws URISyntaxException if {@link #getArchive()} file is not a valid URI
     */
    public File extractTo(final File destination) throws URISyntaxException {
        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(mock(Logger.class));
        ua.setSourceFile(getArchive());
        ua.setDestDirectory(destination);
        ua.extract();
        return new File(destination, getReponame());
    }

    /**
     * Generate the XML fixture file name according to the repository.
     *
     * @param file file base name, one of: {@link #ALL_CHANGES}, {@link #ALL_SUMMARIZED_CHANGES},
     *        {@link #LAST_SUMMARIZED_CHANGES}
     * @param version used Darcs version to check against.
     * @return file name
     */
    private String generateXmlFixtureFileName(final String file, final DarcsVersion version) {
        return String.format(FIXTURE_FORMAT, FIXTURE_BASE, file, reponame, version);
    }

    /**
     * Get the repositories last summarized change log XML.
     *
     * @param version used Darcs version to check against.
     * @return XML string
     * @throws URISyntaxException if resource URI is malformed
     * @throws IOException if I/O error happened
     */
    public String lastSummarizedChanges(final DarcsVersion version) throws URISyntaxException, IOException {
        return readResource(generateXmlFixtureFileName(LAST_SUMMARIZED_CHANGES, version));
    }

    /**
     * Get the repositories all summarized change log XML.
     *
     * @param version used Darcs version to check against.
     * @return XML string
     * @throws URISyntaxException if resource URI is malformed
     * @throws IOException if I/O error happened
     */
    public String allSummarizedChanges(final DarcsVersion version) throws URISyntaxException, IOException {
        return readResource(generateXmlFixtureFileName(ALL_SUMMARIZED_CHANGES, version));
    }

    /**
     * Get the repositories all change log XML.
     *
     * @param version used Darcs version to check against.
     * @return XML string
     * @throws URISyntaxException if resource URI is malformed
     * @throws IOException if I/O error happened
     */
    public String allChanges(final DarcsVersion version) throws URISyntaxException, IOException {
        return readResource(generateXmlFixtureFileName(ALL_CHANGES, version));
    }

}
