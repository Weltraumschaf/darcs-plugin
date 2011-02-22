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

import junit.framework.TestCase;
import org.junit.Ignore;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSetListTest extends TestCase {

	public DarcsChangeSetListTest(String name) {
		super(name);
	}

	protected static DarcsChangeSet createChangeSet(String suffix) {
		DarcsChangeSet cs = new DarcsChangeSet();
		cs.setAuthor("author" + suffix);
		cs.setDate("date" + suffix);
		cs.setLocalDate("local_date" + suffix);
		cs.setHash("hash" + suffix);
		cs.setInverted(false);

		return cs;
	}
	
	public void testIsEmpty() {
		DarcsChangeSetList sut;

		sut = new DarcsChangeSetList(null, new ArrayList<DarcsChangeSet>());
		assertTrue(sut.isEmptySet());
		assertEquals(0, sut.size());

		List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();
		list.add(createChangeSet("1"));
		list.add(createChangeSet("2"));
		list.add(createChangeSet("3"));
		sut = new DarcsChangeSetList(null, list);
		assertFalse(sut.isEmptySet());
		assertEquals(3, sut.size());
	}

	@Ignore("Not ready yet.")
	public void testDigest() {
		
	}
}
