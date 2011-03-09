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

    /**
     * Set of the changes.
     */
    private final List<DarcsChangeSet> changeSets;
    /**
     * Lazy computed digest over all changeset hashes.
     */
    private String digest;

    /**
     * Constructs without build.
     * 
     * @param changes 
     */
    public DarcsChangeSetList(List<DarcsChangeSet> changes) {
        this(null, changes);
    }
    
    /**
     * Constructs with build and changes.
     * 
     * @param build
     * @param changes 
     */
    public DarcsChangeSetList(AbstractBuild build, List<DarcsChangeSet> changes) {
        super(build);
        this.changeSets = Collections.unmodifiableList(changes);

        for (DarcsChangeSet log : changes) {
            log.setParent(this);
        }
    }

    /**
     * Returns whether the change set list is empty or not.
     * 
     * @return 
     */
    @Override
    public boolean isEmptySet() {
        return getChangeSets().isEmpty();
    }

    /**
     * Returns an iterator for the list.
     * 
     * @return 
     */
    public Iterator<DarcsChangeSet> iterator() {
        return getChangeSets().iterator();
    }

    /**
     * Returns the count of change sets.
     * 
     * @return 
     */
    public int size() {
        return getChangeSets().size();
    }

    /**
     * Returns the change set list.
     * 
     * @return 
     */
    public List<DarcsChangeSet> getChangeSets() {
        return changeSets;
    }

    /**
     * Returns the kind as string.
     * 
     * @return 
     */
    @Override
    public String getKind() {
        return "darcs";
    }

    /**
     * Calculates md5 digest over all changesets darcs hashes.
     *
     * Inspired by http://www.stratos.me/2008/05/java-string-calculate-md5/
     * 
     * @return
     */
    protected String calcDigest() {
        StringBuilder res = new StringBuilder();

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();

            if (isEmptySet()) {
                algorithm.update("".getBytes());
            } else {
                for (DarcsChangeSet cs : this) {
                    algorithm.update(cs.getHash().getBytes());
                }
            }
            
            byte[] md5 = algorithm.digest();
            String tmp = "";

            for (int i = 0; i < md5.length; i++) {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                
                if (tmp.length() == 1) {
                    res.append("0");
                }

                res.append(tmp);
            }
        } catch (NoSuchAlgorithmException ex) {
            res.append("");
        }

        return res.toString();
    }

    /**
     * Returns the digest for the whole changeset.
     *
     * @return
     */
    public String digest() {
        if (null == digest) {
            digest = calcDigest();
        }

        return digest;
    }

    /**
     * Compares over the digest of the change set list.
     * 
     * @param other
     * @return 
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;

        if (other instanceof DarcsChangeSetList) {
            DarcsChangeSetList that = (DarcsChangeSetList) other;
            return digest().equals(that.digest());
        }

        return result;
    }

    /**
     * Returns the hash code for the digest string.
     * 
     * @return 
     */
    @Override
    public int hashCode() {
        return digest().hashCode();
    }

    @Override
    public String toString() {
        return "DarcsChangeSetList{changeSets=" + changeSets + ", digest=" + digest + '}';
    }
}
