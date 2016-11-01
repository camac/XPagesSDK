package org.openntf.xsp.sdk.preferences;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.openntf.xsp.sdk.Activator;
import org.openntf.xsp.sdk.exceptions.XPagesSDKException;
import org.openntf.xsp.sdk.preferences.XPagesSDKPreferences.Target;

@Deprecated
public class OldPreferences {

	public final static String RCP_BASE = "RCP_BASE";
	public final static String RCP_TARGET = "RCP_TARGET";
	public final static String RCP_DATA = "RCP_DATA";
	public final static String DOMRCP_BASE = "DOMRCP_BASE";
	public final static String DOMRCP_TARGET = "DOMRCP_TARGET";
	public final static String DOMSHARED_TARGET = "DOMSHARED_TARGET";
	public final static String DOMRCP_DATA = "DOMRCP_DATA";
	public final static String DOTSRCP_BASE = "DOTSRCP_BASE";
	public final static String DOTSRCP_TARGET = "DOTSRCP_TARGET";
	public final static String DOTSSHARED_TARGET = "DOTSSHARED_TARGET";
	public final static String DOTSRCP_DATA = "DOTSRCP_DATA";
	public final static String RCP_TARGET_FOLDER = "/framework/rcp/eclipse";
	public final static String RCP_PLUGIN_FOLDER = "/framework/rcp/eclipse/plugins";
	public final static String RCP_DATA_FOLDER = "/workspace";
	public final static String RCP_BASE_FOLDER_PREFIX = "com.ibm.rcp.base_";
	public final static String DOMRCP_TARGET_FOLDER = "/osgi/rcp/eclipse";
	public final static String DOMRCP_PLUGIN_FOLDER = "/osgi/rcp/eclipse/plugins";
	public final static String DOMSHARED_TARGET_FOLDER = "/osgi/shared/eclipse";
	public final static String DOMRCP_DATA_FOLDER = "/domino/workspace";
	public final static String DOMRCP_BASE_FOLDER_PREFIX = "com.ibm.rcp.base_";
	public final static String DOTSRCP_TARGET_FOLDER = "/osgi-dots/rcp/eclipse";
	public final static String DOTSRCP_PLUGIN_FOLDER = "/osgi-dots/rcp/eclipse/plugins";
	public final static String DOTSSHARED_TARGET_FOLDER = "/osgi-dots/shared/eclipse";
	public final static String DOTSRCP_DATA_FOLDER = "/domino/workspace-dots";

	public static String resolveConstant(String var) {
//		if ("notes_install".equals(var))
//			return NOTES_INSTALL;
//		if ("notes_data".equals(var))
//			return NOTES_DATA;
//		if ("rcp_base".equals(var))
//			return RCP_BASE;
//		if ("rcp_target".equals(var))
//			return RCP_TARGET;
//		if ("rcp_data".equals(var))
//			return RCP_DATA;
//		if ("domino_install".equals(var))
//			return DOMINO_INSTALL;
//		if ("domino_data".equals(var))
//			return DOMINO_DATA;
//		if ("domino_rcp_base".equals(var))
//			return DOMRCP_BASE;
//		if ("domino_rcp_target".equals(var))
//			return DOMRCP_TARGET;
//		if ("domino_rcp_data".equals(var))
//			return DOMRCP_DATA;
//		if ("domino_shared_target".equals(var))
//			return DOMSHARED_TARGET;
//		if ("remote_domino".equals(var))
//			return REMOTE_DOMINO;
//		if ("REMOTE_CONFIG_AREA".equals(var))
//			return REMOTE_CONFIG_AREA;
//		
//		if ("dots_data".equals(var))
//			return DOMINO_DATA;
//		if ("dots_rcp_base".equals(var))
//			return DOTSRCP_BASE;
//		if ("dots_rcp_target".equals(var))
//			return DOTSRCP_TARGET;
//		if ("dots_rcp_data".equals(var))
//			return DOTSRCP_DATA;
//		if ("dots_shared_target".equals(var))
//			return DOTSSHARED_TARGET;
	
		return null;
	}

	private static IPreferenceStore getStore() {
		IPreferenceStore result = Activator.getDefault().getPreferenceStore();
		return result;
	}

	public static void updateDefaults(IPreferenceStore store) {
		// No defaults.
	}

	public static String getNotesInstall() {
//		return getStore().getString(NOTES_INSTALL);
		return null;
	}

	public static String getNotesData() {
//		return getStore().getString(NOTES_DATA);
		return null;
	}

