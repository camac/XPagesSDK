package com.ibm.domino.osgi.debug.launch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.launching.IPDELauncherConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.exceptions.AbortException;
import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.preferences.XspPreferences;
import org.openntf.xsp.sdk.utils.CommonUtils;
import org.openntf.xsp.sdk.utils.StringUtil;

public class LaunchUtils {

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
		File pdeLaunchIniFile = getPDELaunchIni(dominoLaunch);
		FileOutputStream fos = null;
		try {
			String configDir = getConfigDir(dominoLaunch, configuration);

			fos = new FileOutputStream(pdeLaunchIniFile);
			
			Properties props = new Properties();
			props.setProperty("configuration", dominoLaunch.getName());
			props.setProperty("osgi.configuration.area", configDir);
			props.store(fos, "Generated by OpenNTF XPages SDK");
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
		
		return pdeLaunchIniFile.getAbsolutePath();
	}

	public static String getConfigDir(AbstractDominoLaunchConfiguration dominoLaunch, ILaunchConfiguration configuration) {
		String configDir = fixPathSeparators(dominoLaunch.getConfigDir(configuration).getAbsolutePath()); 
		
		String result = toJunctionPath(configDir, dominoLaunch.getNotesDominoPlatform());
		
		if(CommonUtils.isEmpty(result)) {
			throw new RuntimeException("Unable to convert the configuration directory into a local representation (" + configDir + ")");
		}
		
		return result;
	}

	public static Collection<String> populateBundleList(String osgiBundles, INotesDominoPlatform ndPlatform) {
		Set<String> bundles = new LinkedHashSet<>();
		
		for(String osgiBundle: osgiBundles.split(",")) {
			String localPath = toJunctionPath(osgiBundle.substring("reference:file:".length()), ndPlatform);
			
			if(CommonUtils.isEmpty(localPath)) {
				String message = MessageFormat.format("Unable to convert the bundle \"{0}\" to a local representation. Check your settings.", osgiBundle);
				logger.log(new Status(Status.WARNING, Activator.PLUGIN_ID, message));
			} else {
				bundles.add("reference:file:"+localPath);
			}
		}
		
		return bundles;
	}

	/*
	 * Read all of the .link files - this will add the Upgrade Pack
	 * plugins to the config
	 */
	public static Collection<String> findLinkedRepos(File linksDir) {
		Collection<String> linkedRepos = new LinkedHashSet<>();
	
		if (linksDir.exists() && linksDir.isDirectory()) {
			File[] links = linksDir.listFiles();
			
			if (links != null) {
				for (File link : links) {
					FileReader reader = null;
	
					try {
						reader = new FileReader(link);
						Properties linkProps = new Properties();
						linkProps.load(reader);
						String linkPath = StringUtil.prunePath(linkProps.getProperty("path"));
						
						if(!StringUtil.isEmpty(linkPath)) {
							linkedRepos.add(linkPath + "/eclipse");
						}
					} catch (Exception e) {
						e.printStackTrace(System.err);
					} finally {
						try {
							if(null!=reader) {
								reader.close();
							}
						} catch (IOException e) {}
					}
				}
			}
		}
	
		return linkedRepos;
	}

	public static String fixPathSeparators(String path) {
		if(CommonUtils.isEmpty(path)) {
			return "";
		}
		
		return path.replace('\\', '/');
	}

	public static String toLocalPath(String remotePath, INotesDominoPlatform ndPlatform) {
		// We trust Eclipse classes on that incoming url should be well formed in terms of path separators.
		if(ndPlatform.isLocal()) {
			// no change needed.
			return remotePath;
		}
		
		String localInstall = fixPathSeparators(ndPlatform.getLocalInstallFolder());
		String localData = fixPathSeparators(ndPlatform.getLocalDataFolder());
		
		// Is it under install directory?
		if(CommonUtils.startsWithIgnoreCase(remotePath, ndPlatform.getRemoteInstallFolder())) {
			return localInstall + remotePath.substring(ndPlatform.getRemoteInstallFolder().length());
		}
		
		// Is it under data directory?
		if(CommonUtils.startsWithIgnoreCase(remotePath, ndPlatform.getRemoteDataFolder())) {
			return localData + remotePath.substring(ndPlatform.getRemoteDataFolder().length());
		}
		
		// If not, we can't support any conversion
		return null;
	}
	
	public static String toRemotePath(String localPath, INotesDominoPlatform ndPlatform) {
		String result = fixPathSeparators(localPath);
		
		if (ndPlatform.isLocal()) {
			// no change needed
			return result;
		}
		
		String localInstall = fixPathSeparators(ndPlatform.getLocalInstallFolder());
		String localData = fixPathSeparators(ndPlatform.getLocalDataFolder());
		
		// Is it under install directory?
		if(CommonUtils.startsWithIgnoreCase(result, localInstall)) {
			return ndPlatform.getRemoteInstallFolder() + localPath.substring(localInstall.length());
		}
		
		// Is it under data directory?
		if(CommonUtils.startsWithIgnoreCase(result, localData)) {
			return ndPlatform.getRemoteDataFolder() + localPath.substring(localData.length());
		}
		
		// If not, we can't support any conversion
		return null;
	}
	
	public static String toJunctionPath(String remotePath, INotesDominoPlatform ndPlatform) {
		// We trust Eclipse classes on that incoming url should be well formed in terms of path separators.
		if(ndPlatform.isLocal()) {
			// no change needed.
			return remotePath;
		}
		
		/** 
		 * The naming might be confusing here. Here is the explanation:
		 * 
		 * The local junction is local to the Eclipse machine. For example, it's your home folder (/Users/HomerSimpson)
		 * The remote junction is how the remote machine access it. For example, suppose you have mapped your home folder as (Z:\) in the Domino machine.
		 * 
		 * The magic will work only if your project files and Eclipse files are UNDER your junction point.
		 * 	
		 */
		
		String localJunction = XspPreferences.getPreferenceString(XspPreferences.LOCAL_JUNCTION);
		String remoteJunction = fixPathSeparators(XspPreferences.getPreferenceString(XspPreferences.REMOTE_JUNCTION));
		
		// Is it under the Junction path?
		if(CommonUtils.startsWithIgnoreCase(remotePath, localJunction)) {
			// Fix: We need to cut off from the start.
			int cutOffPoint = (localJunction.length()>1) ? localJunction.length() : 0;
			
			return StringUtil.prunePath(remoteJunction) + remotePath.substring(cutOffPoint);
		}
		
		// If not, we can't support any conversion
		return null;
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

}
