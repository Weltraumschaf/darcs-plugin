/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.io.File;
import java.io.FileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Parses the output of darcs log.
 * 
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeLogParser  extends ChangeLogParser {

    @Override
    public ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        List<DarcsChangeSet> entries = new ArrayList<DarcsChangeSet>();

        XMLReader xr = XMLReaderFactory.createXMLReader();
        DarcsSaxHandler handler = new DarcsSaxHandler();
	xr.setContentHandler(handler);
	xr.setErrorHandler(handler);

        FileReader r = new FileReader(changelogFile);
        xr.parse(new InputSource(r));

        return new DarcsChangeSetList(build, entries);
    }

}
