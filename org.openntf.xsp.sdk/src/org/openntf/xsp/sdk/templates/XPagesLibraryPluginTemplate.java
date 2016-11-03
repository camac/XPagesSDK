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
