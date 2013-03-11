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

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.util.ArgumentListBuilder;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.jenkinsci.plugins.darcs.cmd.DarcsChangesBuilder;
import org.jenkinsci.plugins.darcs.cmd.DarcsCommand;
import org.jenkinsci.plugins.darcs.cmd.DarcsGetBuilder;
import org.jenkinsci.plugins.darcs.cmd.DarcsPullBuilder;

/**
 * Abstracts the Darcs command.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsCmd {

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
    public DarcsCmd(final Launcher launcher, final Map<String, String> envs, final String darcsExe, final FilePath workingDir) {
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

    public ByteArrayOutputStream lastSummarizedChanges(final String repo, final int n) throws DarcsCmdException {
        return getChanges(repo, true, n);
    }

    public ByteArrayOutputStream allSummarizedChanges(final String repo) throws DarcsCmdException {
        return getChanges(repo, true);
    }

    public ByteArrayOutputStream allChanges(final String repo) throws DarcsCmdException {
        return getChanges(repo, false);
    }

    private ByteArrayOutputStream getChanges(final String repo, final boolean summarize) throws DarcsCmdException {
        return getChanges(repo, summarize, 0);
    }

    private ByteArrayOutputStream getChanges(final String repo, final boolean summarize, final int lastPatches)
            throws DarcsCmdException {
        final DarcsChangesBuilder builder = DarcsCommand.builder(darcsExe).changes();
        builder.repoDir(repo).xmlOutput();

        if (summarize) {
            builder.summary();
        }

        if (lastPatches > 0) {
            builder.last(lastPatches);
        }

        final DarcsCommand cmd = builder.create();

        try {
            if (0 != cmd.execute(createProc())) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception ex) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, ex);
        }

        return (ByteArrayOutputStream) cmd.getOut(); // TODO remove cast
    }

    public int countChanges(final String repo) throws DarcsCmdException {
        final DarcsChangesBuilder builder = DarcsCommand.builder(darcsExe).changes();
        builder.repoDir(repo).count();
        final DarcsCommand cmd = builder.create();

        try {
            if (0 != cmd.execute(createProc())) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception ex) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, ex);
        }

        return Integer.parseInt(cmd.getErr().toString().trim());
    }

    public void pull(final String repo, final String from) throws DarcsCmdException {
        final DarcsPullBuilder builder = DarcsCommand.builder(darcsExe).pull();
        builder.from(from).repoDir(repo).all().verbose();
        final DarcsCommand cmd = builder.create();
        cmd.setOut(launcher.getListener().getLogger());

        try {
            final ProcStarter proc = createProc();
            proc.stdout(this.launcher.getListener());
            final int ret = cmd.execute(proc);

            if (0 != ret) {
                throw new DarcsCmdException(String.format("Can't do darcs changes in repo %s! Return code: %d",
                        repo, ret));
            }
        } catch (Exception ex) {
            throw new DarcsCmdException(String.format("Can't do darcs changes in repo %s!", repo), ex);
        }
    }

    /**
     * Do a fresh checkout of a repository.
     *
     * FIXME Make a chdir into the repository directory.
     *
     * @param repo where to checkout
     * @param from from where to get the repository
     * @throws DarcsCmdException if can't do checkout
     */
    public void get(final String repo, final String from) throws DarcsCmdException {
        final DarcsGetBuilder builder = DarcsCommand.builder(darcsExe).get();
        builder.from(from).to(repo);
        final DarcsCommand cmd = builder.create();

        try {
            final ProcStarter proc = createProc();
            proc.stdout(this.launcher.getListener());
            final int ret = cmd.execute(proc);

            if (0 != ret) {
                throw new DarcsCmdException(String.format("Getting repo with args %s failed! Return code: %d",
                        cmd.toString(), ret));
            }
        } catch (Exception ex) {
            throw new DarcsCmdException(String.format("Can't get repo with args: %s", cmd.toString()), ex);
        }
    }

    /**
     * Darcs command exception.
     *
     * TODO rename to DarcsCommandException and move into cmd package.
     */
    public static class DarcsCmdException extends RuntimeException {

        /**
         * Creates exception with message.
         *
         * @param string exception message
         */
        public DarcsCmdException(final String string) {
            super(string);
        }

        /**
         * Creates exception with message and a previous exception.
         *
         * @param string exception message
         * @param thrwbl previous exception
         */
        public DarcsCmdException(final String string, final Throwable thrwbl) {
            super(string, thrwbl);
        }
    }
}
