/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(DarcsChangeLogParser.class.getName());
    
    @Override
    public ChangeLogSet<? extends Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
        XMLReader       xr      = XMLReaderFactory.createXMLReader();
        DarcsSaxHandler handler = new DarcsSaxHandler();
        FileReader      r       = new FileReader(changelogFile);

	xr.setContentHandler(handler);
	xr.setErrorHandler(handler); 
        xr.parse(new InputSource(r));

        return new DarcsChangeSetList(build, handler.getChangeSets());
    }

}
