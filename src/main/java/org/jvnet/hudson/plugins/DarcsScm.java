
package org.jvnet.hudson.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.Hudson;
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
import hudson.scm.SCMDescriptor;
import hudson.util.FormValidation;

import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.framework.io.ByteBuffer;

/**
 *
 */
public class DarcsScm extends SCM implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(DarcsScm.class.getName());
    
    /**
     * Source repository URL from which we pull.
     */
    private final String source;
    private final boolean clean;

    @DataBoundConstructor
    public DarcsScm(String source, boolean clean) {
        this.source = source;
        this.clean  = clean;
    }

    public String getSource() {
        return source;
    }

    public boolean isClean() {
        return clean;
    }
    
    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        PrintStream output = listener.getLogger();
        output.println("Getting local revision...");

        return new DarcsRevisionState();
    }

    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        boolean canUpdate = workspace.act(new FileCallable<Boolean>() {

            private static final long serialVersionUID = 1L;

            public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                File file = new File(ws, "_darcs");
                return file.exists();
            }
        });

        if (canUpdate && !isClean()) {
            return pullRepo(build, launcher, workspace, listener, changelogFile);
        } else {
            return getRepo(build, launcher, workspace, listener, changelogFile);
        }
    }

    private boolean pullRepo(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException, IOException {
        return false;
    }

    private boolean getRepo(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, File changelogFile) throws InterruptedException {
        return false;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> ap, Launcher lnchr, FilePath fp, TaskListener tl, SCMRevisionState scmrs) throws IOException, InterruptedException {
        PrintStream output = tl.getLogger();
        output.printf("Getting current remote revision...");

        final DarcsRevisionState remote = new DarcsRevisionState();
        final Change change;
        
        if (true) { // is changed?
            change = Change.SIGNIFICANT;
        } else {
            change = Change.NONE;
        }

        return new PollingResult(scmrs, remote, change);
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new DarcsChangeLogParser();
    }

    public static final class DescriptorImpl extends SCMDescriptor<DarcsScm> {
        @Extension
        public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
        private String darcsExe;
        private transient String version;

        private DescriptorImpl() {
            super(DarcsScm.class, null);
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
            
            return scm;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            darcsExe = req.getParameter("darcs.darcsExe");
            version  = null;
            save();

            return true;
        }

        public FormValidation doDarcsExeCheck(@QueryParameter final String value) throws IOException, ServletException {
            return FormValidation.validateExecutable(value, new FormValidation.FileValidator() {
                @Override public FormValidation validate(File exe) {
                    try {
                       ByteBuffer baos   = new ByteBuffer();
                       Launcher launcher = Hudson.getInstance().createLauncher(TaskListener.NULL);
                       ProcStarter proc  = launcher.launch()
                                                   .cmds(getDarcsExe(), "--version")
                                                   .stdout(baos);
                       if (proc.join() == 0) {
                          return FormValidation.ok();
                       } else {
                          return FormValidation.warning("Could not locate the executable in path");
                       }
                    } catch (IOException e) {
                        // failed
                    } catch (InterruptedException e) {
                        // failed
                    }

                    return FormValidation.error("Unable to check darcs version");
                }
            });
        }

        /**
         * UUID version string.
         * This appears to be used for snapshot builds. See issue #1683
         */
        private static final Pattern UUID_VERSION_STRING = Pattern.compile("\\(version ([0-9a-f]+)");
    }
}
