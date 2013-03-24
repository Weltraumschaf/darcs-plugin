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

import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import hudson.Util;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSetListTest {

    @Test
    public void testIsEmpty() {
        DarcsChangeSetList sut;

        sut = new DarcsChangeSetList(null, new ArrayList<DarcsChangeSet>());
        assertTrue(sut.isEmptySet());
        assertEquals(0, sut.size());

        final List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
        list.add(FixtureFactory.createChangeSet("1"));
        list.add(FixtureFactory.createChangeSet("2"));
        list.add(FixtureFactory.createChangeSet("3"));
        sut = new DarcsChangeSetList(list);
        assertFalse(sut.isEmptySet());
        assertEquals(3, sut.size());
    }

    @Test
    public void testDigest() {
        DarcsChangeSetList sut;
        sut = new DarcsChangeSetList(new ArrayList<DarcsChangeSet>());
        assertThat(sut.digest(), is("d41d8cd98f00b204e9800998ecf8427e"));

        final DarcsChangeSet cs1 = FixtureFactory.createChangeSet("1");
        final DarcsChangeSet cs2 = FixtureFactory.createChangeSet("2");
        final DarcsChangeSet cs3 = FixtureFactory.createChangeSet("3");

        final List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
        list.add(cs1);
        list.add(cs2);
        list.add(cs3);
        sut = new DarcsChangeSetList(list);
        assertThat(sut.digest(), is("20e0da2026fb244e6b7f120a15c7be5c"));
    }

    @Test
    public void testDigestOnOrderOfPatches() {
        final DarcsChangeSet cs1 = FixtureFactory.createChangeSet("1");
        final DarcsChangeSet cs2 = FixtureFactory.createChangeSet("2");
        final DarcsChangeSet cs3 = FixtureFactory.createChangeSet("3");
        final List<DarcsChangeSet> list1 = new ArrayList<DarcsChangeSet>();
        final List<DarcsChangeSet> list2 = new ArrayList<DarcsChangeSet>();

        list1.add(cs1);
        list1.add(cs2);
        list1.add(cs3);
        list2.add(cs3);
        list2.add(cs2);
        list2.add(cs1);

        final DarcsChangeSetList sut1 = new DarcsChangeSetList(list1);
        final DarcsChangeSetList sut2 = new DarcsChangeSetList(list2);
        assertEquals(sut1.digest(), sut2.digest());
    }

    @Test
    public void testGetKind() {
        final DarcsChangeSetList sut = new DarcsChangeSetList(new ArrayList<DarcsChangeSet>());
        assertEquals("darcs", sut.getKind());
    }

    @Test
    public void testEquals() {
        final DarcsChangeSet cs1 = FixtureFactory.createChangeSet("1");
        final DarcsChangeSet cs2 = FixtureFactory.createChangeSet("2");
        final DarcsChangeSet cs3 = FixtureFactory.createChangeSet("3");
        final List<DarcsChangeSet> list1 = new ArrayList<DarcsChangeSet>();
        final List<DarcsChangeSet> list2 = new ArrayList<DarcsChangeSet>();
        final List<DarcsChangeSet> list3 = new ArrayList<DarcsChangeSet>();

        list1.add(cs1);
        list1.add(cs2);
        final DarcsChangeSetList sut1 = new DarcsChangeSetList(list1);
        list2.add(cs1);
        list2.add(cs2);
        final DarcsChangeSetList sut2 = new DarcsChangeSetList(list2);
        list3.add(cs1);
        list3.add(cs2);
        list3.add(cs3);
        final DarcsChangeSetList sut3 = new DarcsChangeSetList(list3);

        assertTrue(sut1.equals(sut1));
        assertTrue(sut2.equals(sut2));
        assertTrue(sut3.equals(sut3));

        assertTrue(sut1.equals(sut2));
        assertTrue(sut2.equals(sut1));

        assertFalse(sut1.equals(sut3));
        assertFalse(sut3.equals(sut1));
        assertFalse(sut2.equals(sut3));
        assertFalse(sut3.equals(sut2));
    }

    @Test
    public void testHashCode() {
        final DarcsChangeSet cs1 = FixtureFactory.createChangeSet("1");
        final DarcsChangeSet cs2 = FixtureFactory.createChangeSet("2");
        final DarcsChangeSet cs3 = FixtureFactory.createChangeSet("3");
        final List<DarcsChangeSet> list1 = new ArrayList<DarcsChangeSet>();
        final List<DarcsChangeSet> list2 = new ArrayList<DarcsChangeSet>();
        final List<DarcsChangeSet> list3 = new ArrayList<DarcsChangeSet>();

        list1.add(cs1);
        list1.add(cs2);
        final DarcsChangeSetList sut1 = new DarcsChangeSetList(list1);
        list2.add(cs1);
        list2.add(cs2);
        final DarcsChangeSetList sut2 = new DarcsChangeSetList(list2);
        list3.add(cs1);
        list3.add(cs2);
        list3.add(cs3);
        final DarcsChangeSetList sut3 = new DarcsChangeSetList(list3);

        assertThat(sut1.hashCode(), is(sut1.hashCode()));
        assertThat(sut2.hashCode(), is(sut2.hashCode()));
        assertThat(sut3.hashCode(), is(sut3.hashCode()));

        assertThat(sut1.hashCode(), is(sut2.hashCode()));
        assertThat(sut2.hashCode(), is(sut1.hashCode()));

        assertThat(sut1.hashCode(), is(not(sut3.hashCode())));
        assertThat(sut3.hashCode(), is(not(sut1.hashCode())));
        assertThat(sut2.hashCode(), is(not(sut3.hashCode())));
        assertThat(sut3.hashCode(), is(not(sut2.hashCode())));
    }

    @Test
    public void testToString() {
        final DarcsChangeSetList sut1 = new DarcsChangeSetList();
        assertThat(sut1.toString(), is("DarcsChangeSetList{changeSets=[], digest=d41d8cd98f00b204e9800998ecf8427e}"));

        final List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
        list.add(FixtureFactory.createChangeSet("1"));
        final DarcsChangeSetList sut2 = new DarcsChangeSetList(list);
        assertThat(sut2.toString(), is("DarcsChangeSetList{changeSets=[DarcsChangeSet{added=[], author=author1, "
                + "date=date1, deleted=[], hash=hash1, inverted=false, localDate=local_date1, modified=[], name=<NULL>"
                + "}], digest=00c6ee2e21a7548de6260cf72c4f4b5b}"));

    }

}
