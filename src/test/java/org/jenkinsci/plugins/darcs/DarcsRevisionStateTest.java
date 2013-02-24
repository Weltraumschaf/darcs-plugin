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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsRevisionStateTest {

    @Test
    public void testToString() {
        DarcsChangeSetList csl;
        DarcsRevisionState sut;

        csl = Helper.createChangeSetList(0);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.digest(), sut.toString());

        csl = Helper.createChangeSetList(3);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.digest(), sut.toString());
    }

    @Test
    public void testEquals() {
        DarcsChangeSetList csl1 = Helper.createChangeSetList(3),
                           csl2 = Helper.createChangeSetList(2);
        DarcsRevisionState sut1, sut2;

        sut1 = new DarcsRevisionState(csl1);
        sut2 = new DarcsRevisionState(csl1);
        assertTrue(sut1.equals(sut2));
        assertTrue(sut2.equals(sut1));

        sut2 = new DarcsRevisionState(csl2);
        assertFalse(sut1.equals(sut2));
        assertFalse(sut2.equals(sut1));
    }

    @Test
    public void testHashCode() {
        DarcsChangeSetList csl;
        DarcsRevisionState sut;

        csl = Helper.createChangeSetList(0);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.hashCode(), sut.hashCode());

        csl = Helper.createChangeSetList(3);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.hashCode(), sut.hashCode());
    }
}
