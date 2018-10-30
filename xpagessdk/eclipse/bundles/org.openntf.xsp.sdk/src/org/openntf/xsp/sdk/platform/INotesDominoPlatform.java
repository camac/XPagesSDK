/**
 * Copyright © 2011-2018 Nathan T. Freeman, Serdar Basegmez, Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
