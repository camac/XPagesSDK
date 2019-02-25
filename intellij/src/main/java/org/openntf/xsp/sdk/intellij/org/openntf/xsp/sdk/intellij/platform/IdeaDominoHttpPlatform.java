package org.openntf.xsp.sdk.intellij.org.openntf.xsp.sdk.intellij.platform;

import org.jetbrains.annotations.NotNull;
import org.openntf.xsp.sdk.commons.exceptions.XPagesSDKException;
import org.openntf.xsp.sdk.commons.platform.AbstractNotesDominoPlatform;
import org.openntf.xsp.sdk.commons.platform.IDominoHttpPlatform;
import org.openntf.xsp.sdk.intellij.DominoRunProperties;

import java.util.HashMap;
import java.util.Map;

public class IdeaDominoHttpPlatform extends AbstractNotesDominoPlatform implements IDominoHttpPlatform {
    private final Map<String, String> properties;

    public IdeaDominoHttpPlatform(@NotNull Map<String, String> properties) {
        this.properties = new HashMap<>(properties);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isLocal() {
       return DominoRunProperties.getLocation(properties) == DominoRunProperties.Location.LOCAL;
    }

    @Override
    public String getNotesIniFilePath() {
        return DominoRunProperties.getNotesIni(properties);
    }

    @Override
    public String getRemoteInstallFolder() {
        return DominoRunProperties.getProgramDir(properties);
    }

    @Override
    public String getRemoteDataFolder() {
        return DominoRunProperties.getDataDir(properties);
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
