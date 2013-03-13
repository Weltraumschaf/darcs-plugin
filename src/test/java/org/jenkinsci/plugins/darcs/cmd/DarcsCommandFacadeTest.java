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

    private String prepareTestRepo() throws URISyntaxException {
        final File destDir = tmpDir.getRoot();
        final TarGZipUnArchiver ua = new TarGZipUnArchiver();
        ua.enableLogging(mock(Logger.class));
        ua.setSourceFile(repoArchive);
        ua.setDestDirectory(destDir);
        ua.extract();
        final File repo = new File(destDir, REPO_DIR_NAME);
        assertThat(repo, is(notNullValue()));
        return repo.getAbsolutePath();
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
    public void allSummarizedChanges() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.allSummarizedChanges(prepareTestRepo()).getBytes(), // Use bytes because of mixed encoding.
                   //CHECKSTYLE:OFF
                   is("<changelog>\n<patch author='ich@weltraumschaf.de' date='20110224143546' local_date='Thu Feb 24 15:35:46 CET 2011' inverted='False' hash='20110224143546-7677a-359f8967374ac52adc87dedac6f4ad458a7b6446.gz'>\n\t<name>German Umlauts in ISO8859-15 encoding: ???????</name>\n\t<comment>Ignore-this: 7c74b888addef772a93a63b69a144836</comment>\n    <summary>\n    <modify_file>\n    Foo.java<removed_lines num='1'/><added_lines num='1'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110224141706' local_date='Thu Feb 24 15:17:06 CET 2011' inverted='False' hash='20110224141706-7677a-b79e15c79bd5776b3e669a7338e181b4bd303609.gz'>\n\t<name>German Umlauts in UTF-8 encoding: ??????????????</name>\n\t<comment>Ignore-this: 77565bbaae7ec954f242fd414ca70033</comment>\n    <summary>\n    <modify_file>\n    Foo.java<added_lines num='3'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203531' local_date='Mon Feb 14 21:35:31 CET 2011' inverted='False' hash='20110214203531-7677a-1b935a82ba6408ffa9add3642cb52f233ff4ef54.gz'>\n\t<name>Implemented toString()</name>\n\t<comment>Ignore-this: 7c0271b552e03728baa7d4f33cb545f9</comment>\n    <summary>\n    <modify_file>\n    Bar.java<added_lines num='5'/>\n    </modify_file>\n    <modify_file>\n    Baz.java<added_lines num='5'/>\n    </modify_file>\n    <modify_file>\n    Foo.java<added_lines num='5'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203417' local_date='Mon Feb 14 21:34:17 CET 2011' inverted='False' hash='20110214203417-7677a-261f33e2608d68f088f15b077f7dcde2cc18a4b7.gz'>\n\t<name>Implemented value in class Foo</name>\n\t<comment>Ignore-this: 79225cd08e4f7ec7dfc7a6cb4e7f5948</comment>\n    <summary>\n    <modify_file>\n    Foo.java<removed_lines num='1'/><added_lines num='9'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203402' local_date='Mon Feb 14 21:34:02 CET 2011' inverted='False' hash='20110214203402-7677a-5eb558d8bd3df5b8edfa005479d8ff1e8139abe0.gz'>\n\t<name>Implemented value in class Baz</name>\n\t<comment>Ignore-this: fd9e1a81cc792fd826a128794e92ba64</comment>\n    <summary>\n    <modify_file>\n    Baz.java<removed_lines num='1'/><added_lines num='9'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203334' local_date='Mon Feb 14 21:33:34 CET 2011' inverted='False' hash='20110214203334-7677a-3f9e4a67068618fcfde454c0c097d1f8b96301df.gz'>\n\t<name>Implemented value in class Bar</name>\n\t<comment>Ignore-this: 40e46e42b5023572d7e45607df47cab1</comment>\n    <summary>\n    <modify_file>\n    Bar.java<removed_lines num='2'/><added_lines num='10'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201649' local_date='Mon Feb 14 21:16:49 CET 2011' inverted='False' hash='20110214201649-7677a-9c3c62c42467fe20e75a9ab62e52441ef7cdc8ba.gz'>\n\t<name>Implemented class Foo</name>\n\t<comment>Ignore-this: 85d0cdf2679dfcab72469629d6a80945</comment>\n    <summary>\n    <modify_file>\n    Foo.java<added_lines num='7'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201618' local_date='Mon Feb 14 21:16:18 CET 2011' inverted='False' hash='20110214201618-7677a-631eee269c4a252d953fa2f61b1127874bbc57d4.gz'>\n\t<name>Implemented class Baz</name>\n\t<comment>Ignore-this: 751c6c8a858104b5b15833496fce9a2</comment>\n    <summary>\n    <modify_file>\n    Baz.java<added_lines num='7'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201544' local_date='Mon Feb 14 21:15:44 CET 2011' inverted='False' hash='20110214201544-7677a-1cd5bbbc18b5e78240a80c9d1576faaa63c9f7d7.gz'>\n\t<name>Implemented class Bar</name>\n\t<comment>Ignore-this: e2eb7de380585ad9e4cb9515d8b21621</comment>\n    <summary>\n    <modify_file>\n    Bar.java<added_lines num='7'/>\n    </modify_file>\n    </summary>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201356' local_date='Mon Feb 14 21:13:56 CET 2011' inverted='False' hash='20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz'>\n\t<name>inital files added</name>\n\t<comment>Ignore-this: 391a1ff64b0f64546446368f2d45fbc8</comment>\n    <summary>\n    <add_file>\n    Bar.java\n    </add_file>\n    <add_file>\n    Baz.java\n    </add_file>\n    <add_file>\n    Foo.java\n    </add_file>\n    </summary>\n</patch>\n</changelog>\n".getBytes()));
                   //CHECKSTYLE:ON
    }

    @Test
    public void allChanges() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.allChanges(prepareTestRepo()).getBytes(), // Use bytes because of mixed encoding.
                   //CHECKSTYLE:OFF
                   is("<changelog>\n<patch author='ich@weltraumschaf.de' date='20110224143546' local_date='Thu Feb 24 15:35:46 CET 2011' inverted='False' hash='20110224143546-7677a-359f8967374ac52adc87dedac6f4ad458a7b6446.gz'>\n\t<name>German Umlauts in ISO8859-15 encoding: ???????</name>\n\t<comment>Ignore-this: 7c74b888addef772a93a63b69a144836</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110224141706' local_date='Thu Feb 24 15:17:06 CET 2011' inverted='False' hash='20110224141706-7677a-b79e15c79bd5776b3e669a7338e181b4bd303609.gz'>\n\t<name>German Umlauts in UTF-8 encoding: ??????????????</name>\n\t<comment>Ignore-this: 77565bbaae7ec954f242fd414ca70033</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203531' local_date='Mon Feb 14 21:35:31 CET 2011' inverted='False' hash='20110214203531-7677a-1b935a82ba6408ffa9add3642cb52f233ff4ef54.gz'>\n\t<name>Implemented toString()</name>\n\t<comment>Ignore-this: 7c0271b552e03728baa7d4f33cb545f9</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203417' local_date='Mon Feb 14 21:34:17 CET 2011' inverted='False' hash='20110214203417-7677a-261f33e2608d68f088f15b077f7dcde2cc18a4b7.gz'>\n\t<name>Implemented value in class Foo</name>\n\t<comment>Ignore-this: 79225cd08e4f7ec7dfc7a6cb4e7f5948</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203402' local_date='Mon Feb 14 21:34:02 CET 2011' inverted='False' hash='20110214203402-7677a-5eb558d8bd3df5b8edfa005479d8ff1e8139abe0.gz'>\n\t<name>Implemented value in class Baz</name>\n\t<comment>Ignore-this: fd9e1a81cc792fd826a128794e92ba64</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214203334' local_date='Mon Feb 14 21:33:34 CET 2011' inverted='False' hash='20110214203334-7677a-3f9e4a67068618fcfde454c0c097d1f8b96301df.gz'>\n\t<name>Implemented value in class Bar</name>\n\t<comment>Ignore-this: 40e46e42b5023572d7e45607df47cab1</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201649' local_date='Mon Feb 14 21:16:49 CET 2011' inverted='False' hash='20110214201649-7677a-9c3c62c42467fe20e75a9ab62e52441ef7cdc8ba.gz'>\n\t<name>Implemented class Foo</name>\n\t<comment>Ignore-this: 85d0cdf2679dfcab72469629d6a80945</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201618' local_date='Mon Feb 14 21:16:18 CET 2011' inverted='False' hash='20110214201618-7677a-631eee269c4a252d953fa2f61b1127874bbc57d4.gz'>\n\t<name>Implemented class Baz</name>\n\t<comment>Ignore-this: 751c6c8a858104b5b15833496fce9a2</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201544' local_date='Mon Feb 14 21:15:44 CET 2011' inverted='False' hash='20110214201544-7677a-1cd5bbbc18b5e78240a80c9d1576faaa63c9f7d7.gz'>\n\t<name>Implemented class Bar</name>\n\t<comment>Ignore-this: e2eb7de380585ad9e4cb9515d8b21621</comment>\n</patch>\n<patch author='ich@weltraumschaf.de' date='20110214201356' local_date='Mon Feb 14 21:13:56 CET 2011' inverted='False' hash='20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz'>\n\t<name>inital files added</name>\n\t<comment>Ignore-this: 391a1ff64b0f64546446368f2d45fbc8</comment>\n</patch>\n</changelog>\n".getBytes()));
                   //CHECKSTYLE:ON
    }

    @Test
    public void countChanges() throws URISyntaxException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.countChanges(prepareTestRepo()), is(10));
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
