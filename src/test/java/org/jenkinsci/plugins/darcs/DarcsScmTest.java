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
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import hudson.util.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
    @Rule public TemporaryFolder tmpDir = new TemporaryFolder();
    @Rule public ExpectedException thrown = ExpectedException.none();
    //CHECKSTYLE:ON

    private DarcsScm2 createSut() {
        return new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
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

    @Test @Ignore
    public void checkout_emptyWorkspace() {

    }

    @Test @Ignore
    public void checkout_emptyWorkspaceWithClean() {

    }

    @Test @Ignore
    public void checkout_notEmptyWorkspace() {

    }

    @Test @Ignore
    public void checkout_notEmptyWorkspaceWithClean() {

    }

    @Test @Ignore
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

}
