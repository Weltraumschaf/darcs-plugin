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
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsProcStarterTest {

    @Test
    public void nullProcStarter() throws IOException, InterruptedException {
        final DarcsProcStarter sut = new DarcsProcStarter();
        sut.cmds(new ArgumentListBuilder()); // Mustn not throw NPE.
        final List<String> emptyList = new ArrayList<String>();
        assertThat(sut.cmds(), is(emptyList));
        sut.stdout(mock(OutputStream.class)); // Mustn not throw NPE.
        assertThat(sut.stdout(), is(nullValue()));
        sut.stderr(mock(OutputStream.class)); // Mustn not throw NPE.
        assertThat(sut.stderr(), is(nullValue()));
        assertThat(sut.join(), is(0));
    }

}
