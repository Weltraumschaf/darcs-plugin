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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsRevisionStateTest extends TestCase {
    
    protected static DarcsChangeSetList createChangeSetList(int count) {
        List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
        
        for (int i = 0; i < count ; i++) {
            DarcsChangeSet cs = DarcsChangeSetListTest.createChangeSet(Integer.toString(i));
            list.add(cs);
        }
        
        return new DarcsChangeSetList(list);
    }
    
    public DarcsRevisionStateTest(String testName) {
        super(testName);
    }

	public void testToString() {
        DarcsChangeSetList csl;
        DarcsRevisionState sut;
                
        csl = createChangeSetList(0);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.digest(), sut.toString());
        
        csl = createChangeSetList(3);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.digest(), sut.toString());
	}

	public void testEquals() {
        DarcsChangeSetList csl1 = createChangeSetList(3),
                           csl2 = createChangeSetList(2);
        DarcsRevisionState sut1, sut2;
        
        sut1 = new DarcsRevisionState(csl1);
        sut2 = new DarcsRevisionState(csl1);
        assertTrue(sut1.equals(sut2));
        assertTrue(sut2.equals(sut1));
        
        sut2 = new DarcsRevisionState(csl2);
        assertFalse(sut1.equals(sut2));
        assertFalse(sut2.equals(sut1));
	}
    
    public void testHashCode() {
        DarcsChangeSetList csl;
        DarcsRevisionState sut;
                
        csl = createChangeSetList(0);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.hashCode(), sut.hashCode());
        
        csl = createChangeSetList(3);
        sut = new DarcsRevisionState(csl);
        assertEquals(csl.hashCode(), sut.hashCode());
    }
}
