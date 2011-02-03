/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author sxs
 */
public class DarcsSaxHandler extends DefaultHandler {
    
    private static final Logger logger = Logger.getLogger(DarcsSaxHandler.class.getName());

    public enum DarcsChangelogTag {
        CHANGELOG,
        PATCH,
        NAME,
        COMMENT,
        SUMMARY,
        MODIFY_FILE,
        ADD_FILE,
        REMOVE_FILE,
        ADDED_LINES,
        REMOVED_LINES;
    }

    private DarcsChangelogTag currentTag;
    private boolean ready;

    public DarcsSaxHandler() {
        super();
        ready = false;
    }

    public boolean isReady() { return ready; }

//    @Override
//    public void startDocument () {
//	logger.log(Level.INFO, "Start XML document");
//    }

    @Override
    public void endDocument () {
	ready = true;
    }

    private void recognizeTag(String tagName) {
        if ("changelog".equals(tagName)) {
            currentTag = DarcsChangelogTag.CHANGELOG;
        } else if ("patch".equals(tagName)) {
            currentTag = DarcsChangelogTag.PATCH;
        } else if ("name".equals(tagName)) {
            currentTag = DarcsChangelogTag.NAME;
        } else if ("comment".equals(tagName)) {
            currentTag = DarcsChangelogTag.COMMENT;
        } else if ("summary".equals(tagName)) {
            currentTag = DarcsChangelogTag.SUMMARY;
        } else if ("modify_file".equals(tagName)) {
            currentTag = DarcsChangelogTag.MODIFY_FILE;
        } else if ("add_file".equals(tagName)) {
            currentTag = DarcsChangelogTag.ADD_FILE;
        } else if ("remove_file".equals(tagName)) {
            currentTag = DarcsChangelogTag.REMOVE_FILE;
        } else if ("added_lines".equals(tagName)) {
            currentTag = DarcsChangelogTag.ADDED_LINES;
        } else if ("removed_lines".equals(tagName)) {
            currentTag = DarcsChangelogTag.REMOVED_LINES;
        }
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
	recognizeTag(qName);
	logger.log(Level.INFO, "found tag: {0}", currentTag);
	
    }

    @Override
    public void endElement(String uri, String name, String qName) {
	logger.log(Level.INFO, "End element: {0}",  qName);
    }
    
    @Override
    public void characters(char ch[], int start, int length) {
	System.out.print("Characters:    \"");

	for (int i = start; i < start + length; i++) {
	    switch (ch[i]) {
                case '\\':
//                    System.out.print("\\\\");
                    break;
                case '"':
                    System.out.print("\\\"");
                    break;
                case '\n':
//                    System.out.print("\\n");
                    break;
                case '\r':
//                    System.out.print("\\r");
                    break;
                case '\t':
//                    System.out.print("\\t");
                    break;
                default:
                    System.out.print(ch[i]);
                    break;
                }
	}

        System.out.print("\"\n");
    }
}
