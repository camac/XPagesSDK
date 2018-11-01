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

// Most of the features are the same as Domino HTTP
public class DominoDotsPlatformEclipse extends DominoHttpPlatformEclipse {

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

	@Override
	public String getSystemFragmentFileName() {
		return "com.ibm.dots.sharedlib_1.0.0.jar";
	}

}
