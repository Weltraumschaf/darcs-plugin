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

import hudson.Launcher;
import hudson.Launcher.LocalLauncher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsCommandTest {

    private final DarcsCommandBuilder sut = new DarcsCommandBuilder("foo");

    @Test
    public void builder() {
        final DarcsCommandBuilder builder = DarcsCommand.builder("foobar");
        assertThat(builder.getDarcsExe(), is("foobar"));
    }

    @Test
    public void prepare() {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add("foo").add("bar").add("baz");
        final DarcsCommand command = new DarcsCommand(args);
        final Launcher launcher = new LocalLauncher(TaskListener.NULL);
        final Launcher.ProcStarter proc = launcher.launch();
        command.prepare(proc);
        assertThat(proc.stderr(), sameInstance(command.getErr()));
        assertThat(proc.stdout(), sameInstance(command.getOut()));
        assertThat(proc.cmds(), is(args.toList()));
    }

    @Test
    public void createWithoutAnyParameter() {
        final DarcsCommand cmd = sut.create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo")));
    }

    @Test
    public void createWithVersionParameter() {
        final DarcsCommand cmd = sut.version().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "--version")));
    }

    @Test
    public void createWithExactVersionParameter() {
        final DarcsCommand cmd = sut.exactVersion().create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "--exact-version")));
    }

    @Test
    public void eitherVersionOrExactVersionIsUsed() {
        DarcsCommand cmd = sut.exactVersion().version().exactVersion().create();
        ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "--exact-version")));

        cmd = sut.version().exactVersion().version().create();
        args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "--version")));
    }
}
