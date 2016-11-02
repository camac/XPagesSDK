/*
 * � Copyright IBM Corp. 2012
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.PluginPathFinder;
import org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper;
import org.eclipse.pde.launching.EquinoxLaunchConfiguration;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.ui.PlatformUI;
import org.openntf.xsp.sdk.exceptions.AbortException;
import org.openntf.xsp.sdk.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.utils.StringUtil;

/**
 * @author dtaieb
 * @author doconnor
 * 
 *         An abstract Eclipse OSGi Framework launch configuration.
 *         Configurations extending from this class will modify the Domino OSGi
 *         configuration.
 */

//@SuppressWarnings("restriction")
public abstract class AbstractDominoLaunchConfiguration extends EquinoxLaunchConfiguration {

//	private TargetBundle osgiTargetBundle;
	private IPluginModelBase osgiFrameworkModel;
	private String selectedProfile=null;
	
	/**
	 * 
	 */
	public AbstractDominoLaunchConfiguration() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.launching.AbstractPDELaunchConfiguration#launch(org.
	 * eclipse.debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.debug.core.ILaunch,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		try {
			doLaunch(configuration, mode, launch, monitor);
		} finally {
			// For now we don't connect to the Domino JVM process, so manually
			// remove the launch from debug perspective otherwise it will stay
			// there forever
			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
		}
	}

	@Override
	public File getConfigDir(ILaunchConfiguration configuration) {
		return super.getConfigDir(configuration);
	}

	public String getSelectedProfile() {
		return selectedProfile;
	}

	public void setSelectedProfile(String selectedProfile) {
		this.selectedProfile = selectedProfile;
	}

	/**
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param monitor
	 */
	private void doLaunch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
		try {
			boolean autostart = configuration.getAttribute(IPDELauncherConstants.DEFAULT_AUTO_START, true);
			if (autostart) {
				// Auto-start must be set to false!
				LaunchUtils.displayMessage(true, "Error", 
						"The \"Default Auto-Start\" attribute within your {0} configuration must be set "
						+ "to \"false\". A value of \"true\" has been detected.\n\nThe configuration will not be applied!",
						AbstractDominoLaunchConfiguration.this.getName());
				return;
			}

			final LaunchHandler launchHandler = new LaunchHandler(this);

			PlatformUI.getWorkbench().getDisplay().syncExec(new LaunchDialog.LaunchThread(launchHandler));

			if (launchHandler.getReturnCode() == IDialogConstants.CANCEL_ID) {
				return;
			}

			// Call preLaunchCheck to fill the bundleMap
			try {
				preLaunchCheck(configuration, launch, new SubProgressMonitor(monitor, 2));
			} catch (CoreException e) {
				if (e.getStatus().getSeverity() == IStatus.CANCEL) {
					monitor.setCanceled(true);
					return;
				}
				throw e;
			}
			
			getProgramArguments(configuration);

			// Update the config.ini with any environment variables set by the
			// configuration
			updateConfigIni(configuration);

			// Create the pde.launch.ini
			String filePath = LaunchUtils.createPDELaunchIni(this, configuration);

			LaunchUtils.displayMessage(false, "Success",
						"Successfully updated \"{0}\".\nTo run normally, please delete this file.", 
						filePath);

		} catch (final Exception e) {
			monitor.setCanceled(true);
			
			String errorMessage = e.getMessage();
			if (e instanceof AbortException) {
				errorMessage = "Domino OSGi launch configuration aborted by user";
			} else {
				e.printStackTrace(System.err);
			}
			
			if(StringUtil.isEmpty(errorMessage)) {
				errorMessage = e.getClass().getName();
			}
			
			LaunchUtils.displayMessage(true, "Error", errorMessage,	"");
			
		}
	}

	/**
	 * @param config
	 * @param configuration
	 * @throws CoreException
	 * @throws IOException
	 */
	private void updateConfigIni(ILaunchConfiguration configuration) throws CoreException, IOException {
		INotesDominoPlatform ndPlatform = this.getNotesDominoPlatform();
		
		File configIni = new File(getConfigDir(configuration), "config.ini");

		if (!configIni.exists()) {
			// No config ini to update
			String errMsg = MessageFormat.format(
						"Error occured populating config.ini file. Cannot find the file '{0}'",
						configIni.getAbsolutePath());
			throw new RuntimeException(errMsg);
		}

		Map<String, String> envMap = configuration.getAttribute("org.eclipse.debug.core.environmentVariables",
				(Map<String, String>) null);

		if (envMap != null) {
			// Add all the env variables that start with "domino.osgi" to the
			// config.ini
			Map<String, String> dominoVars = new HashMap<String, String>();
			for (String key : envMap.keySet()) {
				if (key instanceof String && key.startsWith("domino.osgi")) {
					String value = envMap.get(key);
					dominoVars.put(key, value);
				}
			}

			if (!dominoVars.isEmpty()) {
				FileWriter fw = null;
				try {
					fw = new FileWriter(configIni, true);
					for (String key : dominoVars.keySet()) {
						fw.append(key + "=" + dominoVars.get(key));
					}
				} finally {
					if (fw != null) {
						fw.close();
					}
				}
			}
		}

		if (LaunchUtils.isTargetPlatformPluginsEnabled(configuration)) {
			throw new RuntimeException("Please do not enable any target platform plugins.");
		} else {
			// Load the properties from config.ini
			Properties props = new Properties();
			FileInputStream fis = null;
			try {
				props.load(fis = new FileInputStream(configIni));
			} finally {
				if (fis != null) {
					fis.close();
				}
			}

			Collection<String> osgiBundleList = LaunchUtils.populateBundleList(props.getProperty("osgi.bundles"), ndPlatform);
			
			osgiBundleList.addAll(computeOsgiBundles(ndPlatform, ndPlatform.getRemoteRcpTargetFolder()));
			osgiBundleList.addAll(computeOsgiBundles(ndPlatform, ndPlatform.getRemoteRcpSharedFolder()));
			
			String wsPluginPath = ndPlatform.getRemoteWorkspaceFolder(getSelectedProfile()) + "/applications/eclipse";
			osgiBundleList.addAll(computeOsgiBundles(ndPlatform, wsPluginPath));
			
			// TODO Add link files
			Collection<String> linkedRepos = LaunchUtils.findLinkedRepos(new File(ndPlatform.getRemoteRcpTargetFolder() + "/links"));
			
			for(String linkedRepo: linkedRepos) {
				osgiBundleList.addAll(computeOsgiBundles(ndPlatform, LaunchUtils.toRemotePath(linkedRepo, ndPlatform)));
			}
			
			String systemFragmentJar = ndPlatform.getLocalWorkspaceFolder(getSelectedProfile()).replace('\\', '/') + 
										"/.config/domino/eclipse/plugins/" + ndPlatform.getSystemFragmentFileName();
			osgiBundleList.add("reference:file:"+systemFragmentJar);

			StringBuffer bundles=new StringBuffer();
			for(String osgiBundle: osgiBundleList) {
				if(bundles.length()>0) {
					bundles.append(",");
				}
				bundles.append(osgiBundle);
			}
			
			props.setProperty("osgi.bundles", bundles.toString());
			props.setProperty("osgi.install.area", "file:" + ndPlatform.getLocalRcpSharedFolder().replace('\\', '/'));

			if (osgiFrameworkModel != null) {
				props.put("osgi.framework", getBundleUrl(osgiFrameworkModel, false));
			}

			// Save the configuration
			FileOutputStream fos = null;
			try {
				props.store(fos = new FileOutputStream(configIni), "Created by OpenNTF XPages SDK");
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
	}
	
	/**
	 * @param osgiFrameworkModel2
	 * @param b
	 * @return
	 */
	private String getBundleUrl(IPluginModelBase model, boolean bIncludeReference) {
		if (model == null || model.getInstallLocation() == null) {
			return null;
		}

		return LaunchConfigurationHelper.getBundleURL(model, bIncludeReference);
	}

	/**
	 * @param workspacePlugins
	 * @param eclipseLocation
	 * @return
	 */
	private Map<String, IPluginModelBase> computeTargetModels(Map<?, ?> workspacePlugins, String eclipseLocation) {
		Map<String, IPluginModelBase> modelMap = new HashMap<String, IPluginModelBase>();
		
		URL[] pluginPaths = PluginPathFinder.getPluginPaths(eclipseLocation, false);
		PDEState pdeState = new PDEState(pluginPaths, true, true, new NullProgressMonitor());
		IPluginModelBase[] models = pdeState.getTargetModels();

		for (IPluginModelBase model : models) {
			String id = model.getPluginBase().getId();
			
			if ("org.eclipse.osgi".equals(id)) {
				osgiFrameworkModel = model;
			} else {
				if (workspacePlugins.containsKey(id)) {
					// already selected, continue
					continue;
				} else {
					modelMap.put(id, model);
				}
			}
		}		
		
		return modelMap;
	}

	private Collection<String> computeOsgiBundles(INotesDominoPlatform ndPlatform, String remotePath) {
		Set<String> bundles = new LinkedHashSet<String>();
		
		// Scan folder
		Map<String, IPluginModelBase> modelMap = computeTargetModels(fAllBundles, remotePath);

		for(Map.Entry<String, IPluginModelBase> entry: modelMap.entrySet()) {
			String id = entry.getKey();
			IPluginModelBase bundle = entry.getValue();
			
			String bundleUrl = LaunchUtils.toLocalBundleUrl(getBundleUrl(bundle, false), ndPlatform);				
			String suffix = LaunchUtils.getBundleSuffix(id);

			bundles.add("reference:" + bundleUrl + suffix);
		}

		System.out.println(bundles.size() + " bundles found in '" + remotePath + "'");
		
		return bundles;
	}
	
	public abstract INotesDominoPlatform getNotesDominoPlatform();
	public abstract String[] getProfiles();
	public abstract String getName();
	
}
