/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs.browsers;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsQueryBuilderTest {

    //CHECKSTYLE:OFF
    @Rule public ExpectedException thrown = ExpectedException.none();
    //CHECKSTYLE:ON

    @Test
    public void emptyQuery() {
        final DarcsQueryBuilder sut = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.SLASHES);
        assertEquals("Test the seperator type", DarcsQueryBuilder.Separators.SLASHES, sut.getType());
        assertEquals("Test empty query string.", "", sut.toString());
    }

    @Test
    public void addToSlashedQuery() {
        final DarcsQueryBuilder sut = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.SLASHES);
        assertEquals("Test the seperator type", DarcsQueryBuilder.Separators.SLASHES, sut.getType());
        sut.add("foo").add("bar").add("baz");
        assertEquals("Test a slashed query.", "/foo/bar/baz", sut.toString());
    }

    @Test
    public void addToSemicolonedQuery() {
        DarcsQueryBuilder sut;

        sut = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.SEMICOLONS);
        assertEquals("Test the seperator type", DarcsQueryBuilder.Separators.SEMICOLONS, sut.getType());
        sut.add("foo").add("bar").add("baz");
        assertEquals("Test a semicoloned query.", "?foo;bar;baz", sut.toString());

        sut = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.SEMICOLONS);
        assertEquals("Test the seperator type", DarcsQueryBuilder.Separators.SEMICOLONS, sut.getType());
        sut.add("foo=1").add("bar=2").add("baz=3");
        assertEquals("Test a semicoloned query.", "?foo=1;bar=2;baz=3", sut.toString());
    }

    @Test
    public void addToAmpersandedQuery() {
        DarcsQueryBuilder sut;

        sut = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.AMPERSANDS);
        assertEquals("Test the seperator type", DarcsQueryBuilder.Separators.AMPERSANDS, sut.getType());
        sut.add("foo").add("bar").add("baz");
        assertEquals("Test a ampersanded query.", "?foo&bar&baz", sut.toString());

        sut = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.AMPERSANDS);
        assertEquals("Test the seperator type", DarcsQueryBuilder.Separators.AMPERSANDS, sut.getType());
        sut.add("foo=1").add("bar=2").add("baz=3");
        assertEquals("Test a ampersanded query.", "?foo=1&bar=2&baz=3", sut.toString());
    }

    @Test
    public void addToUnsupportedSeparatorThrowsException() {
        final DarcsQueryBuilder sut = new DarcsQueryBuilder(DarcsQueryBuilder.Separators.UNSUPPORTED);
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Unsupported separator type 'UNSUPPORTED'!");
        sut.add("foobar");
    }
}
