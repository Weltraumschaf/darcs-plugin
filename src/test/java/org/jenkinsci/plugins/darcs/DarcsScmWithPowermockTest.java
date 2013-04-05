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

package org.jenkinsci.plugins.darcs;

import hudson.AbortException;
import hudson.FilePath;
import hudson.scm.RepositoryBrowser;
import java.io.IOException;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FilePath.class)
public class DarcsScmWithPowermockTest {

    private DarcsScm2 createSut() {
        return new DarcsScm2("", "", false, mock(RepositoryBrowser.class));
    }

    @Test
    public void clean_throwsIoException() throws IOException, InterruptedException {
        final DarcsScm2 sut = createSut();
        final FilePath localPath = mock(FilePath.class);
        final Throwable inner = new IOException();
        doThrow(inner).when(localPath).deleteRecursive();

        try {
            sut.clean(localPath);
            fail("Expected exception not thrown!");
        } catch (AbortException ex) {
            assertThat(ex.getMessage(), is(Messages.DarcsScm_failedToCleanTheWorkspace()));
            assertThat(ex.getCause(), is(sameInstance(inner)));
        }
    }

    @Test
    public void clean_throwsInterruptedException() throws AbortException, IOException, InterruptedException {
        final DarcsScm2 sut = createSut();
        final FilePath localPath = mock(FilePath.class);
        final Throwable inner = new InterruptedException();
        doThrow(inner).when(localPath).deleteRecursive();

        try {
            sut.clean(localPath);
            fail("Expected exception not thrown!");
        } catch (AbortException ex) {
            assertThat(ex.getMessage(), is(Messages.DarcsScm_failedToCleanTheWorkspace()));
            assertThat(ex.getCause(), is(sameInstance(inner)));
        }
    }

}
