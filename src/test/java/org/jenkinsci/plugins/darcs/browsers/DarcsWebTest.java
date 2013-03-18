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
import java.net.URL;
import java.net.MalformedURLException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;

/**
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsWebTest {

    private static final String URL = "http://www.foobar.com/";
    private static final String REPO = "arepo";
    private DarcsWeb sut;

    @Before
    public void createSut() throws MalformedURLException {
        sut = new DarcsWeb(new URL(URL), REPO);
    }

    @Test
    public void createDefaultQuery() throws MalformedURLException {
        assertThat(sut.createDefaultQuery().toString(), is("?r=" + REPO));

    }

    @Test
    public void createDefaultQueryWithAction() throws MalformedURLException {
        assertThat(sut.createDefaultQuery("foobar").toString(), is("?r=" + REPO + ";a=foobar"));

    }

    @Test
    public void getChangeSetLink() throws MalformedURLException {
        final DarcsChangeSet cs = new DarcsChangeSet();
        cs.setHash("1234-the-commit-hash.gz");
        assertThat(sut.getChangeSetLink(cs).toString(), is(URL + "?r=" + REPO + ";a=commit;h=" + cs.getHash()));

    }

    @Test
    public void getFileDiffLink() throws MalformedURLException {
        final String file = "a/file/name";
        final DarcsChangeSet cs = new DarcsChangeSet();
        cs.setHash("1234-the-commit-hash.gz");
        assertThat(sut.getFileDiffLink(cs, file).toString(),
                   is(URL + "?r=" + REPO + ";a=filediff;h=" + cs.getHash() + ";f=" + file));

    }
}
