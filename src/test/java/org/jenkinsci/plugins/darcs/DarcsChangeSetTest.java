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
import junit.framework.TestCase;
import org.junit.Ignore;
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
        List<String> p = sut.getAffectedPaths();
        assertTrue(p instanceof ArrayList);
        assertEquals(9, p.size());
        assertEquals("/foo/1", p.get(0));
        assertEquals("/foo/2", p.get(1));
        assertEquals("/foo/3", p.get(2));
        assertEquals("/bar/1", p.get(3));
        assertEquals("/bar/2", p.get(4));
        assertEquals("/bar/3", p.get(5));
        assertEquals("/baz/1", p.get(6));
        assertEquals("/baz/2", p.get(7));
        assertEquals("/baz/3", p.get(8));
    }

    @Ignore("not ready yet")
    public void testGetPaths() {
        DarcsChangeSet sut = createSut();
        List<String> add = sut.getPaths(EditType.ADD);
        assertTrue(add instanceof ArrayList);
        assertEquals(3, add.size());
        assertEquals("/foo/1", add.get(0));
        assertEquals("/foo/2", add.get(1));
        assertEquals("/foo/3", add.get(2));

        List<String> del = sut.getPaths(EditType.DELETE);
        assertTrue(del instanceof ArrayList);
        assertEquals(3, del.size());
        assertEquals("/bar/1", del.get(0));
        assertEquals("/bar/2", del.get(1));
        assertEquals("/bar/3", del.get(2));

        List<String> mod = sut.getPaths(EditType.EDIT);
        assertTrue(del instanceof ArrayList);
        assertEquals(3, del.size());
        assertEquals("/baz/1", mod.get(0));
        assertEquals("/baz/2", mod.get(1));
        assertEquals("/baz/3", mod.get(2));
    }
}
