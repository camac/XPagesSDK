package org.openntf.xsp.sdk.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.openntf.xsp.sdk.Activator;
import org.osgi.service.prefs.Preferences;

public class XspPreferences {

	// Removed default values because every environment are different from another.
	// Defaults are just making things more difficult. By default, the SDK should not validate anything.

	// Preference Variable Names
	public final static String NOTES_STATUS = "NOTES_STATUS";
	public final static String NOTES_INIFILE_PATH = "NOTES_INIFILE_PATH";
	public final static String NOTES_INSTALL_FOLDER = "NOTES_INSTALL_FOLDER";
	public final static String NOTES_DATA_FOLDER = "NOTES_DATA_FOLDER";
	public final static String NOTES_AUTO_JRE = "NOTES_AUTO_JRE";

	public final static String DOMINO_STATUS = "DOMINO_STATUS";
	public final static String DOMINO_INIFILE_PATH = "DOMINO_INIFILE_PATH";
	public final static String DOMINO_INSTALL_FOLDER = "DOMINO_INSTALL_FOLDER";
	public final static String DOMINO_DATA_FOLDER = "DOMINO_DATA_FOLDER";
	public final static String DOMINO_AUTO_JRE = "DOMINO_AUTO_JRE";

	public final static String REMOTE_JUNCTION = "REMOTE_JUNCTION";
	public final static String LOCAL_JUNCTION = "LOCAL_JUNCTION";

	// Preference Variable Values
	public final static String STATUS_DISABLED = "DISABLED";
	public final static String STATUS_LOCAL = "LOCAL";
	public final static String STATUS_REMOTE = "REMOTE";

	// public static final String PREF_DOMINO_BIN_DIR = "domino.bin.directory";
	// public static final String PREF_DOMINO_DATA_DIR = "domino.data.directory";
	public static final String PREF_PROFILE = "domino.osgi.profile";
	
	private final static Map<String, String> defaultValues = new HashMap<String, String>();
	
	static {
		defaultValues.put(NOTES_STATUS, STATUS_DISABLED);
		defaultValues.put(DOMINO_STATUS, STATUS_DISABLED);
	}
	
	
	public static void setDefaults(Preferences preferences) {
		for(String key: defaultValues.keySet()) {
			preferences.put(key, defaultValues.get(key));
		}
	}

	public static IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static String getPreferenceString(String name) {
		return getPreferenceString(name, defaultValues.get(name));
	}
	
	public static String getPreferenceString(String name, String defaultValue) {
		String value = getPreferenceStore().getString(name);
		
		if(value!=null && value.length()>0) {
			return value;
		} else {
			return defaultValue;
		}
	}

	public static void setPreferenceString(String name, String value) {
		getPreferenceStore().setValue(name, value);
	}
	
}
