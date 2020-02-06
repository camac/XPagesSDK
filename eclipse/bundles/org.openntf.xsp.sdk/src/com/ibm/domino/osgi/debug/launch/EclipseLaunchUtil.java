package com.ibm.domino.osgi.debug.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.commons.osgi.LaunchUtil;
import org.openntf.xsp.sdk.exceptions.AbortException;
import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.preferences.XspPreferences;
import org.openntf.xsp.sdk.commons.utils.CommonUtils;

public class EclipseLaunchUtil {

	private static final ILog logger = Activator.getDefault().getLog();

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static boolean isTargetPlatformPluginsEnabled(ILaunchConfiguration configuration) throws CoreException {
		String selectedTargetBundles = configuration.getAttribute(IPDELauncherConstants.TARGET_BUNDLES, (String) null);
		return selectedTargetBundles != null && selectedTargetBundles.length() > 0;
	}


	private static File getPDELaunchIni(AbstractDominoLaunchConfiguration dominoLaunch) throws IOException {
		String selectedProfile = dominoLaunch.getSelectedProfile();
		String workspacePath = dominoLaunch.getNotesDominoPlatform().getRemoteWorkspaceFolder(selectedProfile);
		
		final File dominoWorkspaceDir = new File(workspacePath);
		if (!dominoWorkspaceDir.exists() || !dominoWorkspaceDir.isDirectory()) {
			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				boolean bCreate = MessageDialog.openQuestion(shell, "Question",
						MessageFormat.format(
								"The directory \"{0}\" does not exist.\nThis may be because the OSGi configuration has never been run previously.\nWould you like to create it?",
								dominoWorkspaceDir.getAbsolutePath()));
				if (bCreate) {
					dominoWorkspaceDir.mkdirs();
				}
			});
		}

		if (!dominoWorkspaceDir.exists() || !dominoWorkspaceDir.isDirectory()) {
			throw new AbortException();
		}
		return new File(dominoWorkspaceDir, "pde.launch.ini");
	}

	public static String createPDELaunchIni(AbstractDominoLaunchConfiguration dominoLaunch, ILaunchConfiguration configuration) throws IOException {
		return LaunchUtil.createPDELaunchIni(getPDELaunchIni(dominoLaunch), getConfigDir(dominoLaunch, configuration), dominoLaunch.getName());
	}

	public static String getConfigDir(AbstractDominoLaunchConfiguration dominoLaunch, ILaunchConfiguration configuration) {
		String configDir = LaunchUtil.fixPathSeparators(dominoLaunch.getConfigDir(configuration).getAbsolutePath());
		
		String result = LaunchUtil.toJunctionPath(configDir, dominoLaunch.getNotesDominoPlatform(), XspPreferences.getPreferenceString(XspPreferences.LOCAL_JUNCTION), XspPreferences.getPreferenceString(XspPreferences.REMOTE_JUNCTION));
		
		if(CommonUtils.isEmpty(result)) {
			throw new RuntimeException("Unable to convert the configuration directory into a local representation (" + configDir + ")");
		}
		
		return result;
	}

	public static Collection<String> populateBundleList(String osgiBundles, INotesDominoPlatform ndPlatform) {
		String localJunction = XspPreferences.getPreferenceString(XspPreferences.LOCAL_JUNCTION);
		String remoteJunction = XspPreferences.getPreferenceString(XspPreferences.REMOTE_JUNCTION);
		return LaunchUtil.populateBundleList(osgiBundles, ndPlatform, localJunction, remoteJunction, m -> logger.log(new Status(Status.WARNING, Activator.PLUGIN_ID, m)));
	}

	public static void displayMessage(final boolean isError, final String title, String message, Object... args) {
		final String dialogMessage = MessageFormat.format(message, args);

		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			if(isError) {
				MessageDialog.openError(shell, title, dialogMessage);
			} else {
				MessageDialog.openInformation(shell, title, dialogMessage);
			}
		});

	}

	// Returns contents of resource file as a String
	public static String readResource(String resourcePath) {

		StringBuilder result = new StringBuilder();

		try {

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

	public static void removeDuplicatePlugins(ILaunchConfigurationWorkingCopy configuration) {

		try {

			String test = configuration.getAttribute("selected_target_plugins", "none");

			if (CommonUtils.equalsIgnoreCase(test, "none")) {
				return;
			}

			String[] plugins = test.split(",");

			String lastPlugin = "";
			String lastPluginId = "";

			List<String> removeme = new ArrayList<String>();
			List<String> keepme = new ArrayList<String>();

			for (String plugin : plugins) {

				String pluginId = plugin.split("\\*")[0];

				if (lastPluginId.equals(pluginId)) {
					removeme.add(lastPlugin);
				} else {
					keepme.add(lastPlugin);
				}

				lastPlugin = plugin;
				lastPluginId = pluginId;
			}

			keepme.add(lastPlugin);

			StringBuffer result = new StringBuffer();
			for (Iterator<String> iterator = keepme.iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();
				if (result.length() > 0) {
					result.append(',');
				}
				result.append(name);
			}
			if (result.length() > 0) {
				configuration.setAttribute("selected_target_plugins", result.toString());
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

}
