package org.openntf.xsp.sdk.test.misc;

import org.openntf.xsp.sdk.commons.exceptions.XPagesSDKException;
import org.openntf.xsp.sdk.commons.platform.AbstractNotesDominoPlatform;
import org.openntf.xsp.sdk.commons.platform.IDominoHttpPlatform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ExampleXPagesPlatform extends AbstractNotesDominoPlatform implements IDominoHttpPlatform {
    public static final ExampleXPagesPlatform INSTANCE = new ExampleXPagesPlatform(Collections.emptyMap());

    private final Map<String, String> properties;

    public ExampleXPagesPlatform(Map<String, String> properties) {
        this.properties = new HashMap<>(properties);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public String getNotesIniFilePath() {
        return getRemoteDataFolder() + "/notes.ini";
    }

    @Override
    public String getRemoteInstallFolder() {
        return "/Volumes/Remote C/Domino";
    }

    @Override
    public String getRemoteDataFolder() {
        return "/Volumes/Remote C/Domino/data";
    }

    @Override
    public String getLocalDataFolder() {
        return "C:/Domino/data";
    }

    @Override
    public String getLocalInstallFolder() {
        return "C:/Domino";
    }

    @Override
    public Properties getNotesIniProperties() {
        return new Properties();
    }

    @Override
    public String getRcpBase() throws Exception {
        String rcpBaseFolder = findRcpBaseFolder(null);
        if (rcpBaseFolder == null) {
            throw new XPagesSDKException("Unable to find rcpBaseFolder!");
        }
        return rcpBaseFolder.replace('\\', '/');
    }
}
