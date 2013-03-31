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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
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
    public DarcsScm2(final String source, final String localDir, final boolean clean, final RepositoryBrowser<DarcsChangeSet> browser) {
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
    public SCMRevisionState calcRevisionsFromBuild(final AbstractBuild<?, ?> build, final Launcher launcher, final TaskListener listener) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(final AbstractProject<?, ?> project, final Launcher launcher, final FilePath workspace, final TaskListener listener, final SCMRevisionState baseline) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public boolean checkout(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) throws IOException, InterruptedException {
        final FilePath localPath = createLocalPath(workspace);

        if (isClean()) {
            wipeRepo(localPath);
        }

        if (existsRepo(localPath)) {
            pullRepo(build, launcher, workspace, listener, changelogFile);
        } else {
            getRepo(build, launcher, workspace, listener, changelogFile);
        }

        return true; // In favor of indicating errors by AbortException always return true.
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

    boolean existsRepo(final FilePath localPath) throws IOException, InterruptedException {
        // TODO Use {@link DarcsCommandFacade#isRepository()}.
        return localPath.act(new FilePath.FileCallable<Boolean>() {
            public Boolean invoke(final File ws, final VirtualChannel channel) throws IOException {
                return new File(ws, "_darcs").exists();
            }
        });
    }

    private void wipeRepo(final FilePath localPath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void pullRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void getRepo(final AbstractBuild<?, ?> build, final Launcher launcher, final FilePath workspace, final BuildListener listener, final File changelogFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
