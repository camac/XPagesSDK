/*
 * © Copyright IBM Corp. 2012
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

import com.ibm.domino.osgi.debug.utils.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
import org.eclipse.pde.core.target.ITargetLocation;
import org.eclipse.pde.core.target.ITargetPlatformService;
import org.eclipse.pde.core.target.TargetBundle;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PDEState;
import org.eclipse.pde.internal.core.PluginPathFinder;
import org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper;
import org.eclipse.pde.launching.EquinoxLaunchConfiguration;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author dtaieb
 * @author doconnor
 * 
 *         An abstract Eclipse OSGi Framework launch configuration.
 *         Configurations extending from this class will modify the Domino OSGi
 *         configuration.
 */
@SuppressWarnings("restriction")
public abstract class AbstractDominoOSGILaunchConfiguration extends EquinoxLaunchConfiguration {

	private IPluginModelBase osgiFrameworkModel;
	private TargetBundle osgiTargetBundle;

	/**
	 * 
	 */
	public AbstractDominoOSGILaunchConfiguration() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.pde.launching.AbstractPDELaunchConfiguration#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
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
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						String dialogTitle = "Error";
						String dialogMessage = "The \"Default Auto-Start\" attribute within your {0} configuration must be set to \"false\". A value of \"true\" has been detected.\n\nThe configuration will not be applied!";
						dialogMessage = MessageFormat.format(dialogMessage,
								AbstractDominoOSGILaunchConfiguration.this.getName());
						Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

