/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs.browser;

import org.jvnet.hudson.plugins.darcs.DarcsChangeSet;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;

import net.sf.json.JSONObject;

import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.browsers.QueryBuilder;
import hudson.Extension;
import hudson.model.Descriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author sxs
 */
public class DarcsWeb extends RepositoryBrowser<DarcsChangeSet> {
    private static final long serialVersionUID = 1L;
    private final URL url;

    @DataBoundConstructor
    public DarcsWeb(String url) throws MalformedURLException {
        this.url = new URL(url);
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public URL getChangeSetLink(DarcsChangeSet changeSet) throws IOException {
        String urlPart = param().add("a=annotate_shade;")
                                .add("r=REPONAME")
                                .add("h=" + changeSet.getHash())
                                .add("f=FILENAMS")
                                .toString();
        
        return new URL(url, url.getPath() + urlPart);
    }

    private QueryBuilder param() {
        return new QueryBuilder(url.getQuery());
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
            final String spec = "blob/" + path.getChangeSet().getId() + "/" + path.getPath();
            return new URL(url, url.getPath() + spec);
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

        if (path.getChangeSet().getParentCommit() == null) { return null; }

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
        final DarcsChangeSet changeSet = path.getChangeSet();
        final ArrayList<String> affectedPaths = new ArrayList<String>(changeSet.getAffectedPaths());
        // Github seems to sort the output alphabetically by the path.
        Collections.sort(affectedPaths);
        final String pathAsString = path.getPath();
        final int i = Collections.binarySearch(affectedPaths, pathAsString);
        assert i >= 0;
        return new URL(getChangeSetLink(changeSet), "#diff-" + String.valueOf(i));
    }

    @Extension
    public static class DarcsWebDescriptor extends Descriptor<RepositoryBrowser<?>> {
        public String getDisplayName() {
            return "darcsweb";
        }

        @Override
	public DarcsWeb newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
		return req.bindParameters(DarcsWeb.class, "darcsweb.");
	}
    }
}
