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

package org.jenkinsci.plugins.darcs.cmd;

import hudson.Launcher.ProcStarter;
import hudson.util.ArgumentListBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorator for {@link hudson.Launcher.ProcStarter} for better testability.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsProcStarter {

    /**
     * Decorated object.
     *
     * May be {@value null}.
     */
    private final ProcStarter proc;

    /**
     * Initializes {@link #proc} with {@value null}.
     *
     * Objects instantiated with this constructor will doe nothing.
     */
    public DarcsProcStarter() {
        this(null);
    }

    /**
     * Dedicated constructor.
     *
     * @param proc decorated object
     */
    public DarcsProcStarter(final ProcStarter proc) {
        super();
        this.proc = proc;
    }

    /**
     * Delegates to {@link ProcStarter#cmds(hudson.util.ArgumentListBuilder)}, unless {@link #proc} is {@value null}.
     *
     * @param args command argument list
     * @return itself for method chaining
     */
    public DarcsProcStarter cmds(final ArgumentListBuilder args) {
        if (null != proc) {
            proc.cmds(args);
        }

        return this;
    }

    /**
     * Delegates to {@link ProcStarter#cmds(}, unless {@link #proc} is {@value null}.
     *
     * @return returns list of strings, if {@link #proc} is {@value null} empty list
     */
    public List<String> cmds() {
        if (null != proc) {
            return proc.cmds();
        }

        return new ArrayList<String>();
    }

    /**
     * Delegates to {@link ProcStarter#stdout(java.io.OutputStream)}, unless {@link #proc} is {@value null}.
     *
     * @param out stream for STDOUT
     * @return itself for method chaining
     */
    public DarcsProcStarter stdout(final OutputStream out) {
        if (null != proc) {
            proc.stdout(out);
        }

        return this;
    }

    /**
     * Delegates to {@link ProcStarter#stdout()}, unless {@link #proc} is {@value null}.
     *
     * @return output stream, or {@value null} if {@link #proc} is {@value null}
     */
    public OutputStream stdout() {
        if (null != proc) {
            return proc.stdout();
        }

        return null;
    }

    /**
     * Delegates to {@link ProcStarter#stderr(java.io.OutputStream)}, unless {@link #proc} is {@value null}.
     *
     * @param err stream for STDERR
     * @return itself for method chaining
     */
    public DarcsProcStarter stderr(final OutputStream err) {
        if (null != proc) {
            proc.stderr(err);
        }

        return this;
    }

    /**
     * Delegates to {@link ProcStarter#stderr()}, unless {@link #proc} is {@value null}.
     *
     * @return output stream, or {@value null} if {@link #proc} is {@value null}
     */
    public OutputStream stderr() {
        if (null != proc) {
            return proc.stderr();
        }

        return null;
    }

    /**
     * Delegates to {@link ProcStarter#join()}, unless {@link #proc} is {@code null}.
     *
     * @return process return code or {@value 0} if {@link #proc} is {@value null}
     * @throws IOException if IO error happened
     * @throws InterruptedException if thread was interrupted
     */
    public int join() throws IOException, InterruptedException {
        if (null != proc) {
            return proc.join();
        }

        return 0;
    }

}
