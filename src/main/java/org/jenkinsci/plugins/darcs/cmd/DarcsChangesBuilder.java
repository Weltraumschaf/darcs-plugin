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
 * Builder for `darcs changes` command.
 *
 * Examples:
 * <pre>
 * `darcs changes --repo=REPDIR --xml-output [--summary] [--last=N]`
 * `darcs changes --repo=REPDIR --count`
 * </pre>
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
class DarcsChangesBuilder extends DarcsBaseCommandBuilder implements DarcsCommandCreator {

    /**
     * Darcs subcommand.
     */
    private static final String COMMAND = "changes";
    /**
     * Where to pull in.
     */
    private String repoDir = "";
    /**
     * Whether to create XML output.
     */
    private boolean xmlOutput;
    /**
     * Whether to print summary.
     */
    private boolean summary;
    /**
     * Whether to count patches.
     */
    private boolean count;
    /**
     * How many last changes.
     */
    private int last;

    /**
     * Initializes the {@link DarcsBaseCommandBuilder#command} with {@value #COMMAND}.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     */
    DarcsChangesBuilder(final String darcsExe) {
        super(darcsExe, COMMAND);
    }

    /**
     * Directory of repository to pull in.
     *
     * @param directory path to repository
     * @return the builder itself
     * CHECKSTYLE:OFF
     * @throws IllegalArgumentException if location is {@code null} or empty
     * CHECKSTYLE:ON
     */
    public DarcsChangesBuilder repoDir(final String directory) {
        Validate.notEmpty(directory);
        repoDir = directory;
        return this;
    }

    /**
     * Switch XML output on.
     *
     * @return the builder itself
     */
    public DarcsChangesBuilder xmlOutput() {
        xmlOutput = true;
        return this;
    }

    /**
     * Switch summary output on.
     *
     * @return the builder itself
     */
    public DarcsChangesBuilder summary() {
        summary = true;
        return this;
    }

    /**
     * Switch patch count on.
     *
     * If count is on all other options except {@link #repoDir} will be ignored.
     *
     * @return the builder itself
     */
    public DarcsChangesBuilder count() {
        count = true;
        return this;
    }

    /**
     * How many last patches to show changes for.
     *
     * @param amount amount of last patches
     * @return the builder itself
     */
    public DarcsChangesBuilder last(final int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("Amount must not be less than 1!");
        }

        last = amount;
        return this;
    }

    @Override
    public DarcsCommand create() {
        final ArgumentListBuilder arguments = createArgumentList();

        if (repoDir.length() > 0) {
            arguments.add(String.format("--repo=%s", repoDir));
        }

        if (count) {
            arguments.add("--count");
            return new DarcsCommand(arguments);
        }

        if (xmlOutput) {
            arguments.add("--xml-output");
        }

        if (summary) {
            arguments.add("--summary");
        }

        if (last > 0) {
            arguments.add(String.format("--last=%d", last));
        }

        return new DarcsCommand(arguments);
    }
}
