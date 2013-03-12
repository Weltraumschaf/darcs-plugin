/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */

package org.jenkinsci.plugins.darcs.cmd;

import java.io.File;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsCommandFacadeTest {

    @Rule public ExpectedException thrown = ExpectedException.none();
    @Rule public TemporaryFolder folder= new TemporaryFolder();

    private File prepareTestRepo() {
        File tmpDir = folder.getRoot();
        return tmpDir;
    }

    @Test
    @Ignore("not ready yet")
    public void lastSummarizedChanges() {
        // TODO Implement test
    }

    @Test
    @Ignore("not ready yet")
    public void allSummarizedChanges() {
        // TODO Implement test
    }

    @Test
    @Ignore("not ready yet")
    public void allChanges() {
        // TODO Implement test
    }

    @Test
    @Ignore("not ready yet")
    public void countChanges() {
        // TODO Implement test
    }

    @Test
    @Ignore("not ready yet")
    public void pull() {
        // TODO Implement test
    }

    @Test
    @Ignore("not ready yet")
    public void get() {
        // TODO Implement test
    }



}
