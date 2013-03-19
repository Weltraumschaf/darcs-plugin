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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;
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

    private enum Binary {

        LINUX_BIN("darcs_bin_linux_2.8", "2.8.0 (release)\n", "/exact_version_2.8"),
        MACOS_BIN("darcs_bin_macos_2.5", "2.5 (release)\n", "/exact_version_2.5");
        private final String name;
        private final String version;
        private final String exactVersion;

        private Binary(final String name, final String version, final String exactVersion) {
            this.name = name;
            this.version = version;
            this.exactVersion = exactVersion;
        }

        File getBin() throws URISyntaxException {
            final File bin = new File(getClass().getResource(FIXTURE_BASE + "/" + name).toURI());
            bin.setExecutable(true);
            return bin;
        }

        String getShortVersion() {
            return version.substring(0, 3);
        }

        String getVersion() {
            return version;
        }

        String getExactVersion() throws URISyntaxException, IOException {
            return readResource(FIXTURE_BASE + exactVersion);
        }

        static Binary determine() {
            final String os = System.getProperty("os.name", "unknown").toLowerCase();

            if (os.indexOf("linux") >= 0) {
                return LINUX_BIN;
            } else if (os.indexOf("mac os x") >= 0) {
                return MACOS_BIN;
            } else {
                throw new IllegalArgumentException(String.format("Unsupported os '%s'!", os));
            }
        }
    }

    private enum Repository {

        EMPTY("empty_repo"),
        DIRTY_REPO("dirty_repo"),
        REPO("repo");
        private static final String FIXTURE_FORMAT = "%s/%s_%s_%s.xml";
        private final String reponame;

        private Repository(String filename) {
            this.reponame = filename;
        }

        File getArchive() throws URISyntaxException {
            return new File(getClass().getResource(FIXTURE_BASE + "/" + reponame + ".tar.gz").toURI());
        }

        String getReponame() {
            return reponame;
        }

        File extractTo(final File destination) throws URISyntaxException {
            final TarGZipUnArchiver ua = new TarGZipUnArchiver();
            ua.enableLogging(mock(Logger.class));
            ua.setSourceFile(getArchive());
            ua.setDestDirectory(destination);
            ua.extract();
            return new File(destination, getReponame());
        }

        private String fixtureFile(final String file, final String version) {
            return String.format(FIXTURE_FORMAT, FIXTURE_BASE, file, reponame, version);
        }

        String lastSummarizedChanges(final String version) throws URISyntaxException, IOException {
            return readResource(fixtureFile("last_summarized_changes", version));
        }

        String allSummarizedChanges(final String version) throws URISyntaxException, IOException {
            return readResource(fixtureFile("all_summarized_changes", version));
        }

        String allChanges(final String version) throws URISyntaxException, IOException {
            return readResource(fixtureFile("all_changes", version));
        }

    }
    //CHECKSTYLE:OFF
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();
    //CHECKSTYLE:ON
    private Binary darcsExe;

    @Before
    public void loadDarcsExe() throws URISyntaxException {
        darcsExe = Binary.determine();
        assertThat(darcsExe.getBin(), is(notNullValue()));
        assertThat(darcsExe.getBin().exists(), is(true));
    }

    private DarcsCommandFacade createSut() throws URISyntaxException {
        return new DarcsCommandFacade(
                new Launcher.LocalLauncher(TaskListener.NULL),
                new HashMap<String, String>(),
                darcsExe.getBin().getAbsolutePath(),
                new FilePath(tmpDir.getRoot()));
    }

    static String readResource(final String filename) throws URISyntaxException, IOException {
        return FileUtils.readFileToString(
            new File(DarcsCommandFacadeTest.class.getResource(filename).toURI()),
            "utf-8");
    }

    @Test
    public void lastSummarizedChanges_emptyRepo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.lastSummarizedChanges(repo.getAbsolutePath(), 3), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void lastSummarizedChanges_repo() throws URISyntaxException, IOException {
        final DarcsCommandFacade sut = createSut();
        final Repository repo = Repository.REPO;
        final File extractedRepo = repo.extractTo(tmpDir.getRoot());
        assertThat(extractedRepo, is(notNullValue()));
        assertThat(sut.lastSummarizedChanges(extractedRepo.getAbsolutePath(), 3),
                is(repo.lastSummarizedChanges(darcsExe.getShortVersion())));
    }

    @Test
    public void allSummarizedChanges_repo() throws URISyntaxException, IOException {
        final DarcsCommandFacade sut = createSut();
        final Repository repo = Repository.REPO;
        final File extractedRepo = repo.extractTo(tmpDir.getRoot());
        assertThat(extractedRepo, is(notNullValue()));
        assertThat(sut.allSummarizedChanges(extractedRepo.getAbsolutePath()),
                is(repo.allSummarizedChanges(darcsExe.getShortVersion())));

    }

    @Test
    public void allSummarizedChanges_emptyRepo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.allChanges(repo.getAbsolutePath()), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void allChanges_repo() throws URISyntaxException, IOException {
        final DarcsCommandFacade sut = createSut();
        final Repository repo = Repository.REPO;
        final File extractedRepo = repo.extractTo(tmpDir.getRoot());
        assertThat(extractedRepo, is(notNullValue()));
        assertThat(sut.allChanges(extractedRepo.getAbsolutePath()),
                is(repo.allChanges(darcsExe.getShortVersion())));
    }

    @Test
    public void allChanges_emptyRepo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.allChanges(repo.getAbsolutePath()), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void countChanges_repo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.countChanges(repo.getAbsolutePath()), is(6));
    }

    @Test
    public void countChanges_emptyRepo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.countChanges(repo.getAbsolutePath()), is(0));
    }

    @Test
    @Ignore("not ready yet")
    public void pull_emptyRepo() {
        // TODO Implement test
    }

    @Test
    @Ignore("not ready yet")
    public void pull_repo() {
        // TODO Implement test
    }

    @Test
    @Ignore("not ready yet")
    public void get_repo() throws URISyntaxException, IOException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        final File desitnation = tmpDir.newFolder();
        sut.get(desitnation.getAbsolutePath(), repo.getAbsolutePath());
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation.getAbsolutePath()), is(6));
    }

    @Test
    @Ignore("not ready yet")
    public void get_emptyRepo() throws URISyntaxException, IOException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        final File desitnation = tmpDir.newFolder();
        sut.get(desitnation.getAbsolutePath(), repo.getAbsolutePath());
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation.getAbsolutePath()), is(0)); // TODO chek if it is a darcs repo
    }

    @Test
    public void isRepository() throws IOException, URISyntaxException {
        final DarcsCommandFacade sut = createSut();

        final File notExisting = new File("foobar");
        assertThat(sut.isRepository(notExisting), is(false));

        final File notAdirectory = tmpDir.newFile();
        assertThat(sut.isRepository(notAdirectory), is(false));

        final File directoryWithoutDarcsDir = tmpDir.newFolder();
        assertThat(sut.isRepository(directoryWithoutDarcsDir), is(false));

        final File directoryWithDarcsDir = tmpDir.newFolder();
        assertThat(new File(directoryWithDarcsDir, "_darcs").mkdir(), is(true));
        assertThat(sut.isRepository(directoryWithDarcsDir), is(true));
    }

    @Test
    public void version() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.version(), is(Binary.determine().getVersion()));
    }

    @Test
    public void exactVersion() throws URISyntaxException, IOException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.version(true), is(Binary.determine().getExactVersion()));
    }
}
