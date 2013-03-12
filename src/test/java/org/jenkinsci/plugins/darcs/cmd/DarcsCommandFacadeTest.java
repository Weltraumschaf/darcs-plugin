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
import java.net.URI;
import java.net.URISyntaxException;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsCommandFacadeTest {
    private static final String FIXTURE_BASE = "/org/jenkinsci/plugins/darcs/cmd";
    private static final String LINUX_BIN = "darcs_bin_linux_2.8";
    private static final String MACOS_BIN = "darcs_bin_macos_2.5";
    private static final String REPO_ARCHIVE = FIXTURE_BASE + "/repo.tar.gz";
    private static final String REPO_DIR_NAME = "repo";

    //CHECKSTYLE:OFF
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    //CHECKSTYLE:ON
    private File repo;

    @Before
    public void loadRepoArchive() throws URISyntaxException {
        final URI resource = getClass().getResource(REPO_ARCHIVE).toURI();
        repo = new File(resource);
        assertThat(repo.exists(), is(true));
    }

    private File prepareTestRepo() throws URISyntaxException {
        final File tmpDir = folder.getRoot();
        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(mock(Logger.class));
        ua.setSourceFile(repo);
        ua.setDestDirectory(tmpDir);
        ua.extract();
        return new File(tmpDir, REPO_DIR_NAME);
    }

    @Test
    @Ignore("not ready yet")
    public void lastSummarizedChanges() throws URISyntaxException {
        // TODO Implement test
        final File repo = prepareTestRepo();
        assertThat(repo, is(notNullValue()));
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
