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
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsPullBuilderTest {

    @Rule public ExpectedException thrown = ExpectedException.none();
    private final DarcsPullBuilder sut = new DarcsPullBuilder("foo");

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
    public void callRepodirWithEmptyThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        sut.repoDir("");
    }

    @Test
    public void callRepodirWithNullThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        sut.repoDir(null);
    }

    @Test
    public void createWithoutFromThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Set from where to pull the patches!");
        sut.create();
    }

    @Test
    public void createWithFrom() {
        final DarcsCommand cmd = sut.from("from").create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "pull", "from")));
    }

    @Test
    public void createWithRepoDir() {
        final DarcsCommand cmd = sut.from("from").repoDir("repodir").create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "pull", "from", "--repo=repodir")));
    }

    @Test
    public void createWithAll() {
        final DarcsCommand cmd = sut.from("from").all().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "pull", "from", "--all")));
    }

    @Test
    public void createWithVerbose() {
        final DarcsCommand cmd = sut.from("from").verbose().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "pull", "from", "--verbose")));
    }

    @Test
    public void createWithAllParameters() {
        final DarcsCommand cmd = sut.from("from").repoDir("repodir").all().verbose().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "pull", "from", "--repo=repodir", "--all", "--verbose")));
    }
}