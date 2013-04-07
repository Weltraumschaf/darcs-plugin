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

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Before;

/**
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsCommandFacadeTest {

    //CHECKSTYLE:OFF
    @Rule public TemporaryFolder tmpDir = new TemporaryFolder();
    //CHECKSTYLE:ON
    private DarcsBinary darcsExe;

    @Before
    public void loadDarcsExe() throws URISyntaxException {
        darcsExe = DarcsBinary.determine();
        final File bin = darcsExe.getBin();
        assertThat(bin, is(notNullValue()));
        assertThat(bin.exists(), is(true));
    }

    private DarcsCommandFacade createSut() throws URISyntaxException {
        return new DarcsCommandFacade(
                new Launcher.LocalLauncher(TaskListener.NULL),
                new EnvVars(),
                darcsExe.getBin().getAbsolutePath(),
                new FilePath(tmpDir.getRoot()));
    }

    static String readResource(final String filename) throws URISyntaxException, IOException {
        return FileUtils.readFileToString(
            new File(DarcsCommandFacadeTest.class.getResource(filename).toURI()),
            "utf-8");
    }

    @Test
    public void lastSummarizedChanges_emptyRepo() throws URISyntaxException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File repo = DarcsRepository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.lastSummarizedChanges(repo.getAbsolutePath(), 3), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void lastSummarizedChanges_repo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final DarcsRepository repo = DarcsRepository.REPO;
        final File extractedRepo = repo.extractTo(tmpDir.getRoot());
        assertThat(extractedRepo, is(notNullValue()));
        assertThat(sut.lastSummarizedChanges(extractedRepo.getAbsolutePath(), 3),
                is(repo.lastSummarizedChanges(darcsExe.getVersion())));
    }

    @Test
    public void allSummarizedChanges_repo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final DarcsRepository repo = DarcsRepository.REPO;
        final File extractedRepo = repo.extractTo(tmpDir.getRoot());
        assertThat(extractedRepo, is(notNullValue()));
        assertThat(sut.allSummarizedChanges(extractedRepo.getAbsolutePath()),
                is(repo.allSummarizedChanges(darcsExe.getVersion())));

    }

    @Test
    public void allSummarizedChanges_emptyRepo() throws URISyntaxException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File repo = DarcsRepository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.allChanges(repo.getAbsolutePath()), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void allChanges_repo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final DarcsRepository repo = DarcsRepository.REPO;
        final File extractedRepo = repo.extractTo(tmpDir.getRoot());
        assertThat(extractedRepo, is(notNullValue()));
        assertThat(sut.allChanges(extractedRepo.getAbsolutePath()),
                is(repo.allChanges(darcsExe.getVersion())));
    }

    @Test
    public void allChanges_emptyRepo() throws URISyntaxException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File repo = DarcsRepository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.allChanges(repo.getAbsolutePath()), is("<changelog>\n</changelog>\n"));
    }

    @Test
    public void countChanges_repo() throws URISyntaxException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File repo = DarcsRepository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.countChanges(repo.getAbsolutePath()), is(6));
    }

    @Test
    public void countChanges_emptyRepo() throws URISyntaxException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File repo = DarcsRepository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        assertThat(sut.countChanges(repo.getAbsolutePath()), is(0));
    }

    @Test
    public void pull_repo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File origin = DarcsRepository.REPO.extractTo(tmpDir.getRoot());
        assertThat(origin, is(notNullValue()));
        final File desitnation = tmpDir.newFolder("destination");
        sut.init(desitnation);
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation), is(0));
        sut.pull(desitnation, origin);
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation), is(6));
    }

    @Test
    public void pull_emptyRepo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File origin = DarcsRepository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(origin, is(notNullValue()));
        final File desitnation = tmpDir.newFolder("destination");
        sut.init(desitnation);
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation), is(0));
        sut.pull(desitnation, origin);
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation), is(0));
    }

    @Test
    public void get_repo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File repo = DarcsRepository.REPO.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        final File desitnation = new File(tmpDir.getRoot(), "checkout");
        sut.get(desitnation.getAbsolutePath(), repo.getAbsolutePath());
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation.getAbsolutePath()), is(6));
    }

    @Test
    public void get_emptyRepo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        final File repo = DarcsRepository.EMPTY.extractTo(tmpDir.getRoot());
        assertThat(repo, is(notNullValue()));
        final File desitnation = new File(tmpDir.getRoot(), "checkout");
        sut.get(desitnation.getAbsolutePath(), repo.getAbsolutePath());
        assertThat(sut.isRepository(desitnation), is(true));
        assertThat(sut.countChanges(desitnation.getAbsolutePath()), is(0));
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
    public void version() throws URISyntaxException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.version(), is(DarcsBinary.determine().getVersion().getNormalVersion()));
    }

    @Test
    public void exactVersion() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        assertThat(sut.version(true), is(DarcsBinary.determine().getExactVersion()));
    }

    @Test
    public void init() throws URISyntaxException, DarcsCommadException {
        final DarcsCommandFacade sut = createSut();
        sut.init(tmpDir.getRoot());
        assertThat(sut.isRepository(tmpDir.getRoot()), is(true));
        assertThat(sut.countChanges(tmpDir.getRoot()), is(0));
    }

}
