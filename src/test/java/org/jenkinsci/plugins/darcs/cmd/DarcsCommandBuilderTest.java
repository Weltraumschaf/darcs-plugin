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
import static org.hamcrest.CoreMatchers.is;
import org.junit.Ignore;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsCommandBuilderTest {

    private static final String DARCS_EXE = "foobar";
    private final DarcsCommandBuilder sut = new DarcsCommandBuilder(DARCS_EXE);

    @Test
    public void changesBuilderHasDarcsExe() {
        final DarcsChangesBuilder changesBuilder = sut.changes();
        assertThat(changesBuilder.getDarcsExe(), is(DARCS_EXE));
    }

    @Test
    public void pullBuilderHasDarcsExe() {
        final DarcsPullBuilder pullBuilder = sut.pull();
        assertThat(pullBuilder.getDarcsExe(), is(DARCS_EXE));
    }

    @Test
    public void getBuilderHasDarcsExe() {
        final DarcsGetBuilder getBuilder = sut.get();
        assertThat(getBuilder.getDarcsExe(), is(DARCS_EXE));
    }

    @Test
    public void builder() {
        final DarcsCommandBuilder builder = DarcsCommand.builder("foo");
        assertThat(builder.getDarcsExe(), is("foo"));
    }

    @Test @Ignore
    public void execute() {
        // TODO Implement test
    }
}
