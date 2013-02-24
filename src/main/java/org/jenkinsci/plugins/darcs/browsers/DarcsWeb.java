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
public class DarcsWeb extends DarcsRepositoryBrowser {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    public final URL url;
    public final String repo;

    @DataBoundConstructor
    public DarcsWeb(final URL url, final String repo) throws MalformedURLException {
        this.url  = url;
        this.repo = repo;
    }

    protected DarcsQueryBuilder createDefaultQuery() {
        final DarcsQueryBuilder query = new DarcsQueryBuilder(DarcsQueryBuilder.SeparatorType.SEMICOLONS);
        return query.add("r=" + repo);
    }

    protected DarcsQueryBuilder createDefaultQuery(final String action) {
        final DarcsQueryBuilder query = createDefaultQuery();
        return query.add("a=" + action);
    }

    /**
     * Get the change set URI.
     *
     * Format: {@literal http://localhost/cgi-bin/darcsweb.cgi?r=REPO;a=commit;h=HASH}
     *
     * @param changeSet
     * @return
     * @throws IOException
     */
    public URL getChangeSetLink(final DarcsChangeSet changeSet) throws IOException {
        final DarcsQueryBuilder query = createDefaultQuery("commit");
        query.add("h=" + changeSet.getHash());

        return new URL(url + query.toString());
    }

    /**
     * Get file difference link.
     *
     * Format: {@literal http://localhost/cgi-bin/darcsweb.cgi?r=REPO;a=filediff;h=HASH;f=FILE}
     *
     * @param changeSet
     * @param file
     * @return
     * @throws IOException
     */
    public URL getFileDiffLink(final DarcsChangeSet changeSet, final String file) throws IOException {
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

        private static final Pattern URI_PATTERN = Pattern.compile(".+/cgi-bin/darcsweb.cgi");

        public String getDisplayName() {
            return "Darcsweb";
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

                    if (!uri.startsWith("http://") && ! uri.startsWith("https://")) {
                        return FormValidation.errorWithMarkup("The URI should start either with <tt>http://</tt> or "
                                + "<tt>https://</tt>!");
                    }

                    if (!URI_PATTERN.matcher(uri).matches()) {
                        return FormValidation.errorWithMarkup("The URI should look like <tt>"
                                + "http://.../cgi-bin/darcsweb.cgi</tt>!");
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
            }).check();
        }

        @Override
        public DarcsWeb newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindParameters(DarcsWeb.class, "darcsweb.darcs.");
        }
    }

}
