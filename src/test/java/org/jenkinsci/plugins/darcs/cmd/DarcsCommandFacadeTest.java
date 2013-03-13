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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.Logger;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
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
    @Rule public TemporaryFolder tmpDir = new TemporaryFolder();
    //CHECKSTYLE:ON
    private File repoArchive;
    private File darcsExe;

    @Before
    public void loadRepoArchive() throws URISyntaxException {
        final URI resource = getClass().getResource(REPO_ARCHIVE).toURI();
        repoArchive = new File(resource);
        assertThat(repoArchive, is(notNullValue()));
        assertThat(repoArchive.exists(), is(true));
    }

    @Before
    public void loadDarcsExe() throws URISyntaxException {
        final String os = System.getProperty("os.name", "unknown").toLowerCase();
        String binary;

        if (os.indexOf("linux") >= 0) {
            binary = LINUX_BIN;
        } else if (os.indexOf("mac os x") >= 0) {
            binary = MACOS_BIN;
        } else {
            throw new IllegalArgumentException(String.format("Unsupported os '%s'!", os));
        }

        final URI resource = getClass().getResource(FIXTURE_BASE + "/" + binary).toURI();
        darcsExe = new File(resource);
        assertThat(darcsExe, is(notNullValue()));
        assertThat(darcsExe.exists(), is(true));
    }

    private File prepareTestRepo() throws URISyntaxException {
        final File destDir = tmpDir.getRoot();
        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(mock(Logger.class));
        ua.setSourceFile(repoArchive);
        ua.setDestDirectory(destDir);
        ua.extract();
        final File repo = new File(destDir, REPO_DIR_NAME);
        assertThat(repo, is(notNullValue()));
        return repo;
    }

    private DarcsCommandFacade createSut() {
        return new DarcsCommandFacade(
            new Launcher.LocalLauncher(TaskListener.NULL),
            new HashMap<String, String>(),
            darcsExe.getAbsolutePath(),
            new FilePath(tmpDir.getRoot()));
    }

    @Test
    @Ignore("not ready yet")
    public void lastSummarizedChanges() throws URISyntaxException {
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
    public void countChanges() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.countChanges(prepareTestRepo().getAbsolutePath()), is(10));
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
