package org.openntf.xsp.sdk.intellij;

import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;

import java.util.Map;

public class DominoRunProperties extends GenericRunProperties {
    public static enum Location {
        LOCAL, REMOTE
    }

    private static final String PROP_INI = "notesIni";
    private static final String PROP_PROGRAMDIR = "programDir";
    private static final String PROP_DATADIR = "dataDir";
    private static final String PROP_SHAREDDIR = "sharedDir";
    private static final String PROP_MAPPEDREMOTE = "mappedRemote";
    private static final String PROP_LOCATION = "location";

    public static String getNotesIni(@NotNull Map<String, String> properties) {
        return properties.get(PROP_INI);
    }

    public static void setNotesIni(@NotNull Map<String, String> properties, String notesIni) {
        properties.put(PROP_INI, notesIni);
    }

    public static String getProgramDir(@NotNull Map<String, String> properties) {
        return properties.get(PROP_PROGRAMDIR);
    }

    public static void setProgramDir(@NotNull Map<String, String> properties, String programDir) {
        properties.put(PROP_PROGRAMDIR, programDir);
    }

    public static String getDataDir(@NotNull Map<String, String> properties) {
        return properties.get(PROP_DATADIR);
    }

    public static void setDataDir(@NotNull Map<String, String> properties, String dataDir) {
        properties.put(PROP_DATADIR, dataDir);
    }

    public static String getSharedDir(@NotNull Map<String, String> properties) {
        return properties.get(PROP_SHAREDDIR);
    }

    public static void setSharedDir(@NotNull Map<String, String> properties, String sharedDir) {
        properties.put(PROP_SHAREDDIR, sharedDir);
    }

    public static String getMappedRemotePath(@NotNull Map<String, String> properties) {
        return properties.get(PROP_MAPPEDREMOTE);
    }

    public static void setMappedRemotePath(@NotNull Map<String, String> properties, String mappedRemotePath) {
        properties.put(PROP_MAPPEDREMOTE, mappedRemotePath);
    }

    public static Location getLocation(@NotNull Map<String, String> properties) {
        return Location.valueOf(properties.getOrDefault(PROP_LOCATION, Location.LOCAL.name()));
    }
    public static void setLocation(@NotNull Map<String, String> properties, @NotNull Location location) {
        properties.put(PROP_LOCATION, location.name());
    }
}
