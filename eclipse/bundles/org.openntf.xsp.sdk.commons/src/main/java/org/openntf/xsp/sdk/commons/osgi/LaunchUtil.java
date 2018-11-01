package org.openntf.xsp.sdk.commons.osgi;

import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.commons.utils.CommonUtils;
import org.openntf.xsp.sdk.commons.utils.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

public enum LaunchUtil {
    ;

    public static String getBundleSuffix(String id) {
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

    public static String toJunctionPath(String remotePath, INotesDominoPlatform ndPlatform, String localJunction, String remoteJunctionParam) {
        // We trust Eclipse classes on that incoming url should be well formed in terms of path separators.
        if(ndPlatform.isLocal()) {
            // no change needed.
            return remotePath;
        }

        /*
         * The naming might be confusing here. Here is the explanation:
         *
         * The local junction is local to the developer's machine. For example, it's your home folder (/Users/HomerSimpson)
         * The remote junction is how the remote machine access it. For example, suppose you have mapped your home folder as (Z:\) in the Domino machine.
         *
         * The magic will work only if your project files and workspace are UNDER your junction point.
         *
         */

        String remoteJunction = fixPathSeparators(remoteJunctionParam);

        // Is it under the Junction path?
        if(CommonUtils.startsWithIgnoreCase(remotePath, localJunction)) {
            // Fix: We need to cut off from the start.
            int cutOffPoint = (localJunction.length()>1) ? localJunction.length() : 0;

            return StringUtil.prunePath(remoteJunction) + remotePath.substring(cutOffPoint);
        }

        // If not, we can't support any conversion
        return null;
    }

    public static String fixPathSeparators(String path) {
        if(CommonUtils.isEmpty(path)) {
            return "";
        }

        return path.replace('\\', '/');
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

    public static String createPDELaunchIni(File pdeLaunchIniFile, String configDir, String configurationName) throws IOException {
       try(FileOutputStream fos = new FileOutputStream(pdeLaunchIniFile)) {
            Properties props = new Properties();
            props.setProperty("configuration", configurationName);
            props.setProperty("osgi.configuration.area", configDir);
            props.store(fos, "Generated by OpenNTF XPages SDK");
        }

        return pdeLaunchIniFile.getAbsolutePath();
    }

    public static Collection<String> populateBundleList(String osgiBundles, INotesDominoPlatform ndPlatform, String localJunction, String remoteJunction, Consumer<String> logConsumer) {
        Set<String> bundles = new LinkedHashSet<>();

        for(String osgiBundle: osgiBundles.split(",")) {
            String localPath = LaunchUtil.toJunctionPath(osgiBundle.substring("reference:file:".length()), ndPlatform, localJunction, remoteJunction);

            if(CommonUtils.isEmpty(localPath)) {
                String message = MessageFormat.format("Unable to convert the bundle \"{0}\" to a local representation. Check your settings.", osgiBundle);
                logConsumer.accept(message);
            } else {
                bundles.add("reference:file:"+localPath);
            }
        }

        return bundles;
    }
}
