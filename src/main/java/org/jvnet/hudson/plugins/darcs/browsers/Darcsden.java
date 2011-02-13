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

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.RepositoryBrowser;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public final class Darcsden extends DarcsRepositoryBrowser {
    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public String getDisplayName() {
            return "Darcsden";
        }
    }


    private static final long serialVersionUID = 1L;

    public final URL url;
    public final String repo;
    public final String username;

    @DataBoundConstructor
    public Darcsden(URL url, String repo, String username) throws MalformedURLException {
        this.url      = normalizeToEndWithSlash(url);
        this.repo     = repo;
        this.username = username;
    }

    public URL getChangeSetLink(DarcsChangeSet changeSet) throws IOException {
        QueryBuilder query = new QueryBuilder(QueryBuilder.SeparatorType.SEMICOLONS);
        query.add(repo)
             .add(username);

        return new URL(url + query.toString());
    }


    public URL getFileDiffLink(DarcsChangeSet changeSet, String file) throws IOException {
        return null;
    }
}
