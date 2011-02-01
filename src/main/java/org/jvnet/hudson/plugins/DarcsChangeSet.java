/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import java.util.Collection;

import org.kohsuke.stapler.export.Exported;

/**
 *
 * @author sxs
 */
public class DarcsChangeSet extends ChangeLogSet.Entry {
    private String author;
    private String date;
    private String localDate;
    private boolean inverted;
    private String hash;
    private String name;
    private String comment;

    @Exported
    public User getAuthor() {
        return User.get(author);
    }

    @Exported
    public String getComment() {
        return comment;
    }

    @Exported
    public String getDate() {
        return date;
    }

    @Exported
    public String getHash() {
        return hash;
    }

    @Exported
    public boolean isInverted() {
        return inverted;
    }

    @Exported
    public String getLocalDate() {
        return localDate;
    }

    @Exported
    public String getName() {
        return name;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Collection<String> getAffectedPaths() {
        return null;
    }

    @Override
    public String getMsg() {
        return getName();
    }

    @Override
    protected void setParent(ChangeLogSet parent) {
        super.setParent(parent);
    }
}
