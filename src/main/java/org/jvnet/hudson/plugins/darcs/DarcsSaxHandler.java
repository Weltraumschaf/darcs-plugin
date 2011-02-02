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
    
    public DarcsSaxHandler() {
        super();
    }

    @Override
    public void startDocument () {
	logger.log(Level.INFO, "Start XML document");
    }

    @Override
    public void endDocument () {
	logger.log(Level.INFO, "End XML document");
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
	if ("".equals (uri)) {
	    logger.log(Level.INFO, "Start element: " + qName);
	} else {
	    logger.log(Level.INFO, "Start element: {" + uri + "}" + name);
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
	if ("".equals (uri)) {
	    logger.log(Level.INFO, "End element: " + qName);
	} else {
	    logger.log(Level.INFO, "End element:   {" + uri + "}" + name);
        }
    }
}
