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
 * Builder for `darcs pull` command.
 *
 * Example: {@literal `darcs pull FROM [--repo=REPDIR] [--all] [--verbose]`}
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
class DarcsPullBuilder extends DarcsBaseCommandBuilder implements DarcsCommandCreator {

    /**
     * Darcs subcommand.
     */
    private static final String COMMAND = "pull";
    /**
     * From where to get.
     */
    private String from = "";
    /**
     * Where to pull in.
     */
    private String repoDir = "";
    /**
     * Whether to pull all patches.
     */
    private boolean all;
    /**
     * Whether to use verbose output.
     */
    private boolean verbose;

    /**
     * Initializes the {@link DarcsBaseCommandBuilder#command} with {@value #COMMAND}.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     */
    DarcsPullBuilder(final String darcsExe) {
        super(darcsExe, COMMAND);
    }

    /**
     * From where to pull into the repository.
     *
     * @param location file or URI
     * @return the builder itself
     * CHECKSTYLE:OFF
     * @throws IllegalArgumentException if location is {@code null} or empty
     * CHECKSTYLE:ON
     */
    public DarcsPullBuilder from(final String location) {
        Validate.notEmpty(location);
        from = location;
        return this;
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
    public DarcsPullBuilder repoDir(final String directory) {
        Validate.notEmpty(directory);
        repoDir = directory;
        return this;
    }

    /**
     * Pull all patches.
     *
     * @return the builder itself
     */
    public DarcsPullBuilder all() {
        all = true;
        return this;
    }

    /**
     * Use verbose output.
     *
     * @return the builder itself
     */
    public DarcsPullBuilder verbose() {
        verbose = true;
        return this;
    }

    @Override
    public DarcsCommand create() {
        Validate.notEmpty(from, "Set from where to pull the patches!");
        final ArgumentListBuilder arguments = createArgumentList();
        arguments.add(from);

        if (repoDir.length() > 0) {
            arguments.add(String.format("--repo=%s", repoDir));
        }

        if (all) {
            arguments.add("--all");
        }

        if (verbose) {
            arguments.add("--verbose");
        }

        return new DarcsCommand(arguments);
    }

}
