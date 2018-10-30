package org.openntf.xsp.sdk.platform;

import org.eclipse.core.runtime.CoreException;

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

	public String getRcpBase() throws Exception;
	public String getSystemFragmentFileName();
	
	public String resolveVariable(String varName) throws CoreException;
}
