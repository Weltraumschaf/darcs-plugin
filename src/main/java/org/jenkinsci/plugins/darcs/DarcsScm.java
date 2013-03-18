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

import org.jenkinsci.plugins.darcs.cmd.DarcsCommandFacade;
import org.jenkinsci.plugins.darcs.browsers.DarcsRepositoryBrowser;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Launcher.LocalLauncher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import hudson.util.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * <a href="http://darcs.net/">Darcs</a> is a patch based distributed version control system.
 *
 * Contains the job configuration options as fields.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 * @author Ralph Lange <Ralph.Lange@gmx.de>
 */
public class DarcsScm extends SCM implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 3L;
    /**
     * Logging facility.
     */
    private static final Logger LOGGER = Logger.getLogger(DarcsScm.class.getName());
    /**
     * Source repository URL from which we pull.
     */
    private final String source;
    /**
     * Local directory with repository.
     */
    private final String localDir;
    /**
     * Whether to wipe the checked out repository.
     */
    private final boolean clean;
    /**
     * Used repository browser.
     */
    private final DarcsRepositoryBrowser browser;

    /**
     * Convenience constructor.
     *
     * Sets local directory to {@code ""}, clean to {@code false} and browser to {@code null}.
     *
     * @param source repository URL from which we pull
     */
    public DarcsScm(final String source) {
        this(source, "", false, null);
    }

    /**
     * Dedicated constructor.
     *
     * @param source repository URL from which we pull
     * @param localDir Local directory in the workspace
     * @param clean {@code true} cleans the workspace, {@code false} not
     * @param browser the browser used to browse the repository
     */
    @DataBoundConstructor
    public DarcsScm(final String source, final String localDir, final boolean clean,
        final DarcsRepositoryBrowser browser) {
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
    public DarcsRepositoryBrowser getBrowser() {
        return browser;
    }

    @Override
    public boolean supportsPolling() {
        return false;
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild(final AbstractBuild<?, ?> build, final Launcher launcher,
        final TaskListener listener) throws IOException, InterruptedException {
        final FilePath localPath = createLocalPath(build.getWorkspace());
        final DarcsRevisionState local = getRevisionState(
            launcher,
            listener,
            localPath.getRemote(),
            build.getWorkspace());

        if (null == local) {
            listener.getLogger().println(Messages.DarcsScm_gotNullAsRevisionState());
            return SCMRevisionState.NONE;
        }

        listener.getLogger().println(Messages.DarcsScm_calculateRevisionFromBuild(local));
        return local;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(final AbstractProject<?, ?> project, final Launcher launcher,
        final FilePath workspace, final TaskListener listener, final SCMRevisionState baseline)
        throws IOException, InterruptedException {
        final PrintStream logger = listener.getLogger();
        final SCMRevisionState localRevisionState;

        if (baseline instanceof DarcsRevisionState) {
            localRevisionState = (DarcsRevisionState) baseline;
        } else if (null != project && null != project.getLastBuild()) {
            localRevisionState = calcRevisionsFromBuild(project.getLastBuild(), launcher, listener);
        } else {
            localRevisionState = new DarcsRevisionState();
        }

        if (null != project && null != project.getLastBuild()) {
            logger.println(Messages.DarcsScm_lastBuild(project.getLastBuild().getNumber()));
        } else {
            // If we've never been built before, well, gotta build!
            logger.println(Messages.DarcsScm_noPReviousBuild());

            return PollingResult.BUILD_NOW;
        }

        final Change change;
        final DarcsRevisionState remoteRevisionState = getRevisionState(launcher, listener, source, workspace);

        logger.printf(Messages.DarcsScm_currentRemoteRevisionIsLocalIs(
                remoteRevisionState, localRevisionState));

        if (SCMRevisionState.NONE.equals(localRevisionState)) {
            logger.println(Messages.DarcsScm_doesNotHaveLocalRevisionState());
            change = Change.SIGNIFICANT;
        } else if (localRevisionState.getClass() != DarcsRevisionState.class) {
            // appears that other instances of None occur - its not a singleton.
            // so do a (fugly) class check.
            logger.println(Messages.DarcsScm_localRevisionStateIsNotOfTypeDarcs());
            change = Change.SIGNIFICANT;
        } else if (null != remoteRevisionState && !remoteRevisionState.equals(localRevisionState)) {
            logger.println(Messages.DarcsScm_localRevisionStateDiffersFromRemote());

            if (remoteRevisionState.getChanges().size()
                    < ((DarcsRevisionState) localRevisionState).getChanges().size()) {
                final FilePath ws = project.getLastBuild().getWorkspace();

                logger.println(Messages.DarcsScm_remoteRepoHasLessPatchesThanLocal(
                        remoteRevisionState.getChanges().size(),
                        ((DarcsRevisionState) localRevisionState).getChanges().size(),
                        (null != ws) ? ws.getRemote() : "null"));

                if (null != ws) {
                    ws.deleteRecursive();
                }
            }

            change = Change.SIGNIFICANT;
        } else {
            change = Change.NONE;
        }

        return new PollingResult(localRevisionState, remoteRevisionState, change);
    }

    /**
     * Calculates the revision state of a repository (local or remote).
     *
     * @param launcher hides the difference between running programs locally vs remotely
     * @param listener receives events that happen during some lengthy operation
     * @param repo repository location in the workspace
     * @param workspace the job workspace
     * @return revision state object
     * @throws InterruptedException if thread was interrupted
     */
    DarcsRevisionState getRevisionState(final Launcher launcher, final TaskListener listener, final String repo,
        final FilePath workspace)
        throws InterruptedException {
        final DarcsCommandFacade cmd;

        if (null == launcher) {
            /* Create a launcher on master
             * TODO better grab a launcher on 'any slave'
             */
            cmd = new DarcsCommandFacade(
                new LocalLauncher(listener),
                EnvVars.masterEnvVars,
                getDescriptor().getDarcsExe(),
                workspace);
        } else {
            cmd = new DarcsCommandFacade(
                launcher,
                EnvVars.masterEnvVars,
                getDescriptor().getDarcsExe(),
                workspace);
        }

        DarcsRevisionState rev = null;

        try {
            final String changes = cmd.allChanges(repo);
            rev = new DarcsRevisionState(((DarcsChangeLogParser) createChangeLogParser()).parse(changes));
        } catch (Exception e) {
            listener.getLogger().println(Messages.DarcsScm_failedToGetRevisionState(repo));
        }

        return rev;
    }

    /**
     * Writes the change log of the last numPatches to the changeLog file.
     *
     * @param launcher hides the difference between running programs locally vs remotely
     * @param numPatches number of patches
     * @param workspace build workspace
     * @param changeLog log of current changes
     * @param listener receives events that happen during some lengthy operation
     * @throws InterruptedException if thread was interrupted
     */
    private void createChangeLog(final Launcher launcher, final int numPatches, final FilePath workspace,
            final File changeLog, final BuildListener listener) throws InterruptedException {
        if (0 == numPatches) {
            LOGGER.info(Messages.DarcsScm_createEmptyChangelog()); // TODO consider using launchers log
            createEmptyChangeLog(changeLog, listener, "changelog");
            return;
        }

        final DarcsCommandFacade cmd = new DarcsCommandFacade(
            launcher,
            EnvVars.masterEnvVars,
            getDescriptor().getDarcsExe(),
            workspace.getParent());
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(changeLog);
            final FilePath localPath = createLocalPath(workspace);
            final String changes = cmd.lastSummarizedChanges(localPath.getRemote(), numPatches);
            fos.write(changes.getBytes());
        } catch (Exception e) {
            final StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            LOGGER.warning(Messages.DarcsScm_failedToGetLogFromRepo(w)); // TODO consider using launchers log
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    @Override
    public boolean checkout(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
            final BuildListener listener, final File changelogFile) throws IOException, InterruptedException {
        final FilePath localPath = createLocalPath(workspace);
        final boolean existsRepoinWorkspace = localPath.act(new FileCallable<Boolean>() {
            private static final long serialVersionUID = 1L;

            public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                final File file = new File(ws, "_darcs");
                return file.exists();
            }
        });

        if (existsRepoinWorkspace && !isClean()) {
            return pullRepo(build, launcher, workspace, listener, changelogFile);
        } else {
            return getRepo(build, launcher, workspace, listener, changelogFile);
        }
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
    private int countPatches(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
            final BuildListener listener) {
        try {
            final DarcsCommandFacade cmd = new DarcsCommandFacade(
                launcher,
                build.getEnvironment(listener),
                getDescriptor().getDarcsExe(),
                workspace.getParent());
            final FilePath localPath = createLocalPath(workspace);
            return cmd.countChanges(localPath.getRemote());
        } catch (Exception e) {
            listener.error(Messages.DarcsScm_failedToCountPatchesInWorkspace(e));
            return 0;
        }
    }

    /**
     * Pulls all patches from a remote repository in the workspace repository.
     *
     * @param build associated build
     * @param launcher hides the difference between running programs locally vs remotely
     * @param workspace build workspace
     * @param listener receives events that happen during some lengthy operation
     * @param changelogFile log of current changes
     * @return {@code true } on success, else {@code false}
     * @throws InterruptedException if thread was interrupted
     */
    private boolean pullRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
            final BuildListener listener, final File changelogFile) throws InterruptedException  {
        LOGGER.info(Messages.DarcsScm_pullingRepoFrom(source));  // TODO consider using launchers log
        final int preCnt = countPatches(build, launcher, workspace, listener);
        LOGGER.info(Messages.DarcsScm_countOfPatchesPrePullingIs(preCnt));  // TODO consider using launchers log

        try {
            final DarcsCommandFacade cmd = new DarcsCommandFacade(
                launcher,
                build.getEnvironment(listener),
                getDescriptor().getDarcsExe(),
                workspace.getParent());
            final FilePath localPath = createLocalPath(workspace);
            cmd.pull(localPath.getRemote(), source);
        } catch (Exception e) {
            listener.error(Messages.DarcsScm_failedToPull(e));
            return false;
        }

        final int postCnt = countPatches(build, launcher, workspace, listener);
        LOGGER.info(Messages.DarcsScm_countOfPatchesPostPullingIs(preCnt));  // TODO consider using launchers log
        createChangeLog(launcher, postCnt - preCnt, workspace, changelogFile, listener);

        return true;
    }

    /**
     * Gets a fresh copy of a remote repository.
     *
     * @param build associated build
     * @param launcher hides the difference between running programs locally vs remotely
     * @param workspace workspace of build
     * @param listener receives events that happen during some lengthy operation
     * @param changeLog log of current changes
     * @return {@code true } on success, else {@code false}
     * @throws InterruptedException if thread is interrupted
     */
    private boolean getRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace,
            final BuildListener listener, final File changeLog) throws InterruptedException {
        LOGGER.info(Messages.DarcsScm_gettingRepoFrom(source));  // TODO consider using launchers log

        try {
            final FilePath localPath = createLocalPath(workspace);
            localPath.deleteRecursive();
        } catch (IOException e) {
            e.printStackTrace(listener.error(Messages.DarcsScm_failedToCleanTheWorkspace()));
            return false;
        }

        try {
            final DarcsCommandFacade cmd = new DarcsCommandFacade(
                launcher, build.getEnvironment(listener),
                getDescriptor().getDarcsExe(),
                workspace.getParent());
            final FilePath localPath = createLocalPath(workspace);
            cmd.get(localPath.getRemote(), source);
        } catch (Exception e) {
            e.printStackTrace(listener.error(Messages.DarcsScm_failedToGetRepoFrom(source)));
            return false;
        }

        return createEmptyChangeLog(changeLog, listener, "changelog");
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new DarcsChangeLogParser();
    }

    @Override
    public DarcsScmDescriptor getDescriptor() {
        return (DarcsScmDescriptor) super.getDescriptor();
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
    private FilePath createLocalPath(final FilePath base) {
        if (null != localDir && localDir.length() > 0) {
            return new FilePath(base, localDir);
        }

        return base;
    }

}
