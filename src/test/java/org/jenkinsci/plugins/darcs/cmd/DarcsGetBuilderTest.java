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
public class DarcsGetBuilderTest {

    //CHECKSTYLE:OFF
    @Rule public ExpectedException thrown = ExpectedException.none();
    //CHECKSTYLE:ON
    private final DarcsGetBuilder sut = new DarcsGetBuilder("foo");

    @Test
    public void callFromWithEmptyThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        sut.from("");
    }

    @Test
    public void callFromWithNullThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        sut.from(null);
    }

    @Test
    public void callToWithNullThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        sut.to(null);
    }

    @Test
    public void createWithoutFromThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Set from where to get the repo!");
        sut.create();
    }

    @Test
    public void createWithoutTo() {
        final DarcsCommand cmd = sut.from("from").create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "get", "from")));
    }

    @Test
    public void createWithTo() {
        final DarcsCommand cmd = sut.from("from").to("to").create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "get", "from", "to")));
    }

}
