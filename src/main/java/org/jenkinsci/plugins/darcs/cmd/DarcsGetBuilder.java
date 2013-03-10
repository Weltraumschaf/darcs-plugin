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
 * Example: {@literal `darcs get FROM TO`}
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsGetBuilder extends DarcsBaseCommandBuilder implements DarcsCommandCreator {

    /**
     * Darcs subcommand.
     */
    private static final String COMMAND = "get";
    /**
     * From where to get.
     */
    private String from = "";
    /**
     * To where to get.
     */
    private String to = "";

    /**
     * Initializes the {@link DarcsBaseCommandBuilder#command} with {@value #COMMAND}.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     */
    DarcsGetBuilder(final String darcsExe) {
        super(darcsExe, COMMAND);
    }

    /**
     * From where to get the repository.
     *
     * @param location file or URI
     * @return the builder itself
     * CHECKSTYLE:OFF
     * @throws IllegalArgumentException if location is {@code null} or empty
     * CHECKSTYLE:ON
     */
    public DarcsGetBuilder from(final String location) {
        Validate.notEmpty(location);
        from = location;
        return this;
    }

    /**
     * To where the repository will be checked out.
     *
     * @param location file path
     * @return the builder itself
     * CHECKSTYLE:OFF
     * @throws IllegalArgumentException if location is {@code null}
     * CHECKSTYLE:ON
     */
    public DarcsGetBuilder to(final String location) {
        Validate.notNull(location);
        to = location;
        return this;
    }

    @Override
    public DarcsCommand create() {
        Validate.notEmpty(from, "Set from where to get the repo!");
        final ArgumentListBuilder arguments = createArgumentList();
        arguments.add(from);

        if (to.length() > 0) {
            arguments.add(to);
        }

        return new DarcsCommand(arguments);
    }

}
