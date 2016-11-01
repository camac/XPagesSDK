package org.openntf.xsp.sdk.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.openntf.xsp.sdk.Activator;
import org.osgi.service.prefs.Preferences;

public class XPagesSDKPreferencesInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		Preferences preferences = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
		XPagesSDKPreferences.setDefaults(preferences);
	}

}
