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

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.darcs.browsers.DarcsRepositoryBrowser;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.framework.io.ByteBuffer;

/**
 * Inner class of the SCM descriptor.
 *
 * Contains the global configuration options as fields.
 *
 * @author Sven Strittmatter <weltraumschaf@googlemail.com>
 */
@Extension
public class DarcsScmDescriptor extends SCMDescriptor<DarcsScm> {

    private static final Logger LOGGER = Logger.getLogger(DarcsScmDescriptor.class.getName());

    private String darcsExe;

    public DarcsScmDescriptor() {
        super(DarcsScm.class, DarcsRepositoryBrowser.class);
        load();
    }

    public String getDisplayName() {
        return "Darcs";
    }

    public String getDarcsExe() {
        return (null == darcsExe) ? "darcs" : darcsExe;
    }

    @Override
    public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        return super.newInstance(req, formData);
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
        darcsExe = Util.fixEmpty(req.getParameter("darcs.darcsExe").trim());
        save();

        return true;
    }

    public FormValidation doDarcsExeCheck(@QueryParameter final String value) throws IOException, ServletException {
        return FormValidation.validateExecutable(value, new FormValidation.FileValidator() {
            @Override
            public FormValidation validate(final File exe) {
                try {
                    ByteBuffer baos = new ByteBuffer();
                    Launcher launcher = Hudson.getInstance()
                            .createLauncher(TaskListener.NULL);
                    Launcher.ProcStarter proc = launcher.launch()
                            .cmds(exe, "--version")
                            .stdout(baos);

                    if (proc.join() == 0) {
                        return FormValidation.ok();
                    } else {
                        return FormValidation.warning("Could not locate the executable in path");
                    }
                } catch (IOException e) {
                    // failed
                    LOGGER.log(Level.WARNING, e.toString());
                } catch (InterruptedException e) {
                    // failed
                    LOGGER.log(Level.WARNING, e.toString());
                }

                return FormValidation.error("Unable to check darcs version");
            }
        });
    }
}
