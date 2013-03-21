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
 * Repository browser for <a href="http://darcsden.com/">Darcsden</a>.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public final class Darcsden extends DarcsRepositoryBrowser {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Base URL of the DarcsWeb.
     */
    private final URL url;

    /**
     * Dedicated constructor.
     *
     * @param url base URL to the Darcsden repository
     * @throws MalformedURLException if malformed URL will result
     */
    @DataBoundConstructor
    public Darcsden(final URL url) throws MalformedURLException {
        super();
        this.url = new URL(Util.removeTrailingSlash(url.toString()));
    }

    /**
     * Get the change set URI.
     *
     * @param changeSet changes to get link for
     * @return URL to the commit view
     * @throws MalformedURLException if malformed URL will result
     */
    public URL getChangeSetLink(final DarcsChangeSet changeSet) throws MalformedURLException  {
        final String hash = changeSet.getHash();
        final String shortHash = hash.substring(0, hash.lastIndexOf('-'));
        final DarcsQueryBuilder query = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.SLASHES);
        query.add("patch")
             .add(shortHash);

        return new URL(url + query.toString());
    }

    @Override
    public URL getFileDiffLink(final DarcsChangeSet changeSet, final String file) throws MalformedURLException {
        final URL changestUrl = getChangeSetLink(changeSet);
        return new URL(changestUrl.toString() + "#" + file);
    }

    /**
     * Darcsden repository browser description.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        /**
         * Pattern to verify a Darcsden URL.
         */
        private static final Pattern URI_PATTERN = Pattern.compile("http://darcsden.com/.+");

        @Override
        public String getDisplayName() {
            return "Darcsden";
        }

        /**
         * Validates the URL given in the configuration form.
         *
         * TODO implement check.
         *
         * @param value the given URL
         * @return a form validation instance
         * @throws IOException if URL can't be opened
         * @throws ServletException if servlet encounters difficulty
         */
        public FormValidation doCheck(@QueryParameter final String value) throws IOException, ServletException {

            return new FormValidation.URLCheck() {

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
            }.check();
        }

        @Override
        public DarcsWeb newInstance(final StaplerRequest req, final JSONObject formData)
            throws Descriptor.FormException {
            return req.bindParameters(DarcsWeb.class, "darcsden.darcs.");
        }
    }

}
