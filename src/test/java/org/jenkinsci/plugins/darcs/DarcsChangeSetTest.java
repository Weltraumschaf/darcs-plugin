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
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSetTest {

    private static DarcsChangeSet createSutWithPaths() {
        return createSut(true);
    }

    private static DarcsChangeSet createSutWithoutPaths() {
        return createSut(false);
    }

    private static DarcsChangeSet createSut(final boolean withPaths) {
        final DarcsChangeSet sut = new DarcsChangeSet();
        sut.setAuthor("Weltraumschaf");
        sut.setComment("Test patch.");
        sut.setDate("date");
        sut.setHash("hash");
        sut.setLocalDate("localDate");
        sut.setInverted(true);
        sut.setName("a name");

        if (withPaths) {
            sut.getAddedPaths().add("/foo/1");
            sut.getAddedPaths().add("/foo/2");
            sut.getAddedPaths().add("/foo/3");
            sut.getDeletedPaths().add("/bar/1");
            sut.getDeletedPaths().add("/bar/2");
            sut.getDeletedPaths().add("/bar/3");
            sut.getModifiedPaths().add("/baz/1");
            sut.getModifiedPaths().add("/baz/2");
            sut.getModifiedPaths().add("/baz/3");
        }

        return sut;
    }

    @Test
    public void testGetAffectedPaths() {
        final DarcsChangeSet sut = createSutWithPaths();
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
        final DarcsChangeSet sut = createSutWithPaths();

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

    @Test
    public void testEquals() {
        final DarcsChangeSet sut1 = createSutWithPaths();
        final DarcsChangeSet sut2 = createSutWithPaths();
        final DarcsChangeSet sut3 = new DarcsChangeSet();

        assertThat(sut1.equals(sut1), is(true));
        assertThat(sut1.equals(sut2), is(true));
        assertThat(sut2.equals(sut1), is(true));
        assertThat(sut2.equals(sut2), is(true));

        assertThat(sut3.equals(sut3), is(true));
        assertThat(sut3.equals(sut1), is(false));
        assertThat(sut1.equals(sut3), is(false));
        assertThat(sut3.equals(sut2), is(false));
        assertThat(sut2.equals(sut3), is(false));
    }

    @Test
    public void testEquals_author() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        final DarcsChangeSet sut2 = createSutWithoutPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.setAuthor("foobar");
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_comment() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        final DarcsChangeSet sut2 = createSutWithoutPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.setComment("foobar");
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_date() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        final DarcsChangeSet sut2 = createSutWithoutPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.setDate("foobar");
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_hash() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        final DarcsChangeSet sut2 = createSutWithoutPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.setHash("foobar");
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_localDate() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        final DarcsChangeSet sut2 = createSutWithoutPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.setLocalDate("foobar");
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_inverted() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        final DarcsChangeSet sut2 = createSutWithoutPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.setInverted(false);
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_name() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        final DarcsChangeSet sut2 = createSutWithoutPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.setName("foobar");
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_deleted() {
        final DarcsChangeSet sut1 = createSutWithPaths();
        final DarcsChangeSet sut2 = createSutWithPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.getDeletedPaths().clear();
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_added() {
        final DarcsChangeSet sut1 = createSutWithPaths();
        final DarcsChangeSet sut2 = createSutWithPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.getAddedPaths().clear();
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_modified() {
        final DarcsChangeSet sut1 = createSutWithPaths();
        final DarcsChangeSet sut2 = createSutWithPaths();
        assertThat(sut1.equals(sut2), is(true));
        sut2.getModifiedPaths().clear();
        assertThat(sut1.equals(sut2), is(false));
    }

    @Test
    public void testEquals_wrongType() {
        final DarcsChangeSet sut1 = createSutWithoutPaths();
        assertThat(sut1.equals(new Object()), is(false));
    }

    @Test
    public void testHashCode() {
        final DarcsChangeSet sut1 = createSutWithPaths();
        final DarcsChangeSet sut2 = createSutWithPaths();
        final DarcsChangeSet sut3 = new DarcsChangeSet();

        assertThat(sut1.hashCode(), is(sut1.hashCode()));
        assertThat(sut1.hashCode(), is(sut2.hashCode()));
        assertThat(sut2.hashCode(), is(sut1.hashCode()));
        assertThat(sut2.hashCode(), is(sut2.hashCode()));

        assertThat(sut3.hashCode(), is(sut3.hashCode()));
        assertThat(sut3.hashCode(), is(not(sut1.hashCode())));
        assertThat(sut3.hashCode(), is(not(sut2.hashCode())));
    }

    @Test
    public void getEditTypes() {
        final DarcsChangeSet sut = new DarcsChangeSet();
        final List<EditType> editTypes = sut.getEditTypes();
        assertThat(editTypes.size(), is(3));
        assertThat(editTypes.contains(EditType.ADD), is(true));
        assertThat(editTypes.contains(EditType.EDIT), is(true));
        assertThat(editTypes.contains(EditType.DELETE), is(true));
    }

    @Test
    public void getPaths_forEditTypeAdd() {
        final DarcsChangeSet sut = createSutWithPaths();
        final List<String> paths = sut.getPaths(EditType.ADD);
        assertThat(paths, is(sut.getAddedPaths()));
        assertThat(paths, is(not(sut.getModifiedPaths())));
        assertThat(paths, is(not(sut.getDeletedPaths())));
    }

    @Test
    public void getPaths_forEditTypeEdit() {
        final DarcsChangeSet sut = createSutWithPaths();
        final List<String> paths = sut.getPaths(EditType.EDIT);
        assertThat(paths, is(sut.getModifiedPaths()));
        assertThat(paths, is(not(sut.getAddedPaths())));
        assertThat(paths, is(not(sut.getDeletedPaths())));
    }

    @Test
    public void getPaths_forEditTypeDelete() {
        final DarcsChangeSet sut = createSutWithPaths();
        final List<String> paths = sut.getPaths(EditType.DELETE);
        assertThat(paths, is(sut.getDeletedPaths()));
        assertThat(paths, is(not(sut.getAddedPaths())));
        assertThat(paths, is(not(sut.getModifiedPaths())));
    }

    @Test
    public void getPaths_nullForOtherEditType() {
        final DarcsChangeSet sut = createSutWithPaths();
        assertThat(sut.getPaths(new EditType("foobar", "The file was foobared")), is(nullValue()));
    }

    @Test
    public void getMsg() {
        final DarcsChangeSet sut = createSutWithoutPaths();
        assertThat(sut.getMsg(), is(sut.getComment()));
    }
}
