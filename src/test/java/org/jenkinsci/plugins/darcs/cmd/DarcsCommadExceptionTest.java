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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsCommadExceptionTest {

    @Test
    public void createWithMessage() {
        final DarcsCommadException sut = new DarcsCommadException("foobar");
        assertThat(sut.getMessage(), is("foobar"));
        assertThat(sut.getReturnCode(), is(0));
        assertThat(sut.getCause(), is(nullValue()));
    }

    @Test
    public void createWithMessageAndReturnCode() {
        final DarcsCommadException sut = new DarcsCommadException("foobar", 5);
        assertThat(sut.getMessage(), is("foobar"));
        assertThat(sut.getReturnCode(), is(5));
        assertThat(sut.getCause(), is(nullValue()));
    }

    @Test
    public void createWithCause() {
        final Throwable cause = new Throwable();
        final DarcsCommadException sut = new DarcsCommadException(cause);
        assertThat(sut.getMessage(), is(cause.getClass().getName()));
        assertThat(sut.getReturnCode(), is(0));
        assertThat(sut.getCause(), is(sameInstance(cause)));
    }

    @Test
    public void createWithMessageAndCause() {
        final Throwable cause = new Throwable();
        final DarcsCommadException sut = new DarcsCommadException("foobar", cause);
        assertThat(sut.getMessage(), is("foobar"));
        assertThat(sut.getReturnCode(), is(0));
        assertThat(sut.getCause(), is(sameInstance(cause)));
    }

    @Test
    public void createWithMessageReturnCodeAndCause() {
        final Throwable cause = new Throwable();
        final DarcsCommadException sut = new DarcsCommadException("foobar", 5, cause);
        assertThat(sut.getMessage(), is("foobar"));
        assertThat(sut.getReturnCode(), is(5));
        assertThat(sut.getCause(), is(sameInstance(cause)));
    }

}
