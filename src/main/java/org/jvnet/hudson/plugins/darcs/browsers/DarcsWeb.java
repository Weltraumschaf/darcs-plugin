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
import java.util.regex.Pattern;
import javax.servlet.ServletException;

import hudson.Extension;
//import hudson.Util;
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

    @Extension
    public static class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        private static final Pattern URL_PATTERN = Pattern.compile(".+/cgi-bin/darcsweb.cgi");
        
        public String getDisplayName() {
            return "Darcsweb";
        }

        public FormValidation doCheck(@QueryParameter final String value) throws IOException, ServletException {
            return new FormValidation.URLCheck() {
                @Override
                protected FormValidation check() throws IOException, ServletException {

                    return FormValidation.ok();
                }
            }.check();
        }

        @Override
        public DarcsWeb newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return req.bindParameters(DarcsWeb.class, "darcsweb.darcs.");
        }
    }

    private static final long serialVersionUID = 1L;
    public final URL url;
    public final String repo;

    @DataBoundConstructor
    public DarcsWeb(URL url, String repo) throws MalformedURLException {
        this.url  = normalizeToEndWithSlash(url);
        this.repo = repo;
    }

    public URL getChangeSetLink(DarcsChangeSet changeSet) throws IOException {
//        return new URL(String.format("%s?r=%s;a=annotate_shade;h=%",
//                                     getBaseUrlString(), this.repo, changeSet.getHash()));
        return new URL("changesetlink");
    }

    public URL getFileLink(DarcsChangeSet.Path path) throws IOException {
        return new URL("filelink");
    }
}
