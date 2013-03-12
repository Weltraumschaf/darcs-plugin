/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs.cmd;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.util.ArgumentListBuilder;
import java.util.Map;

/**
 * Facade for the Darcs command.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public final class DarcsCommandFacade {

    /**
     * Used to start a process.
     */
    private final Launcher launcher;
    /**
     * Name of the Darcs executable binary.
     */
    private final String darcsExe;
    /**
     * Environment variables.
     */
    private final Map<String, String> envs;
    /**
     * Working directory of Darcs command.
     */
    private final FilePath workingDir;

    /**
     * Creates a Darcs command object.
     *
     * @param launcher starts a process
     * @param envs environment variables
     * @param darcsExe executable name
     * @param workingDir working dir for darcs command
     */
    public DarcsCommandFacade(final Launcher launcher, final Map<String, String> envs, final String darcsExe, final FilePath workingDir) {
        super();
        this.envs = envs;
        this.launcher = launcher;
        this.darcsExe = darcsExe;
        this.workingDir = workingDir;
    }

    /**
     * Create proc starter w/o arguments.
     *
     * @return a new process starter object
     */
    private ProcStarter createProc() {
        return createProc(null);
    }

    /**
     * Creates process starter.
     *
     * @param args builds argument list for command
     * @return a new process starter object
     */
    private ProcStarter createProc(final ArgumentListBuilder args) {
        final ProcStarter proc = launcher.launch();
        if (null != args) {
            proc.cmds(args);
        }
        proc.envs(envs);
        proc.pwd(workingDir);
        return proc;
    }

    /**
     *
     * @param repo
     * @param n
     * @return
     * @throws DarcsCommadException
     */
    public String lastSummarizedChanges(final String repo, final int n) throws DarcsCommadException {
        return getChanges(repo, true, n);
    }

    /**
     *
     * @param repo
     * @return
     * @throws DarcsCommadException
     */
    public String allSummarizedChanges(final String repo) throws DarcsCommadException {
        return getChanges(repo, true);
    }

    /**
     *
     * @param repo
     * @return
     * @throws DarcsCommadException
     */
    public String allChanges(final String repo) throws DarcsCommadException {
        return getChanges(repo, false);
    }

    /**
     *
     * @param repo
     * @param summarize
     * @return
     * @throws DarcsCommadException
     */
    private String getChanges(final String repo, final boolean summarize) throws DarcsCommadException {
        return getChanges(repo, summarize, 0);
    }

    /**
     *
     * @param repo
     * @param summarize
     * @param lastPatches
     * @return
     * @throws DarcsCommadException
     */
    private String getChanges(final String repo, final boolean summarize, final int lastPatches) throws DarcsCommadException {
        final DarcsChangesBuilder builder = DarcsCommand.builder(darcsExe).changes();
        builder.repoDir(repo).xmlOutput();

        if (summarize) {
            builder.summary();
        }

        if (lastPatches > 0) {
            builder.last(lastPatches);
        }

        final DarcsCommand cmd = builder.create();
        cmd.execute(createProc());
        return cmd.getOut().toString();
    }

    /**
     *
     * @param repo
     * @return
     * @throws DarcsCommadException
     */
    public int countChanges(final String repo) throws DarcsCommadException {
        final DarcsChangesBuilder builder = DarcsCommand.builder(darcsExe).changes();
        builder.repoDir(repo).count();
        final DarcsCommand cmd = builder.create();
        cmd.execute(createProc());
        final String count = cmd.getErr().toString().trim();
        return count.length() > 0 ? Integer.parseInt(count) : 0;
    }

    /**
     * Pull all patches from remote.
     *
     * @param repo to pull in
     * @param from remote repository to pull from
     * @throws DarcsCommadException if command execution fails
     */
    public void pull(final String repo, final String from) throws DarcsCommadException {
        final DarcsPullBuilder builder = DarcsCommand.builder(darcsExe).pull();
        builder.from(from).repoDir(repo).all().verbose();
        final DarcsCommand cmd = builder.create();
        cmd.setOut(launcher.getListener().getLogger());
        final ProcStarter proc = createProc();
        proc.stdout(launcher.getListener());
        cmd.execute(proc);
    }

    /**
     * Do a fresh checkout of a repository.
     *
     * @param repo where to checkout
     * @param from from where to get the repository
     * @throws DarcsCommadException if can't do checkout
     */
    public void get(final String repo, final String from) throws DarcsCommadException {
        final DarcsGetBuilder builder = DarcsCommand.builder(darcsExe).get();
        builder.from(from).to(repo);
        final DarcsCommand cmd = builder.create();
        final ProcStarter proc = createProc();
        proc.stdout(launcher.getListener());
        cmd.execute(proc);
    }
}
