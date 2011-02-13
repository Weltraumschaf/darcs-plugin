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

import junit.framework.TestCase;
import org.junit.Ignore;
import java.net.URL;
import java.net.MalformedURLException;


/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsWebTest extends TestCase {
    public class ExposingDarcsWeb extends DarcsWeb {
        public ExposingDarcsWeb() throws MalformedURLException {
            super(new URL("http://www.foobar.com/"), "arepo");
        }

        public QueryBuilder exposedCreateDefaultQuery() {
            return createDefaultQuery();
        }

        public QueryBuilder exposedCreateDefaultQuery(String action) {
            return createDefaultQuery(action);
        }
    }

    public DarcsWebTest(String testName) {
        super(testName);
    }

    public void testCreateDefaultQuery() {
        try {
            ExposingDarcsWeb sut = new ExposingDarcsWeb();
            assertEquals("Query", 
                         "?r=" + sut.repo,
                         sut.exposedCreateDefaultQuery().toString());
        } catch (MalformedURLException e) {
            fail("Can not create SUT!");
        }
    }

    public void testCreateDefaultQueryWithAction() {
        try {
            ExposingDarcsWeb sut = new ExposingDarcsWeb();
            assertEquals("Query",
                         "?r=" + sut.repo + ";a=foobar",
                         sut.exposedCreateDefaultQuery("foobar").toString());
        } catch (MalformedURLException e) {
            fail("Can not create SUT!");
        }
    }

    @Ignore("not ready yet")
    public void testGetChangeSetLink() {
    }

    @Ignore("not ready yet")
    public void testGetFileDiffLink() {
    }
}
