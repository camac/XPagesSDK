package org.openntf.xsp.sdk.commons.osgi;

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
}
