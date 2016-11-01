package org.openntf.xsp.sdk.platform;

import org.openntf.xsp.sdk.preferences.XPagesSDKPreferences;

public class NotesPlatform extends AbstractNotesDominoPlatform {

	@Override
	public String getName() {
		return "Notes Platform";
	}

	@Override
	public boolean isEnabled() {
		String status = XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.NOTES_STATUS);
		
		return !XPagesSDKPreferences.STATUS_DISABLED.equals(status);
	}

	@Override
	public boolean isLocal() {
		String status = XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.NOTES_STATUS);
		
		return XPagesSDKPreferences.STATUS_LOCAL.equals(status);
	}
	
	@Override
	public String getNotesIniFilePath() {
		return XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.NOTES_INIFILE_PATH);
	}

	@Override
	public String getRemoteInstallFolder() {
		return XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.NOTES_INSTALL_FOLDER);
	}

	@Override
	public String getRemoteDataFolder() {
		return XPagesSDKPreferences.getPreferenceString(XPagesSDKPreferences.NOTES_DATA_FOLDER);
	}

	@Override
	public String getLocalRcpTargetFolder() {
		return getLocalInstallFolder() + "/framework/rcp/eclipse";
	}

	@Override
	public String getRemoteRcpTargetFolder() {
		return getRemoteInstallFolder() + "/framework/rcp/eclipse";
	}

	@Override
	public String getLocalRcpSharedFolder() {
		return getLocalInstallFolder() + "/framework/shared/eclipse";
	}

	@Override
	public String getRemoteRcpSharedFolder() {
		return getRemoteInstallFolder() + "/framework/shared/eclipse";
	}

	@Override
	public String getLocalWorkspaceFolder() {
		return getLocalWorkspaceFolder(null);
	}

	@Override
	public String getLocalWorkspaceFolder(String profileName) {
		// Ignore profile name!
		return getLocalDataFolder() + "/workspace";
	}

	@Override
	public String getRemoteWorkspaceFolder() {
		return getRemoteWorkspaceFolder(null);
	}

	@Override
	public String getRemoteWorkspaceFolder(String profileName) {
		// Ignore profile name!
		return getRemoteDataFolder() + "/workspace";
	}

}
