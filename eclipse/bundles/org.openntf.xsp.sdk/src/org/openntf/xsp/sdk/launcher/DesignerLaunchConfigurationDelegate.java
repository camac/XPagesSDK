package org.openntf.xsp.sdk.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.pde.launching.EclipseApplicationLaunchConfiguration;

public class DesignerLaunchConfigurationDelegate extends EclipseApplicationLaunchConfiguration
		implements ILaunchConfigurationDelegate {

	public DesignerLaunchConfigurationDelegate() {
		super();
	}

	@Override
	protected void preLaunchCheck(ILaunchConfiguration configuration, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		
		super.preLaunchCheck(configuration, launch, monitor);

		
	}

	

}
