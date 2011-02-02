/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins.darcs;

import hudson.scm.SCMRevisionState;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsRevisionState extends SCMRevisionState {
    @Override
    public String toString() {
        return "foobar";
    }
}
