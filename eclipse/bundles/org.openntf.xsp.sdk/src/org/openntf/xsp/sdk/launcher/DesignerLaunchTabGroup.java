package org.openntf.xsp.sdk.launcher;

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.internal.launching.launcher.VMHelper;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.pde.ui.launcher.EclipseLauncherTabGroup;

import com.ibm.domino.osgi.debug.launch.EclipseLaunchUtil;

public class DesignerLaunchTabGroup extends EclipseLauncherTabGroup {

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		super.setDefaults(configuration);

		String args = getArgs();
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);

		String vmargs = getVmArgs();
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmargs);

		configuration.setAttribute(IPDELauncherConstants.CONFIG_GENERATE_DEFAULT, false);
		configuration.setAttribute(IPDELauncherConstants.CONFIG_TEMPLATE_LOCATION, "${notes_rcp_base}/config.ini");

		configuration.setAttribute(IPDELauncherConstants.USE_PRODUCT, true);
		configuration.setAttribute(IPDELauncherConstants.PRODUCT, "com.ibm.designer.domino.product.dde");
		
		configuration.setAttribute(IPDELauncherConstants.AUTOMATIC_VALIDATE, false);
				
		IVMInstall jre = VMHelper.getVMInstall("XPages Notes JRE");
		IPath jrePath = JavaRuntime.newJREContainerPath(jre);
		String attr = jrePath.toPortableString();
				
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, attr);
			
		
	}

	private String getArgs() {
		
		String res = "org/openntf/xsp/sdk/launcher/designer11args.txt";
		return EclipseLaunchUtil.readResource(res);
		
	}
	

	private String getVmArgs() {

		String res = "org/openntf/xsp/sdk/launcher/designer11vmargs.txt";
		return EclipseLaunchUtil.readResource(res);

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		super.performApply(configuration);
		EclipseLaunchUtil.removeDuplicatePlugins(configuration);
		
	}

}
