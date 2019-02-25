package org.openntf.xsp.sdk.commons.platform;

/**
 * Specialized variant of {@link INotesDominoPlatform} for use with the Domino HTTP server runtime.
 */
public interface IDominoHttpPlatform extends INotesDominoPlatform {
    @Override
    default public String getName() {
        return "Domino HTTP Platform";
    }

    @Override
    default public String getRemoteWorkspaceFolder() {
        return getRemoteWorkspaceFolder(null);
    }

    @Override
    default public String getLocalRcpTargetFolder() {
        return getLocalInstallFolder() + "/osgi/rcp/eclipse";
    }

    @Override
    default public String getRemoteRcpTargetFolder() {
        return getRemoteInstallFolder() + "/osgi/rcp/eclipse";
    }

    @Override
    default public String getLocalRcpSharedFolder() {
        return getLocalInstallFolder() + "/osgi/shared/eclipse";
    }

    @Override
    default public String getRemoteRcpSharedFolder() {
        return getRemoteInstallFolder() + "/osgi/shared/eclipse";
    }

    @Override
    default public String getLocalWorkspaceFolder() {
        return getLocalWorkspaceFolder(null);
    }

    @Override
    default public String getLocalWorkspaceFolder(String profileName) {
        // Ignore profile name!
        return getLocalDataFolder() + "/domino/workspace";
    }

    @Override
    default public String getRemoteWorkspaceFolder(String profileName) {
        // Ignore profile name!
        return getRemoteDataFolder() + "/domino/workspace";
    }

    @Override
    default public String getSystemFragmentFileName() {
        return "com.ibm.domino.osgi.sharedlib_1.0.0.jar";
    }
}
