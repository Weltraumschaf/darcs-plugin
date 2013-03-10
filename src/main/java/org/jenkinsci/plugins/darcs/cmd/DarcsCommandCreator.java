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
 * Builders implementing this interface can produce a {@link DarcsCommand}.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public interface DarcsCommandCreator {

    /**
     * Create the parameterized command object.
     *
     * @return creates always new instance
     */
    DarcsCommand create();

}
