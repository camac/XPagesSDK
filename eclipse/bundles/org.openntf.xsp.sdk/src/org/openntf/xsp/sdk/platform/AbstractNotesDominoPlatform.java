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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.exceptions.XPagesSDKError;
import org.openntf.xsp.sdk.exceptions.XPagesSDKException;
import org.openntf.xsp.sdk.commons.utils.CommonUtils;

public abstract class AbstractNotesDominoPlatform implements INotesDominoPlatform {

	protected static final String INIVAR_INSTALLFOLDER = "NotesProgram";
	protected static final String INIVAR_DATAFOLDER = "Directory";
		
	private Properties notesIniProperties;
	
	public AbstractNotesDominoPlatform() {
	}
	
	protected Properties getNotesIniProperties() {
		if(notesIniProperties == null) {
			Properties props = new Properties();
			
			try {
				loadNotesIniVars(getNotesIniFilePath(), props);
			} catch (IOException e) {
				throw new XPagesSDKError("Unable to find notes.ini file for " + getName() + " : " + getNotesIniFilePath());
			}
			
			notesIniProperties = props;
		}
		
		return notesIniProperties;
	}
	
	protected String getNotesIniProperty(String propertyName, String defaultValue) {
		return getNotesIniProperties().getProperty(propertyName, defaultValue);
	}

	@Override
	public String getLocalInstallFolder() {
		return getNotesIniProperty(INIVAR_INSTALLFOLDER, "");
	}

	@Override
	public String getLocalDataFolder() {
		return getNotesIniProperty(INIVAR_DATAFOLDER, "");
	}
	
	private static void loadNotesIniVars(String fileLocation, Properties props) throws IOException {
		// TODO Error handling
		BufferedReader br = null;

		try {

			String line;

			br = new BufferedReader(new FileReader(fileLocation));

			while ((line = br.readLine()) != null) {
				if(line.contains("=")) {
					String iniParamName = StringUtils.substringBefore(line, "=");
					String paramValue= StringUtils.substringAfter(line, "=");

					props.put(iniParamName, paramValue);
				}
			}

		} finally {
			try {
				if (null != br) 
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	protected String getRcpBaseFolderPrefix() {
		return "com.ibm.rcp.base_";
	}

	@Override
	public String getRcpBase() throws Exception {
		String rcpBaseFolder = findRcpBaseFolder();
		if (rcpBaseFolder == null) {
			IStatus status = new Status(Status.INFO, Activator.PLUGIN_ID, "Unable to find rcpBaseFolder!");
			Activator.getDefault().getLog().log(status);
			throw new XPagesSDKException("Unable to find rcpBaseFolder!");
		}
		return rcpBaseFolder.replace('\\', '/');
	}
	
	protected String findRcpBaseFolder() throws Exception {
		IPath rcpPluginPath = new Path(getRemoteRcpTargetFolder() + "/plugins");
		File rcp = new File(rcpPluginPath.toOSString());
		final String rcpBasePrefix = getRcpBaseFolderPrefix();

		if(rcp.isDirectory()) {
			File[] baseFolders = rcp.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(rcpBasePrefix);
				}
			});
			
			if (baseFolders.length >= 1) {
				return baseFolders[0].getAbsolutePath();
			} else {
				IStatus status = new Status(Status.WARNING, Activator.PLUGIN_ID, "Unable to find base folder " + rcpBasePrefix);
				Activator.getDefault().getLog().log(status);
				throw new XPagesSDKException("Unable to find base folder " + rcpBasePrefix);
			}
		} else {
			IStatus status = new Status(Status.INFO, Activator.PLUGIN_ID, "Container for location " + rcpPluginPath
					+ " is NOT a folder from root ");
			Activator.getDefault().getLog().log(status);
			throw new XPagesSDKException("Notes container for location " + rcpPluginPath + " is NOT a folder from root ");
		}
	}

	@Override
	public String resolveVariable(String variableName) {
		if(CommonUtils.isEmpty(variableName)) {
			return "";
		}

		String varName = variableName.toLowerCase(Locale.US);

		if (varName.endsWith("_install")) {
			return getRemoteInstallFolder();
		} else if (varName.endsWith("_rcp_data")) {
			return getRemoteWorkspaceFolder();
		} else if (varName.endsWith("_rcp_base")) {
			try {
				return getRcpBase();
			} catch (Exception e) {
				throw new RuntimeException(new CoreException(Status.CANCEL_STATUS));
			}
		} else if (varName.endsWith("_rcp_target")) {
			return getRemoteRcpTargetFolder();
		} else if (varName.endsWith("_shared_target")) {
			return getRemoteRcpSharedFolder();
		}
		
		return "";
	}
	
}
