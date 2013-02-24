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
        list.add(Helper.createChangeSet("1"));
        list.add(Helper.createChangeSet("2"));
        list.add(Helper.createChangeSet("3"));
        sut = new DarcsChangeSetList(list);
        assertFalse(sut.isEmptySet());
        assertEquals(3, sut.size());
    }

    @Test
    public void testDigest() {
        DarcsChangeSetList sut;
        sut = new DarcsChangeSetList(new ArrayList<DarcsChangeSet>());
        assertEquals(Util.getDigestOf(""), sut.digest());

        final DarcsChangeSet cs1 = Helper.createChangeSet("1"),
                             cs2 = Helper.createChangeSet("2"),
                             cs3 = Helper.createChangeSet("3");

        List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
        list.add(cs1);
        list.add(cs2);
        list.add(cs3);
        sut = new DarcsChangeSetList(list);
        assertEquals(Util.getDigestOf(cs1.getHash() + cs2.getHash() + cs3.getHash()),
                     sut.digest());
    }

    @Test
    public void testDigestOnOrderOfPatches() {
        final DarcsChangeSet cs1 = Helper.createChangeSet("1"),
                             cs2 = Helper.createChangeSet("2"),
                             cs3 = Helper.createChangeSet("3");
        final List<DarcsChangeSet> list1 = new ArrayList<DarcsChangeSet>(),
                                   list2 = new ArrayList<DarcsChangeSet>();
        list1.add(cs1);
        list1.add(cs2);
        list1.add(cs3);
        list2.add(cs3);
        list2.add(cs2);
        list2.add(cs1);

        final DarcsChangeSetList sut1 = new DarcsChangeSetList(list1),
                                 sut2 = new DarcsChangeSetList(list2);
        assertEquals(sut1.digest(), sut2.digest());
    }

    @Test
    public void testGetKind() {
        DarcsChangeSetList sut = new DarcsChangeSetList(new ArrayList<DarcsChangeSet>());
        assertEquals("darcs", sut.getKind());
    }

    @Test
    public void testEquals() {
        final DarcsChangeSet cs1 = Helper.createChangeSet("1");
        final DarcsChangeSet cs2 = Helper.createChangeSet("2");
        final DarcsChangeSet cs3 = Helper.createChangeSet("3");
        final List<DarcsChangeSet> list1 = new ArrayList<DarcsChangeSet>(),
                                   list2 = new ArrayList<DarcsChangeSet>(),
                                     list3 = new ArrayList<DarcsChangeSet>();
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

        // equals to itself
        assertTrue(sut1.equals(sut1));
        assertTrue(sut2.equals(sut2));
        assertTrue(list3.equals(list3));
        // list1 and list2 are euqal
        assertTrue(sut1.equals(sut2));
        assertTrue(sut2.equals(sut1));
        // list3 has one more patch
        assertFalse(sut1.equals(sut3));
        assertFalse(sut2.equals(sut3));
        // wron types
        assertFalse(sut1.equals(cs3));
    }
}
