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

	@DataBoundConstructor
    public Darcsden(URL url) throws MalformedURLException {
        this.url = normalizeToEndWithSlash(url);
    }

    public URL getChangeSetLink(DarcsChangeSet changeSet) throws IOException {
        return new URL("");
    }


    public URL getFileLink(DarcsChangeSet.Path path) throws IOException {
		return new URL("");
    }
}
