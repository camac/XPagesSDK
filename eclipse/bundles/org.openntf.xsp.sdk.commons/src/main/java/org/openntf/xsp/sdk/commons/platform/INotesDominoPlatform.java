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
	String INIVAR_INSTALLFOLDER = "NotesProgram";
	String INIVAR_DATAFOLDER = "Directory";

	Properties getNotesIniProperties();

	/**
	 * Reads the specified property from the configured notes.ini
	 *
	 * @param propertyName the INI property to read, case-sensitive
	 * @param defaultValue the default value to return if the property is not set
	 * @return the value of the property, or the default value if it is not set
	 */
	default String getNotesIniProperty(String propertyName, String defaultValue) {
		return getNotesIniProperties().getProperty(propertyName, defaultValue);
	}

	/**
	 * @return the program directory on the target Notes/Domino installation, according to the notes.ini
	 */
	default String getLocalInstallFolder() {
		return getNotesIniProperty(INIVAR_INSTALLFOLDER, "");
	}

	/**
	 * @return the data directory on the target Notes/Domino installation, according to the notes.ini
	 */
	default String getLocalDataFolder() {
		return getNotesIniProperty(INIVAR_DATAFOLDER, "");
	}

	String getName();
	boolean isEnabled();
	boolean isLocal();
	
	String getNotesIniFilePath();

	String getRemoteInstallFolder();
	String getRemoteDataFolder();

	String getLocalRcpTargetFolder();
	String getLocalRcpSharedFolder();
	String getLocalWorkspaceFolder();
	String getLocalWorkspaceFolder(String profileName);

	String getRemoteRcpTargetFolder();
	String getRemoteRcpSharedFolder();
	String getRemoteWorkspaceFolder();
	String getRemoteWorkspaceFolder(String profileName);

	String getRcpBase() throws Exception;
	String getSystemFragmentFileName();
	
	String resolveVariable(String varName);
}
