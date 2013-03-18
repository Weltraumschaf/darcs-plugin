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

        LINUX_BIN("darcs_bin_linux_2.8", "2.8.0 (release)\n", "darcs compiled on Apr 25 2012, at 12:17:55\n"
        + "\n"
        + "Context:\n"
        + "\n"
        + "[TAG 2.8.0\n"
        + "Florent Becker <florent.becker@ens-lyon.org>**20120422151242\n"
        + " Ignore-this: 150f3335021c2b37bcd1572ff55f8654\n"
        + "] \n"
        + "\n"
        + "Compiled with:\n"
        + "\n"
        + "array-0.3.0.2\n"
        + "base-4.3.1.0\n"
        + "bytestring-0.9.1.10\n"
        + "containers-0.4.0.0\n"
        + "directory-1.1.0.0\n"
        + "extensible-exceptions-0.1.1.2\n"
        + "filepath-1.2.0.0\n"
        + "hashed-storage-0.5.7\n"
        + "haskeline-0.6.4.0\n"
        + "html-1.0.1.2\n"
        + "mmap-0.5.7\n"
        + "mtl-2.0.1.0\n"
        + "old-time-1.0.0.6\n"
        + "parsec-3.1.1\n"
        + "process-1.0.1.5\n"
        + "random-1.0.0.3\n"
        + "regex-compat-0.95.1\n"
        + "tar-0.3.1.0\n"
        + "terminfo-0.3.1.3\n"
        + "text-0.11.1.1\n"
        + "unix-2.4.2.0\n"
        + "vector-0.9\n"
        + "zlib-0.5.3.1\n"),
        MACOS_BIN("darcs_bin_macos_2.5", "2.5 (release)\n", "darcs compiled on Oct 31 2010, at 14:40:05\n"
        + "\n"
        + "Context:\n"
        + "\n"
        + "[TAG 2.5\n"
        + "Reinier Lamers <tux_rocker@reinier.de>**20101024151805\n"
        + " Ignore-this: 1561ce30bfb1950a440c03371e0e2f20\n"
        + "] \n"
        + "\n"
        + "Compiled with:\n"
        + "\n"
        + "HTTP-4000.0.8\n"
        + "array-0.3.0.1\n"
        + "base-4.2.0.2\n"
        + "bytestring-0.9.1.7\n"
        + "containers-0.3.0.0\n"
        + "directory-1.0.1.1\n"
        + "extensible-exceptions-0.1.1.1\n"
        + "filepath-1.1.0.4\n"
        + "hashed-storage-0.5.2\n"
        + "haskeline-0.6.2.2\n"
        + "html-1.0.1.2\n"
        + "mmap-0.5.6\n"
        + "mtl-1.1.0.2\n"
        + "network-2.2.1.5\n"
        + "old-time-1.0.0.5\n"
        + "parsec-2.1.0.1\n"
        + "process-1.0.1.3\n"
        + "random-1.0.0.2\n"
        + "regex-compat-0.93.1\n"
        + "tar-0.3.1.0\n"
        + "terminfo-0.3.1.2\n"
        + "text-0.7.2.1\n"
        + "unix-2.4.0.2\n"
        + "zlib-0.5.2.0\n"
        + "HUnit-1.2.2.1\n"
        + "QuickCheck-2.1.1.1\n"
        + "test-framework-0.3.2\n"
        + "test-framework-hunit-0.2.6\n"
        + "test-framework-quickcheck2-0.2.7\n");
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

        public String getVersion() {
            return version;
        }

        public String getExactVersion() {
            return exactVersion;
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
    }
    //CHECKSTYLE:OFF
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();
    //CHECKSTYLE:ON
    private File repoArchive;
    private File darcsExe;

    @Before
    public void loadRepoArchive() throws URISyntaxException {
        repoArchive = Repository.DIRTY_REPO.getArchive();
        assertThat(repoArchive, is(notNullValue()));
        assertThat(repoArchive.exists(), is(true));
    }

    @Before
    public void loadDarcsExe() throws URISyntaxException {
        darcsExe = Binary.determine().getBin();
        assertThat(darcsExe, is(notNullValue()));
        assertThat(darcsExe.exists(), is(true));
    }

    private DarcsCommandFacade createSut() {
        return new DarcsCommandFacade(
                new Launcher.LocalLauncher(TaskListener.NULL),
                new HashMap<String, String>(),
                darcsExe.getAbsolutePath(),
                new FilePath(tmpDir.getRoot()));
    }

    @Test
    public void lastSummarizedChanges_emptyRepo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.lastSummarizedChanges(repo.getAbsolutePath(), 3), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void lastSummarizedChanges_repo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        //CHECKSTYLE:OFF
        assertThat(sut.lastSummarizedChanges(repo.getAbsolutePath(), 3),
                is("<changelog>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223954' local_date='Mon Mar 18 23:39:54 CET 2013' inverted='False' hash='20130318223954-7677a-c98dc9ccca9db155a9f707f441b1b17f1c2f6644.gz'>\n"
                + "	<name>Add property baz to class Baz.</name>\n"
                + "	<comment>Ignore-this: 4d6065691e6e1d5cb6573f1d457d535f</comment>\n"
                + "    <summary>\n"
                + "    <modify_file>\n"
                + "    Baz.java<added_lines num='1'/>\n"
                + "    </modify_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223928' local_date='Mon Mar 18 23:39:28 CET 2013' inverted='False' hash='20130318223928-7677a-f55af47435c293ec7fd8bb9f590941245cea4dd0.gz'>\n"
                + "	<name>Ad property bar to class Baz.</name>\n"
                + "	<comment>Ignore-this: b1ef0a68930b6224bf7a1a8b5582305e</comment>\n"
                + "    <summary>\n"
                + "    <modify_file>\n"
                + "    Baz.java<added_lines num='1'/>\n"
                + "    </modify_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223900' local_date='Mon Mar 18 23:39:00 CET 2013' inverted='False' hash='20130318223900-7677a-58b6e653111071d14c11173b549b568b93a51012.gz'>\n"
                + "	<name>Implement class Baz.</name>\n"
                + "	<comment>Ignore-this: 40d90f56559035790c098de502bf72b6</comment>\n"
                + "    <summary>\n"
                + "    <add_file>\n"
                + "    Baz.java\n"
                + "    </add_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "</changelog>\n"));
        //CHECKSTYLE:ON
    }

    @Test
    public void allSummarizedChanges_repo() throws URISyntaxException, UnsupportedEncodingException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        //CHECKSTYLE:OFF
        assertThat(sut.allSummarizedChanges(repo.getAbsolutePath()),
                is("<changelog>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223954' local_date='Mon Mar 18 23:39:54 CET 2013' inverted='False' hash='20130318223954-7677a-c98dc9ccca9db155a9f707f441b1b17f1c2f6644.gz'>\n"
                + "	<name>Add property baz to class Baz.</name>\n"
                + "	<comment>Ignore-this: 4d6065691e6e1d5cb6573f1d457d535f</comment>\n"
                + "    <summary>\n"
                + "    <modify_file>\n"
                + "    Baz.java<added_lines num='1'/>\n"
                + "    </modify_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223928' local_date='Mon Mar 18 23:39:28 CET 2013' inverted='False' hash='20130318223928-7677a-f55af47435c293ec7fd8bb9f590941245cea4dd0.gz'>\n"
                + "	<name>Ad property bar to class Baz.</name>\n"
                + "	<comment>Ignore-this: b1ef0a68930b6224bf7a1a8b5582305e</comment>\n"
                + "    <summary>\n"
                + "    <modify_file>\n"
                + "    Baz.java<added_lines num='1'/>\n"
                + "    </modify_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223900' local_date='Mon Mar 18 23:39:00 CET 2013' inverted='False' hash='20130318223900-7677a-58b6e653111071d14c11173b549b568b93a51012.gz'>\n"
                + "	<name>Implement class Baz.</name>\n"
                + "	<comment>Ignore-this: 40d90f56559035790c098de502bf72b6</comment>\n"
                + "    <summary>\n"
                + "    <add_file>\n"
                + "    Baz.java\n"
                + "    </add_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223646' local_date='Mon Mar 18 23:36:46 CET 2013' inverted='False' hash='20130318223646-7677a-dd64aeefc518b37c804d949eec60e2701fabbac3.gz'>\n"
                + "	<name>Implement class Bar.</name>\n"
                + "	<comment>Ignore-this: a644b7768c5055c355f38015d4d38f0a</comment>\n"
                + "    <summary>\n"
                + "    <add_file>\n"
                + "    Bar.java\n"
                + "    </add_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223419' local_date='Mon Mar 18 23:34:19 CET 2013' inverted='False' hash='20130318223419-7677a-f53a8f63dd1cb8409d576151c85855861107706f.gz'>\n"
                + "	<name>Remove trailing spaces.</name>\n"
                + "	<comment>Ignore-this: 2bb4eb18937d42a0386423251171e95a</comment>\n"
                + "    <summary>\n"
                + "    <modify_file>\n"
                + "    Foo.java<removed_lines num='1'/><added_lines num='1'/>\n"
                + "    </modify_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223324' local_date='Mon Mar 18 23:33:24 CET 2013' inverted='False' hash='20130318223324-7677a-9301c1b55b25285c289ec2eb0a94ca94e508bc10.gz'>\n"
                + "	<name>Add class Foo</name>\n"
                + "	<comment>Ignore-this: be93c7e09542368df7ab1fbb4f80c044</comment>\n"
                + "    <summary>\n"
                + "    <add_file>\n"
                + "    Foo.java\n"
                + "    </add_file>\n"
                + "    </summary>\n"
                + "</patch>\n"
                + "</changelog>\n"));
        //CHECKSTYLE:ON

    }

    @Test
    public void allSummarizedChanges_emptyRepo() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.allChanges(repo.getAbsolutePath()), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void allChanges_repo() throws URISyntaxException, UnsupportedEncodingException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        //CHECKSTYLE:OFF
        assertThat(sut.allChanges(repo.getAbsolutePath()),
                is("<changelog>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223954' local_date='Mon Mar 18 23:39:54 CET 2013' inverted='False' hash='20130318223954-7677a-c98dc9ccca9db155a9f707f441b1b17f1c2f6644.gz'>\n"
                + "	<name>Add property baz to class Baz.</name>\n"
                + "	<comment>Ignore-this: 4d6065691e6e1d5cb6573f1d457d535f</comment>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223928' local_date='Mon Mar 18 23:39:28 CET 2013' inverted='False' hash='20130318223928-7677a-f55af47435c293ec7fd8bb9f590941245cea4dd0.gz'>\n"
                + "	<name>Ad property bar to class Baz.</name>\n"
                + "	<comment>Ignore-this: b1ef0a68930b6224bf7a1a8b5582305e</comment>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223900' local_date='Mon Mar 18 23:39:00 CET 2013' inverted='False' hash='20130318223900-7677a-58b6e653111071d14c11173b549b568b93a51012.gz'>\n"
                + "	<name>Implement class Baz.</name>\n"
                + "	<comment>Ignore-this: 40d90f56559035790c098de502bf72b6</comment>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223646' local_date='Mon Mar 18 23:36:46 CET 2013' inverted='False' hash='20130318223646-7677a-dd64aeefc518b37c804d949eec60e2701fabbac3.gz'>\n"
                + "	<name>Implement class Bar.</name>\n"
                + "	<comment>Ignore-this: a644b7768c5055c355f38015d4d38f0a</comment>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223419' local_date='Mon Mar 18 23:34:19 CET 2013' inverted='False' hash='20130318223419-7677a-f53a8f63dd1cb8409d576151c85855861107706f.gz'>\n"
                + "	<name>Remove trailing spaces.</name>\n"
                + "	<comment>Ignore-this: 2bb4eb18937d42a0386423251171e95a</comment>\n"
                + "</patch>\n"
                + "<patch author='ich@weltraumschaf.de' date='20130318223324' local_date='Mon Mar 18 23:33:24 CET 2013' inverted='False' hash='20130318223324-7677a-9301c1b55b25285c289ec2eb0a94ca94e508bc10.gz'>\n"
                + "	<name>Add class Foo</name>\n"
                + "	<comment>Ignore-this: be93c7e09542368df7ab1fbb4f80c044</comment>\n"
                + "</patch>\n"
                + "</changelog>\n"));
        //CHECKSTYLE:ON
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
    public void isRepository() throws IOException {
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
    public void version() {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.version(), is(Binary.determine().getVersion()));
    }

    @Test
    public void exactVersion() {
        final DarcsCommandFacade sut = createSut();
        //CHECKSTYLE:OFF
        assertThat(sut.version(true), is(Binary.determine().getExactVersion()));
        //CHECKSTYLE:ON
    }
}
