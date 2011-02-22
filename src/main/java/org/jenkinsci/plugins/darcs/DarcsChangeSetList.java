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

import hudson.scm.ChangeLogSet;
import hudson.model.AbstractBuild;

import java.util.List;
import java.util.Collections;
import java.util.Iterator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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

    /**
     * Calculates md5 digest over all changesets hashes
     *
     * Inspired by http://www.stratos.me/2008/05/java-string-calculate-md5/
     *
     * @return
     */
    public String digest() {
        String res = "";

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();

            for (DarcsChangeSet cs : this) {
                algorithm.update(cs.getHash().getBytes());
            }

            byte[] md5 = algorithm.digest();
            String tmp = "";

            for (int i = 0; i < md5.length; i++) {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1) {
                    res += "0" + tmp;
                } else {
                    res += tmp;
                }
            }

        } catch (NoSuchAlgorithmException ex) {}

        return res;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;

        if (other instanceof DarcsChangeSetList) {
            DarcsChangeSetList that = (DarcsChangeSetList) other;
            return digest().equals(that.digest());
        }

        return result;
    }
}
