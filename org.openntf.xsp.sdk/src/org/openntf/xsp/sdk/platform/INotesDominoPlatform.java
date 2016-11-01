package org.openntf.xsp.sdk.platform;

public interface INotesDominoPlatform {

	public String getName();
	public boolean isEnabled();
	public boolean isLocal();
	
	public String getNotesIniFilePath();
		
	public String getLocalInstallFolder();
	public String getLocalDataFolder();

	public String getRemoteInstallFolder();
	public String getRemoteDataFolder();

	public String getLocalRcpTargetFolder();
	public String getLocalRcpSharedFolder();
	public String getLocalWorkspaceFolder();
	public String getLocalWorkspaceFolder(String profileName);

	public String getRemoteRcpTargetFolder();
	public String getRemoteRcpSharedFolder();
	public String getRemoteWorkspaceFolder();
	public String getRemoteWorkspaceFolder(String profileName);

}
