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
public class DarcsChangesBuilderTest {

    //CHECKSTYLE:OFF
    @Rule public ExpectedException thrown = ExpectedException.none();
    //CHECKSTYLE:ON
    private final DarcsChangesBuilder sut = new DarcsChangesBuilder("foo");

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
    public void createWithRepoDir() {
        final DarcsCommand cmd = sut.repoDir("repodir").create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "changes", "--repo=repodir")));
    }

    @Test
    public void createWithXmlOutput() {
        final DarcsCommand cmd = sut.xmlOutput().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "changes", "--xml-output")));
    }

    @Test
    public void createWithSummary() {
        final DarcsCommand cmd = sut.summary().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "changes", "--summary")));
    }

    @Test
    public void createWithCount() {
        final DarcsCommand cmd = sut.count().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "changes", "--count")));
    }

    @Test
    public void createWithCountIgnoresAllExceptRepoDir() {
        final DarcsCommand cmd = sut.repoDir("repodir").xmlOutput().summary().count().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "changes", "--repo=repodir", "--count")));
    }

    @Test
    public void createWithLast() {
        final DarcsCommand cmd = sut.last(5).create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "changes", "--last=5")));
    }

    @Test
    public void createWithLastLessThanOneThrowsException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Amount must not be less than 1!");
        sut.last(0);
    }

    @Test
    public void createWithAllParams() {
        final DarcsCommand cmd = sut.repoDir("repodir").xmlOutput().summary().last(4).create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "changes", "--repo=repodir", "--xml-output", "--summary",
                "--last=4")));
    }
}
