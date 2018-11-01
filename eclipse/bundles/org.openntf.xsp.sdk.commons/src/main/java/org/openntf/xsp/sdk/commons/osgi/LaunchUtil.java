package org.openntf.xsp.sdk.commons.osgi;

import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.commons.utils.CommonUtils;
import org.openntf.xsp.sdk.commons.utils.StringUtil;

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
}
