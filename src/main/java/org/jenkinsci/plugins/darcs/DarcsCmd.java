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

    public class DarcsCmdException extends Exception {
        public DarcsCmdException(String string) {
            super(string);
        }

        public DarcsCmdException(String string, Throwable thrwbl) {
            super(string, thrwbl);
        }
    }

    private final Launcher            launcher;
    private final String              darcsExe;
    private final Map<String, String> envs;

    public DarcsCmd(Launcher launcher, Map<String, String> envs) {
        this(launcher, envs, "darcs");
    }

    public DarcsCmd(Launcher launcher, Map<String, String> envs, String darcsExe) {
        this.envs     = envs;
        this.launcher = launcher;
        this.darcsExe = darcsExe;
    }

    public ProcStarter createProc(ArgumentListBuilder args) {
        ProcStarter proc = launcher.launch();
        proc.cmds(args);
        proc.envs(envs);
        
        return proc;
    }

    public ByteArrayOutputStream lastSummarizedChanges(String repo, int n) throws DarcsCmdException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
            .add("changes")
            .add("--repodir=" + repo)
            .add("--xml-output")
            .add("--summary")
            .add("--last=" + n);


        ProcStarter proc = createProc(args);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        proc.stdout(baos);
        
        try {
            int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception $e) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, $e);
        }

        return baos;
    }

//    public ByteArrayOutputStream changes(String repo, boolean xmlOutput, boolean summary, int last) throws DarcsCmdException {
//        ArgumentListBuilder args = new ArgumentListBuilder();
//        args.add(darcsExe)
//            .add("changes")
//            .add("--repodir=" + repo);
//
//        if (xmlOutput) {
//            args.add("--xml-output");
//        }
//
//        if (summary) {
//            args.add("--summary");
//        }
//
//        if (0 < last) {
//            args.add(String.format("--last=%1", last));
//        }
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ProcStarter proc = createProc(args);
//        proc.stdout(baos);
//
//        try {
//            int ret = proc.join();
//
//            if (0 != ret) {
//                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
//            }
//        } catch (Exception $e) {
//            throw new DarcsCmdException("can not do darcs changes in repo " + repo, $e);
//        }
//
//        return baos;
//    }
//
//    public ByteArrayOutputStream changes(String repo) throws DarcsCmdException {
//        return changes(repo, true, true, 0);
//    }
//
//    public ByteArrayOutputStream changes(String repo, int last) throws DarcsCmdException {
//        return changes(repo, true, true, last);
//    }

    public int countChanges(String repo) throws DarcsCmdException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            ArgumentListBuilder args = new ArgumentListBuilder();
            args.add(darcsExe)
                .add("changes")
                .add("--repodir=" + repo)
                .add("--count");
            ProcStarter proc = createProc(args);
            proc.stdout(baos);
            int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception $e) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, $e);
        }

        return Integer.parseInt(baos.toString().trim());
    }
    
    public void pull(String repo, String from) throws DarcsCmdException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
            .add("pull")
            .add(from)
            .add("--repodir=" + repo)
            .add("--all")
            .add("--verbose");
        
        try {
            ProcStarter proc = createProc(args);
            proc.stdout(this.launcher.getListener());
            int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("can not do darcs changes in repo " + repo);
            }
        } catch (Exception $e) {
            throw new DarcsCmdException("can not do darcs changes in repo " + repo, $e);
        }
    }

    public void get(String repo, String from) throws DarcsCmdException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe)
            .add("get")
            .add(from)
            .add(repo);
        
        try {
            ProcStarter proc = createProc(args);
            proc.stdout(this.launcher.getListener());
            int ret = proc.join();

            if (0 != ret) {
                throw new DarcsCmdException("Getting repo with args " + args.toStringWithQuote() + " returne " + ret);
            }
        } catch (Exception $e) {
            throw new DarcsCmdException("Can not get repo with args: " + args.toStringWithQuote(), $e);
        }
    }
}
