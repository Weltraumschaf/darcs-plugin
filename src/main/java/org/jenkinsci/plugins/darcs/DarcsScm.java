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
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.Util;
import hudson.model.Hudson;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCMDescriptor;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.framework.io.ByteBuffer;

/**
 * Darcs is a patch based distributed version controll system.
 *
 * Contains the job configuration options as fields.
 *
 * Feedback from mailing list:
 * stephen.alan.connolly@gmail.com:
 * <quote>
 * I think you would be better served by computing the sha1 or md5 of all the hashes as strings.  Relying on Collections.hashCode is dangerous.  Relying on String.hashCode, which is just:
 *
 *     public int hashCode() {
 *         int h = hash;
 *         if (h == 0) {
 *             int off = offset;
 *             char val[] = value;
 *             int len = count;
 *
 *             for (int i = 0; i < len; i++) {
 *                 h = 31*h + val[off++];
 *             }
 *             hash = h;
 *         }
 *         return h;
 *     }
 *
 * Is going to be a tad weak given that all the characters are from the set [0-9a-f-] i.e. there are only 17 out of 255.
 *
 * The List.hashCode speci is
 *     public int hashCode() {
 *         int hashCode = 1;
 *         Iterator<E> i = iterator();
 *         while (i.hasNext()) {
 *             E obj = i.next();
 *             hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
 *         }
 *         return hashCode;
 *     }
 *
 * Also I would recommend sorting the lists before hashing them.
 *
 * However, if you are looking for a short path to saying there is a difference, the Collections.sort(list1); Collections.sort(list2); if (list1.hashCode() != list2.hashCode()) check should be OK...
 *
 * Just remember that list1.hashCode() == list2.hashCode() does not in anyway claim that there is no change, so you will have to go down the long path anyway.
 * </quote>
 *
 * kkawaguchi@cloudbees.com:
 * <quote>
 * There's utility code in Jenkins that computes MD5 digest of arbitrary byte stream or string. I think that seems like a good and cheap enough hashing for this kind of purpose.
 * </quote>
 *
 * @see http://darcs.net/
 * 
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsScm extends SCM implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DarcsScm.class.getName());
    /**
     * Source repository URL from which we pull.
     */
    private final String source;
    /**
     * Whether to wipe the checked out repo.
     */
    private final boolean clean;
    DarcsRepositoryBrowser browser;

    public DarcsScm(String source) {
        this(source, false, null);
    }

    @DataBoundConstructor
    public DarcsScm(String source, boolean clean, DarcsRepositoryBrowser browser) {
        this.source  = source;
        this.clean   = clean;
        this.browser = browser;
    }

    public String getSource() {
        return source;
    }

    public boolean isClean() {
        return clean;
    }

    @Override
    public DarcsRepositoryBrowser getBrowser() {
        return browser;
    }

    @Override
    public boolean supportsPolling() {
        return true;
    }

    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    /**
     * darcs pull --dry --xml-output --repodir=REPO
     *
     * @param build
     * @param launcher
     * @param listener
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public DarcsRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        PrintStream output = listener.getLogger();
        output.println("Getting local revision...");
        DarcsRevisionState local = getRevisionState(launcher, 
                                                    listener,
                                                    build.getWorkspace().getRemote());
        output.println(local);
        
        return local;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> ap, Launcher launcher, FilePath fp, TaskListener listener, SCMRevisionState localRevisionState) throws IOException, InterruptedException {
        PrintStream output = listener.getLogger();
        final Change change;
        final DarcsRevisionState remote = getRevisionState(launcher, listener, source);
        
        output.printf("Getting current remote revision...");
        output.println(remote);
        output.printf("Baseline is %s.\n", localRevisionState);

        if ((localRevisionState == SCMRevisionState.NONE)
            // appears that other instances of None occur - its not a singleton.
            // so do a (fugly) class check.
            || (localRevisionState.getClass() != DarcsRevisionState.class)
            || (!remote.equals(localRevisionState))) {
            change = Change.SIGNIFICANT;
        } else {
            change = Change.NONE;
        }

        return new PollingResult(localRevisionState, remote, change);
    }

    /**
     * Calculates the revision state of a repository.
     *
     * @todo implement.
     * 
     * @param launcher
     * @param listener
     * @param root
     * @return
     * @throws InterruptedException
     */
    private DarcsRevisionState getRevisionState(Launcher launcher, TaskListener listener, String root) throws InterruptedException {
        DarcsRevisionState rev = null;

        return rev;
    }
    
    /**
     * Writes the cahngelog of the last numPatches to the changeLog file.
     * 
     * @param launcher
     * @param numPatches
     * @param workspace
     * @param changeLog
     * @throws InterruptedException
     */
    private void getLog(Launcher launcher, int numPatches, FilePath workspace, File changeLog) throws InterruptedException {
        try {
            int ret;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ProcStarter proc = launcher.launch().cmds(getDescriptor().getDarcsExe(), "changes", "--xml-output", "--last=" + numPatches, "--summary").envs(EnvVars.masterEnvVars).stdout(baos).pwd(workspace);
            ret = proc.join();

            if (ret != 0) {
                LOGGER.log(Level.WARNING, "darcs changes  --last=" + numPatches + " returned {0}", ret);
            } else {
                FileOutputStream fos = new FileOutputStream(changeLog);
                fos.write(baos.toByteArray());
                fos.close();
            }
        } catch (IOException e) {
            StringWriter w = new StringWriter();
            e.printStackTrace(new PrintWriter(w));
            LOGGER.log(Level.WARNING, "Failed to poll repository: ", e);
        }
    }

    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        boolean existsRepoinWorkspace = workspace.act(new FileCallable<Boolean>() {

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
     * Counts the patches in an repo.
     * 
     * @param build
     * @param launcher
     * @param workspace
     * @param listener
     * @return int
     * @throws InterruptedException
     * @throws IOException
     */
    private int countPatches(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener) throws InterruptedException, IOException {
        ByteBuffer baos = new ByteBuffer();
        ProcStarter proc = launcher.launch().cmds(getDescriptor().getDarcsExe(), "changes", "--count", "--repodir=" + workspace).envs(build.getEnvironment(listener)).stdout(baos);
        if (proc.join() != 0) {
            listener.error("Failed to count patches in workspace repo!");
            return 0;
        }

        return Integer.parseInt(baos.toString().trim());
    }

    /**
     * Pulls all patches from a remote repo in the workspace repo.
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
    private boolean pullRepo(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException, IOException {
        LOGGER.log(Level.INFO, "Pulling repo from: {0}", source);
        int preCnt = 0, postCnt = 0;

        try {
            preCnt = countPatches(build, launcher, workspace, listener);
            LOGGER.log(Level.INFO, "Count of patches pre pulling is {0}", preCnt);
            ProcStarter proc = launcher.launch().cmds(getDescriptor().getDarcsExe(), "pull", source, "--all", "--repodir=" + workspace).envs(build.getEnvironment(listener)).stdout(listener.getLogger()).pwd(workspace);

            if (proc.join() != 0) {
                listener.error("Failed to pull");

                return false;
            }

            postCnt = countPatches(build, launcher, workspace, listener);
            LOGGER.log(Level.INFO, "Count of patches post pulling is {0}", preCnt);
            getLog(launcher, postCnt - preCnt, workspace, changelogFile);
        } catch (IOException e) {
            listener.error("Failed to pull");

            return false;
        }

        return true;
    }

    /**
     * Gets a fresh copy of a remote repo.
     *
     * @param build
     * @param launcher
     * @param workspace
     * @param listener
     * @param changelogFile
     * @return boolean
     * @throws InterruptedException
     */
    private boolean getRepo(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException {
        LOGGER.log(Level.INFO, "Getting repo from: {0}", source);

        try {
            workspace.deleteRecursive();
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to clean the workspace"));
            return false;
        }

        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(getDescriptor().getDarcsExe(), "get");
        args.add(source, workspace.getRemote());

        try {
            ProcStarter proc = launcher.launch().cmds(args).envs(build.getEnvironment(listener)).stdout(listener.getLogger());

            if (proc.join() != 0) {
                listener.error("Failed to get " + source);

                return false;
            }
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to get " + source));

            return false;
        }

        return createEmptyChangeLog(changelogFile, listener, "changelog");
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new DarcsChangeLogParser();
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DescriptorImpl.DESCRIPTOR;
    }

    /**
     * Inner class of the SCM descitopr.
     *
     * Contains the global configuration options as fields.
     */
    public static final class DescriptorImpl extends SCMDescriptor<DarcsScm> {

        @Extension
        public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
        private String darcsExe;

        private DescriptorImpl() {
            super(DarcsScm.class, DarcsRepositoryBrowser.class);
            load();
        }

        public String getDisplayName() {
            return "Darcs";
        }

        public String getDarcsExe() {
            return (null == darcsExe) ? "darcs" : darcsExe;
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            DarcsScm scm = req.bindJSON(DarcsScm.class, formData);
            scm.browser = RepositoryBrowsers.createInstance(DarcsRepositoryBrowser.class,
                    req,
                    formData,
                    "browser");

            return scm;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            darcsExe = Util.fixEmpty(req.getParameter("darcs.darcsExe").trim());
            save();

            return true;
        }

        public FormValidation doDarcsExeCheck(@QueryParameter final String value) throws IOException, ServletException {
            return FormValidation.validateExecutable(value, new FormValidation.FileValidator() {

                @Override
                public FormValidation validate(File exe) {
                    try {
                        ByteBuffer baos = new ByteBuffer();
                        Launcher launcher = Hudson.getInstance().createLauncher(TaskListener.NULL);
                        ProcStarter proc = launcher.launch().cmds(getDarcsExe(), "--version").stdout(baos);
                        if (proc.join() == 0) {
                            return FormValidation.ok();
                        } else {
                            return FormValidation.warning("Could not locate the executable in path");
                        }
                    } catch (IOException e) {
                        // failed
                        return FormValidation.error(e.toString());
                    } catch (InterruptedException e) {
                        // failed
                        return FormValidation.error(e.toString());
                    }

                    //return FormValidation.error("Unable to check darcs version");
                }
            });
        }
    }
}
