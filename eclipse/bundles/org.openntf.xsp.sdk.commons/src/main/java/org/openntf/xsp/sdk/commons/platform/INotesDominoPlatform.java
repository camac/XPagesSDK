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
package org.openntf.xsp.sdk.commons.platform;

import java.util.Properties;

public interface INotesDominoPlatform {
	public static final String INIVAR_INSTALLFOLDER = "NotesProgram";
	public static final String INIVAR_DATAFOLDER = "Directory";

	Properties getNotesIniProperties();

	default String getNotesIniProperty(String propertyName, String defaultValue) {
		return getNotesIniProperties().getProperty(propertyName, defaultValue);
	}

	default String getLocalInstallFolder() {
		return getNotesIniProperty(INIVAR_INSTALLFOLDER, "");
	}

	default String getLocalDataFolder() {
		return getNotesIniProperty(INIVAR_DATAFOLDER, "");
	}

	public String getName();
	public boolean isEnabled();
	public boolean isLocal();
	
	public String getNotesIniFilePath();

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
	
	public String resolveVariable(String varName);
}
