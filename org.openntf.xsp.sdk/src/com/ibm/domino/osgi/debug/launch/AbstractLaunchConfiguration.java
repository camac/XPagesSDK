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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.PluginPathFinder;
import org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper;
import org.eclipse.pde.launching.EquinoxLaunchConfiguration;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
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
public abstract class AbstractLaunchConfiguration extends EquinoxLaunchConfiguration {

//	private TargetBundle osgiTargetBundle;
	private IPluginModelBase osgiFrameworkModel;

	/**
	 * 
	 */
	public AbstractLaunchConfiguration() {
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

	private class AbortException extends RuntimeException {
		private static final long serialVersionUID = 1L;
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
				displayMessage("Error", 
						"The \"Default Auto-Start\" attribute within your {0} configuration must be set "
						+ "to \"false\". A value of \"true\" has been detected.\n\nThe configuration will not be applied!",
						AbstractLaunchConfiguration.this.getName());
				return;
			}

			final LaunchHandler config = new LaunchHandler(this);
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					new LaunchDialog(config,
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).open();
				}
			});

			if (config.getReturnCode() == IDialogConstants.CANCEL_ID) {
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
			updateConfigIni(config, configuration);

			// Create the pde.launch.ini
			createPDELaunchIni(config, configuration);

			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					try {
						displayMessage("Success",
								"Successfully updated \"{0}\".\nTo run normally, please delete this file.", 
								getPDELaunchIni(config).getAbsolutePath());
					} catch (IOException e) {
						MessageDialog.openError(shell, "Error", e.getMessage());
					}
				}
			});

		} catch (final Exception e) {
			monitor.setCanceled(true);
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					String message = e.getMessage();
					if (e instanceof AbortException) {
						message = "Domino OSGi launch configuration aborted by user";
					}
					
					if(StringUtil.isEmpty(message)) {
						message = e.getClass().getName();
					}
					
					MessageDialog.openError(null, "Error!", message);
				}
			});
		}
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private boolean isTargetPlatformPluginsEnabled(ILaunchConfiguration configuration) throws CoreException {
		String selectedTargetBundles = configuration.getAttribute(IPDELauncherConstants.TARGET_BUNDLES, (String) null);
		return selectedTargetBundles != null && selectedTargetBundles.length() > 0;
	}

	/**
	 * @param config
	 * @param configuration
	 * @throws CoreException
	 * @throws IOException
	 */
	private void updateConfigIni(LaunchHandler config, ILaunchConfiguration configuration)
			throws CoreException, IOException {
		INotesDominoPlatform ndPlatform = this.getNotesDominoPlatform();
		
		File configIni = new File(getConfigDir(configuration), "config.ini");

		if (!configIni.exists()) {
			// No config ini to update
			// TODO Error message in case? 
			return;
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

		if (isTargetPlatformPluginsEnabled(configuration)) {
			// TODO Error message here
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

			StringBuffer osgiBundles = new StringBuffer(props.getProperty("osgi.bundles"));

			// Get the osgi.bundles key and augment it
			String binPath = ndPlatform.getRemoteInstallFolder();

			Set<String> bundlePaths = new LinkedHashSet<String>();
			Map<String, IPluginModelBase> modelMap;
			
			// First scan rcp folder.
			modelMap = computeTargetModels(fAllBundles, ndPlatform.getRemoteRcpTargetFolder());

			for(Map.Entry<String, IPluginModelBase> entry: modelMap.entrySet()) {
				String id = entry.getKey();
				IPluginModelBase bundle = entry.getValue();
				
				String suffix = getBundleSuffix(id);
				
			}
			
//			
//			
//			String targetBundles = computeTargetBundles_OLD(fAllBundles,
//					binPath + "/" + getOSGIDirectoryName() + "/rcp/eclipse");
//			if (targetBundles != null && targetBundles.length() > 0) {
//				if (osgiBundles == null) {
//					osgiBundles = targetBundles;
//				} else {
//					osgiBundles = targetBundles + "," + osgiBundles;
//				}
//			}
//
//			targetBundles = computeTargetBundles_OLD(fAllBundles,
//					binPath + "/" + getOSGIDirectoryName() + "/shared/eclipse");
//			if (targetBundles != null && targetBundles.length() > 0) {
//				if (osgiBundles == null) {
//					osgiBundles = targetBundles;
//				} else {
//					osgiBundles = osgiBundles + "," + targetBundles;
//				}
//			}
//
//			String dataPath = config.getDominoDataPath();
//			dataPath = prunePath(dataPath);
//			targetBundles = computeTargetBundles_OLD(fAllBundles,
//					dataPath + "/" + getWorkspaceRelativePath() + "/applications/eclipse");
//			if (targetBundles != null && targetBundles.length() > 0) {
//				if (osgiBundles == null) {
//					osgiBundles = targetBundles;
//				} else {
//					osgiBundles = osgiBundles + "," + targetBundles;
//				}
//			}
//			/*
//			 * Read all of the .link files - this will add the Upgrade Pack
//			 * plugins to the config
//			 */
//			File linksDir = new File(binPath + "/" + getOSGIDirectoryName() + "/rcp/eclipse/links");
//			if (linksDir.exists() && linksDir.isDirectory()) {
//				File[] links = linksDir.listFiles();
//				if (links != null) {
//					for (File link : links) {
//						FileReader reader = new FileReader(link);
//						BufferedReader lineReader = new BufferedReader(reader);
//						String linkPath = lineReader.readLine();
//						if (!StringUtil.isEmpty(linkPath)) {
//							if (linkPath.indexOf('=') != -1) {
//								linkPath = linkPath.substring(linkPath.indexOf('=') + 1);
//								int index = linkPath.indexOf(':');
//								if (index != -1) {
//									if (linkPath.charAt(index - 1) == '\\') {
//										linkPath = linkPath.substring(0, index - 1) + linkPath.substring(index);
//									}
//
//									// XXX link path should be modified for
//									// remote
//									linkPath = prunePath(linkPath);
//									linkPath = linkPath + "/eclipse";
//									targetBundles = computeTargetBundles_OLD(fAllBundles, linkPath);
//									if (targetBundles != null && targetBundles.length() > 0) {
//										if (osgiBundles == null) {
//											osgiBundles = targetBundles;
//										} else {
//											osgiBundles = osgiBundles + "," + targetBundles;
//										}
//									}
//								}
//							}
//
//						}
//					}
//				}
//			}
//
//			// XXX System Fragment file should be modified for remote
//			// Add the dynamically generated fragment to the system bundle
//			String systemFragment = getSystemFragmentFileName();
//			if (systemFragment != null) {
//				osgiBundles += ",reference:file:" + dataPath + "/" + getWorkspaceRelativePath()
//						+ "/.config/domino/eclipse/plugins/" + systemFragment;
//			}
//
//			props.setProperty("osgi.bundles", osgiBundles);
//
//			// XXX Install area should be modified for remote
//			props.setProperty("osgi.install.area", "file:" + binPath + "/" + getOSGIDirectoryName() + "/rcp/eclipse");
//
//			if (osgiFrameworkModel != null) {
//				props.put("osgi.framework", getBundleURL(osgiFrameworkModel, false));
//			}
//
//			// Save the configuration
//			FileOutputStream fos = null;
//			try {
//				props.store(fos = new FileOutputStream(configIni), "");
//			} finally {
//				if (fos != null) {
//					fos.close();
//				}
//			}
		}
	}

	private String getBundleSuffix(String id) {
		if ("org.eclipse.equinox.common".equals(id)) {
			return "@2:start";
		} else if ("org.eclipse.core.runtime".equals(id)) {
			return "@start";
		} else if ("org.eclipse.equinox.common".equals(id)) {
			return "@2:start";
		} else if ("org.eclipse.core.jobs".equals(id)) {
			return "@4:start";
		} else if ("org.eclipse.equinox.registry".equals(id)) {
			return "@4:start";
		} else if ("org.eclipse.equinox.preferences".equals(id)) {
			return "@4:start";
		}

		return "";
	}

	protected String prunePath(String path) {
		if (!StringUtil.isEmpty(path)) {
			if (path.endsWith("/") || path.endsWith("\\")) {
				return path.substring(0, path.length() - 1);
			}
		}
		return path;
	}

//	private ITargetPlatformService getTargetService() {
//		return (ITargetPlatformService) PlatformUI.getWorkbench().getService(ITargetPlatformService.class);
//	}
//	
//	private Map<String, TargetBundle> computeTargetBundles(Map<?, ?> workspacePlugins, String eclipseLocation) {
//		Map<String, TargetBundle> bundleMap = new HashMap<String, TargetBundle>();
//		
//		ITargetLocation location = getTargetService().newDirectoryLocation(eclipseLocation);
//		TargetBundle[] bundles = location.getBundles();
//		
//		// FIXME fix this
//		if(bundles == null) {
//			System.out.println("Nothing found: " + eclipseLocation);
//			return bundleMap;
//		}
//		
//		for (TargetBundle bundle: bundles) {
//			String id = bundle.getBundleInfo().getSymbolicName();
//			
//			if ("org.eclipse.osgi".equals(id)) {
//				osgiTargetBundle = bundle;
//			} else {
//				if (workspacePlugins.containsKey(id)) {
//					// already selected, continue
//					continue;
//				} else {
//					BundleInfo bundleInfo = bundle.getBundleInfo();
//					System.out.println(bundleInfo);
//					bundleMap.put(id, bundle);
//				}
//			}
//		}		
//		
//		return bundleMap;
//
//	}
	
	
	/**
	 * @param osgiFrameworkModel2
	 * @param b
	 * @return
	 */
	private String getBundleURL(IPluginModelBase model, boolean bIncludeReference) {
		if (model == null || model.getInstallLocation() == null) {
			return null;
		}

		// XXX getBundleURL should be modified for remote
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
//				osgiFrameworkModel = model;
			} else {
				if (workspacePlugins.containsKey(id)) {
					// already selected, continue
					continue;
				} else {
					modelMap.put(id, model);
					
					System.out.println(getBundleURL(model, false));
					
				}
			}
		}		
		
		return modelMap;
	}

	/**
	 * @param workspacePlugins
	 * @param models
	 * @return
	 */
	private String getTargetBundles(Map<?, ?> workspacePlugins, IPluginModelBase[] models) {
		StringBuffer buffer = new StringBuffer();
		for (IPluginModelBase model : models) {
			String id = model.getPluginBase().getId();
			// if ( "com.ibm.pvc.servlet".equals( id ) ){
			// continue;
			// }
			if (workspacePlugins.containsKey(id)) {
				if ("org.eclipse.osgi".equals(id)) {
//					osgiFrameworkModel = model;
				}
				// already selected, continue
				continue;
			}
			if (!"org.eclipse.osgi".equals(id)) {
				if (buffer.length() > 0) {
					buffer.append(",");
				}
				buffer.append(getBundleURL(model, true));
				if ("org.eclipse.equinox.common".equals(id)) {
					buffer.append("@2:start");
				} else if ("org.eclipse.core.runtime".equals(id)) {
					buffer.append("@start");
				} else if ("org.eclipse.equinox.common".equals(id)) {
					buffer.append("@2:start");
				} else if ("org.eclipse.core.jobs".equals(id)) {
					buffer.append("@4:start");
				} else if ("org.eclipse.equinox.registry".equals(id)) {
					buffer.append("@4:start");
				} else if ("org.eclipse.equinox.preferences".equals(id)) {
					buffer.append("@4:start");
				}
			} else {
//				osgiFrameworkModel = model;
			}
		}
		return buffer.toString();
	}

	/**
	 * @param configuration
	 * @throws IOException
	 */
	private void createPDELaunchIni(LaunchHandler osgiConfig, ILaunchConfiguration configuration)
			throws IOException {
		File pdeLaunchIniFile = getPDELaunchIni(osgiConfig);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pdeLaunchIniFile);
			Properties props = new Properties();
			props.setProperty("configuration", configuration.getName());

			// XXX Modify configuration area for Remote
			props.setProperty("osgi.configuration.area",
					getConfigDir(configuration).getAbsolutePath().replace('\\', '/'));
			props.store(fos, "Generated by IBM Lotus Domino OSGi Debug Plug-in");
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * @param osgiConfig
	 * @return
	 * @throws IOException
	 */
	private File getPDELaunchIni(LaunchHandler osgiConfig) throws IOException {
		// FIXME Profile support
		String workspacePath = getNotesDominoPlatform().getRemoteWorkspaceFolder();
		
		final File dominoWorkspaceDir = new File(workspacePath);
		if (!dominoWorkspaceDir.exists() || !dominoWorkspaceDir.isDirectory()) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					boolean bCreate = MessageDialog.openQuestion(shell, "Question",
							MessageFormat.format(
									"The directory \"{0}\" does not exist.\nThis may be because the OSGi configuration has never been run previously.\nWould you like to create it?",
									dominoWorkspaceDir.getAbsolutePath()));
					if (bCreate) {
						dominoWorkspaceDir.mkdirs();
					}
				}
			});
		}

		if (!dominoWorkspaceDir.exists() || !dominoWorkspaceDir.isDirectory()) {
			throw new AbortException();
		}
		return new File(dominoWorkspaceDir, "pde.launch.ini");
	}
	
	protected abstract INotesDominoPlatform getNotesDominoPlatform();
	
	/**
	 * @return the list of defined profiles
	 */
	public abstract String[] getProfiles();

	/**
	 * @param selectedProfile
	 */
	public abstract void setProfile(String selectedProfile);

	public abstract String getName();

	protected abstract String getSystemFragmentFileName();

	protected void displayMessage(String title, String message, Object... args) {
		final String dialogTitle = title;
		final String dialogMessage = MessageFormat.format(message, args);
		
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(shell, dialogTitle, dialogMessage);
			}
		});

	}
	
}
