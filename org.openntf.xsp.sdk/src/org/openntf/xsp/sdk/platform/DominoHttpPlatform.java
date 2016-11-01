package org.openntf.xsp.sdk.platform;

import org.openntf.xsp.sdk.preferences.XPagesSDKPreferences;

public class DominoHttpPlatform extends AbstractNotesDominoPlatform {

	@Override
	public String getName() {
		return "Domino HTTP Platform";
	}

	@Override
	public boolean isEnabled() {
		String status = XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.DOMINO_STATUS);
		
		return !XPagesSDKPreferences.STATUS_DISABLED.equals(status);
	}

	@Override
	public boolean isLocal() {
		String status = XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.DOMINO_STATUS);
		
		return XPagesSDKPreferences.STATUS_LOCAL.equals(status);
	}
	
	@Override
	public String getNotesIniFilePath() {
		return XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.DOMINO_INIFILE_PATH);
	}

	@Override
	public String getRemoteInstallFolder() {
		return XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.DOMINO_INSTALL_FOLDER);
	}

	@Override
	public String getRemoteDataFolder() {
		return XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.DOMINO_DATA_FOLDER);
	}

	@Override
	public String getLocalRcpTargetFolder() {
		return getLocalInstallFolder() + "/osgi/rcp/eclipse";
	}

	@Override
	public String getRemoteRcpTargetFolder() {
		return getRemoteInstallFolder() + "/osgi/rcp/eclipse";
	}

	@Override
	public String getLocalRcpSharedFolder() {
		return getLocalInstallFolder() + "/osgi/shared/eclipse";
	}

	@Override
	public String getRemoteRcpSharedFolder() {
		return getRemoteInstallFolder() + "/osgi/shared/eclipse";
	}

	@Override
	public String getLocalWorkspaceFolder() {
		return getLocalWorkspaceFolder(null);
	}

	@Override
	public String getLocalWorkspaceFolder(String profileName) {
		// Ignore profile name!
		return getLocalDataFolder() + "/domino/workspace";
	}

	@Override
	public String getRemoteWorkspaceFolder() {
		return getRemoteWorkspaceFolder(null);
	}

	@Override
	public String getRemoteWorkspaceFolder(String profileName) {
		// Ignore profile name!
		return getRemoteDataFolder() + "/domino/workspace";
	}

}
