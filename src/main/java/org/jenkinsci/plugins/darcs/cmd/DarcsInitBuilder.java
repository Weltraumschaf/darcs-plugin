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
 * Builder for `darcs get` command.
 *
 * Example: {@literal `darcs init`}
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
final class DarcsInitBuilder extends DarcsBaseCommandBuilder implements DarcsCommandCreator {

    /**
     * Darcs subcommand.
     */
    private static final String COMMAND = "init";

    /**
     * Where to pull in.
     */
    private String repoDir = "";

    /**
     * Initializes the {@link DarcsBaseCommandBuilder#command} with {@value #COMMAND}.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     */
    DarcsInitBuilder(final String darcsExe) {
        super(darcsExe, COMMAND);
    }

    /**
     * Directory where to initialize repository.
     *
     * @param directory path to repository
     * @return the builder itself
     * CHECKSTYLE:OFF
     * @throws IllegalArgumentException if location is {@code null} or empty
     * CHECKSTYLE:ON
     */
    public DarcsInitBuilder repoDir(final String directory) {
        Validate.notEmpty(directory);
        repoDir = directory;
        return this;
    }

    @Override
    public DarcsCommand create() {
        final ArgumentListBuilder arguments = createArgumentList();

        if (repoDir.length() > 0) {
            arguments.add(String.format("--repo=%s", repoDir));
        }

        return new DarcsCommand(arguments);
    }

}
