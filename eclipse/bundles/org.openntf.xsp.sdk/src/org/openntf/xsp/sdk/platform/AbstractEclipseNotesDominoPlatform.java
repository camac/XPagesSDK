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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.commons.platform.AbstractNotesDominoPlatform;
import org.openntf.xsp.sdk.commons.exceptions.XPagesSDKException;

public abstract class AbstractEclipseNotesDominoPlatform extends AbstractNotesDominoPlatform {

	@Override
	public String getRcpBase() throws Exception {
		String rcpBaseFolder = findRcpBaseFolder(m -> Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, m)));
		if (rcpBaseFolder == null) {
			IStatus status = new Status(Status.INFO, Activator.PLUGIN_ID, "Unable to find rcpBaseFolder!");
			Activator.getDefault().getLog().log(status);
			throw new XPagesSDKException("Unable to find rcpBaseFolder!");
		}
		return rcpBaseFolder.replace('\\', '/');
	}

}
