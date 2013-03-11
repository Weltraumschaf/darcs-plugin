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
    private OutputStream out = new ByteArrayOutputStream();
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
     * @throws DarcsCommadException if command execution fails
     */
    public void execute(final ProcStarter proc) throws DarcsCommadException {
        prepare(proc);
        int returnCode = 0;
        try {
            returnCode = proc.join();
        } catch (IOException ex) {
            throw new DarcsCommadException(ex);
        } catch (InterruptedException ex) {
            throw new DarcsCommadException(ex);
        }

        if (0 != returnCode) {
            throw new DarcsCommadException(String.format("Error on performing command: %s", args.toStringWithQuote()),
                                           returnCode);
        }
    }

    /**
     * Prepares the proc starter.
     *
     * @param proc proc starter to prepare
     */
    void prepare(final ProcStarter proc) {
        proc.cmds(args);
        proc.stdout(out);
        proc.stderr(err);
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
     * Set output stream for STDOUT.
     *
     * @param out output stream
     */
    public void setOut(final OutputStream out) {
        this.out = out;
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

    @Override
    public String toString() {
        return args.toStringWithQuote();
    }

}
