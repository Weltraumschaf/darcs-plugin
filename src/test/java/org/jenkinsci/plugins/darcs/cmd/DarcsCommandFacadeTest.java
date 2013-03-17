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

        LINUX_BIN("darcs_bin_linux_2.8", "", ""),
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
        repoArchive = Repository.REPO.getArchive();
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
    @Ignore("not ready yet")
    public void lastSummarizedChanges() throws URISyntaxException {
        // TODO Implement test
    }

    @Test
    public void allSummarizedChanges_repo() throws URISyntaxException {
        // TODO Does not work with Java 1.6. Implement with clean encoded repo.
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.allSummarizedChanges(repo.getAbsolutePath()).getBytes(), // Use bytes because of mixed encoding.
            //CHECKSTYLE:OFF
            is("<changelog>\n<patch author='ich@weltraumschaf.de' date='20110224143546' local_date='Thu Feb 24 15:35:46 CET 2011' inverted='False' hash='20110224143546-7677a-359f8967374ac52adc87dedac6f4ad458a7b6446.gz'>\n\t<name>German Umlauts in ISO8859-15 encoding: ???????</name>\n\t<comment>Ignore-this: 7c74b888addef772a93a63b69a144836</comment>\n    <summary>\n    <modify_file>\n    Foo.java<removed_lines num='1'/><added_lines num='1'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110224141706' local_date='Thu Feb 24 15:17:06 CET 2011' inverted='False' hash='20110224141706-7677a-b79e15c79bd5776b3e669a7338e181b4bd303609.gz'>\n\t<name>German Umlauts in UTF-8 encoding: ??????????????</name>\n\t<comment>Ignore-this: 77565bbaae7ec954f242fd414ca70033</comment>\n    <summary>\n    <modify_file>\n    Foo.java<added_lines num='3'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203531' local_date='Mon Feb 14 21:35:31 CET 2011' inverted='False' hash='20110214203531-7677a-1b935a82ba6408ffa9add3642cb52f233ff4ef54.gz'>\n\t<name>Implemented toString()</name>\n\t<comment>Ignore-this: 7c0271b552e03728baa7d4f33cb545f9</comment>\n    <summary>\n    <modify_file>\n    Bar.java<added_lines num='5'/>\n    </modify_file>\n    <modify_file>\n    Baz.java<added_lines num='5'/>\n    </modify_file>\n    <modify_file>\n    Foo.java<added_lines num='5'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203417' local_date='Mon Feb 14 21:34:17 CET 2011' inverted='False' hash='20110214203417-7677a-261f33e2608d68f088f15b077f7dcde2cc18a4b7.gz'>\n\t<name>Implemented value in class Foo</name>\n\t<comment>Ignore-this: 79225cd08e4f7ec7dfc7a6cb4e7f5948</comment>\n    <summary>\n    <modify_file>\n    Foo.java<removed_lines num='1'/><added_lines num='9'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203402' local_date='Mon Feb 14 21:34:02 CET 2011' inverted='False' hash='20110214203402-7677a-5eb558d8bd3df5b8edfa005479d8ff1e8139abe0.gz'>\n\t<name>Implemented value in class Baz</name>\n\t<comment>Ignore-this: fd9e1a81cc792fd826a128794e92ba64</comment>\n    <summary>\n    <modify_file>\n    Baz.java<removed_lines num='1'/><added_lines num='9'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203334' local_date='Mon Feb 14 21:33:34 CET 2011' inverted='False' hash='20110214203334-7677a-3f9e4a67068618fcfde454c0c097d1f8b96301df.gz'>\n\t<name>Implemented value in class Bar</name>\n\t<comment>Ignore-this: 40e46e42b5023572d7e45607df47cab1</comment>\n    <summary>\n    <modify_file>\n    Bar.java<removed_lines num='2'/><added_lines num='10'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201649' local_date='Mon Feb 14 21:16:49 CET 2011' inverted='False' hash='20110214201649-7677a-9c3c62c42467fe20e75a9ab62e52441ef7cdc8ba.gz'>\n\t<name>Implemented class Foo</name>\n\t<comment>Ignore-this: 85d0cdf2679dfcab72469629d6a80945</comment>\n    <summary>\n    <modify_file>\n    Foo.java<added_lines num='7'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201618' local_date='Mon Feb 14 21:16:18 CET 2011' inverted='False' hash='20110214201618-7677a-631eee269c4a252d953fa2f61b1127874bbc57d4.gz'>\n\t<name>Implemented class Baz</name>\n\t<comment>Ignore-this: 751c6c8a858104b5b15833496fce9a2</comment>\n    <summary>\n    <modify_file>\n    Baz.java<added_lines num='7'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201544' local_date='Mon Feb 14 21:15:44 CET 2011' inverted='False' hash='20110214201544-7677a-1cd5bbbc18b5e78240a80c9d1576faaa63c9f7d7.gz'>\n\t<name>Implemented class Bar</name>\n\t<comment>Ignore-this: e2eb7de380585ad9e4cb9515d8b21621</comment>\n    <summary>\n    <modify_file>\n    Bar.java<added_lines num='7'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201356' local_date='Mon Feb 14 21:13:56 CET 2011' inverted='False' hash='20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz'>\n\t<name>inital files added</name>\n\t<comment>Ignore-this: 391a1ff64b0f64546446368f2d45fbc8</comment>\n    <summary>\n    <add_file>\n    Bar.java\n    </add_file>\n    <add_file>\n    Baz.java\n    </add_file>\n    <add_file>\n    Foo.java\n    </add_file>\n    </summary>\n</patch>\n</changelog>\n".getBytes()));
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
    public void allChanges_repo() throws URISyntaxException {
        // TODO Does not work with Java 1.6. Implement with clean encoded repo.
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.allChanges(repo.getAbsolutePath()).getBytes(), // Use bytes because of mixed encoding.
            //CHECKSTYLE:OFF
            is("<changelog>\n<patch author='ich@weltraumschaf.de' date='20110224143546' local_date='Thu Feb 24 15:35:46 CET 2011' inverted='False' hash='20110224143546-7677a-359f8967374ac52adc87dedac6f4ad458a7b6446.gz'>\n\t<name>German Umlauts in ISO8859-15 encoding: ???????</name>\n\t<comment>Ignore-this: 7c74b888addef772a93a63b69a144836</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110224141706' local_date='Thu Feb 24 15:17:06 CET 2011' inverted='False' hash='20110224141706-7677a-b79e15c79bd5776b3e669a7338e181b4bd303609.gz'>\n\t<name>German Umlauts in UTF-8 encoding: ??????????????</name>\n\t<comment>Ignore-this: 77565bbaae7ec954f242fd414ca70033</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203531' local_date='Mon Feb 14 21:35:31 CET 2011' inverted='False' hash='20110214203531-7677a-1b935a82ba6408ffa9add3642cb52f233ff4ef54.gz'>\n\t<name>Implemented toString()</name>\n\t<comment>Ignore-this: 7c0271b552e03728baa7d4f33cb545f9</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203417' local_date='Mon Feb 14 21:34:17 CET 2011' inverted='False' hash='20110214203417-7677a-261f33e2608d68f088f15b077f7dcde2cc18a4b7.gz'>\n\t<name>Implemented value in class Foo</name>\n\t<comment>Ignore-this: 79225cd08e4f7ec7dfc7a6cb4e7f5948</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203402' local_date='Mon Feb 14 21:34:02 CET 2011' inverted='False' hash='20110214203402-7677a-5eb558d8bd3df5b8edfa005479d8ff1e8139abe0.gz'>\n\t<name>Implemented value in class Baz</name>\n\t<comment>Ignore-this: fd9e1a81cc792fd826a128794e92ba64</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203334' local_date='Mon Feb 14 21:33:34 CET 2011' inverted='False' hash='20110214203334-7677a-3f9e4a67068618fcfde454c0c097d1f8b96301df.gz'>\n\t<name>Implemented value in class Bar</name>\n\t<comment>Ignore-this: 40e46e42b5023572d7e45607df47cab1</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201649' local_date='Mon Feb 14 21:16:49 CET 2011' inverted='False' hash='20110214201649-7677a-9c3c62c42467fe20e75a9ab62e52441ef7cdc8ba.gz'>\n\t<name>Implemented class Foo</name>\n\t<comment>Ignore-this: 85d0cdf2679dfcab72469629d6a80945</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201618' local_date='Mon Feb 14 21:16:18 CET 2011' inverted='False' hash='20110214201618-7677a-631eee269c4a252d953fa2f61b1127874bbc57d4.gz'>\n\t<name>Implemented class Baz</name>\n\t<comment>Ignore-this: 751c6c8a858104b5b15833496fce9a2</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201544' local_date='Mon Feb 14 21:15:44 CET 2011' inverted='False' hash='20110214201544-7677a-1cd5bbbc18b5e78240a80c9d1576faaa63c9f7d7.gz'>\n\t<name>Implemented class Bar</name>\n\t<comment>Ignore-this: e2eb7de380585ad9e4cb9515d8b21621</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201356' local_date='Mon Feb 14 21:13:56 CET 2011' inverted='False' hash='20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz'>\n\t<name>inital files added</name>\n\t<comment>Ignore-this: 391a1ff64b0f64546446368f2d45fbc8</comment>\n</patch>\n</changelog>\n".getBytes()));
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
        assertThat(sut.countChanges(repo.getAbsolutePath()), is(10));
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
    public void pull() {
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
        assertThat(sut.countChanges(desitnation.getAbsolutePath()), is(10)); // TODO chek if it is a darcs repo
    }

    @Test
    @Ignore("not ready yet")
    public void get_emptyRepo() throws URISyntaxException, IOException {
        final DarcsCommandFacade sut = createSut();
        final File repo = Repository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        final File desitnation = tmpDir.newFolder();
        sut.get(desitnation.getAbsolutePath(), repo.getAbsolutePath());
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
