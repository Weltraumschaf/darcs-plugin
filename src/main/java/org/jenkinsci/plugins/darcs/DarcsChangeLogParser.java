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
 */
class DarcsChangeLogParser extends ChangeLogParser {

    /**
     * Logger facility.
     */
    private static final Logger LOGGER = Logger.getLogger(DarcsChangeLogParser.class.getName());
    /**
     * Custom SAX Parser.
     */
    private final DarcsSaxHandler handler;
    /**
     * Sanitizes XML character encoding.
     */
    private final DarcsXmlSanitizer sanitizer;

    /**
     * Convenience constructor which initializes all dependencies.
     */
    public DarcsChangeLogParser() {
        this(new DarcsSaxHandler(), new DarcsXmlSanitizer());
    }

    /**
     * Dedicated constructor.
     *
     * @param handler implementation of a SAX parser
     * @param sani sanitize to clean comments
     */
    public DarcsChangeLogParser(final DarcsSaxHandler handler, final DarcsXmlSanitizer sani) {
        super();
        this.handler = handler;
        this.sanitizer = sani;
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
        final StringReader input = new StringReader(sanitizer.cleanse(changelogFile));
        return parse(build, new InputSource(input));
    }

    /**
     * @see #parse(hudson.model.AbstractBuild, java.io.File)
     *
     * @param changeLog stream to read XML from
     * @return change set list w/ current build null
     * @throws IOException on read errors
     * @throws SAXException on parse errors
     * @deprecated use {@link #parse(java.lang.String)} instead
     */
    @Deprecated
    public DarcsChangeSetList parse(final ByteArrayOutputStream changeLog) throws IOException, SAXException {
        final StringReader input = new StringReader(sanitizer.cleanse(changeLog.toByteArray()));
        return parse(null, new InputSource(input));
    }

    /**
     * @see #parse(hudson.model.AbstractBuild, java.io.File)
     *
     * @param build build associated with changelog
     * @param changeLog chnagelog to parse
     * @return change set list
     * @throws IOException on read errors
     * @throws SAXException on parse errors
     */
    private DarcsChangeSetList parse(final AbstractBuild build, final InputSource changeLog)
        throws IOException, SAXException {
        final XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setContentHandler(handler);
        xmlReader.setErrorHandler(handler);
        xmlReader.parse(changeLog);
        return new DarcsChangeSetList(build, handler.getChangeSets());
    }

    /**
     * @see #parse(hudson.model.AbstractBuild, java.io.File)
     *
     * @param changeLog to read XML from
     * @return change set list w/ current build null
     * @throws IOException on read errors
     * @throws SAXException on parse errors
     */
    public DarcsChangeSetList parse(final String changeLog) throws IOException, SAXException {
        final StringReader input = new StringReader(sanitizer.cleanse(changeLog.getBytes())); // TODO consider encoding
        return parse(null, new InputSource(input));
    }

}
