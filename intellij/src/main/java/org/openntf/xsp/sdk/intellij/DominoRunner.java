package org.openntf.xsp.sdk.intellij;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.openntf.xsp.sdk.commons.osgi.LaunchUtil;
import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.commons.utils.CommonUtils;
import org.openntf.xsp.sdk.intellij.org.openntf.xsp.sdk.intellij.platform.IdeaDominoHttpPlatform;
import org.openntf.xsp.sdk.intellij.ui.LaunchDialog;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkRunner;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DominoRunner extends AbstractFrameworkRunner {
    private IdeaDominoHttpPlatform ndPlatform;

    @Override
    public JavaParameters createJavaParameters(@NotNull OsgiRunConfiguration osgiRunConfiguration, @NotNull List<SelectedBundle> list) throws ExecutionException {
        ndPlatform = new IdeaDominoHttpPlatform(osgiRunConfiguration.getAdditionalProperties());

        showDialog(osgiRunConfiguration);

        try {
            applyLaunchConfig(osgiRunConfiguration, list);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return createNoopJavaParameters(osgiRunConfiguration);
    }

    @Override
    protected void setupParameters(@NotNull JavaParameters javaParameters) {
        // NOP
    }

    @Override
    public void dispose() {
        // NOP
    }

    // *******************************************************************************
    // * Process
    // *******************************************************************************

    private void showDialog(@NotNull OsgiRunConfiguration osgiRunConfiguration) {
        LaunchDialog dialog = new LaunchDialog(osgiRunConfiguration);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void applyLaunchConfig(@NotNull OsgiRunConfiguration osgiRunConfiguration, @NotNull List<SelectedBundle> list) throws IOException {
        List<String> bundles = ContainerUtil.newArrayList();
        for(SelectedBundle bundle : list) {
            String product = bundle.getBundlePath();
            if (product != null) {
                boolean isFragment = CachingBundleInfoProvider.isFragmentBundle(product);
                String bundleUrl = this.toFileUri(product);
                if (!isFragment) {
                    int startLevel = this.getBundleStartLevel(bundle);
                    bundleUrl = bundleUrl + "@" + startLevel;
                    if (bundle.isStartAfterInstallation()) {
                        bundleUrl = bundleUrl + ":start";
                    }
                }

                bundles.add(bundleUrl);
            }
        }

        // Update the config.ini with any environment variables set by the
        // configuration
        updateConfigIni(osgiRunConfiguration);

        // Create the pde.launch.ini
        Path dataDir = Paths.get(DominoRunProperties.getDataDir(osgiRunConfiguration.getAdditionalProperties()));
        Path configIni = dataDir.resolve("domino").resolve("workspace").resolve("pde.launch.ini");
        System.out.println("Going to write config with configDir=" + getConfigDir(osgiRunConfiguration) + ", ini=" + configIni + ", name=" + osgiRunConfiguration.getName());
//        String filePath = LaunchUtil.createPDELaunchIni(configIni.toFile(), getConfigDir(osgiRunConfiguration), osgiRunConfiguration.getName());
    }

    public static String getConfigDir(@NotNull OsgiRunConfiguration osgiRunConfiguration) {
        String configDir = osgiRunConfiguration.getWorkingDir();
        String localDir = DominoRunProperties.getSharedDir(osgiRunConfiguration.getAdditionalProperties());
        String remoteDir = DominoRunProperties.getMappedRemotePath(osgiRunConfiguration.getAdditionalProperties());
        String result = LaunchUtil.toJunctionPath(configDir, new IdeaDominoHttpPlatform(osgiRunConfiguration.getAdditionalProperties()), localDir, remoteDir);

        if(CommonUtils.isEmpty(result)) {
            throw new RuntimeException("Unable to convert the configuration directory into a local representation (" + configDir + ")");
        }

        return result;
    }

    // *******************************************************************************
    // * Utility methods
    // *******************************************************************************

    /**
     * Creates a no-op JavaParameters, intended to successfully end execution quickly.
     */
    private JavaParameters createNoopJavaParameters(@NotNull OsgiRunConfiguration osgiRunConfiguration) throws CantRunException {
        JavaParameters javaParameters = new JavaParameters();
        String jreHome = osgiRunConfiguration.isUseAlternativeJre() ? osgiRunConfiguration.getAlternativeJrePath() : null;
        JavaParametersUtil.configureProject(osgiRunConfiguration.getProject(), javaParameters, JavaParameters.JDK_ONLY, jreHome);

        // Exit early, since we don't actually want to run anything
        javaParameters.setMainClass("Foo");
        javaParameters.getVMParametersList().add("-version");

        return javaParameters;
    }

    // Similar to AbstractDominoLaunchConfiguration
    private void updateConfigIni(@NotNull OsgiRunConfiguration configuration) throws IOException {
        Path configIni = Paths.get(configuration.getWorkingDir(), "config.ini");

        // Load the properties from config.ini
        Properties props = new Properties();
        if(Files.isReadable(configIni) && Files.isRegularFile(configIni)) {
            try (InputStream fis = Files.newInputStream(configIni)) {
                props.load(fis);
            }
        }

        String localDir = DominoRunProperties.getSharedDir(configuration.getAdditionalProperties());
        String remoteDir = DominoRunProperties.getMappedRemotePath(configuration.getAdditionalProperties());

        String osgiBundles = configuration.getBundlesToDeploy().stream()
                .map(SelectedBundle::getBundlePath)
                .map(path -> "reference:file:" + path)
                .collect(Collectors.joining(","));
        System.out.println("Got osgiBundles " + osgiBundles);
        Collection<String> osgiBundleList = LaunchUtil.populateBundleList(osgiBundles, ndPlatform, localDir, remoteDir, null);

        osgiBundleList.addAll(computeOsgiBundles(ndPlatform, ndPlatform.getRemoteRcpTargetFolder()));
        osgiBundleList.addAll(computeOsgiBundles(ndPlatform, ndPlatform.getRemoteRcpSharedFolder()));

        String wsPluginPath = ndPlatform.getRemoteWorkspaceFolder() + "/applications/eclipse";
        osgiBundleList.addAll(computeOsgiBundles(ndPlatform, wsPluginPath));

        Collection<String> linkedRepos = LaunchUtil.findLinkedRepos(new File(ndPlatform.getRemoteRcpTargetFolder() + "/links"));

        for(String linkedRepo: linkedRepos) {
            String remoteLinkPath = LaunchUtil.toRemotePath(linkedRepo, ndPlatform);

            if(CommonUtils.isEmpty(remoteLinkPath)) {
                String message = MessageFormat.format(
                        "Your platform points in \"{0}\" via link file. We don't know how to see this directory. These bundles will be ignored.\n\n" +
                                "Please make sure all necessary link files point into a directory under your install or data folder.", linkedRepo);

                System.out.println(message);
            } else {
                osgiBundleList.addAll(computeOsgiBundles(ndPlatform, remoteLinkPath));
            }
        }

        String systemFragmentJar = ndPlatform.getLocalWorkspaceFolder() +
                "/.config/domino/eclipse/plugins/" + ndPlatform.getSystemFragmentFileName();
        osgiBundleList.add("reference:file:"+LaunchUtil.fixPathSeparators(systemFragmentJar));

        StringBuilder bundles = new StringBuilder();
        for(String osgiBundle: osgiBundleList) {
            if(bundles.length()>0) {
                bundles.append(",");
            }
            bundles.append(osgiBundle);
        }

        props.setProperty("osgi.bundles", bundles.toString());
        props.setProperty("osgi.install.area", "file:" + LaunchUtil.fixPathSeparators(ndPlatform.getLocalRcpTargetFolder()));
        System.out.println("Created props " + props);

        // Save the configuration
        try(OutputStream fos = Files.newOutputStream(configIni)) {
            props.store(fos, "Created by OpenNTF XPages SDK");
        }
    }

    private Collection<String> computeOsgiBundles(INotesDominoPlatform ndPlatform, String remotePath) {
        // TODO figure out
        return Collections.emptyList();
    }
}
