/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs.browsers;

import org.jenkinsci.plugins.darcs.DarcsChangeSet;
import java.net.URL;
import hudson.scm.RepositoryBrowser;
import java.net.MalformedURLException;

/**
 * Abstract implementation for Darcs repository browsers.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public abstract class DarcsRepositoryBrowser extends RepositoryBrowser<DarcsChangeSet> {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Determines the link to a single file under Darcs.
     *
     * This page should display all the past revisions of this file, etc.
     *
     * @param changeSet changes to get diff link for
     * @param file file to get diff link for
     * @return null if the browser doesn't have any suitable URL
     * @throws MalformedURLException if malformed URL will result
     */
    public abstract URL getFileDiffLink(DarcsChangeSet changeSet, String file) throws MalformedURLException;

}
