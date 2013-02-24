/*
 * LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 42):
 * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a beer in return.
 */
package org.jenkinsci.plugins.darcs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.xml.sax.SAXException;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeLogParserTest {

    @Test
    public void parse_file() throws IOException, SAXException, URISyntaxException {
        // TODO add comments
        // TODO add removed files
        // TODO add invrt patch
        final DarcsChangeLogParser sut = new DarcsChangeLogParser();
        final URL resource = getClass().getResource("/changes-summary.xml");
        final DarcsChangeSetList list = sut.parse(null, new File(resource.toURI()));

        assertNotNull(list);
        assertEquals(10, list.size());
        final List<DarcsChangeSet> logs = list.getChangeSets();
        assertEquals(10, logs.size());

        int i = 0;
        DarcsChangeSet expected;

        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("inital files added");
        expected.setDate("20110214201356");
        expected.setLocalDate("Mon Feb 14 21:13:56 CET 2011");
        expected.setHash("20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getAddedPaths().addAll(Arrays.asList("Bar.java", "Baz.java", "Foo.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("Implemented class Bar");
        expected.setDate("20110214201544");
        expected.setLocalDate("Mon Feb 14 21:15:44 CET 2011");
        expected.setHash("20110214201544-7677a-1cd5bbbc18b5e78240a80c9d1576faaa63c9f7d7.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Bar.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("Implemented class Baz");
        expected.setDate("20110214201618");
        expected.setLocalDate("Mon Feb 14 21:16:18 CET 2011");
        expected.setHash("20110214201618-7677a-631eee269c4a252d953fa2f61b1127874bbc57d4.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Baz.java"));
        assertThat(logs.get(i), is(equalTo(expected)));


        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("Implemented class Foo");
        expected.setDate("20110214201649");
        expected.setLocalDate("Mon Feb 14 21:16:49 CET 2011");
        expected.setHash("20110214201649-7677a-9c3c62c42467fe20e75a9ab62e52441ef7cdc8ba.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("Implemented value in class Bar");
        expected.setDate("20110214203334");
        expected.setLocalDate("Mon Feb 14 21:33:34 CET 2011");
        expected.setHash("20110214203334-7677a-3f9e4a67068618fcfde454c0c097d1f8b96301df.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Bar.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("Implemented value in class Baz");
        expected.setDate("20110214203402");
        expected.setLocalDate("Mon Feb 14 21:34:02 CET 2011");
        expected.setHash("20110214203402-7677a-5eb558d8bd3df5b8edfa005479d8ff1e8139abe0.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Baz.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("Implemented value in class Foo");
        expected.setDate("20110214203417");
        expected.setLocalDate("Mon Feb 14 21:34:17 CET 2011");
        expected.setHash("20110214203417-7677a-261f33e2608d68f088f15b077f7dcde2cc18a4b7.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("Implemented toString()");
        expected.setDate("20110214203531");
        expected.setLocalDate("Mon Feb 14 21:35:31 CET 2011");
        expected.setHash("20110214203531-7677a-1b935a82ba6408ffa9add3642cb52f233ff4ef54.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Bar.java", "Baz.java", "Foo.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("German Umlauts in UTF-8 encoding: äöüÄÖÜß");
        expected.setDate("20110224141706");
        expected.setLocalDate("Thu Feb 24 15:17:06 CET 2011");
        expected.setHash("20110224141706-7677a-b79e15c79bd5776b3e669a7338e181b4bd303609.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        assertThat(logs.get(i), is(equalTo(expected)));

        i++;
        expected = new DarcsChangeSet();
        expected.setAuthor("ich@weltraumschaf.de");
        expected.setName("German Umlauts in ISO8859-15 encoding: äöüÄÖÜß");
        expected.setDate("20110224143546");
        expected.setLocalDate("Thu Feb 24 15:35:46 CET 2011");
        expected.setHash("20110224143546-7677a-359f8967374ac52adc87dedac6f4ad458a7b6446.gz");
        expected.setComment("");
        expected.setInverted(false);
        expected.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        assertThat(logs.get(i), is(equalTo(expected)));
    }

    @Test
    @Ignore
    public void parse_byteArray() {

    }
}
