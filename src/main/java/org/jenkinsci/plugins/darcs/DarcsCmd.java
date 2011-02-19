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

import hudson.Launcher.ProcStarter;
import hudson.util.ArgumentListBuilder;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsCmd {

    private final String      repoDir;
    private final ProcStarter proc;
    private final String      darcsExe;
    private final String[]    envs;

    public DarcsCmd(String repodir, ProcStarter proc, String[] envs) {
        this(repodir, proc, envs, "darcs");
    }

    public DarcsCmd(String repodir, ProcStarter proc, String[] envs, String darcsExe) {
        this.repoDir  = repodir;
        this.envs     = envs;
        this.proc     = proc;
        this.darcsExe = darcsExe;
    }

    public String changes(boolean xmlOutput, boolean summary, int last) throws DarcsCmdException {
        ArgumentListBuilder args = new ArgumentListBuilder();
        args.add(darcsExe).add("changes");

        if (xmlOutput) {
            args.add("--xml-output");
        }

        if (summary) {
            args.add("--summary");
        }

        if (0 < last) {
            args.add(String.format("--last=%1", last));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        proc.cmds(args)
            .envs(envs)
            .stdout(baos)
            .pwd(repoDir);

        try {
            int ret = proc.join();
            
            if (0 != ret) {
                throw new DarcsCmdException("can not do darcs changes on repo " + repoDir);
            }
        } catch (Exception $e) {
            throw new DarcsCmdException("can not do darcs changes on repo " + repoDir, $e);
        }
                
        return baos.toString();
    }

    public String changes() throws DarcsCmdException {
        return changes(true, true, 0);
    }

    public String changes(int last) throws DarcsCmdException {
        return changes(true, true, last);
    }

    public void pull() {
    }

    public void get() {
    }
}
