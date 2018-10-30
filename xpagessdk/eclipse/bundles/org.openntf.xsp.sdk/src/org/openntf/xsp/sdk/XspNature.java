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
package org.openntf.xsp.sdk;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.openntf.xsp.sdk.utils.XspProjectUtils;

public class XspNature implements IProjectNature {
	public final static String XSP_NATURE_ID = "org.openntf.xsp.sdk.XspNature";
	public final static String COMPONENT_BUILDER_ID = "org.openntf.xsp.sdk.components.XspComponentBuilder";
	private IProject project;

	public XspNature() {
	}

	public void configure() throws CoreException {
		XspProjectUtils.addBuilder(getProject(), COMPONENT_BUILDER_ID);
	}

	public void deconfigure() throws CoreException {
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject arg0) {
		project = arg0;
	}
}
