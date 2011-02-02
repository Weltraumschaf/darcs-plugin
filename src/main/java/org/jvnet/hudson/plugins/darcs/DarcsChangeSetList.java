/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs;

import hudson.scm.ChangeLogSet;
import hudson.model.AbstractBuild;

import java.util.List;
import java.util.Collections;
import java.util.Iterator;

/**
 * List of changeset that went into a particular build.
 * 
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsChangeSetList extends ChangeLogSet<DarcsChangeSet> {
    private final List<DarcsChangeSet> changeSets;

    public DarcsChangeSetList(AbstractBuild build, List<DarcsChangeSet> logs) {
        super(build);
        this.changeSets = Collections.unmodifiableList(logs);

        for (DarcsChangeSet log : logs) {
            log.setParent(this);
        }
    }

    @Override
    public boolean isEmptySet() {
        return changeSets.isEmpty();
    }

    public Iterator<DarcsChangeSet> iterator() {
        return changeSets.iterator();
    }
    
    public List<DarcsChangeSet> getLogs() {
        return changeSets;
    }

    @Override
    public String getKind() {
        return "darcs";
    }
}
