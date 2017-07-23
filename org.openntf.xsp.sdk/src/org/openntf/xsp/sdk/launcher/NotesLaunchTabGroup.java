package org.openntf.xsp.sdk.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.internal.launching.launcher.VMHelper;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.pde.ui.launcher.EclipseLauncherTabGroup;
import org.openntf.xsp.sdk.Activator;

public class NotesLaunchTabGroup extends EclipseLauncherTabGroup {

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
		configuration.setAttribute(IPDELauncherConstants.PRODUCT, "com.ibm.notes.branding.notes");

		// Need to deal with spaces in Program Files Path
		
		IVMInstall jre = VMHelper.getVMInstall("XPages Notes JRE");
		IPath jrePath = JavaRuntime.newJREContainerPath(jre);
		String attr = jrePath.toPortableString();
				
		configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, attr);
			
		
	}
	
	private String readResource(String resourcePath) {

		StringBuilder result = new StringBuilder();

		try {
			// Url url = new
			// URL("platform:/plugin/de.vogella.rcp.plugin.filereader/files/test.txt");
			URL url = Activator.getDefault().getBundle().getResource(resourcePath);
			InputStream inputStream = url.openConnection().getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				result.append(inputLine);
				result.append(System.lineSeparator());				
			}

			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}

	private String getArgs() {
		
		String res = "org/openntf/xsp/sdk/launcher/notesargs.txt";
		return readResource(res);
		
	}
	
	private String getVmArgs() {

		String res = "org/openntf/xsp/sdk/launcher/notesvmargs.txt";
		return readResource(res);

	}

}
