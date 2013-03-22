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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.xml.sax.SAXException;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeLogParserTest {

    private final List<DarcsChangeSet> expected = new ArrayList<DarcsChangeSet>();

    @Before
    public void generateExpectedChangesets() {
        expected.clear();
        DarcsChangeSet patch;

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("inital files added");
        patch.setDate("20110214201356");
        patch.setLocalDate("Mon Feb 14 21:13:56 CET 2011");
        patch.setHash("20110214201356-7677a-15b1d7313611ef85de46d8daf57123a365d5b800.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getAddedPaths().addAll(Arrays.asList("Bar.java", "Baz.java", "Foo.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Implemented class Bar");
        patch.setDate("20110214201544");
        patch.setLocalDate("Mon Feb 14 21:15:44 CET 2011");
        patch.setHash("20110214201544-7677a-1cd5bbbc18b5e78240a80c9d1576faaa63c9f7d7.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Bar.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Implemented class Baz");
        patch.setDate("20110214201618");
        patch.setLocalDate("Mon Feb 14 21:16:18 CET 2011");
        patch.setHash("20110214201618-7677a-631eee269c4a252d953fa2f61b1127874bbc57d4.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Baz.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Implemented class Foo");
        patch.setDate("20110214201649");
        patch.setLocalDate("Mon Feb 14 21:16:49 CET 2011");
        patch.setHash("20110214201649-7677a-9c3c62c42467fe20e75a9ab62e52441ef7cdc8ba.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Implemented value in class Bar");
        patch.setDate("20110214203334");
        patch.setLocalDate("Mon Feb 14 21:33:34 CET 2011");
        patch.setHash("20110214203334-7677a-3f9e4a67068618fcfde454c0c097d1f8b96301df.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Bar.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Implemented value in class Baz");
        patch.setDate("20110214203402");
        patch.setLocalDate("Mon Feb 14 21:34:02 CET 2011");
        patch.setHash("20110214203402-7677a-5eb558d8bd3df5b8edfa005479d8ff1e8139abe0.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Baz.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Implemented value in class Foo");
        patch.setDate("20110214203417");
        patch.setLocalDate("Mon Feb 14 21:34:17 CET 2011");
        patch.setHash("20110214203417-7677a-261f33e2608d68f088f15b077f7dcde2cc18a4b7.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Implemented toString()");
        patch.setDate("20110214203531");
        patch.setLocalDate("Mon Feb 14 21:35:31 CET 2011");
        patch.setHash("20110214203531-7677a-1b935a82ba6408ffa9add3642cb52f233ff4ef54.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Bar.java", "Baz.java", "Foo.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("German Umlauts in UTF-8 encoding: äöüÄÖÜß");
        patch.setDate("20110224141706");
        patch.setLocalDate("Thu Feb 24 15:17:06 CET 2011");
        patch.setHash("20110224141706-7677a-b79e15c79bd5776b3e669a7338e181b4bd303609.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("German Umlauts in ISO8859-15 encoding: äöüÄÖÜß");
        patch.setDate("20110224143546");
        patch.setLocalDate("Thu Feb 24 15:35:46 CET 2011");
        patch.setHash("20110224143546-7677a-359f8967374ac52adc87dedac6f4ad458a7b6446.gz");
        patch.setComment("");
        patch.setInverted(false);
        patch.getModifiedPaths().addAll(Arrays.asList("Foo.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Remove Bar.java");
        patch.setDate("20130224203531");
        patch.setLocalDate("Sun Feb 24 21:35:31 CET 2011");
        patch.setHash("20130224203531-7677a-1b935a82ba6408ffa9add3642ab52f233ff4ef54.gz");
        patch.setComment("This is a comment.");
        patch.setInverted(false);
        patch.getDeletedPaths().addAll(Arrays.asList("Bar.java"));
        expected.add(patch);

        patch = new DarcsChangeSet();
        patch.setAuthor("ich@weltraumschaf.de");
        patch.setName("Readd Bar.java");
        patch.setDate("20130224204531");
        patch.setLocalDate("Sun Feb 24 21:45:31 CET 2011");
        patch.setHash("20130224204531-7677a-1b935a82ba6408ffa9add3642ab52f233fe4ef54.gz");
        patch.setComment("This is an other comment.");
        patch.setInverted(true);
        patch.getAddedPaths().addAll(Arrays.asList("Bar.java"));
        expected.add(patch);
    }

    @Test
    public void parse_file() throws IOException, SAXException, URISyntaxException {
        final DarcsChangeLogParser sut = new DarcsChangeLogParser();
        final URL resource = getClass().getResource("/org/jenkinsci/plugins/darcs/changes-summary.xml");
        final DarcsChangeSetList list = sut.parse(null, new File(resource.toURI()));

        assertNotNull(list);
        assertThat(list.size(), is(expected.size()));
        final List<DarcsChangeSet> logs = list.getChangeSets();
        assertThat(logs.size(), is(expected.size()));

        for (int i = 0; i < expected.size(); ++i) {
            assertThat(logs.get(i), is(equalTo(expected.get(i))));
        }
    }

}
