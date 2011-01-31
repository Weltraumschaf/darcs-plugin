
package org.jvnet.hudson.plugins;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.SCM;
import hudson.scm.SCMRevisionState;

import java.io.PrintStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 */
public class DarcsSCM extends SCM implements Serializable {
    /**
     * Source repository URL from which we pull.
     */
    private final String source;

    @DataBoundConstructor
    public DarcsSCM(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> ab, Launcher lnchr, TaskListener tl) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean checkout(AbstractBuild<?, ?> ab, Launcher lnchr, FilePath fp, BuildListener bl, File file) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
