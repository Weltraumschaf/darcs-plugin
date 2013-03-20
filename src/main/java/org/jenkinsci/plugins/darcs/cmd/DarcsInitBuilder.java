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
     * Initializes the {@link DarcsBaseCommandBuilder#command} with {@value #COMMAND}.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     */
    DarcsInitBuilder(final String darcsExe) {
        super(darcsExe, COMMAND);
    }

    @Override
    public DarcsCommand create() {
        return new DarcsCommand(createArgumentList());
    }

}
