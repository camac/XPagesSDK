/*
* � Copyright IBM Corp. 2012
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License. 
* You may obtain a copy of the License at:
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
* implied. See the License for the specific language governing 
* permissions and limitations under the License.
*/
package com.ibm.domino.osgi.debug.launch;

import java.io.File;

import org.openntf.xsp.sdk.exceptions.XPagesSDKException;
import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.commons.utils.StringUtil;

/**
 * @author dtaieb Hold information about the Domino OSGi configuration
 */
public class LaunchHandler {

	private int iReturnCode; // Return Code from the Config Dialog
	private final AbstractDominoLaunchConfiguration launchConfiguration;

	/**
	 * @param launchConfiguration
	 * @param workspaceRelativePath
	 */
	public LaunchHandler(AbstractDominoLaunchConfiguration launchConfiguration) {
		this.launchConfiguration = launchConfiguration;
	}

	public INotesDominoPlatform getTargetPlatform() {
		return this.launchConfiguration.getNotesDominoPlatform();
	}

	/**
	 * @return
	 */
	public int getReturnCode() {
		return iReturnCode;
	}

	/**
	 * @param returnCode
	 */
	public void setReturnCode(int returnCode) {
		iReturnCode = returnCode;
	}

	public void isValid() throws XPagesSDKException {
		INotesDominoPlatform targetPlatform = getTargetPlatform();
		
		if (StringUtil.isEmpty(targetPlatform.getRemoteInstallFolder())) {
			throw new XPagesSDKException("Domino Bin Path is empty");
		}
		if (StringUtil.isEmpty(targetPlatform.getRemoteDataFolder())) {
			throw new XPagesSDKException("Domino Data Path is empty");
		}

		File bin = new File(targetPlatform.getRemoteInstallFolder());
		if (!bin.exists() || !bin.isDirectory()) {
			throw new XPagesSDKException("Domino Bin Path does not exist or is not a valid directory");
		}

		File data = new File(targetPlatform.getRemoteDataFolder());
		if (!data.exists() || !data.isDirectory()) {
			throw new XPagesSDKException("Domino Data Path does not exist or is not a valid directory");
		}

		File dominoData = new File(data, "domino");
		if (!dominoData.exists() || !dominoData.isDirectory()) {
			throw new XPagesSDKException("Domino Data Path is not a valid data directory: domino subdirectory is missing");
		}
	}

	/**
	 * @return
	 */
	public String[] getProfiles() {
		return launchConfiguration.getProfiles();
	}

	/**
	 * @param selectedProfile
	 */
	public void setProfile(String selectedProfile) {
		launchConfiguration.setSelectedProfile(selectedProfile);
	}

}
