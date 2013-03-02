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

import hudson.model.TaskListener;


import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsScmTest {

    @Test
    @Ignore("Incomplete!")
    public void testGetRevisionState() throws SAXException, InterruptedException {
        final DarcsScm sut = new DarcsScm("");
        sut.getRevisionState(null, TaskListener.NULL, "");
    }

}
