/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs.browsers;

import org.jvnet.hudson.plugins.darcs.DarcsChangeSet;

import junit.framework.TestCase;
import org.junit.Ignore;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *
 * @author sxs
 */
public class DarcsdenTest extends TestCase {

    public DarcsdenTest(String testName) {
        super(testName);
    }

    @Ignore("not ready yet")
    public void testImplementSome() {

    }

    public void testGetChangeSetLink() {
        try {
            String         hash = "20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz";
            String         url  = "http://darcsden.com/Weltraumschaf/test";
            DarcsChangeSet cs   = new DarcsChangeSet();
            Darcsden       sut  = new Darcsden(new URL(url));
            
            cs.setHash(hash);
            assertEquals("",
                         url + "/patch/20110214201356-7677a",
                         sut.getChangeSetLink(cs).toString());
        } catch (MalformedURLException e) {
            fail("Can not create SUT!");
        } catch (IOException e) {
            fail("Can not create URI!");
        }
    }

    @Ignore("not ready yet")
    public void testGetFileDiffLink() {
    }
}
