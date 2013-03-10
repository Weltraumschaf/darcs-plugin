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
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsCommandTest {

    @Test
    public void builder() {
        final DarcsCommandBuilder builder = DarcsCommand.builder("foobar");
        assertThat(builder.getDarcsExe(), is("foobar"));
    }

    @Test
    public void prepare() {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add("foo").add("bar").add("baz");
        final DarcsCommand sut = new DarcsCommand(args);
        final Launcher launcher = new LocalLauncher(TaskListener.NULL);
        final Launcher.ProcStarter proc = launcher.launch();
        sut.prepare(proc);
        assertThat(proc.stderr(), sameInstance(sut.getErr()));
        assertThat(proc.stdout(), sameInstance(sut.getOut()));
        assertThat(proc.cmds(), is(args.toList()));
    }

}