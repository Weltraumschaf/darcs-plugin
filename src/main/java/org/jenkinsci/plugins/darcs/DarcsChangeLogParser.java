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

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parses the output of Darcs log.
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 * @author Ralph Lange <Ralph.Lange@gmx.de>
 * @author Ilya Perminov <iperminov@dwavesys.com>
 */
class DarcsChangeLogParser extends ChangeLogParser {

    /**
     * Logger facility.
     */
    private static final Logger LOGGER = Logger.getLogger(DarcsChangeLogParser.class.getName());

    /**
     * Factory method for custom SAX parser.
     *
     * @return never {@code null}, always new instance
     */
    private static DarcsSaxHandler makeHandler() {
        return new DarcsSaxHandler();
    }

    /**
     * Factory method for  XML character encoding sanitizer.
     *
     * @return never {@code null}, always new instance
     */
    private static DarcsXmlSanitizer makeSanitizer() {
        return new DarcsXmlSanitizer();
    }

    /**
     * Parses the Darcs change log file.
     *
     * The Darcs change log file is in XML format (as given by the command
     * {@literal darcs changes --xml-output --summary}).
     *
     * @param build the current build
     * @param changelogFile the change log file
     * @return change set list
     * @throws IOException on read errors
     * @throws SAXException on parse errors
     */
    @Override
    public DarcsChangeSetList parse(final AbstractBuild build, final File changelogFile)
        throws IOException, SAXException {
        LOGGER.info(String.format("Parsing changelog file %s...", changelogFile.toString()));
        final StringReader input = new StringReader(makeSanitizer().cleanse(changelogFile));
        return parse(build, new InputSource(input));
    }

    /**
     * @see #parse(hudson.model.AbstractBuild, java.io.File)
     *
     * @param changeLog stream to read XML from
     * @return change set list w/ current build null
     * @throws IOException on read errors
     * @throws SAXException on parse errors
     */
    DarcsChangeSetList parse(final ByteArrayOutputStream changeLog) throws IOException, SAXException {
        final StringReader input = new StringReader(makeSanitizer().cleanse(changeLog.toByteArray()));
        return parse(null, new InputSource(input));
    }

    private DarcsChangeSetList parse(final AbstractBuild build, final InputSource changeLog) throws IOException, SAXException {
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        final DarcsSaxHandler handler = makeHandler();
        xmlReader.setContentHandler(handler);
        xmlReader.setErrorHandler(handler);
        xmlReader.parse(changeLog);
        return new DarcsChangeSetList(build, handler.getChangeSets());
    }
}
