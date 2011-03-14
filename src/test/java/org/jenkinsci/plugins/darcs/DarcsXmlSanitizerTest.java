/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Ralph Lange" <Ralph.Lange@gmx.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */

package org.jenkinsci.plugins.darcs;

import java.io.UnsupportedEncodingException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author lange
 */
public class DarcsXmlSanitizerTest {

    public DarcsXmlSanitizerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of cleanse method, of class DarcsXmlSanitizer.
     */
    @Test
    public void testCleanse() {
        System.out.println("cleanse");
        DarcsXmlSanitizer sani = new DarcsXmlSanitizer();
        try {
            byte[] input = "foo <name>abcdefg</name> bar <comment>abcdefg</comment> foobar".getBytes("US-ASCII");
            String output = "foo <name>abcdefg</name> bar <comment>abcdefg</comment> foobar";
            String result = sani.cleanse(input, "US-ASCII");
            assertEquals(output, result);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported encoding exception" + e);
        }

        try {
            byte[] input = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar".getBytes("UTF-8");
            String output = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar";
            String result = sani.cleanse(input, "UTF-8");
            assertEquals(output, result);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported encoding exception" + e);
        }

        try {
            byte[] input = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar".getBytes("ISO-8859-1");
            String output = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar";
            String result = sani.cleanse(input, "ISO-8859-1");
            assertEquals(output, result);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported encoding exception" + e);
        }

        try {
            byte[] inp_a = "foo <name>äöüÄÖÜß</name> bar ".getBytes("ISO-8859-1");
            byte[] inp_b = "<comment>äöüÄÖÜß</comment> foobar".getBytes("UTF-8");
            byte[] input = new byte[inp_a.length + inp_b.length];
            System.arraycopy(inp_a, 0, input, 0, inp_a.length);
            System.arraycopy(inp_b, 0, input, inp_a.length, inp_b.length);
            String output = "foo <name>äöüÄÖÜß</name> bar <comment>äöüÄÖÜß</comment> foobar";
            String result = sani.cleanse(input, "ISO-8859-1");
            assertEquals(output, result);
        } catch (UnsupportedEncodingException e) {
            System.out.println("Unsupported encoding exception" + e);
        }
    }

}