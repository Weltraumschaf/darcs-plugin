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

import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.kohsuke.stapler.StaplerRequest;
import static org.mockito.Mockito.*;

/**
 *
 * @author Sven Strittmatter <ich@weltraumschaf.de>
 */
public class DarcsScmDescriptorTest {

    @Test
    public void getDisplayName() {
        final DarcsScmDescriptor sut = mock(DarcsScmDescriptor.class, CALLS_REAL_METHODS);
        doNothing().when(sut).load();
        assertThat(sut.getDisplayName(), is("Darcs"));
    }

    @Test
    public void getDarcsExe_default() {
        final DarcsScmDescriptor sut = mock(DarcsScmDescriptor.class, CALLS_REAL_METHODS);
        doNothing().when(sut).load();
        assertThat(sut.getDarcsExe(), is("darcs"));
    }

    @Test
    public void getDarcsExe_configured() throws Descriptor.FormException {
        final DarcsScmDescriptor sut = mock(DarcsScmDescriptor.class, CALLS_REAL_METHODS);
        doNothing().when(sut).load();
        doNothing().when(sut).save();

        final StaplerRequest req = mock(StaplerRequest.class);
        final String parameter = "darcs.darcsExe";
        final String exe = "/foo/bar/baz/darcs";
        when(req.getParameter(parameter)).thenReturn(exe);

        sut.configure(req, new JSONObject());
        verify(req, times(1)).getParameter(parameter);
        verify(sut, times(1)).save();
        assertThat(sut.getDarcsExe(), is(exe));
    }

    @Test
    @Ignore("Not ready yet")
    public void doDarcsExeCheck() {

    }

}
