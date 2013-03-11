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
import java.io.OutputStream;
import java.util.Map;
import org.jenkinsci.plugins.darcs.cmd.DarcsChangesBuilder;
import org.jenkinsci.plugins.darcs.cmd.DarcsCommadException;
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

    public OutputStream lastSummarizedChanges(final String repo, final int n) throws DarcsCommadException {
        return getChanges(repo, true, n);
    }

    public OutputStream allSummarizedChanges(final String repo) throws DarcsCommadException {
        return getChanges(repo, true);
    }

    public OutputStream allChanges(final String repo) throws DarcsCommadException {
        return getChanges(repo, false);
    }

    private OutputStream getChanges(final String repo, final boolean summarize) throws DarcsCommadException {
        return getChanges(repo, summarize, 0);
    }

    private OutputStream getChanges(final String repo, final boolean summarize, final int lastPatches) throws DarcsCommadException {
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
                // TODO throw inside DarcsCommand#execute
                throw new DarcsCommadException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception ex) {
            throw new DarcsCommadException("can not do darcs changes in repo " + repo, ex);
        }

        return cmd.getOut();
    }

    public int countChanges(final String repo) throws DarcsCommadException {
        final DarcsChangesBuilder builder = DarcsCommand.builder(darcsExe).changes();
        builder.repoDir(repo).count();
        final DarcsCommand cmd = builder.create();

        try {
            if (0 != cmd.execute(createProc())) {
                // TODO throw inside DarcsCommand#execute
                throw new DarcsCommadException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception ex) {
            throw new DarcsCommadException("can not do darcs changes in repo " + repo, ex);
        }

        return Integer.parseInt(cmd.getErr().toString().trim());
    }

    public void pull(final String repo, final String from) throws DarcsCommadException {
        final DarcsPullBuilder builder = DarcsCommand.builder(darcsExe).pull();
        builder.from(from).repoDir(repo).all().verbose();
        final DarcsCommand cmd = builder.create();
        cmd.setOut(launcher.getListener().getLogger());

        try {
            final ProcStarter proc = createProc();
            proc.stdout(this.launcher.getListener());
            final int ret = cmd.execute(proc);

            if (0 != ret) {
                // TODO throw inside DarcsCommand#execute
                throw new DarcsCommadException(String.format("Can't do darcs changes in repo %s! Return code: %d",
                        repo, ret));
            }
        } catch (Exception ex) {
            throw new DarcsCommadException(String.format("Can't do darcs changes in repo %s!", repo), ex);
        }
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

        try {
            final ProcStarter proc = createProc();
            proc.stdout(this.launcher.getListener());
            final int ret = cmd.execute(proc);

            if (0 != ret) {
                // TODO throw inside DarcsCommand#execute
                throw new DarcsCommadException(String.format("Getting repo with args %s failed! Return code: %d",
                        cmd.toString(), ret));
            }
        } catch (Exception ex) {
            throw new DarcsCommadException(String.format("Can't get repo with args: %s", cmd.toString()), ex);
        }
    }
}
