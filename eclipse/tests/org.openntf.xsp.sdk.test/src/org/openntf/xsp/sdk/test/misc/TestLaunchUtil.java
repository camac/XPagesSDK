package org.openntf.xsp.sdk.test.misc;

import org.junit.Test;
import org.openntf.xsp.sdk.commons.osgi.LaunchUtil;

import static org.junit.Assert.assertEquals;

public class TestLaunchUtil {

    @Test
    public void testToRemotePath() {
        String local = "C:/Domino/some/path";
        String remote = "/Volumes/Remote C/Domino/some/path";
        assertEquals("Path should match expected", remote, LaunchUtil.toRemotePath(local, ExampleXPagesPlatform.INSTANCE));
    }

    @Test
    public void testToLocalPath() {
        String local = "C:/Domino/some/path";
        String remote = "/Volumes/Remote C/Domino/some/path";
        assertEquals("Path should match expected", local, LaunchUtil.toLocalPath(remote, ExampleXPagesPlatform.INSTANCE));
    }
}
