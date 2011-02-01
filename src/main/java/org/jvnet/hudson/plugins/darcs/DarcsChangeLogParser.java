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
import java.io.IOException;

import org.xml.sax.SAXException;

/**
 *
 * @author sxs
 */
public class DarcsChangeLogParser  extends ChangeLogParser {

    @Override
    public ChangeLogSet<? extends Entry> parse(AbstractBuild ab, File file) throws IOException, SAXException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
