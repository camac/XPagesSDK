package org.openntf.xsp.sdk.platform;

// Most of the features are the same as Domino HTTP
public class DominoDotsPlatform extends DominoHttpPlatform {

	@Override
	public String getName() {
		return "Domino DOTS Platform";
	}

	@Override
	public String getLocalRcpTargetFolder() {
		return getLocalInstallFolder() + "/osgi-dots/rcp/eclipse";
	}

	@Override
	public String getRemoteRcpTargetFolder() {
		return getRemoteInstallFolder() + "/osgi-dots/rcp/eclipse";
	}

	@Override
	public String getLocalRcpSharedFolder() {
		return getLocalInstallFolder() + "/osgi-dots/shared/eclipse";
	}

	@Override
	public String getRemoteRcpSharedFolder() {
		return getRemoteInstallFolder() + "/osgi-dots/shared/eclipse";
	}

	@Override
	public String getLocalWorkspaceFolder() {
		return getLocalWorkspaceFolder("dots");
	}

	@Override
	public String getLocalWorkspaceFolder(String profileName) {
		return getLocalDataFolder() + "/domino/workspace-" + profileName;
	}

	@Override
	public String getRemoteWorkspaceFolder() {
		return getRemoteWorkspaceFolder("dots");
	}

	@Override
	public String getRemoteWorkspaceFolder(String profileName) {
		return getRemoteDataFolder() + "/domino/workspace-" + profileName;
	}

}
