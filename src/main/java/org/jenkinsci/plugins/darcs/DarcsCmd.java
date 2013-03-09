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

import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.util.ArgumentListBuilder;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Abstracts the Darcs command.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsCmd {

    /**
     * `darcs changes` command.
     */
    private static final String CMD_CHANGES = "changes";
    /**
     * `darcs pull` command.
     */
    private static final String CMD_PULL = "pull";
    /**
     * `darcs get` command.
     */
    private static final String CMD_GET = "get";
    // Command options
    private static final String OPT_REPO = "--repo=";
    private static final String OPT_XML_OUTPUT = "--xml-output";
    private static final String OPT_SUMMARY = "--summary";
    private static final String OPT_LAST = "--last=";
    private static final String OPT_REPODIR = "--repodir=";
    private static final String OPT_COUNT = "--count";
    private static final String OPT_ALL = "--all";
    private static final String OPT_VERBOSE = "--verbose";
    private final Launcher launcher;
    private final String darcsExe;
    private final Map<String, String> envs;

    /**
     * Creates a Darcs command object.
     *
     * @param launcher used to start a process
     * @param envs environment variables
     * @param darcsExe executable name
     */
    public DarcsCmd(final Launcher launcher, final Map<String, String> envs, final String darcsExe) {
        super();
        this.envs = envs;
        this.launcher = launcher;
        this.darcsExe = darcsExe;
    }

    /**
     * Creates process starter.
     *
     * @param args builds argument list for command
     * @return a process starter object
     */
    public ProcStarter createProc(final ArgumentListBuilder args) {
        final ProcStarter proc = launcher.launch();
        proc.cmds(args);
        proc.envs(envs);

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

    private ByteArrayOutputStream getChanges(final String repo, final boolean summarize, final int n)
            throws DarcsCmdException {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
                .add(CMD_CHANGES)
                .add(OPT_REPO + repo)
                .add(OPT_XML_OUTPUT);

        if (summarize) {
            args.add(OPT_SUMMARY);
        }

        if (n > 0) {
            args.add(OPT_LAST + n);
        }

        final ProcStarter proc = createProc(args);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        proc.stdout(baos);

        try {
            final int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception ex) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, ex);
        }

        return baos;
    }

    public int countChanges(final String repo) throws DarcsCmdException {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
                .add(CMD_CHANGES)
                .add(OPT_REPODIR + repo)
                .add(OPT_COUNT);

        final ProcStarter proc = createProc(args);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        proc.stdout(baos);

        try {
            final int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception ex) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, ex);
        }

        return Integer.parseInt(baos.toString().trim());
    }

    public void pull(final String repo, final String from) throws DarcsCmdException {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
                .add(CMD_PULL)
                .add(from)
                .add(OPT_REPODIR + repo)
                .add(OPT_ALL)
                .add(OPT_VERBOSE);

        try {
            final ProcStarter proc = createProc(args);
            proc.stdout(this.launcher.getListener());
            final int ret = proc.join();

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
     * @throws DarcsCmd.DarcsCmdException if can't do checkout
     */
    public void get(final String repo, final String from) throws DarcsCmdException {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
                .add(CMD_GET)
                .add(from)
                .add(repo);

        try {
            final ProcStarter proc = createProc(args);
            proc.stdout(this.launcher.getListener());
            final int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException(String.format("Getting repo with args %s failed! Return code: %d",
                        args.toStringWithQuote(), ret));
            }
        } catch (Exception ex) {
            throw new DarcsCmdException(String.format("Can't get repo with args: %s", args.toStringWithQuote()), ex);
        }
    }

    /**
     * Darcs command exception.
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
