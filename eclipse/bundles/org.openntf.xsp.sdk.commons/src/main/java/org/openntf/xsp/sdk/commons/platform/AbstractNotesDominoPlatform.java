package org.openntf.xsp.sdk.commons.platform;

import org.openntf.xsp.sdk.commons.exceptions.XPagesSDKError;
import org.openntf.xsp.sdk.commons.exceptions.XPagesSDKException;
import org.openntf.xsp.sdk.commons.utils.CommonUtils;
import org.openntf.xsp.sdk.commons.utils.HttpPlatformUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Consumer;

public abstract class AbstractNotesDominoPlatform implements INotesDominoPlatform {

    protected static final String INIVAR_INSTALLFOLDER = "NotesProgram";
    protected static final String INIVAR_DATAFOLDER = "Directory";
    protected Properties notesIniProperties;

    protected Properties getNotesIniProperties() {
        if(notesIniProperties == null) {
            Properties props = new Properties();

            try {
                HttpPlatformUtil.loadNotesIniVars(getNotesIniFilePath(), props);
            } catch (IOException e) {
                throw new XPagesSDKError("Unable to find notes.ini file for " + getName() + " : " + getNotesIniFilePath());
            }

            notesIniProperties = props;
        }

        return notesIniProperties;
    }

    protected String getNotesIniProperty(String propertyName, String defaultValue) {
        return getNotesIniProperties().getProperty(propertyName, defaultValue);
    }

    @Override
    public String getLocalInstallFolder() {
        return getNotesIniProperty(INIVAR_INSTALLFOLDER, "");
    }

    @Override
    public String getLocalDataFolder() {
        return getNotesIniProperty(INIVAR_DATAFOLDER, "");
    }

    protected String getRcpBaseFolderPrefix() {
        return "com.ibm.rcp.base_";
    }

    protected String findRcpBaseFolder(Consumer<String> messageHandler) throws Exception {
        Path rcpPluginPath = Paths.get(getRemoteRcpTargetFolder(),"plugins");
        File rcp = rcpPluginPath.toFile();
        final String rcpBasePrefix = getRcpBaseFolderPrefix();

        if(rcp.isDirectory()) {
            File[] baseFolders = rcp.listFiles((dir, name) -> name.startsWith(rcpBasePrefix));

            if (baseFolders.length >= 1) {
                return baseFolders[0].getAbsolutePath();
            } else {
                if(messageHandler != null) {
                    messageHandler.accept("Unable to find base folder " + rcpBasePrefix);
                }
                throw new XPagesSDKException("Unable to find base folder " + rcpBasePrefix);
            }
        } else {
            if(messageHandler != null) {
                messageHandler.accept("Container for location " + rcpPluginPath + " is NOT a folder from root ");
            }
            throw new XPagesSDKException("Notes container for location " + rcpPluginPath + " is NOT a folder from root ");
        }
    }

    @Override
    public String resolveVariable(String variableName) {
        if(CommonUtils.isEmpty(variableName)) {
            return "";
        }

        String varName = variableName.toLowerCase(Locale.US);

        if (varName.endsWith("_install")) {
            return getRemoteInstallFolder();
        } else if (varName.endsWith("_rcp_data")) {
            return getRemoteWorkspaceFolder();
        } else if (varName.endsWith("_rcp_base")) {
            try {
                return getRcpBase();
            } catch (Exception e) {
                throw new RuntimeException("Cannot get RCP Base", e);
            }
        } else if (varName.endsWith("_rcp_target")) {
            return getRemoteRcpTargetFolder();
        } else if (varName.endsWith("_shared_target")) {
            return getRemoteRcpSharedFolder();
        }

        return "";
    }
}
