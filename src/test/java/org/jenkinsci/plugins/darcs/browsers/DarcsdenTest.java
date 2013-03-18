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
import org.junit.Before;
import static org.hamcrest.Matchers.*;

/**
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsdenTest {

    private static final String URL = "http://darcsden.com/Weltraumschaf/test";
    private Darcsden sut;

    @Before
    public void createSut() throws MalformedURLException {
        sut = new Darcsden(new URL(URL));
    }

    @Test
    public void testGetChangeSetLink() throws MalformedURLException {
        final DarcsChangeSet cs = new DarcsChangeSet();
        cs.setHash("20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz");
        assertThat(sut.getChangeSetLink(cs).toString(), is(URL + "/patch/20110214201356-7677a"));
    }

    @Test
    public void testGetFileDiffLink() throws MalformedURLException {
        final DarcsChangeSet cs = new DarcsChangeSet();
        cs.setHash("20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz");
        assertThat(sut.getFileDiffLink(cs, "Foo.java").toString(), is(URL + "/patch/20110214201356-7677a#Foo.java"));
    }
}
