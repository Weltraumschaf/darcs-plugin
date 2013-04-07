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

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.StreamBuildListener;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import hudson.util.IOUtils;
import hudson.util.NullStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import org.jenkinsci.plugins.darcs.cmd.DarcsBinary;
import org.jenkinsci.plugins.darcs.cmd.DarcsCommadException;
import org.jenkinsci.plugins.darcs.cmd.DarcsCommandFacade;
import org.jenkinsci.plugins.darcs.cmd.DarcsRepository;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;

/**
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsScmTest {

    //CHECKSTYLE:OFF
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    //CHECKSTYLE:ON

    private final DarcsBinary darcsExe = DarcsBinary.determine();

    @Before public void createDarcsBinary() throws URISyntaxException {
        final DarcsBinary darcsExe = DarcsBinary.determine();
        final File bin = darcsExe.getBin();
        assertThat(bin, is(notNullValue()));
        assertThat(bin.exists(), is(true));
    }

    public DarcsScmDescriptor createDescriptor() throws URISyntaxException {
        return new DarcsScmDescriptor() {
            @Override
            public void load() {
                // Don't load anything from a in tests file.
            }

            @Override
            public String getDarcsExe() {
                try {
                    return darcsExe.getBin().getAbsolutePath();
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    private DarcsScm2 createSut() {
        return createSut("");
    }

    private DarcsScm2 createSut(final String source) {
        return new DarcsScm2(source, "", false, mock(RepositoryBrowser.class));
    }

    @Test
    public void initialization() throws MalformedURLException {
        final RepositoryBrowser browser = mock(RepositoryBrowser.class);
        final DarcsScm2 sut = new DarcsScm2("foo", "bar", true, browser);
        assertThat(sut.getSource(), is("foo"));
        assertThat(sut.getLocalDir(), is("bar"));
        assertThat(sut.isClean(), is(true));
        assertThat(sut.getBrowser(), is(sameInstance(browser)));
    }

    @Test
    public void supportsPolling() {
        final DarcsScm2 sut = createSut();
        assertThat(sut.supportsPolling(), is(false));
    }

    @Test
    public void requiresWorkspaceForPolling() {
        final DarcsScm2 sut = createSut();
        assertThat(sut.requiresWorkspaceForPolling(), is(false));
    }

    @Test
    public void createChangeLogParser() {
        final DarcsScm2 sut = createSut();
        final ChangeLogParser p1 = sut.createChangeLogParser();
        assertThat(p1, is(not(nullValue())));
        assertThat(p1, is(instanceOf(DarcsChangeLogParser.class)));
        final ChangeLogParser p2 = sut.createChangeLogParser();
        assertThat(p2, is(instanceOf(DarcsChangeLogParser.class)));
        assertThat(p2, is(not(nullValue())));
        assertThat(p1, is(not(sameInstance(p2))));
    }

    @Test
    public void createLocalPath_localDirIsNull() {
        final DarcsScm2 sut = new DarcsScm2("", null, false, mock(RepositoryBrowser.class));
        final FilePath base = new FilePath(new File("foo"));
        assertThat(sut.createLocalPath(base), is(sameInstance(base)));
    }

    @Test
    public void createLocalPath_localDirIsEmpty() {
        final DarcsScm2 sut = createSut();
        final FilePath base = new FilePath(new File("foo"));
        assertThat(sut.createLocalPath(base), is(sameInstance(base)));
    }

    @Test
    public void createLocalPath_localDirIsSet() {
        final String localDir = "bar";
        final DarcsScm2 sut = new DarcsScm2("", localDir, false, mock(RepositoryBrowser.class));
        final FilePath base = new FilePath(new File("foo"));
        assertThat(sut.createLocalPath(base), is(new FilePath(base, localDir)));
    }

    @Test
    public void existsRepo_returnTrueInRepo() throws IOException, InterruptedException {
        final DarcsScm2 sut = createSut();
        tmpDir.newFolder("_darcs");
        assertThat(sut.existsRepo(new FilePath(tmpDir.getRoot())), is(true));
    }

    @Test
    public void existsRepo_returnTrueInNotRepo() throws IOException, InterruptedException {
        final DarcsScm2 sut = new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
        assertThat(sut.existsRepo(new FilePath(tmpDir.getRoot())), is(false));
    }

    @Test
    public void abort_withoutCause() throws AbortException {
        final DarcsScm2 sut = createSut();
        final String message = "foobar";
        thrown.expect(AbortException.class);
        thrown.expectMessage(message);
        sut.abort(message);
    }

    @Test
    public void abort_withCauseButEmptyMessage() {
        final DarcsScm2 sut = createSut();
        final String message = "foobar";
        final Throwable cause = new RuntimeException();

        try {
            sut.abort(message, cause);
            fail("Expected exception not thrown!");
        } catch (AbortException ex) {
            assertThat(ex.getMessage(), is(message));
            assertThat(ex.getCause(), is(sameInstance(cause)));
        }
    }

    @Test
    public void abort_withCauseWithMessage() {
        final DarcsScm2 sut = createSut();
        final String message = "foobar";
        final Throwable cause = new RuntimeException("cause");

        try {
            sut.abort(message, cause);
            fail("Expected exception not thrown!");
        } catch (AbortException ex) {
            assertThat(ex.getMessage(), is(message + " (cause)"));
            assertThat(ex.getCause(), is(sameInstance(cause)));
        }
    }

    @Test
    public void clean_emptyDirectory() throws AbortException, IOException, InterruptedException {
        final FilePath localPath = new FilePath(tmpDir.getRoot());
        assertThat(localPath.exists(), is(true));
        assertThat(localPath.list().size(), is(0));
        final DarcsScm2 sut = createSut();
        sut.clean(localPath);
        assertThat(localPath.exists(), is(false));
    }

    @Test
    public void clean_nonEmptyDirectory() throws AbortException, IOException, InterruptedException {
        tmpDir.newFolder();
        tmpDir.newFolder();
        tmpDir.newFile();
        tmpDir.newFile();
        final FilePath localPath = new FilePath(tmpDir.getRoot());
        assertThat(localPath.exists(), is(true));
        assertThat(localPath.list().size(), is(4));
        final DarcsScm2 sut = createSut();
        sut.clean(localPath);
        assertThat(localPath.exists(), is(false));
    }

    @Test
    public void info() {
        final TaskListener listener = mock(TaskListener.class);
        when(listener.getLogger()).thenReturn(mock(PrintStream.class));
        final DarcsScm2 sut = spy(createSut());
        sut.info(listener, "foobar");
        verify(sut).log(listener, "INFO", "foobar");
    }

    @Test
    public void warning() {
        final TaskListener listener = mock(TaskListener.class);
        when(listener.getLogger()).thenReturn(mock(PrintStream.class));
        final DarcsScm2 sut = spy(createSut());
        sut.warning(listener, "foobar");
        verify(sut).log(listener, "WARNING", "foobar");
    }

    @Test
    public void log() {
        final TaskListener listener = mock(TaskListener.class);
        final PrintStream stream = mock(PrintStream.class);
        when(listener.getLogger()).thenReturn(stream);
        final DarcsScm2 sut = createSut();
        sut.log(listener, "foo", "bar");
        verify(stream, times(1)).printf("%s: %s", "foo", "bar");
    }

    @Test
    public void checkout_emptyInWorkspace() throws IOException, InterruptedException {
        final FilePath workspace = new FilePath(tmpDir.getRoot());
        final AbstractBuild build = mock(AbstractBuild.class);
        final Launcher.LocalLauncher launcher = new Launcher.LocalLauncher(TaskListener.NULL);
        final StreamBuildListener listener = new StreamBuildListener(new NullStream());
        final File changeLog = tmpDir.newFile();
        final DarcsScm2 sut = spy(createSut());
        doReturn(false).when(sut).isClean();
        doReturn(false).when(sut).existsRepo(workspace);
        doNothing().when(sut).getRepo(build, launcher, workspace, listener, changeLog);
        assertThat(sut.checkout(build, launcher, workspace, listener, changeLog), is(true));
        verify(sut, times(1)).createLocalPath(workspace);
        verify(sut, times(0)).clean(workspace);
        verify(sut, times(1)).getRepo(build, launcher, workspace, listener, changeLog);
    }

    @Test
    public void checkout_emptyInWorkspaceWithClean() throws IOException, InterruptedException {
        final FilePath workspace = new FilePath(tmpDir.getRoot());
        final AbstractBuild build = mock(AbstractBuild.class);
        final Launcher.LocalLauncher launcher = new Launcher.LocalLauncher(TaskListener.NULL);
        final StreamBuildListener listener = new StreamBuildListener(new NullStream());
        final File changeLog = tmpDir.newFile();
        final DarcsScm2 sut = spy(createSut());
        doReturn(true).when(sut).isClean();
        doReturn(false).when(sut).existsRepo(workspace);
        doNothing().when(sut).getRepo(build, launcher, workspace, listener, changeLog);
        assertThat(sut.checkout(build, launcher, workspace, listener, changeLog), is(true));
        verify(sut, times(1)).createLocalPath(workspace);
        verify(sut, times(1)).clean(workspace);
        verify(sut, times(1)).getRepo(build, launcher, workspace, listener, changeLog);
    }

    @Test
    public void checkout_notInEmptyWorkspace() throws IOException, InterruptedException {
        final FilePath workspace = new FilePath(tmpDir.getRoot());
        final AbstractBuild build = mock(AbstractBuild.class);
        final Launcher.LocalLauncher launcher = new Launcher.LocalLauncher(TaskListener.NULL);
        final StreamBuildListener listener = new StreamBuildListener(new NullStream());
        final File changeLog = tmpDir.newFile();
        final DarcsScm2 sut = spy(createSut());
        doReturn(false).when(sut).isClean();
        doReturn(true).when(sut).existsRepo(workspace);
        doNothing().when(sut).getRepo(build, launcher, workspace, listener, changeLog);
        assertThat(sut.checkout(build, launcher, workspace, listener, changeLog), is(true));
        verify(sut, times(1)).createLocalPath(workspace);
        verify(sut, times(0)).clean(workspace);
        verify(sut, times(1)).pullRepo(build, launcher, workspace, listener, changeLog);
    }

    @Test
    public void checkout_notInEmptyWorkspaceWithClean() throws IOException, InterruptedException {
        final FilePath workspace = new FilePath(tmpDir.getRoot());
        final AbstractBuild build = mock(AbstractBuild.class);
        final Launcher.LocalLauncher launcher = new Launcher.LocalLauncher(TaskListener.NULL);
        final StreamBuildListener listener = new StreamBuildListener(new NullStream());
        final File changeLog = tmpDir.newFile();
        final DarcsScm2 sut = spy(createSut());
        doReturn(true).when(sut).isClean();
        doReturn(true).when(sut).existsRepo(workspace);
        doNothing().when(sut).getRepo(build, launcher, workspace, listener, changeLog);
        assertThat(sut.checkout(build, launcher, workspace, listener, changeLog), is(true));
        verify(sut, times(1)).createLocalPath(workspace);
        verify(sut, times(1)).clean(workspace);
        verify(sut, times(1)).pullRepo(build, launcher, workspace, listener, changeLog);
    }

    @Test
    @Ignore
    public void createChangeLog() {
    }

    @Test
    @SuppressWarnings("OBL_UNSATISFIED_OBLIGATION")
    public void createEmptyChangeLog() throws IOException {
        final File changelogFile = tmpDir.newFile();
        final DarcsScm2 sut = createSut();
        assertThat(sut.createEmptyChangeLog(changelogFile, mock(BuildListener.class)), is(true));
        final InputStream in = new FileInputStream(changelogFile);
        assertThat(IOUtils.toString(in), is("<changelog/>"));
        IOUtils.closeQuietly(in);
    }

    @Test
    public void pullRepo() throws URISyntaxException, IOException, DarcsCommadException {
        final DarcsRepository repo = DarcsRepository.REPO;
        final File source = repo.extractTo(tmpDir.getRoot());
        final DarcsScm2 sut = spy(createSut(source.getAbsolutePath()));
        doReturn(createDescriptor()).when(sut).getDescriptor();
        final File workspace = DarcsRepository.EMPTY.extractTo(tmpDir.getRoot());
        final File changeLogFile = tmpDir.newFile();

        sut.pullRepo(
            mock(AbstractBuild.class),
            new Launcher.LocalLauncher(TaskListener.NULL),
            new FilePath(workspace),
            new StreamBuildListener(new NullStream()),
            changeLogFile);
        final DarcsCommandFacade cmd = new DarcsCommandFacade(
            new Launcher.LocalLauncher(TaskListener.NULL),
            new EnvVars(),
            darcsExe.getBin().getAbsolutePath(),
            new FilePath(tmpDir.getRoot()));
        assertThat(cmd.allChanges(workspace), is(repo.allChanges(darcsExe.getVersion())));
        assertThat(IOUtils.toString(new FileInputStream(changeLogFile)),
                is(repo.allSummarizedChanges(darcsExe.getVersion())));
    }

    @Test
    public void getRepo() throws IOException, URISyntaxException, DarcsCommadException {
        final DarcsRepository repo = DarcsRepository.REPO;
        final File source = repo.extractTo(tmpDir.getRoot());
        final DarcsScm2 sut = spy(createSut(source.getAbsolutePath()));
        doReturn(createDescriptor()).when(sut).getDescriptor();
        final File workspace = new File(tmpDir.getRoot(), "workspace");
        final File changeLogFile = tmpDir.newFile();

        sut.getRepo(
            mock(AbstractBuild.class),
            new Launcher.LocalLauncher(TaskListener.NULL),
            new FilePath(workspace),
            new StreamBuildListener(new NullStream()),
            changeLogFile);
        final DarcsCommandFacade cmd = new DarcsCommandFacade(
            new Launcher.LocalLauncher(TaskListener.NULL),
            new EnvVars(),
            darcsExe.getBin().getAbsolutePath(),
            new FilePath(tmpDir.getRoot()));
        assertThat(cmd.isRepository(workspace), is(true));
        assertThat(cmd.allChanges(workspace), is(repo.allChanges(darcsExe.getVersion())));
        assertThat(IOUtils.toString(new FileInputStream(changeLogFile)), is("<changelog/>"));
    }
}