	public static String getRcpBase() throws Exception {
		String rcpBaseFolder = findRcpBaseFolder(Target.NOTES);
		if (rcpBaseFolder == null) {
			IStatus status = new Status(Status.INFO, Activator.PLUGIN_ID, "Unable to find rcpBaseFolder!");
			Activator.getDefault().getLog().log(status);
			throw new XPagesSDKException("Unable to find rcpBaseFolder!");
		}
		return rcpBaseFolder.replace('\\', '/');
	
	}

	public static String getDominoInstall() {
//		return getStore().getString(DOMINO_INSTALL);
		return null;
	}

	public static String getDominoData() {
//		return getStore().getString(DOMINO_DATA);
		return null;
	}

	public static String getDomRcpBase() throws Exception {
		return findRcpBaseFolder(Target.DOMINO);
	}

	public static String getRcpTarget() {
		return (getNotesInstall() + RCP_TARGET_FOLDER).replace('\\', '/');
	}

	public static String getRcpData() {
		return (getNotesData() + RCP_DATA_FOLDER).replace('\\', '/');
	}

	public static String getJvmPath() {
		return getNotesInstall() + "/jvm";
	}

	public static String getDomRcpTarget() {
		return getDominoInstall() + DOMRCP_TARGET_FOLDER;
	}

	public static String getDomSharedTarget() {
		return getDominoInstall() + DOMSHARED_TARGET_FOLDER;
	}

	public static String getDomRcpData() {
		return getDominoData() + DOMRCP_DATA_FOLDER;
	}

	public static String getDotsRcpTarget() {
		return getDominoInstall() + DOTSRCP_TARGET_FOLDER;
	}

	public static String getDotsSharedTarget() {
		return getDominoInstall() + DOTSSHARED_TARGET_FOLDER;
	}

	public static String getDotsRcpData() {
		return getDominoData() + DOTSRCP_DATA_FOLDER;
	}

	public static String getDomJvmPath() {
		return getDominoInstall() + "/jvm";
	}

	public static String getJvmPath(String basePath) {
		return basePath + "/jvm";
	}

	static String findRcpBaseFolder(final XPagesSDKPreferences.Target target) throws Exception {
		IPath basePath;
		if (target == XPagesSDKPreferences.Target.DOMINO || target == XPagesSDKPreferences.Target.DOTS) {
			basePath = new Path(getDominoInstall());
		} else {
			basePath = new Path(getNotesInstall());
		}
		File install = new File(basePath.toOSString());
	
		if (install.isDirectory()) {
			IPath rcpPluginPath = null;
			if (target == XPagesSDKPreferences.Target.DOMINO) {
				rcpPluginPath = new Path(DOMRCP_PLUGIN_FOLDER);
			} else if (target == XPagesSDKPreferences.Target.DOTS) {
				rcpPluginPath = new Path(DOTSRCP_PLUGIN_FOLDER);
			} else {
				rcpPluginPath = new Path(RCP_PLUGIN_FOLDER);
			}
			IPath rcpPath = basePath.append(rcpPluginPath);
			File rcp = new File(rcpPath.toOSString());
			File[] baseFolders = rcp.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					String prefix = "";
					if (target == XPagesSDKPreferences.Target.DOMINO) {
						prefix = DOMRCP_BASE_FOLDER_PREFIX;
					} else if (target == XPagesSDKPreferences.Target.DOTS) {
						prefix = DOMRCP_BASE_FOLDER_PREFIX;
					} else {
						prefix = RCP_BASE_FOLDER_PREFIX;
					}
					return name.startsWith(prefix);
				}
			});
	
			if (baseFolders.length >= 1) {
				return baseFolders[0].getAbsolutePath();
			} else {
				IStatus status = new Status(Status.WARNING, Activator.PLUGIN_ID, "Unable to find base folder " + RCP_BASE_FOLDER_PREFIX);
				Activator.getDefault().getLog().log(status);
				throw new XPagesSDKException("Unable to find base folder " + RCP_BASE_FOLDER_PREFIX);
			}
		} else {
			IStatus status = new Status(Status.INFO, Activator.PLUGIN_ID, "Notes container for location " + basePath
					+ " is NOT a folder from root ");
			Activator.getDefault().getLog().log(status);
			throw new XPagesSDKException("Notes container for location " + basePath + " is NOT a folder from root ");
		}
	}

	public static String getDotsRcpBase() throws Exception {
		return findRcpBaseFolder(XPagesSDKPreferences.Target.DOTS);
	}

}
