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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsCommandTest {

    //CHECKSTYLE:OFF
    @Rule public ExpectedException thrown = ExpectedException.none();
    //CHECKSTYLE:ON
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
        final DarcsProcStarter proc = new DarcsProcStarter(launcher.launch());
        command.prepare(proc);
        assertThat(proc.stderr(), sameInstance(command.getErr()));
        assertThat(proc.stdout(), sameInstance(command.getOut()));
        assertThat(proc.cmds(), is(args.toList()));
    }

    @Test
    public void execute() throws IOException, InterruptedException, DarcsCommadException {
        final DarcsCommand command = spy(new DarcsCommand());
        final DarcsProcStarter proc = mock(DarcsProcStarter.class);
        when(proc.join()).thenReturn(0);
        command.execute(proc);
        verify(command).prepare(proc);
        verify(proc).join();
    }

    @Test
    public void executeRethrowIOException() throws IOException, InterruptedException, DarcsCommadException {
        final DarcsCommand command = new DarcsCommand();
        final DarcsProcStarter proc = mock(DarcsProcStarter.class);
        stub(proc.join()).toThrow(new IOException("foobar"));
        thrown.expect(DarcsCommadException.class);
        thrown.expectMessage("foobar");
        command.execute(proc);
    }

    @Test
    public void executeRethrowInterruptedException() throws IOException, InterruptedException, DarcsCommadException {
        final DarcsCommand command = new DarcsCommand();
        final DarcsProcStarter proc = mock(DarcsProcStarter.class);
        stub(proc.join()).toThrow(new InterruptedException("foobar"));
        thrown.expect(DarcsCommadException.class);
        thrown.expectMessage("foobar");
        command.execute(proc);
    }

    @Test
    public void executeThrowExceptionIfReturnCodeUnequalZero() throws IOException, InterruptedException, DarcsCommadException {
        final DarcsCommand command = new DarcsCommand();
        final DarcsProcStarter proc = mock(DarcsProcStarter.class);
        when(proc.join()).thenReturn(23);
        thrown.expect(DarcsCommadException.class);
        thrown.expectMessage("Error on performing command: ''! Return code 23.");
        command.execute(proc);
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

    @Test
    public void testToString() {
        final ArgumentListBuilder args = new ArgumentListBuilder();
        args.add("foo").add("bar").add("baz");
        final DarcsCommand command = new DarcsCommand(args);
        assertThat(command.toString(), is(args.toStringWithQuote()));
    }

    @Test
    public void setAndGetOut() {
        final OutputStream out1 = mock(OutputStream.class);
        final DarcsCommand command = new DarcsCommand();
        assertThat(command.getOut(), is(not(nullValue())));
        command.setOut(out1);
        assertThat(command.getOut(), is(out1));
        final OutputStream out2 = mock(OutputStream.class);
        command.setOut(out2);
        assertThat(command.getOut(), is(out2));
    }

    @Test
    public void setAndGetErr() {
        final OutputStream out1 = mock(OutputStream.class);
        final DarcsCommand command = new DarcsCommand();
        assertThat(command.getErr(), is(not(nullValue())));
        command.setErr(out1);
        assertThat(command.getErr(), is(out1));
        final OutputStream out2 = mock(OutputStream.class);
        command.setErr(out2);
        assertThat(command.getErr(), is(out2));
    }

}
