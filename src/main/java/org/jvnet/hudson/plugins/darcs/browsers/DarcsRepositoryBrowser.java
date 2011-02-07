/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */

package org.jvnet.hudson.plugins.darcs.browsers;

import org.jvnet.hudson.plugins.darcs.DarcsChangeSet;

import hudson.scm.RepositoryBrowser;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public abstract class DarcsRepositoryBrowser extends RepositoryBrowser<DarcsChangeSet> {
    private static final long serialVersionUID = 1L;
}
