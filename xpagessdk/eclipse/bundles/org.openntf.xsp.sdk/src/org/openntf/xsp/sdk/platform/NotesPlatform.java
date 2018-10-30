package org.openntf.xsp.sdk.platform;

import org.openntf.xsp.sdk.preferences.XspPreferences;

public class NotesPlatform extends AbstractNotesDominoPlatform {

	@Override
	public String getName() {
		return "Notes Platform";
	}

	@Override
	public boolean isEnabled() {
		String status = XspPreferences.getPreferenceString(XspPreferences.NOTES_STATUS);
		
		return !XspPreferences.STATUS_DISABLED.equals(status);
	}

	@Override
	public boolean isLocal() {
		String status = XspPreferences.getPreferenceString(XspPreferences.NOTES_STATUS);
		
		return XspPreferences.STATUS_LOCAL.equals(status);
	}
	
	@Override
	public String getNotesIniFilePath() {
		return XspPreferences.getPreferenceString(XspPreferences.NOTES_INIFILE_PATH);
	}

	@Override
	public String getRemoteInstallFolder() {
		return XspPreferences.getPreferenceString(XspPreferences.NOTES_INSTALL_FOLDER);
	}

	@Override
	public String getRemoteDataFolder() {
		return XspPreferences.getPreferenceString(XspPreferences.NOTES_DATA_FOLDER);
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

	@Override
	public String getSystemFragmentFileName() {
		// Used by Launch configurations. No need here.
		return null;
	}

}
