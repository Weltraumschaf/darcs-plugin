/*
 *  LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 43):
 * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a non alcohol-free beer in return.
 *
 * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
 */

package org.jenkinsci.plugins.darcs;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import hudson.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import org.jenkinsci.plugins.darcs.cmd.DarcsCommandFacade;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * <a href="http://darcs.net/">Darcs</a> is a patch based distributed version control system.
 *
 * Contains the job configuration options as fields.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 * @author Ralph Lange <Ralph.Lange@gmx.de>
 */
public class DarcsScm2  extends SCM implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 4L;
    private static final String CHANGELOG_ROOT_TAG = "changelog";
    private static final String INFO_PREFIX = "INFO";
    private static final String WARNING_PREFIX = "WARNING";

    /**
     * Source repository URL from which we pull.
     */
    private final String source;
    /**
     * Local directory with repository.
     *
     * TODO Consider using {@link #getModuleRoot(hudson.FilePath, hudson.model.AbstractBuild)}.
     */
    private final String localDir;
    /**
     * Whether to wipe the checked out repository.
     */
    private final boolean clean;
    /**
     * Used repository browser.
     */
    private final RepositoryBrowser<DarcsChangeSet> browser;

    /**
     * Dedicated constructor.
     *
     * @param source repository URL from which we pull
     * @param localDir Local directory in the workspace
     * @param clean {@code true} cleans the workspace, {@code false} not
     * @param browser the browser used to browse the repository
     */
    @DataBoundConstructor
    //CHECKSTYLE:OFF
    public DarcsScm2(final String source, final String localDir, final boolean clean, final RepositoryBrowser<DarcsChangeSet> browser) {
    //CHECKSTYLE:ON
        super();
        this.source = source;
        this.clean = clean;
        this.browser = browser;
        this.localDir = localDir;
    }

    /**
     * Get the repositories source URL.
     *
     * @return URL as string
     */
    public String getSource() {
        return source;
    }

    /**
     * Get the local directory in the workspace.
     *
     * @return relative path as string
     */
    public String getLocalDir() {
        return localDir;
    }

    /**
     * Whether to clean the workspace or not.
     *
     * @return {@code true} if clean is performed, {@code false} else
     */
    public boolean isClean() {
        return clean;
    }

    @Override
    public RepositoryBrowser<DarcsChangeSet> getBrowser() {
        return browser;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new DarcsChangeLogParser();
    }

    @Override
    public DarcsScmDescriptor getDescriptor() {
        return (DarcsScmDescriptor) super.getDescriptor();
    }

    @Override
    public boolean supportsPolling() {
        // TODO Enable poling
        return false;
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    @Override
    //CHECKSTYLE:OFF
    public SCMRevisionState calcRevisionsFromBuild(final AbstractBuild<?, ?> build, final Launcher launcher, final TaskListener listener) throws IOException, InterruptedException {
    //CHECKSTYLE:ON
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    //CHECKSTYLE:OFF
    protected PollingResult compareRemoteRevisionWith(final AbstractProject<?, ?> project, final Launcher launcher, final FilePath workspace, final TaskListener listener, final SCMRevisionState baseline) throws IOException, InterruptedException {
    //CHECKSTYLE:ON
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    //CHECKSTYLE:OFF
    public boolean checkout(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) throws IOException, InterruptedException {
    //CHECKSTYLE:ON
        final FilePath localPath = createLocalPath(workspace);

        if (isClean()) {
            clean(localPath);
        }

        if (existsRepo(localPath)) {
            pullRepo(build, launcher, workspace, listener, changelogFile);
        } else {
            getRepo(build, launcher, workspace, listener, changelogFile);
        }

        return true; // In favor of indicating errors by throwing AbortException always return true.
    }

    /**
     * Creates a local path relative to the given base.
     *
     * If {@link #localDir} is not {@code null} and not empty a relative path to the given base is created, else the
     * base pat itself is returned. *
     *
     * @param base base of the local path
     * @return local file path.
     */
    FilePath createLocalPath(final FilePath base) {
        if (null != localDir && localDir.length() > 0) {
            return new FilePath(base, localDir);
        }

        return base;
    }

    /**
     * Checks if the given local path is a directory containing a Darcs repository.
     *
     * TODO Use {@link DarcsCommandFacade#isRepository()}.
     *
     * @param localPath local path to examine
     * @return {@code true} if it is a Darcs repository, else {@code false}
     * @throws IOException if I/O error happened
     * @throws InterruptedException if thread was interrupted
     */
    boolean existsRepo(final FilePath localPath) throws IOException, InterruptedException {
        return localPath.act(new FilePath.FileCallable<Boolean>() {
            public Boolean invoke(final File ws, final VirtualChannel channel) throws IOException {
                return new File(ws, "_darcs").exists();
            }
        });
    }

    /**
     * Delete given file recursively.
     *
     * @param localPath file which will be deleted recursively.
     * @throws AbortException if any error happened during operation
     */
    void clean(final FilePath localPath) throws AbortException {
        try {
            localPath.deleteRecursive();
        } catch (IOException ex) {
            abort(Messages.DarcsScm_failedToCleanTheWorkspace(), ex);
        } catch (InterruptedException ex) {
            abort(Messages.DarcsScm_failedToCleanTheWorkspace(), ex);
        }
    }

    /**
     * Throws {@link AbortException}.
     *
     * @param message for the exception
     * @throws AbortException always thrown
     */
    void abort(final String message) throws AbortException {
        abort(message, null);
    }

    /**
     * Throws {@link AbortException}.
     *
     * @param message for the exception
     * @param cause previous exception
     * @throws AbortException always thrown
     */
    void abort(final String message, final Throwable cause) throws AbortException {
        final StringBuilder extendedMessage = new StringBuilder(message);

        if (null != cause && null != cause.getMessage() && cause.getMessage().length() > 0) {
            extendedMessage.append(" (").append(cause.getMessage()).append(")");
        }

        final AbortException exception = new AbortException(extendedMessage.toString());

        if (null != cause) {
            exception.initCause(cause);
        }

        throw exception;
    }

    /**
     * Pulls all patches from a remote repository in the workspace repository.
     *
     * @param build associated build
     * @param launcher hides the difference between running programs locally vs remotely
     * @param workspace workspace of build
     * @param listener receives events that happen during some lengthy operation
     * @param changelogFile log of current changes
     * @throws AbortException if any error happened during execution
     */
    //CHECKSTYLE:OFF
    void pullRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) throws AbortException {
    //CHECKSTYLE:ON
        try {
            info(listener, Messages.DarcsScm_pullingRepoFrom(source));
            final int preCnt = countPatches(build, launcher, workspace, listener);
            info(listener, Messages.DarcsScm_countOfPatchesPrePullingIs(preCnt));
            final DarcsCommandFacade cmd = createCommand(build, launcher, workspace, listener);
            final FilePath localPath = createLocalPath(workspace);
            cmd.pull(localPath.getRemote(), source);
            final int postCnt = countPatches(build, launcher, workspace, listener);
            info(listener, Messages.DarcsScm_countOfPatchesPostPullingIs(postCnt));
            final int numPatches = postCnt - preCnt;
            createChangeLog(build, launcher, numPatches, workspace, changelogFile, listener);
        } catch (Exception e) {
            listener.error(Messages.DarcsScm_failedToPull(e));
        }
    }

    /**
     * Writes the change log of the last numPatches to the changeLog file.
     *
     * @param launcher hides the difference between running programs locally vs remotely
     * @param numPatches number of patches
     * @param workspace build workspace
     * @param changelogFile log of current changes
     * @param listener receives events that happen during some lengthy operation
     * @throws InterruptedException if thread was interrupted
     */
    //CHECKSTYLE:OFF
    void createChangeLog(final AbstractBuild<?, ?> build, final Launcher launcher, final int numPatches, final FilePath workspace, final File changelogFile, final BuildListener listener) throws InterruptedException, IOException {
    //CHECKSTYLE:ON
        if (0 == numPatches) {
            info(listener, "INFO: " + Messages.DarcsScm_createEmptyChangelog());
            createEmptyChangeLog(changelogFile, listener);
            return;
        }

        final DarcsCommandFacade cmd = createCommand(build, launcher, workspace, listener);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(changelogFile);
            final FilePath localPath = createLocalPath(workspace);
            final String changes = cmd.lastSummarizedChanges(localPath.getRemote(), numPatches);
            fos.write(changes.getBytes()); // XXX Consider encoding.
        } catch (Exception e) {
            final StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            warning(listener, Messages.DarcsScm_failedToGetLogFromRepo(w));
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    /**
     * Creates an empty change log file with root tag of {@link #CHANGELOG_ROOT_TAG}.
     *
     * @param changelogFile created change log
     * @param listener used for logging
     * @return {@code true} on success, else {@code false}
     */
    boolean createEmptyChangeLog(final File changelogFile, final BuildListener listener) {
        return createEmptyChangeLog(changelogFile, listener, CHANGELOG_ROOT_TAG);
    }

    /**
     * Counts the patches in a repository.
     *
     * @param build associated build
     * @param launcher hides the difference between running programs locally vs remotely
     * @param workspace build workspace
     * @param listener receives events that happen during some lengthy operation
     * @return number of patches, if an error occurred on counting 0 is returned
     */
    private int countPatches(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener) {
        try {
            final DarcsCommandFacade cmd = createCommand(build, launcher, workspace, listener);
            final FilePath localPath = createLocalPath(workspace);
            return cmd.countChanges(localPath.getRemote());
        } catch (Exception e) {
            listener.error(Messages.DarcsScm_failedToCountPatchesInWorkspace(e));
            return 0;
        }
    }

    /**
     * Gets a fresh copy of a remote repository.
     *
     * @param build associated build
     * @param launcher hides the difference between running programs locally vs remotely
     * @param workspace workspace of build
     * @param listener receives events that happen during some lengthy operation
     * @param changelogFile log of current changes
     * @throws AbortException if any error happened during execution
     */
    private void getRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) throws AbortException {
        try {
            final DarcsCommandFacade cmd = createCommand(build, launcher, workspace, listener);
            final FilePath localPath = createLocalPath(workspace);
            cmd.get(localPath.getRemote(), source);
            createEmptyChangeLog(changelogFile, listener);
        } catch (Exception ex) {
            abort(Messages.DarcsScm_failedToGetRepoFrom(source), ex);
        }
    }

    private DarcsCommandFacade createCommand(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener) throws IOException, InterruptedException {
        return new DarcsCommandFacade(
            launcher,
            build.getEnvironment(listener),
            getDescriptor().getDarcsExe(),
            workspace.getParent());
    }

    void info(final TaskListener listener, final String message) {
        log(listener, INFO_PREFIX, message);
    }

    void warning(final TaskListener listener, final String message) {
        log(listener, WARNING_PREFIX, message);
    }

    void log(final TaskListener listener, final String prefix, final String message) {
        listener.getLogger().printf("%s: %s", prefix, message);
    }
}
