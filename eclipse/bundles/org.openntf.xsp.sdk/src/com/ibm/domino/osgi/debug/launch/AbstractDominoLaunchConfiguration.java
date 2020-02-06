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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
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
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.commons.osgi.LaunchUtil;
import org.openntf.xsp.sdk.exceptions.AbortException;
import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.commons.utils.CommonUtils;
import org.openntf.xsp.sdk.commons.utils.StringUtil;

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

	private IPluginModelBase osgiFrameworkModel;
	private String selectedProfile=null;
	
	protected static final ILog logger = Activator.getDefault().getLog();
	
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
				EclipseLaunchUtil.displayMessage(true, "Error",
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
			String filePath = EclipseLaunchUtil.createPDELaunchIni(this, configuration);

			EclipseLaunchUtil.displayMessage(false, "Success",
						"Successfully updated \"{0}\".\nTo run normally, please delete this file.", 
						filePath);

		} catch (final Exception e) {
			monitor.setCanceled(true);
			
			String errorMessage = e.getMessage();
			if (e instanceof AbortException) {
				errorMessage = "Domino OSGi launch configuration aborted by user";
			}
			
			if(StringUtil.isEmpty(errorMessage)) {
				errorMessage = e.getClass().getName();
			}

			logger.log(new Status(Status.ERROR, Activator.PLUGIN_ID, errorMessage, e));
			EclipseLaunchUtil.displayMessage(true, "Error", errorMessage,	"");
		}
	}

	/**
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

		if (EclipseLaunchUtil.isTargetPlatformPluginsEnabled(configuration)) {
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

			Collection<String> osgiBundleList = EclipseLaunchUtil.populateBundleList(props.getProperty("osgi.bundles"), ndPlatform);
			
			osgiBundleList.addAll(computeOsgiBundles(ndPlatform, ndPlatform.getRemoteRcpTargetFolder()));
			osgiBundleList.addAll(computeOsgiBundles(ndPlatform, ndPlatform.getRemoteRcpSharedFolder()));
			
			String wsPluginPath = ndPlatform.getRemoteWorkspaceFolder(getSelectedProfile()) + "/applications/eclipse";
			osgiBundleList.addAll(computeOsgiBundles(ndPlatform, wsPluginPath));
			
			Collection<String> linkedRepos = LaunchUtil.findLinkedRepos(new File(ndPlatform.getRemoteRcpTargetFolder() + "/links"));
			
			for(String linkedRepo: linkedRepos) {
				String remoteLinkPath = LaunchUtil.toRemotePath(linkedRepo, ndPlatform);
				
				if(CommonUtils.isEmpty(remoteLinkPath)) {
					String message = MessageFormat.format(
							"Your platform points in \"{0}\" via link file. We don't know how to see this directory. These bundles will be ignored.\n\n" +
							"Please make sure all necessary link files point into a directory under your install or data folder.", linkedRepo);
	
					logger.log(new Status(Status.WARNING, Activator.PLUGIN_ID, message));
				} else {
					osgiBundleList.addAll(computeOsgiBundles(ndPlatform, remoteLinkPath));
				}
			}
			
			String systemFragmentJar = ndPlatform.getLocalWorkspaceFolder(getSelectedProfile()) + 
										"/.config/domino/eclipse/plugins/" + ndPlatform.getSystemFragmentFileName();
			osgiBundleList.add("reference:file:"+LaunchUtil.fixPathSeparators(systemFragmentJar));

			StringBuffer bundles=new StringBuffer();
			for(String osgiBundle: osgiBundleList) {
				if(bundles.length()>0) {
					bundles.append(",");
				}
				bundles.append(osgiBundle);
			}
			
			props.setProperty("osgi.bundles", bundles.toString());
			props.setProperty("osgi.install.area", "file:" + LaunchUtil.fixPathSeparators(ndPlatform.getLocalRcpTargetFolder()));

			if (osgiFrameworkModel != null) {
				String remotePath = getBundleUrl(osgiFrameworkModel, false).substring("file:".length());
				String localPath = LaunchUtil.toLocalPath(remotePath, ndPlatform);
				
				if(CommonUtils.isEmpty(localPath)) {
					String message = MessageFormat.format(
							"Your platform needs the bundle \"{0}\" as the \"osgi.framework\". However we don't know how to see this bundle.", remotePath);
					logger.log(new Status(Status.WARNING, Activator.PLUGIN_ID, message));
				} else {
					props.put("osgi.framework", "file:"+localPath);
				}
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
	 * @param model
	 * @param bIncludeReference
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
		PDEState pdeState;
		// The signature for PDEState's constructor changed in Eclipse 2019-03
		// https://github.com/eclipse/eclipse.pde.ui/commit/3db2f1e50aa7ae5efc0e07a1f26eecedd80a7159#diff-a56311895a435ece21aa9ef829607884
		Constructor<PDEState> ctor;
		try {
			// Check for the 2019-03 version
			ctor = PDEState.class.getConstructor(URI[].class, boolean.class, boolean.class, IProgressMonitor.class);
			URI[] pluginPathURIs = Arrays.stream(pluginPaths).map(u -> URI.create(u.toString().replace(" ", "%20"))).toArray(URI[]::new);
			pdeState = ctor.newInstance(pluginPathURIs, true, true, new NullProgressMonitor());
		} catch(NoSuchMethodException e1) {
			// Failing that, check for the pre-2019-03 version
			try {
				ctor = PDEState.class.getConstructor(URL[].class, boolean.class, boolean.class, IProgressMonitor.class);
				pdeState = ctor.newInstance(pluginPaths, true, true, new NullProgressMonitor());
			} catch(NoSuchMethodException e2) {
				throw new RuntimeException("Unable to locate usable constructor for PDEState", e2);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e2a) {
				throw new RuntimeException("Unable to create PDEState", e2a);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1a) {
			throw new RuntimeException("Unable to create PDEState", e1a);
		}
		
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
			
			String suffix = LaunchUtil.getBundleSuffix(id);
			String remoteUrl = getBundleUrl(bundle, false);
			String localPath = LaunchUtil.toLocalPath(remoteUrl.substring("file:".length()), ndPlatform);

			if(CommonUtils.isEmpty(localPath)) {
				logger.log(new Status(Status.WARNING, Activator.PLUGIN_ID, 
							MessageFormat.format("Bundle \"{0}\" cannot be converted to a local representation.",remoteUrl)));
			} else {
				bundles.add("reference:file:" + localPath + suffix);
			}
		}

		System.out.println(bundles.size() + " bundles found in '" + remotePath + "'");
		
		return bundles;
	}
	
	public abstract INotesDominoPlatform getNotesDominoPlatform();
	public abstract String[] getProfiles();
	public abstract String getName();
	
}
