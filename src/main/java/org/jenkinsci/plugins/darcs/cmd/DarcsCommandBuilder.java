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

/**
 * Main entry point.
 *
 * This builder creates the concrete sub builders of Darcs commands,
 * such as pull, get, changes etc.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public final class DarcsCommandBuilder extends DarcsBaseCommandBuilder {

    /**
     * Not instantiated outside of package.
     *
     * @param darcsExe name of Darcs executable, e.g. "darcs" or "/usr/local/bin/darcs"
     */
    DarcsCommandBuilder(final String darcsExe) {
        super(darcsExe);
    }

    /**
     * Create sub builder to build `darcs changes` command.
     *
     * @return always new instance
     */
    public DarcsChangesBuilder changes() {
        return new DarcsChangesBuilder(getDarcsExe());
    }

    /**
     * Create sub builder to build `darcs pull` command.
     *
     * @return always new instance
     */
    public DarcsPullBuilder pull() {
        return new DarcsPullBuilder(getDarcsExe());
    }

    /**
     * Create sub builder to build `darcs get` command.
     *
     * @return always new instance
     */
    public DarcsGetBuilder get() {
        return new DarcsGetBuilder(getDarcsExe());
    }

}
