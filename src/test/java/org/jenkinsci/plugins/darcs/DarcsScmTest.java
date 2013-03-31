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

import hudson.FilePath;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.Test;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.mock;

/**
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsScmTest {

    //CHECKSTYLE:OFF
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();
    //CHECKSTYLE:ON

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
        final DarcsScm2 sut = new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
        assertThat(sut.supportsPolling(), is(false));
    }

    @Test
    public void requiresWorkspaceForPolling() {
        final DarcsScm2 sut = new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
        assertThat(sut.requiresWorkspaceForPolling(), is(false));
    }

    @Test
    public void createChangeLogParser() {
        final DarcsScm2 sut = new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
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
        final DarcsScm2 sut = new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
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
        final DarcsScm2 sut = new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
        tmpDir.newFolder("_darcs");
        assertThat(sut.existsRepo(new FilePath(tmpDir.getRoot())), is(true));
    }

    @Test
    public void existsRepo_returnTrueInNotRepo() throws IOException, InterruptedException {
        final DarcsScm2 sut = new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
        assertThat(sut.existsRepo(new FilePath(tmpDir.getRoot())), is(false));
    }

}
