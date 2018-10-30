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
package org.openntf.xsp.sdk.templates;

import java.net.URL;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.ui.templates.OptionTemplateSection;

public class XPagesLibraryPluginTemplate extends OptionTemplateSection {
	public String[] getNewFiles() {
		return null;
	}

	public String getUsedExtensionPoint() {
		return null;
	}

	@Override
	protected URL getInstallURL() {
		return null;
	}

	@Override
	public String getSectionId() {
		return null;
	}

	@Override
	protected ResourceBundle getPluginResourceBundle() {
		return null;
	}

	@Override
	protected void updateModel(IProgressMonitor arg0) throws CoreException {
	}
}
