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
import java.io.File;
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
    public DarcsCommandFacade(final Launcher launcher, final Map<String, String> envs, final String darcsExe,
        final FilePath workingDir) {
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
    private DarcsProcStarter createProcessStarter() {
        final ProcStarter proc = launcher.launch();
        proc.envs(envs);
        proc.pwd(workingDir);
        return new DarcsProcStarter(proc);
    }

    /**
     * Return the summary XML from the last n patches.
     *
     * @param repo to get summary of
     * @param lastPatches how many patches to summarize
     * @return XML string
     * @throws DarcsCommadException if command execution fails
     */
    public String lastSummarizedChanges(final File repo, final int lastPatches) throws DarcsCommadException {
        return lastSummarizedChanges(repo.getAbsolutePath(), lastPatches);
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
    public String allSummarizedChanges(final File repo) throws DarcsCommadException {
        return allSummarizedChanges(repo.getAbsolutePath());
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
    public String allChanges(final File repo) throws DarcsCommadException {
        return allChanges(repo.getAbsolutePath());
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
    private String getChanges(final String repo, final boolean summarize, final int lastPatches)
        throws DarcsCommadException {
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
    public int countChanges(final File repo) throws DarcsCommadException {
        return countChanges(repo.getAbsolutePath());
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
     * @param destination to pull in
     * @param from remote repository to pull from
     * @throws DarcsCommadException if command execution fails
     */
    public void pull(final File destination, final File from) throws DarcsCommadException {
        pull(destination.getAbsolutePath(), from.getAbsolutePath());
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
        final DarcsProcStarter proc = createProcessStarter();
        cmd.execute(proc);
    }

    /**
     * Do a fresh checkout of a repository.
     *
     * @param repo where to checkout
     * @param from from where to get the repository
     * @throws DarcsCommadException if can't do checkout
     */
    public void get(final File repo, final File from) throws DarcsCommadException {
        get(repo.getAbsolutePath(), from.getAbsolutePath());
    }

    /**
     * Do a fresh checkout of a repository.
     *
     * @param to where to checkout
     * @param from from where to get the repository
     * @throws DarcsCommadException if can't do checkout
     */
    public void get(final String to, final String from) throws DarcsCommadException {
        final DarcsGetBuilder builder = DarcsCommand.builder(darcsExe).get();
        builder.from(from).to(to);
        final DarcsCommand cmd = builder.create();
        cmd.setOut(launcher.getListener().getLogger());
        final DarcsProcStarter proc = createProcessStarter();
        cmd.execute(proc);
    }

    /**
     * Determines if given repository is a Darcs repository.
     *
     * This is done by checking if in the given directory a subdirectory named "_darcs" exists.
     *
     * @param repository directory to check
     * @return true if given directory is a repository, else false
     */
    public boolean isRepository(final File repository) {
        if (!repository.exists()) {
            return false;
        }

        if (!repository.isDirectory()) {
            return false;
        }

        final File darcsDirectory = new File(repository, "_darcs");
        return darcsDirectory.exists() && darcsDirectory.isDirectory();
    }

    /**
     * Returns short version string like `darcs --version`.
     *
     * @return version string
     */
    public String version() throws DarcsCommadException {
        return version(false);
    }

    /**
     * Returns version of Darcs.
     *
     * @param exact whether to return short (`darcs --version`) version or exact version (`darcs --exact-version`).
     * @return version string
     */
    public String version(final boolean exact) throws DarcsCommadException {
        final DarcsCommandBuilder builder = DarcsCommand.builder(darcsExe);

        if (exact) {
            builder.exactVersion();
        } else {
            builder.version();
        }

        final DarcsCommand cmd = builder.create();
        cmd.execute(createProcessStarter());
        return cmd.getOut().toString();
    }

    /**
     * Initializes a directory as Darcs repository.
     *
     * @param directory where to initialize a repository
     */
    public void init(final File directory) throws DarcsCommadException {
        init(directory.getAbsolutePath());
    }

    /**
     * Initializes a directory as Darcs repository.
     *
     * @param directory where to initialize a repository
     */
    public void init(final String directory) throws DarcsCommadException {
        DarcsCommand.builder(darcsExe)
                    .init()
                    .repoDir(directory)
                    .create()
                    .execute(createProcessStarter());
    }
}
