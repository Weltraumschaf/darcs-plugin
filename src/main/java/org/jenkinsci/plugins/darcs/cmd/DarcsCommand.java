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

import hudson.Launcher;
import hudson.util.ArgumentListBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Abstracts Darcs command.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsCommand {

    /**
     * Argument list representing the command.
     */
    private final ArgumentListBuilder args;
    /**
     * Records STDOUT of command.
     */
    private final OutputStream out = new ByteArrayOutputStream();
    /**
     * Records STDERR of command.
     */
    private final OutputStream err = new ByteArrayOutputStream();

    /**
     * Default constructor.
     *
     * Not instantiated outside of the builders.
     *
     * @param args generated argument list from a builder
     */
    DarcsCommand(final ArgumentListBuilder args) {
        this.args = args;
    }

    /**
     * Main entry point to obtain a command builder.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     * @return always new instance
     */
    public static DarcsCommandBuilder builder(final String darcsExe) {
        return new DarcsCommandBuilder(darcsExe);
    }

    /**
     * Executes the command by joining the passed in process starter.
     *
     * @param proc used to join the command
     * @return the return code of the executed command
     * @throws IOException if there's an error launching/joining a process
     * @throws InterruptedException if a thread is waiting, sleeping, or otherwise occupied, and the thread is
     * interrupted, either before or during the activity
     */
    public int execute(final Launcher.ProcStarter proc) throws IOException, InterruptedException {
        proc.cmds(args);
        proc.stdout(out);
        proc.stderr(err);
        return proc.join();
    }

    /**
     * Get output stream which records STDOUT.
     *
     * @return reference to the output stream
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * Get output stream which records STDOUT.
     *
     * @return reference to the output stream
     */
    public OutputStream getErr() {
        return err;
    }

    /**
     * Get the arguments.
     *
     * @return reference of internal argument list object
     */
    ArgumentListBuilder getArgs() {
        return args;
    }

}
