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

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.scm.RepositoryBrowser;
import hudson.util.FormValidation;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public final class Darcsden extends DarcsRepositoryBrowser {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    public final URL url;

    @DataBoundConstructor
    public Darcsden(final URL url) throws MalformedURLException {
        this.url = new URL(Util.removeTrailingSlash(url.toString()));
    }

    public URL getChangeSetLink(final DarcsChangeSet changeSet) throws IOException {
        final String hash = changeSet.getHash();
        final String shortHash = hash.substring(0, hash.lastIndexOf('-'));
        final DarcsQueryBuilder query = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.SLASHES);
        query.add("patch")
             .add(shortHash);

        return new URL(url + query.toString());
    }

    public URL getFileDiffLink(final DarcsChangeSet changeSet, final String file) throws IOException {
        return null;
    }

    /**
     * Darcsden repository browser description.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        private static final Pattern URI_PATTERN = Pattern.compile("http://darcsden.com/.+");

        public String getDisplayName() {
            return "Darcsden";
        }

        /**
         * Validates the URL given in the config formular.
         *
         * @todo implement check.
         *
         * @param value
         * @return
         * @throws IOException
         * @throws ServletException
         */
        public FormValidation doCheck(@QueryParameter final String value) throws IOException, ServletException {

            return (new FormValidation.URLCheck() {

                @Override
                protected FormValidation check() throws IOException, ServletException {
                    final String uri = Util.fixEmpty(value);

                    if (null == uri) { // nothing entered yet
                        return FormValidation.ok();
                    }

                    if (!URI_PATTERN.matcher(uri).matches()) {
                        return FormValidation.errorWithMarkup("The URI should look like "
                                + "<tt>http://darcsden.com/...</tt>!");
                    }

                    return FormValidation.ok();
                }
            }).check();
        }

        @Override
        public DarcsWeb newInstance(final StaplerRequest req, final JSONObject formData) throws Descriptor.FormException {
            return req.bindParameters(DarcsWeb.class, "darcsden.darcs.");
        }
    }

}
