/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs;

import hudson.scm.EditType;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Ignore;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSetTest extends TestCase {

    public DarcsChangeSetTest(String testName) {
        super(testName);
    }

    public static DarcsChangeSet createSut() {
        DarcsChangeSet sut = new DarcsChangeSet();
        
        sut.getAddedPaths().add("/foo/1");
        sut.getAddedPaths().add("/foo/2");
        sut.getAddedPaths().add("/foo/3");
        sut.getDeletedPaths().add("/bar/1");
        sut.getDeletedPaths().add("/bar/2");
        sut.getDeletedPaths().add("/bar/3");
        sut.getModifiedPaths().add("/baz/1");
        sut.getModifiedPaths().add("/baz/2");
        sut.getModifiedPaths().add("/baz/3");
        
        return sut;
    }

    @Ignore("not ready yet")
    public void testGetAffectedPaths() {
        DarcsChangeSet sut = createSut();
        Collection<String> p = sut.getAffectedPaths();
        assertTrue(p instanceof ArrayList);
        assertEquals(9, p.size());
    }

    @Ignore("not ready yet")
    public void testGetPaths() {
        DarcsChangeSet sut = createSut();
        List<String> add = sut.getPaths(EditType.ADD);
        assertTrue(add instanceof ArrayList);
        assertEquals(3, add.size());

        List<String> del = sut.getPaths(EditType.DELETE);
        assertTrue(del instanceof ArrayList);
        assertEquals(3, del.size());

        List<String> mod = sut.getPaths(EditType.EDIT);
        assertTrue(del instanceof ArrayList);
        assertEquals(3, del.size());
    }
}
