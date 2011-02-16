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

import org.jenkinsci.plugins.darcs.browsers.DarcsRepositoryBrowser;
import org.jenkinsci.plugins.darcs.browsers.DarcsWeb;
import hudson.util.IOUtils;

import junit.framework.TestCase;
import org.junit.Ignore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
        
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import org.mockito.Mockito;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class BrowserChooserTest extends TestCase {
    private final Stapler stapler = Mockito.mock(Stapler.class);
    private final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
    @SuppressWarnings("unchecked")
    private final StaplerRequest staplerRequest = new RequestImpl(stapler,
                                                                  servletRequest,
                                                                  Collections.EMPTY_LIST,
                                                                  null);

    @Ignore("not ready yet")
    public void testDarcsWeb() throws IOException {
        testExistingBrowser(DarcsWeb.class);
    }

    /**
     * @param browserClass
     * @throws IOException
     */
    void testExistingBrowser(final Class<? extends DarcsRepositoryBrowser> browserClass) throws IOException {
//        final JSONObject json = readJson(browserClass);
//        assertSame(browserClass, createBrowserFromJson(json).getClass());
    }

    /**
     * Reads the request data from file scm.json and replaces the invalid browser class in the JSONObject with the class
     * specified as parameter.
     *
     * @param browserClass
     * @return
     * @throws IOException
     */
    JSONObject readJson(Class<? extends DarcsRepositoryBrowser> browserClass) throws IOException {
        final JSONObject json = readJson();
        json.getJSONObject("browser")
            .element("stapler-class", browserClass.getName());

        return json;
    }

    /**
     * Reads the request data from file scm.json.
     *
     * @return
     * @throws IOException
     */
    JSONObject readJson() throws IOException {
        final InputStream stream = this.getClass().getResourceAsStream("scm.json");
        final String scmString;

        try {
            scmString = IOUtils.toString(stream);
        } finally {
            stream.close();
        }

        final JSONObject json = (JSONObject) JSONSerializer.toJSON(scmString);

        return json;
    }

    /**
     * @param json
     * @return
     */
    DarcsRepositoryBrowser createBrowserFromJson(final JSONObject json) {
        DarcsRepositoryBrowser browser = staplerRequest.bindJSON(DarcsRepositoryBrowser.class,
                                                                 json.getJSONObject("browser"));

        return browser;
    }
}
