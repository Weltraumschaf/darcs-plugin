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
 * Repository browser for <a href="http://blitiri.com.ar/p/darcsweb/">DarcsWeb</a>.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public final class DarcsWeb extends DarcsRepositoryBrowser {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Base URL of the DarcsWeb.
     */
    private final URL url;
    /**
     * Name of the repository.
     */
    private final String repo;

    /**
     * Dedicated constructor.
     *
     * @param url base URL to the DarcsWeb
     * @param repo name of repository
     */
    @DataBoundConstructor
    public DarcsWeb(final URL url, final String repo) {
        super();
        this.url = url;
        this.repo = repo;
    }

    /**
     * Creates a query to get a repository.
     *
     * @return newly generated query builder
     */
    DarcsQueryBuilder createDefaultQuery() {
        final DarcsQueryBuilder query = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.SEMICOLONS);
        return query.add("r=" + repo);
    }

    /**
     * Creates a query to get a repository and with an action.
     *
     * @param action action to perform
     * @return newly generated query builder
     */
    DarcsQueryBuilder createDefaultQuery(final String action) {
        final DarcsQueryBuilder query = createDefaultQuery();
        return query.add("a=" + action);
    }

    /**
     * Get the change set URI.
     *
     * Format: {@literal http://localhost/cgi-bin/darcsweb.cgi?r=REPO;a=commit;h=HASH}
     *
     * @param changeSet changes to get link for
     * @return URL to the commit view
     * @throws MalformedURLException if malformed URL will result
     */
    public URL getChangeSetLink(final DarcsChangeSet changeSet) throws MalformedURLException {
        final DarcsQueryBuilder query = createDefaultQuery("commit");
        query.add("h=" + changeSet.getHash());
        return new URL(url + query.toString());
    }

    /**
     * Get file difference link.
     *
     * Format: {@literal http://localhost/cgi-bin/darcsweb.cgi?r=REPO;a=filediff;h=HASH;f=FILE}
     *
     * @param changeSet changes to get diff link for
     * @param file file to get diff link for
     * @return URL to th diff view
     * @throws MalformedURLException if malformed URL will result
     */
    @Override
    public URL getFileDiffLink(final DarcsChangeSet changeSet, final String file) throws MalformedURLException {
        final DarcsQueryBuilder query = createDefaultQuery("filediff");
        query.add("h=" + changeSet.getHash());
        query.add("f=" + file);
        return new URL(url + query.toString());
    }

    /**
     * DarcsWeb repository browser description.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        @Override
        public String getDisplayName() {
            return "Darcsweb";
        }

        /**
         * Validates the URL given in the configuration form.
         *
         * @param value the given URL
         * @return a form validation instance
         * @throws IOException if URL can't be opened
         * @throws ServletException if servlet encounters difficulty
         */
        public FormValidation doCheck(@QueryParameter final String value) throws IOException, ServletException {
            return new UriCheck(value).check();
        }

        @Override
        public DarcsWeb newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            return req.bindParameters(DarcsWeb.class, "darcsweb.darcs.");
        }
    }

    /**
     * Checks an URI if it is a DarcsWeb URI.
     */
    static class UriCheck extends FormValidation.URLCheck {

        /**
         * Pattern to verify a DarcsWeb base URL.
         */
        private static final Pattern URI_PATTERN = Pattern.compile(".+/cgi-bin/darcsweb.cgi");

        /**
         * Value to check if it is a DarcsWeb URI.
         */
        private final String value;

        /**
         * Default constructor.
         *
         * @param value to check if it is a DarcsWeb URI.
         */
        public UriCheck(final String value) {
            super();
            this.value = value;
        }

        @Override
        public FormValidation check() throws IOException, ServletException {
            final String uri = Util.fixEmpty(value);

            if (null == uri) { // nothing entered yet
                return FormValidation.ok();
            }

            if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
                return FormValidation.errorWithMarkup(
                    "The URI should start either with <tt>http://</tt> or <tt>https://</tt>!");
            }

            if (!URI_PATTERN.matcher(uri).matches()) {
                return FormValidation.errorWithMarkup(
                    "The URI should look like <tt>http://.../cgi-bin/darcsweb.cgi</tt>!");
            }

            try {
                if (!findText(open(new URL(uri)), "Crece desde el pueblo el futuro / crece desde el pie")) {
                    return FormValidation.error("This is a valid URI but it doesn't look like DarcsWeb!");
                }
            } catch (IOException e) {
                handleIOException(uri, e);
            }

            return FormValidation.ok();
        }
    }

}
