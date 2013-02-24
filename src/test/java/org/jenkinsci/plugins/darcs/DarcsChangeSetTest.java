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
import org.junit.Ignore;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSetTest {

    private static DarcsChangeSet createSut() {
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

    @Test
    public void testGetAffectedPaths() {
        final DarcsChangeSet sut = createSut();
        final List<String> p = sut.getAffectedPaths();
        assertThat(p.size(), is(9));
        assertThat(p.get(0), is("/foo/1"));
        assertThat(p.get(1), is("/foo/2"));
        assertThat(p.get(2), is("/foo/3"));
        assertThat(p.get(3), is("/bar/1"));
        assertThat(p.get(4), is("/bar/2"));
        assertThat(p.get(5), is("/bar/3"));
        assertThat(p.get(6), is("/baz/1"));
        assertThat(p.get(7), is("/baz/2"));
        assertThat(p.get(8), is("/baz/3"));
    }

    @Test
    public void testGetPaths() {
        final DarcsChangeSet sut = createSut();

        final List<String> add = sut.getPaths(EditType.ADD);
        assertThat(add.size(), is(3));
        assertThat(add.get(0), is("/foo/1"));
        assertThat(add.get(1), is("/foo/2"));
        assertThat(add.get(2), is("/foo/3"));

        final List<String> del = sut.getPaths(EditType.DELETE);
        assertThat(del.size(), is(3));
        assertThat(del.get(0), is("/bar/1"));
        assertThat(del.get(1), is("/bar/2"));
        assertThat(del.get(2), is("/bar/3"));

        final List<String> mod = sut.getPaths(EditType.EDIT);
        assertThat(del.size(), is(3));
        assertThat(mod.get(0), is("/baz/1"));
        assertThat(mod.get(1), is("/baz/2"));
        assertThat(mod.get(2), is("/baz/3"));
    }

}
