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

import org.openntf.xsp.sdk.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.platform.NotesDominoPlatformFactory;

/**
 * @author dtaieb
 *
 */
public class LaunchConfigurationDomino extends AbstractLaunchConfiguration {

	public LaunchConfigurationDomino() {
		super();
	}
		
	@Override
	public String[] getProfiles() {
		return null;
	}
	
    /* (non-Javadoc)
     * @see com.ibm.domino.osgi.debug.launch.AbstractDominoOSGILaunchConfiguration#getName()
     */
    @Override
    public String getName() {
        return "Domino OSGi Framework";
    }

	@Override
	protected INotesDominoPlatform getNotesDominoPlatform() {
		return NotesDominoPlatformFactory.getDominoHttpPlatform();
	}

}