						MessageDialog.openError(shell, dialogTitle, dialogMessage);
					}
				});
				return;
			}
			final DominoOSGIConfig config = new DominoOSGIConfig(this, getWorkspaceRelativePath());
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					new DominoOSGIConfigCreateDialog(config, PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell()).open();
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
						MessageDialog.openInformation(shell, "Success", MessageFormat.format(
								"Successfully updated \"{0}\".\nTo run normally, please delete this file.",
								getPDELaunchIni(config).getAbsolutePath()));
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
					MessageDialog.openError(null, "error", message);
				}
			});
		}
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private boolean isTargetPlatformPluginsDisabled(ILaunchConfiguration configuration) throws CoreException {
		String selectedTargetBundles = configuration.getAttribute(IPDELauncherConstants.TARGET_BUNDLES, (String) null);
		return selectedTargetBundles == null || selectedTargetBundles.length() == 0;
	}

	/**
	 * @param config
	 * @param configuration
	 * @throws CoreException
	 * @throws IOException
	 */
	private void updateConfigIni(DominoOSGIConfig config, ILaunchConfiguration configuration) throws CoreException,
			IOException {
		File configIni = new File(getConfigDir(configuration), "config.ini");
		if (!configIni.exists()) {
			// No config ini to update
			return;
		}

		Map<String, String> envMap = configuration.getAttribute("org.eclipse.debug.core.environmentVariables",
				(Map) null);
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

		if (isTargetPlatformPluginsDisabled(configuration)) {
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

			String osgiBundles = props.getProperty("osgi.bundles");

			// Get the osgi.bundles key and augment it
			String binPath = config.getDominoBinPath();
			binPath = prunePath(binPath);
			String targetBundles = computeTargetBundles_OLD(fAllBundles, binPath + "/" + getOSGIDirectoryName()
					+ "/rcp/eclipse");
			if (targetBundles != null && targetBundles.length() > 0) {
				if (osgiBundles == null) {
					osgiBundles = targetBundles;
				} else {
					osgiBundles = targetBundles + "," + osgiBundles;
				}
			}

			targetBundles = computeTargetBundles_OLD(fAllBundles, binPath + "/" + getOSGIDirectoryName()
					+ "/shared/eclipse");
			if (targetBundles != null && targetBundles.length() > 0) {
				if (osgiBundles == null) {
					osgiBundles = targetBundles;
				} else {
					osgiBundles = osgiBundles + "," + targetBundles;
				}
			}
			String dataPath = config.getDominoDataPath();
			dataPath = prunePath(dataPath);
			targetBundles = computeTargetBundles_OLD(fAllBundles, dataPath + "/" + getWorkspaceRelativePath()
					+ "/applications/eclipse");
			if (targetBundles != null && targetBundles.length() > 0) {
				if (osgiBundles == null) {
					osgiBundles = targetBundles;
				} else {
					osgiBundles = osgiBundles + "," + targetBundles;
				}
			}
			/*
			 * Read all of the .link files - this will add the Upgrade Pack plugins to the config
			 */
			File linksDir = new File(binPath + "/" + getOSGIDirectoryName() + "/rcp/eclipse/links");
			if (linksDir.exists() && linksDir.isDirectory()) {
				File[] links = linksDir.listFiles();
				if (links != null) {
					for (File link : links) {
						FileReader reader = new FileReader(link);
						BufferedReader lineReader = new BufferedReader(reader);
						String linkPath = lineReader.readLine();
						if (!StringUtil.isEmpty(linkPath)) {
							if (linkPath.indexOf('=') != -1) {
								linkPath = linkPath.substring(linkPath.indexOf('=') + 1);
								int index = linkPath.indexOf(':');
								if (index != -1) {
									if (linkPath.charAt(index - 1) == '\\') {
										linkPath = linkPath.substring(0, index - 1) + linkPath.substring(index);
									}
									linkPath = prunePath(linkPath);
									linkPath = linkPath + "/eclipse";
									targetBundles = computeTargetBundles_OLD(fAllBundles, linkPath);
									if (targetBundles != null && targetBundles.length() > 0) {
										if (osgiBundles == null) {
											osgiBundles = targetBundles;
										} else {
											osgiBundles = osgiBundles + "," + targetBundles;
										}
									}
								}
							}

						}
					}
				}
			}

			// Add the dynamically generated fragment to the system bundle
			String systemFragment = getSystemFragmentFileName();
			if (systemFragment != null) {
				osgiBundles += ",reference:file:" + dataPath + "/" + getWorkspaceRelativePath()
						+ "/.config/domino/eclipse/plugins/" + systemFragment;
			}

			props.setProperty("osgi.bundles", osgiBundles);

			props.setProperty("osgi.install.area", "file:" + binPath + "/" + getOSGIDirectoryName() + "/rcp/eclipse");
			if (osgiFrameworkModel != null) {
				props.put("osgi.framework", getBundleURL(osgiFrameworkModel, false));
			}

			// Save the configuration
			FileOutputStream fos = null;
			try {
				props.store(fos = new FileOutputStream(configIni), "");
			} finally {
				if (fos != null) {
					fos.close();
				}
			}
		}
	}

	protected String prunePath(String path) {
		if (!StringUtil.isEmpty(path)) {
			if (path.endsWith("/") || path.endsWith("\\")) {
				return path.substring(0, path.length() - 1);
			}
		}
		return path;
	}

	/**
	 * @param osgiFrameworkModel2
	 * @param b
	 * @return
	 */
	private String getBundleURL(IPluginModelBase model, boolean bIncludeReference) {
		if (model == null || model.getInstallLocation() == null) {
			return null;
		}
		return LaunchConfigurationHelper.getBundleURL(model, bIncludeReference);
		// try {
		// return
		// getBundleFromHelper("org.eclipse.pde.internal.ui.launcher.LaunchConfigurationHelper",
		// model,
		// bIncludeReference);
		// } catch (Throwable t) {
		// // We may be in a 3.6 eclipse, LaunchConfigurationHelper has moved
		// // to another package
		// try {
		// return
		// getBundleFromHelper("org.eclipse.pde.internal.launching.launcher.LaunchConfigurationHelper",
		// model, bIncludeReference);
		// } catch (Throwable e) {
		// // Fallback
		// StringBuilder sb = new StringBuilder();
		// if (bIncludeReference) {
		// sb.append("reference:");
		// }
		// sb.append("file:");
		// sb.append(new
		// Path(model.getInstallLocation()).removeTrailingSeparator().toString());
		// return sb.toString();
		// }
		// }
	}

	/**
	 * @param className
	 * @param model
	 * @param bIncludeRefence
	 * @return
	 * @throws Throwable
	 */
	private String getBundleFromHelper(String className, IPluginModelBase model, boolean bIncludeRefence)
			throws Throwable {
		Class<?> helper = Class.forName(className);
		Method m = helper.getMethod("getBundleURL", IPluginModelBase.class, Boolean.TYPE);

		return (String) m.invoke(null, model, bIncludeRefence);
	}

	/**
	 * Returns the target platform service or <code>null</code> if the service
	 * could
	 * not be acquired.
	 * 
	 * @return target platform service or <code>null</code>
	 */
	private ITargetPlatformService getTargetService() {
		return (ITargetPlatformService) PDECore.getDefault().acquireService(ITargetPlatformService.class.getName());
	}

	private String computeTargetBundlesLuna(Map<?, ?> workspacePlugins, String eclipseLocation) {
		ITargetLocation location = getTargetService().newDirectoryLocation(eclipseLocation);
		TargetBundle[] bundles = location.getBundles();
		StringBuffer buffer = new StringBuffer();
		for (TargetBundle bundle : bundles) {
			String id = bundle.getBundleInfo().getSymbolicName();

			if (workspacePlugins.containsKey(id)) {
				if ("org.eclipse.osgi".equals(id)) {
					osgiTargetBundle = bundle;
				}
				// already selected, continue
				continue;
			}
			if (!"org.eclipse.osgi".equals(id)) {
				if (buffer.length() > 0) {
					buffer.append(",");
				}
				buffer.append(bundle.getBundleInfo().getLocation());
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
				osgiTargetBundle = bundle;
			}
		}
		return buffer.toString();
	}

	/**
	 * @param workspacePlugins
	 * @param eclipseLocation
	 * @return
	 */
	private String computeTargetBundles_OLD(Map<?, ?> workspacePlugins, String eclipseLocation) {
		URL[] pluginPaths = PluginPathFinder.getPluginPaths(eclipseLocation, false);
		PDEState pdeState = new PDEState(pluginPaths, true, true, new NullProgressMonitor());
		IPluginModelBase[] models = pdeState.getTargetModels();
		return getTargetBundles(workspacePlugins, models);
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
					osgiFrameworkModel = model;
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
				osgiFrameworkModel = model;
			}
		}
		return buffer.toString();
	}

	/**
	 * @param configuration
	 * @throws IOException
	 */
	private void createPDELaunchIni(DominoOSGIConfig osgiConfig, ILaunchConfiguration configuration) throws IOException {
		File pdeLaunchIniFile = getPDELaunchIni(osgiConfig);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pdeLaunchIniFile);
			Properties props = new Properties();
			props.setProperty("configuration", configuration.getName());
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
	private File getPDELaunchIni(DominoOSGIConfig osgiConfig) throws IOException {
		File notesDataDir = new File(osgiConfig.getDominoDataPath());
		if (!notesDataDir.exists()) {
			throw new IOException(MessageFormat.format("Data directory does not exist {0}",
					osgiConfig.getDominoDataPath()));
		}
		final File dominoWorkspaceDir = new File(notesDataDir, getWorkspaceRelativePath());
		if (!dominoWorkspaceDir.exists() || !dominoWorkspaceDir.isDirectory()) {
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					boolean bCreate = MessageDialog.openQuestion(
							shell,
							"Question",
							MessageFormat
									.format("The directory \"{0}\" does not exist.\nThis may be because the OSGi configuration has never been run previously.\nWould you like to create it?",
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

	/**
	 * @return
	 */
	protected abstract String getWorkspaceRelativePath();

	/**
	 * @return
	 */
	protected abstract String getOSGIDirectoryName();

	/**
	 * @return
	 */
	public abstract String[] getProfiles();

	/**
	 * @param selectedProfile
	 */
	public abstract void setProfile(String selectedProfile);

	public abstract String getName();

	protected abstract String getSystemFragmentFileName();
}
