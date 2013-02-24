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

//import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
//import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsCmd {

    private final Launcher launcher;
    private final String darcsExe;
    private final Map<String, String> envs;

    public DarcsCmd(final Launcher launcher, final Map<String, String> envs) {
        this(launcher, envs, "darcs");
    }

    public DarcsCmd(final Launcher launcher, final Map<String, String> envs, final String darcsExe) {
        super();
        this.envs = envs;
        this.launcher = launcher;
        this.darcsExe = darcsExe;
    }

    public ProcStarter createProc(final ArgumentListBuilder args) {
        final ProcStarter proc = launcher.launch();
        proc.cmds(args);
        proc.envs(envs);

        return proc;
    }

    private ByteArrayOutputStream getChanges(final String repo, final boolean summarize, final int n)
        throws DarcsCmdException {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
                .add("changes")
                .add("--repo=" + repo)
                .add("--xml-output");

        if (summarize) {
            args.add("--summary");
        }

        if (n != 0) {
            args.add("--last=" + n);
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

    public ByteArrayOutputStream lastSummarizedChanges(final String repo, final int n) throws DarcsCmdException {
        return getChanges(repo, true, n);
    }

    public ByteArrayOutputStream allSummarizedChanges(final String repo) throws DarcsCmdException {
        return getChanges(repo, true, 0);
    }

    public ByteArrayOutputStream allChanges(final String repo) throws DarcsCmdException {
        return getChanges(repo, false, 0);
    }

    public int countChanges(final String repo) throws DarcsCmdException {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
                .add("changes")
                .add("--repodir=" + repo)
                .add("--count");

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
                .add("pull")
                .add(from)
                .add("--repodir=" + repo)
                .add("--all")
                .add("--verbose");

        try {
            final ProcStarter proc = createProc(args);
            proc.stdout(this.launcher.getListener());
            final int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception ex) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, ex);
        }
    }

    public void get(final String repo, final String from) throws DarcsCmdException {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
                .add("get")
                .add(from)
                .add(repo);

        try {
            final ProcStarter proc = createProc(args);
            proc.stdout(this.launcher.getListener());
            final int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("Getting repo with args " + args.toStringWithQuote() + " returne " + ret);
            }
        } catch (Exception ex) {
            throw new DarcsCmdException("Can not get repo with args: " + args.toStringWithQuote(), ex);
        }
    }

    /**
     * Darcs command exception.
     */
    public static class DarcsCmdException extends Exception {

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
