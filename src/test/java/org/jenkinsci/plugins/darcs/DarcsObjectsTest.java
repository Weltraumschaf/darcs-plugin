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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class DarcsObjectsTest {

    //CHECKSTYLE:OFF
    @Rule public ExpectedException thrown = ExpectedException.none();
    //CHECKSTYLE:ONN

    @Test
    public void hashCodeNullObject() {
        assertThat(DarcsObjects.hashCode(null), is(0));
    }

    @Test
    public void hashCodeOneObject() {
        final Object o = new Object();

        assertThat(DarcsObjects.hashCode(o), is(DarcsObjects.hashCode(o)));
    }

    @Test
    public void hashCodeManyObject() {
        final Object o1 = new Object();
        final Object o2 = new Object();
        final Object o3 = new Object();

        assertThat(DarcsObjects.hashCode(o1, o2, o3), is(DarcsObjects.hashCode(o1, o2, o3)));
        assertThat(DarcsObjects.hashCode(o1, o2, o3), is(not(DarcsObjects.hashCode(o1, o3, o2))));
    }

    @Test
    public void equal() {
        final Object o1 = new Object();
        final Object o2 = new Object();

        assertThat(DarcsObjects.equal(o1, o1), is(true));
        assertThat(DarcsObjects.equal(o2, o1), is(false));
        assertThat(DarcsObjects.equal(o1, o2), is(false));
        assertThat(DarcsObjects.equal(o2, o2), is(true));

        assertThat(DarcsObjects.equal(o1, null), is(false));
        assertThat(DarcsObjects.equal(null, o1), is(false));
        assertThat(DarcsObjects.equal(null, null), is(true));
    }

    @Test
    public void notEqual() {
        final Object o1 = new Object();
        final Object o2 = new Object();

        assertThat(DarcsObjects.notEqual(o1, o1), is(false));
        assertThat(DarcsObjects.notEqual(o2, o1), is(true));
        assertThat(DarcsObjects.notEqual(o1, o2), is(true));
        assertThat(DarcsObjects.notEqual(o2, o2), is(false));

        assertThat(DarcsObjects.notEqual(o1, null), is(true));
        assertThat(DarcsObjects.notEqual(null, o1), is(true));
        assertThat(DarcsObjects.notEqual(null, null), is(false));
    }

    @Test
    public void toStringHelperThrowsExceptionIfClassNameIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Name must not be null!");
        DarcsObjects.toString(null);
    }

    @Test
    public void toStringHelperWithNoProperties() {
        assertThat(DarcsObjects.toString("ClassName").toString(), is("ClassName{}"));
    }

    @Test
    public void toStringHelperWithOneProperties() {
        assertThat(
            DarcsObjects.toString("ClassName").add("number", 23).toString(),
            is("ClassName{number=23}")
        );
    }

    @Test
    public void toStringHelperWithMultipleProperties() {
        assertThat(
            DarcsObjects.toString("ClassName")
                        .add("number", 23)
                        .add("str", "foo")
                        .add("bool", Boolean.TRUE)
                        .toString(),
            is("ClassName{bool=true, number=23, str=foo}")
        );
    }

    @Test
    public void toStringHelperWithMultiplePropertiesAndOneIsNull() {
        assertThat(
            DarcsObjects.toString("ClassName")
                        .add("number", 23)
                        .add("str", null)
                        .add("bool", Boolean.TRUE)
                        .toString(),
            is("ClassName{bool=true, number=23, str=<NULL>}")
        );
    }

    @Test
    public void equalizer() {
        assertThat(DarcsObjects.equalizer().equals(), is(true));
        assertThat(DarcsObjects.equalizer()
                               .add("foo", "foo")
                               .add(23, 23)
                               .equals(), is(true));
        assertThat(DarcsObjects.equalizer()
                               .add("foo", "foo")
                               .add("foo", "bar")
                               .add(23, 23)
                               .equals(), is(false));
    }
}
