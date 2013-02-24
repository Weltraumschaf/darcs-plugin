/*
 *  LICENSE
 *
 * "THE BEER-WARE LICENSE" (Revision 43):
 * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
 * As long as you retain this notice you can do whatever you want with
 * this stuff. If we meet some day, and you think this stuff is worth it,
 * you can buy me a non alcohol-free beer in return.
 *
 * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
 */

package org.jenkinsci.plugins.darcs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
public class Helper {

    static DarcsChangeSet createChangeSet(String suffix) {
        DarcsChangeSet cs = new DarcsChangeSet();
        cs.setAuthor("author" + suffix);
        cs.setDate("date" + suffix);
        cs.setLocalDate("local_date" + suffix);
        cs.setHash("hash" + suffix);
        cs.setInverted(false);

        return cs;
    }

    static DarcsChangeSetList createChangeSetList(int count) {
        List<DarcsChangeSet> list = new ArrayList<DarcsChangeSet>();

        for (int i = 0; i < count; i++) {
            DarcsChangeSet cs = Helper.createChangeSet(Integer.toString(i));
            list.add(cs);
        }

        return new DarcsChangeSetList(list);
    }

}
