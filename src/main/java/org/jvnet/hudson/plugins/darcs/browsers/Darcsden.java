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
import java.util.regex.Pattern;
import javax.servlet.ServletException;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.scm.RepositoryBrowser;
import hudson.util.FormValidation;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class Darcsden extends DarcsRepositoryBrowser {
    public final URL url;

    @DataBoundConstructor
    public Darcsden(URL url) {
        this.url = normalizeToEndWithSlash(url);
    }
    
    public URL getChangeSetLink(DarcsChangeSet e) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public URL getFileLink(DarcsChangeSet.Path file) throws IOException {
        return new URL("");
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        @Override
        public String getDisplayName() {
            return "Darcsden";
        }

        public FormValidation doCheck(@QueryParameter final String value) throws IOException, ServletException {
            return new FormValidation.URLCheck() {
                @Override
                protected FormValidation check() throws IOException, ServletException {
//                    String url = Util.fixEmpty(value);
//
//                    if (url == null) {
//                        return FormValidation.ok();
//                    }
//
//                    if (!url.endsWith("/")) {
//                        url += '/';
//                    }
//
//                    if (!URL_PATTERN.matcher(url).matches()) {
//                        return FormValidation.errorWithMarkup("The URL should end like <tt>.../browse/foobar/</tt>");
//                    }
//
//                    try {
//                        if (!findText(open(new URL(url)), "FishEye")) {
//                            return FormValidation.error("This is a valid URL but it doesn't look like FishEye");
//                        }
//                    } catch (IOException e) {
//                        handleIOException(url, e);
//                    }

                    return FormValidation.ok();
                }
            }.check();
        }

        @Override
        public Darcsden newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindParameters(Darcsden.class, "darcsden.darcs.");
        }

        private static final Pattern URL_PATTERN = Pattern.compile("http://darcsden.com/");
    }

    private static final long serialVersionUID = 1L;
}
