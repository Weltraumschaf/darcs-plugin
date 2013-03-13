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
    private ProcStarter createProcessStarter() {
        final ProcStarter proc = launcher.launch();
        proc.envs(envs);
        proc.pwd(workingDir);
        return proc;
    }

    /**
     * Return the summary XML from the last n patches.
     *
     * @param repo to get summary of
     * @param lastPatches how many patches to summarize
     * @return XML string
     * @throws DarcsCommadException if command execution fails
     */
    public String lastSummarizedChanges(final String repo, final int lastPatches) throws DarcsCommadException {
        return getChanges(repo, true, lastPatches);
    }

    /**
     * Return the summary XML from all patches.
     *
     * @param repo to get summary of
     * @return XML string
     * @throws DarcsCommadException if command execution fails
     */
    public String allSummarizedChanges(final String repo) throws DarcsCommadException {
        return getChanges(repo, true);
    }

    /**
     * Return all changes as extended (not summarized) XML.
     *
     * @param repo to get summary of
     * @return XML string
     * @throws DarcsCommadException if command execution fails
     */
    public String allChanges(final String repo) throws DarcsCommadException {
        return getChanges(repo, false);
    }

    /**
     * Get all changes from a repository.
     *
     * @param repo to get changes of
     * @param summarize whether to summarize changes or not
     * @return XML string
     * @throws DarcsCommadException if command execution fails
     */
    private String getChanges(final String repo, final boolean summarize) throws DarcsCommadException {
        return getChanges(repo, summarize, 0);
    }

    /**
     * Get changes from a repository.
     *
     * @param repo to get changes of
     * @param summarize whether to summarize changes or not
     * @param lastPatches if greater than 0, the number of last patches to get changes for
     * @return XML string
     * @throws DarcsCommadException if command execution fails
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
        cmd.execute(createProcessStarter());
        return cmd.getOut().toString();
    }

    /**
     * Count all changes in a repository.
     *
     * @param repo to count changes of
     * @return number of patches in repository
     * @throws DarcsCommadException if command execution fails
     */
    public int countChanges(final String repo) throws DarcsCommadException {
        final DarcsChangesBuilder builder = DarcsCommand.builder(darcsExe).changes();
        builder.repoDir(repo).count();
        final DarcsCommand cmd = builder.create();
        cmd.execute(createProcessStarter());
        final String count = cmd.getOut().toString().trim();
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
        final ProcStarter proc = createProcessStarter();
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
        final ProcStarter proc = createProcessStarter();
        proc.stdout(launcher.getListener());
        cmd.execute(proc);
    }
}
