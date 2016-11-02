package org.openntf.xsp.sdk.platform;

import org.openntf.xsp.sdk.preferences.XspPreferences;

public class DominoHttpPlatform extends AbstractNotesDominoPlatform {

	@Override
	public String getName() {
		return "Domino HTTP Platform";
	}

	@Override
	public boolean isEnabled() {
		String status = XspPreferences.getPreferenceString(XspPreferences.DOMINO_STATUS);
		
		return !XspPreferences.STATUS_DISABLED.equals(status);
	}

	@Override
	public boolean isLocal() {
		String status = XspPreferences.getPreferenceString(XspPreferences.DOMINO_STATUS);
		
		return XspPreferences.STATUS_LOCAL.equals(status);
	}
	
	@Override
	public String getNotesIniFilePath() {
		return XspPreferences.getPreferenceString(XspPreferences.DOMINO_INIFILE_PATH);
	}

	@Override
	public String getRemoteInstallFolder() {
		return XspPreferences.getPreferenceString(XspPreferences.DOMINO_INSTALL_FOLDER);
	}

	@Override
	public String getRemoteDataFolder() {
		return XspPreferences.getPreferenceString(XspPreferences.DOMINO_DATA_FOLDER);
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
