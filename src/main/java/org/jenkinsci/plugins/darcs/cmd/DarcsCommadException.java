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
 * Darcs command exception.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsCommadException extends RuntimeException {

    /**
     * Creates exception with message.
     *
     * @param string exception message
     */
    public DarcsCommadException(final String string) {
        super(string);
    }

    /**
     * Creates exception with message and a previous exception.
     *
     * @param string exception message
     * @param thrwbl previous exception
     */
    public DarcsCommadException(final String string, final Throwable thrwbl) {
        super(string, thrwbl);
    }

}
