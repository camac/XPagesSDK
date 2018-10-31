package org.openntf.xsp.sdk.intellij;

import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.impl.GenericRunProperties;

import java.util.Map;

public class DominoRunProperties extends GenericRunProperties {
    private static final String PROP_INI = "notesIni";
    private static final String PROP_PROGRAMDIR = "programDir";
    private static final String PROP_DATADIR = "dataDir";

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
}
