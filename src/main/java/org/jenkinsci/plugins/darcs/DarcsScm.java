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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Darcs is a patch based distributed version control system.
 *
 * Contains the job configuration options as fields.
 *
 * @see http://darcs.net/
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 * @author Ralph Lange <Ralph.Lange@gmx.de>
 */
public class DarcsScm extends SCM implements Serializable {

    private static final long serialVersionUID = 2L;
    private static final Logger LOGGER = Logger.getLogger(DarcsScm.class.getName());
    /**
     * Reused parser.
     */
    private static final DarcsChangeLogParser CHANGELOG_PARSER = new DarcsChangeLogParser();
    /**
     * Reused log sanitizer.
     */
    private static final DarcsXmlSanitizer XML_SANITIZER = new DarcsXmlSanitizer();
    /**
     * Default local directory is current directory.
     */
    private static final String DEFAULT_LOCAL_DIR = ".";

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
    DarcsRepositoryBrowser browser;

    /**
     * Convenience constructor.
     *
     * Sets local directory to {@link #DEFAULT_LOCAL_DIR}, clean to {@code false} and browser to {@code null}.
     *
     * @param source repository URL from which we pull
     */
    public DarcsScm(final String source) {
        this(source, DEFAULT_LOCAL_DIR, false, null);
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
    public DarcsScm(final String source, final String localDir, final boolean clean, final DarcsRepositoryBrowser browser) {
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
    public DarcsRevisionState calcRevisionsFromBuild(final AbstractBuild<?, ?> build, final Launcher launcher, final TaskListener listener) throws IOException, InterruptedException {
        final FilePath localPath = new FilePath(build.getWorkspace(), localDir);
        final DarcsRevisionState local = getRevisionState(launcher, listener, localPath.getRemote());
        listener.getLogger()
                .println("[poll] Calculate revison from build " + local);

        return local;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(final AbstractProject<?, ?> project, final Launcher launcher, final FilePath workspace, final TaskListener listener, final SCMRevisionState baseline) throws IOException, InterruptedException {
        final PrintStream logger = listener.getLogger();
        final DarcsRevisionState localRevisionState;

        if (baseline instanceof DarcsRevisionState) {
            localRevisionState = (DarcsRevisionState) baseline;
        } else if (project.getLastBuild() != null) {
            localRevisionState = calcRevisionsFromBuild(project.getLastBuild(), launcher, listener);
        } else {
            localRevisionState = new DarcsRevisionState();
        }

        if (null != project.getLastBuild()) {
            logger.println("[poll] Last Build : #" + project.getLastBuild().getNumber());
        } else {
            // If we've never been built before, well, gotta build!
            logger.println("[poll] No previous build, so forcing an initial build.");

            return PollingResult.BUILD_NOW;
        }

        final Change change;
        final DarcsRevisionState remoteRevisionState = getRevisionState(launcher, listener, source);

        logger.printf("[poll] Current remote revision is %s. Local revision is %s.%n",
                remoteRevisionState, localRevisionState);

        if (SCMRevisionState.NONE == localRevisionState) {
            logger.println("[poll] Does not have a local revision state.");
            change = Change.SIGNIFICANT;
        } else if (localRevisionState.getClass() != DarcsRevisionState.class) {
            // appears that other instances of None occur - its not a singleton.
            // so do a (fugly) class check.
            logger.println("[poll] local revision state is not of type darcs.");
            change = Change.SIGNIFICANT;
        } else if (!remoteRevisionState.equals(localRevisionState)) {
            logger.println("[poll] Local revision state differs from remote.");

            if (remoteRevisionState.getChanges().size() < ((DarcsRevisionState) localRevisionState).getChanges().size()) {
                final FilePath ws = project.getLastBuild().getWorkspace();

                logger.printf("[poll] Remote repo has less patches than local: remote(%s) vs. local(%s). Will wipe workspace %s...%n",
                        remoteRevisionState.getChanges().size(),
                        ((DarcsRevisionState) localRevisionState).getChanges().size(),
                        (null != ws) ? ws.getRemote() : "null");

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
     * @param launcher
     * @param listener
     * @param repo
     * @return
     * @throws InterruptedException
     */
    protected DarcsRevisionState getRevisionState(final Launcher launcher, final TaskListener listener, final String repo) throws InterruptedException {
        DarcsRevisionState rev = null;
        final DarcsXmlSanitizer sani = new DarcsXmlSanitizer();
        final DarcsCmd cmd;

        if (null == launcher) {
            /* Create a launcher on master
             * todo better grab a launcher on 'any slave'
             */
            cmd = new DarcsCmd(new LocalLauncher(listener), EnvVars.masterEnvVars, getDescriptor().getDarcsExe());
        } else {
            cmd = new DarcsCmd(launcher, EnvVars.masterEnvVars, getDescriptor().getDarcsExe());
        }

        try {
            byte[] changes = cmd.allChanges(repo).toByteArray();
            XMLReader xr = XMLReaderFactory.createXMLReader();
            DarcsSaxHandler handler = new DarcsSaxHandler();
            StringReader input = new StringReader(sani.cleanse(changes));

            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(input));

            rev = new DarcsRevisionState(new DarcsChangeSetList(null, handler.getChangeSets()));
        } catch (Exception e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            LOGGER.log(Level.WARNING, "Failed to get revision state for repository: ", e);
        }

        return rev;
    }

    /**
     * Writes the change log of the last numPatches to the changeLog file.
     *
     * @param launcher
     * @param numPatches
     * @param workspace
     * @param changeLog
     * @throws InterruptedException
     */
    private void createChangeLog(final Launcher launcher, final int numPatches, final FilePath workspace, final File changeLog, final BuildListener listener) throws InterruptedException {
        if (0 == numPatches) {
            LOGGER.info("Creating empty changelog.");
            createEmptyChangeLog(changeLog, listener, "changelog");
            return;
        }

        final DarcsCmd cmd = new DarcsCmd(launcher, EnvVars.masterEnvVars,
                getDescriptor().getDarcsExe());

        try {
            FileOutputStream fos = new FileOutputStream(changeLog);
            FilePath localPath = new FilePath(workspace, localDir);
            ByteArrayOutputStream changes = cmd.lastSummarizedChanges(localPath.getRemote(), numPatches);

            changes.writeTo(fos);
            fos.close();
        } catch (Exception e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            LOGGER.log(Level.WARNING, "Failed to get log from repository: ", e);
        }
    }

    @Override
    public boolean checkout(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) throws IOException, InterruptedException {
        final FilePath localPath = new FilePath(workspace, localDir); // FIXME NPE
        final boolean existsRepoinWorkspace = localPath.act(new FileCallable<Boolean>() {
            private static final long serialVersionUID = 1L;

            public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                File file = new File(ws, "_darcs");

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
     * @param build
     * @param launcher
     * @param workspace
     * @param listener
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private int countPatches(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener) {
        try {
            DarcsCmd cmd = new DarcsCmd(launcher,
                    build.getEnvironment(listener),
                    getDescriptor().getDarcsExe());

            final FilePath localPath = new FilePath(workspace, localDir);
            return cmd.countChanges(localPath.getRemote());
        } catch (Exception e) {
            listener.error("Failed to count patches in workspace repo:%n", e.toString());
            return 0;
        }
    }

    /**
     * Pulls all patches from a remote repository in the workspace repository.
     *
     * @param build
     * @param launcher
     * @param workspace
     * @param listener
     * @param changelogFile
     * @return boolean
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean pullRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) throws InterruptedException, IOException {
        LOGGER.log(Level.INFO, "Pulling repo from: {0}", source);
        final int preCnt = countPatches(build, launcher, workspace, listener);
        LOGGER.log(Level.INFO, "Count of patches pre pulling is {0}", preCnt);

        try {
            final DarcsCmd cmd = new DarcsCmd(launcher,
                    build.getEnvironment(listener),
                    getDescriptor().getDarcsExe());
            final FilePath localPath = new FilePath(workspace, localDir);
            cmd.pull(localPath.getRemote(), source);
        } catch (Exception e) {
            listener.error("Failed to pull: " + e.toString());

            return false;
        }

        final int postCnt = countPatches(build, launcher, workspace, listener);
        LOGGER.log(Level.INFO, "Count of patches post pulling is {0}", preCnt);
        createChangeLog(launcher, postCnt - preCnt, workspace, changelogFile, listener);

        return true;
    }

    /**
     * Gets a fresh copy of a remote repository.
     *
     * @param build
     * @param launcher
     * @param workspace
     * @param listener
     * @param changelogFile
     * @return boolean
     * @throws InterruptedException
     */
    private boolean getRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changeLog) throws InterruptedException {
        LOGGER.log(Level.INFO, "Getting repo from: {0}", source);

        try {
            FilePath localPath = new FilePath(workspace, localDir);
            localPath.deleteRecursive();
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to clean the workspace"));
            return false;
        }

        try {
            DarcsCmd cmd = new DarcsCmd(launcher,
                    build.getEnvironment(listener),
                    getDescriptor().getDarcsExe());
            FilePath localPath = new FilePath(workspace, localDir);
            cmd.get(localPath.getRemote(), source);
        } catch (Exception e) {
            e.printStackTrace(listener.error("Failed to get repo from " + source));

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
        return new DarcsScmDescriptor();
    }

}
