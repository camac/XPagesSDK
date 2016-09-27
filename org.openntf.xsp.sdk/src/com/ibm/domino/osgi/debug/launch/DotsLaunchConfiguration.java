/*
 * © Copyright IBM Corp. 2012
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ibm.domino.osgi.debug.launch;

import java.util.HashSet;

import org.openntf.xsp.sdk.Activator;

/**
 * @author dtaieb
 *         The OSGi configuration related to DOTs
 */
public class DotsLaunchConfiguration extends AbstractDominoOSGILaunchConfiguration {

	private static final String PREF_LASTUSEPROFILES = "domino.lastuse.osgi.profile";
	private String selectedProfile;

	/**
	 * 
	 */
	public DotsLaunchConfiguration() {
	}

	@Override
	protected String getWorkspaceRelativePath() {
		if (selectedProfile != null) {
			return "domino/workspace-" + selectedProfile.toLowerCase();
		}
		return "domino/workspace-dots"; // Default

	}

	@Override
	protected String getOSGIDirectoryName() {
		return "osgi-dots";
	}

	@Override
	protected String getSystemFragmentFileName() {
		return "com.ibm.dots.sharedlib_1.0.0.jar";
	}

	@Override
	public String[] getProfiles() {
		HashSet<String> profiles = getProfilesFromStore();
		return profiles.toArray(new String[0]);
	}

	/**
	 * @return
	 */
	private HashSet<String> getProfilesFromStore() {
		HashSet<String> profiles = new HashSet<String>();
		profiles.add("DOTS");
		String prefProfiles = Activator.getDefault().getPreferenceStore().getString(PREF_LASTUSEPROFILES);
		if (prefProfiles != null) {
			String[] splits = prefProfiles.split(",");
			for (String split : splits) {
				if (!profiles.contains(split.toUpperCase())) {
					profiles.add(split.toUpperCase());
				}
			}
		}
		return profiles;
	}

	@Override
	public void setProfile(String selectedProfile) {
		this.selectedProfile = selectedProfile;
		// Save to store
		HashSet<String> profiles = getProfilesFromStore();
		if (!profiles.contains(selectedProfile.toUpperCase())) {
			profiles.add(selectedProfile.toUpperCase());
			StringBuilder sb = new StringBuilder();
			for (String profile : profiles) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(profile);
			}
			Activator.getDefault().getPreferenceStore().setValue(PREF_LASTUSEPROFILES, sb.toString());
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.domino.osgi.debug.launch.AbstractDominoOSGILaunchConfiguration#getName()
	 */
	@Override
	public String getName() {
		return "Domino tasklet Framework (dots)";
	}

}
