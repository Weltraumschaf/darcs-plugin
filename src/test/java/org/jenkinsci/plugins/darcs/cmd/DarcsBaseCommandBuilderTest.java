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
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsBaseCommandBuilderTest {

    public @Rule ExpectedException thrown = ExpectedException.none();

    @Test
    public void constructorThrowsExceptionOnEmtpyExe() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Command must not be empty!");
        new DarcsBaseCommandBuilderStub("");
    }

    @Test
    public void constructorThrowsExceptionOnNullExe() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Command must not be empty!");
        new DarcsBaseCommandBuilderStub(null);
    }

    @Test
    public void constructorThrowsExceptionOnNullCommand() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Command must not be null!");
        new DarcsBaseCommandBuilderStub("foo", null);
    }

    @Test
    public void createArgumentListWithEmptyCommand() {
        final DarcsBaseCommandBuilder sut = new DarcsBaseCommandBuilderStub("foo");
        assertThat(sut.getDarcsExe(), is("foo"));
        final ArgumentListBuilder args = sut.createArgumentList();
        assertThat(args.toList(), is(Arrays.asList("foo")));
    }

    @Test
    public void createArgumentListWithCommand() {
        final DarcsBaseCommandBuilder sut = new DarcsBaseCommandBuilderStub("foo", "bar");
        assertThat(sut.getDarcsExe(), is("foo"));
        final ArgumentListBuilder args = sut.createArgumentList();
        assertThat(args.toList(), is(Arrays.asList("foo", "bar")));
    }

    private static class DarcsBaseCommandBuilderStub extends DarcsBaseCommandBuilder {

        public DarcsBaseCommandBuilderStub(String darcsExe) {
            super(darcsExe);
        }

        public DarcsBaseCommandBuilderStub(String darcsExe, String command) {
            super(darcsExe, command);
        }
    }
}