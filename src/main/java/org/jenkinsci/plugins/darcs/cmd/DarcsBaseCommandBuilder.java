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

import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.Validate;

/**
 * Common base implementation for sub builders.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
class DarcsBaseCommandBuilder {

    /**
     * Name of Darcs executable.
     *
     * Examples: "darcs" or "/usr/local/bin/darcs"
     */
    private final String darcsExe;
    /**
     * Subcommand such as `pull`, `get` etc.
     */
    private final String command;

    /**
     * Initializes {@link #command} with empty string.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     */
    DarcsBaseCommandBuilder(final String darcsExe) {
        this(darcsExe, "");
    }

    /**
     * Dedicated constructor.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     * @param command Darcs subcommand such as `pull`, `get` etc.
     */
    DarcsBaseCommandBuilder(final String darcsExe, final String command) {
        super();
        Validate.notEmpty(darcsExe, "Command must not be empty!");
        Validate.notNull(command, "Command must not be null!");
        this.darcsExe = darcsExe;
        this.command = command;
    }

    /**
     * Get the Darcs executable.
     *
     * @return string of the executable
     */
    String getDarcsExe() {
        return darcsExe;
    }

    /**
     * Generates base argument list with executable and subcommand.
     *
     * @return always new instance
     */
    ArgumentListBuilder createArgumentList() {
        final ArgumentListBuilder arguments = new ArgumentListBuilder();
        arguments.add(darcsExe);

        if (!command.isEmpty()) {
            arguments.add(command);
        }

        return arguments;
    }

}
