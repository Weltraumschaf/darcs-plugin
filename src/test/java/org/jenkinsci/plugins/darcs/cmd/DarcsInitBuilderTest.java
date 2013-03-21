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

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsInitBuilderTest {

    private final DarcsInitBuilder sut = new DarcsInitBuilder("foo");

    @Test
    public void create() {
        final DarcsCommand cmd = sut.create();
        final ArgumentListBuilder args = cmd.getArgs();
        assertThat(args.toList(), is(Arrays.asList("foo", "init")));
    }

}
