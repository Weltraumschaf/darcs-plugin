/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hudson.plugins;

import hudson.scm.SCMRevisionState;

/**
 *
 * @author sxs
 */
public class DarcsRevisionState extends SCMRevisionState {
    @Override
    public String toString() {
        return "foobar";
    }
}
