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
import org.jvnet.hudson.plugins.darcs.DarcsChangeSet.Path;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
//import java.util.ArrayList;
//import java.util.Collections;

import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.Extension;
import hudson.model.Descriptor;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsWeb extends DarcsRepositoryBrowser {
    
    private static final long serialVersionUID = 1L;
    private final URL url;
    private final String repo;

    @DataBoundConstructor
    public DarcsWeb(URL url, String repo) throws MalformedURLException {
        this.url  = normalizeToEndWithSlash(url);
        this.repo = repo;
    }
    
    public DarcsWeb(String url, String repo) throws MalformedURLException {
        this(new URL(url), repo);
    }

    public URL getUrl() {
        return url;
    }

    public String getRepo() {
        return repo;
    }

    private String getBaseUrlString() {
        return String.format("%scgi-bin/darcsweb.cgi", this.url);
    }

    @Override
    public URL getChangeSetLink(DarcsChangeSet changeSet) throws IOException {
        return new URL(String.format("%s?r=%s;a=annotate_shade;h=%",
                                     getBaseUrlString(), this.repo, changeSet.getHash()));
    }

    /**
     * Determines the link to the diff between the version
     * in the specified revision of {@link GitChangeSet.Path} to its previous version.
     *
     * @param path affected file path
     * @return
     *      null if the browser doesn't have any URL for diff.
     * @throws IOException
     */
    public URL getDiffLink(DarcsChangeSet.Path path) throws IOException {
        if (path.getEditType().equals(EditType.DELETE)) {
            return getDiffLinkRegardlessOfEditType(path);
        } else {
//            final String spec = "blob/" + path.getChangeSet().getId() + "/" + path.getPath();
//            return new URL(url, url.getPath() /*+ spec*/);
            return new URL("");
        }
    }

    /**
     * Determines the link to a single file under Git.
     * This page should display all the past revisions of this file, etc.
     *
     * @param path affected file path
     * @return
     *      null if the browser doesn't have any suitable URL.
     * @throws IOException
     */
    public URL getFileLink(DarcsChangeSet.Path path) throws IOException {
        if (path.getEditType() != EditType.EDIT) { return null; }

        if (path.getSrc() == null) { return null; }

        if (path.getDst() == null) { return null; }

        return getDiffLinkRegardlessOfEditType(path);
    }

    /**
     * Return a diff link regardless of the edit type by appending the index of the pathname in the changeset.
     *
     * @param path
     * @return
     * @throws IOException
     */
    private URL getDiffLinkRegardlessOfEditType(Path path) throws IOException {
//        final DarcsChangeSet changeSet = path.getChangeSet();
//        final ArrayList<String> affectedPaths = new ArrayList<String>(changeSet.getAffectedPaths());
//        // Github seems to sort the output alphabetically by the path.
//        Collections.sort(affectedPaths);
//        final String pathAsString = path.getPath();
//        final int i = Collections.binarySearch(affectedPaths, pathAsString);
//        assert i >= 0;
//        return new URL(getChangeSetLink(changeSet), "#diff-" + String.valueOf(i));
        return new URL("");
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public DescriptorImpl() {
            super(DarcsWeb.class);
        }
        
        @Override
        public String getDisplayName() {
            return "darcsweb";
        }
    }
}
