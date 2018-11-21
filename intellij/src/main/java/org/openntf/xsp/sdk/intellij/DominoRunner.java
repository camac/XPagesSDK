package org.openntf.xsp.sdk.intellij;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.vfs.VirtualFile;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class DominoRunner extends AbstractFrameworkRunner {
    private IdeaDominoHttpPlatform ndPlatform;

    @Override
    public JavaParameters createJavaParameters(@NotNull OsgiRunConfiguration osgiRunConfiguration, @NotNull List<SelectedBundle> list) throws ExecutionException {
        ndPlatform = new IdeaDominoHttpPlatform(osgiRunConfiguration.getAdditionalProperties());

        showDialog(osgiRunConfiguration, () -> {
            try {
                applyLaunchConfig(osgiRunConfiguration, list);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });

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

    private void showDialog(@NotNull OsgiRunConfiguration osgiRunConfiguration, Runnable callback) {
        LaunchDialog dialog = new LaunchDialog(osgiRunConfiguration, callback);
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

        // Create dev.properties in the local work directory
        createDevProperties(osgiRunConfiguration);

        // Create local working dir links folder
        createLocalLinksFiles(osgiRunConfiguration);

        createSharedLibJar(osgiRunConfiguration);

        // Create the pde.launch.ini
        Path dataDir = Paths.get(DominoRunProperties.getDataDir(osgiRunConfiguration.getAdditionalProperties()));
        Path configIni = dataDir.resolve("domino").resolve("workspace").resolve("pde.launch.ini");
        System.out.println("Going to write config with configDir=" + getConfigDir(osgiRunConfiguration) + ", ini=" + configIni + ", name=" + osgiRunConfiguration.getName());
        try {
            String filePath = LaunchUtil.createPDELaunchIni(configIni.toFile(), getConfigDir(osgiRunConfiguration), osgiRunConfiguration.getName());

            Messages.showInfoMessage("Success", MessageFormat.format("Successfully updated \"{0}\".\nTo run normally, please delete this file.", filePath));
        } catch(Throwable t) {
            Messages.showErrorDialog(osgiRunConfiguration.getProject(),"Error Writing pde.launch.ini", t.getMessage());
        }
    }

    private static String getConfigDir(@NotNull OsgiRunConfiguration osgiRunConfiguration) {
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
    // * File creation
    // *******************************************************************************

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

        final String localSharedDir = DominoRunProperties.getSharedDir(configuration.getAdditionalProperties());
        final String remoteMappedPath = DominoRunProperties.getMappedRemotePath(configuration.getAdditionalProperties());

        // TODO: these selected bundles point to the .jar files in target, but should point to the projects
        Collection<String> osgiBundleList = Arrays.stream(configuration.getModules())
                .map(this::toLocalPath)
                .map(path -> toReferenceUrl(path, localSharedDir, remoteMappedPath))
                .collect(Collectors.toCollection(ArrayList::new));

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

        // Do we need to also create this file? I don't see where it's created in the Eclipse version, but
        //   it could be coming from some internal Eclipse code
        String systemFragmentJar = ndPlatform.getLocalWorkspaceFolder() +
                "/.config/domino/eclipse/plugins/" + ndPlatform.getSystemFragmentFileName();
        osgiBundleList.add("reference:file:"+LaunchUtil.fixPathSeparators(systemFragmentJar));
        
        String bundles = String.join(",", osgiBundleList);

        props.setProperty("osgi.bundles", bundles);
        props.setProperty("osgi.install.area", "file:" + LaunchUtil.fixPathSeparators(ndPlatform.getLocalRcpTargetFolder()));
        props.setProperty("osgi.bundles.defaultStartLevel", String.valueOf(configuration.getDefaultStartLevel()));
        props.setProperty("osgi.configuration.cascaded", "false");

        // Figure out osgi.framework, which in AbstractDominoLaunchConfiguration is found by getting the bundle named org.eclipse.osgi when scanning bundles
        String osgiFrameworkPath = osgiBundleList.stream()
                .filter(path -> path.contains("/org.eclipse.osgi_"))
                .findFirst()
                .get() // We want to throw an exception if it's not found
                .substring("reference:".length());
        props.setProperty("osgi.framework", osgiFrameworkPath);

        // Save the configuration
        Files.createDirectories(configIni.getParent());
        try(OutputStream fos = Files.newOutputStream(configIni, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            props.store(fos, "Created by OpenNTF XPages SDK");
        }
    }

    private void createDevProperties(@NotNull OsgiRunConfiguration configuration) throws IOException {
        Properties devProperties = new Properties();
        devProperties.setProperty("@ignoredot@", "true");

        Arrays.stream(configuration.getModules()).forEach(module -> {
            Path modulePath = Paths.get(ModuleUtil.getModuleDirPath(module));

            VirtualFile outputDir = CompilerPaths.getModuleOutputDirectory(module, false);
            if(outputDir == null) {
                return;
            }
            Path outputPath = Paths.get(outputDir.getPath());

            Path relativeOutput = modulePath.relativize(outputPath);

            // Find embedded jar dependencies
            List<String> embeddedLibs = ModuleRootManager.getInstance(module)
                    .orderEntries()
                    .librariesOnly()
                    .satisfying(entry -> {
                        if(!entry.isValid()) {
                            return false;
                        }
                        VirtualFile[] files = entry.getFiles(OrderRootType.CLASSES);
                        if(files.length < 1) {
                            return false;
                        }

                        String path = files[0].getPath();
                        // We're looking for a jar path, which will end with "!/"
                        if(!path.endsWith("!/")) {
                            return false;
                        }
                        Path jarPath = Paths.get(path.substring(0, path.length()-2));
                        return jarPath.startsWith(modulePath);
                    })
                    .getPathsList().getPathList().stream()
                    .map(Paths::get)
                    .map(modulePath::relativize)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            
            if(embeddedLibs.isEmpty()) {
                devProperties.setProperty(module.getName(), relativeOutput.toString());
            } else {
                String paths = relativeOutput.toString() + "," + String.join(",", embeddedLibs);
                devProperties.setProperty(module.getName(), paths);
            }
        });

        Path propsFile = Paths.get(configuration.getWorkingDir(), "dev.properties");
        // Save the configuration
        Files.createDirectories(propsFile.getParent());
        try(OutputStream fos = Files.newOutputStream(propsFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            devProperties.store(fos, "Created by OpenNTF XPages SDK");
        }
    }

    private void createLocalLinksFiles(@NotNull OsgiRunConfiguration configuration) throws IOException {
        Path linksDir = Paths.get(configuration.getWorkingDir(), "links");
        Files.createDirectories(linksDir);

        // Standard workspace/applications directory
        {
            Path appsLinkFile = linksDir.resolve("apps.link");
            try(OutputStream os = Files.newOutputStream(appsLinkFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                os.write(("path=" + LaunchUtil.fixPathSeparators(ndPlatform.getLocalWorkspaceFolder() + "/applications")).getBytes());
            }
        }

        // Standard shared directory
        {
            Path appsLinkFile = linksDir.resolve("shared.link");
            try(OutputStream os = Files.newOutputStream(appsLinkFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                os.write(("path=" + LaunchUtil.fixPathSeparators(ndPlatform.getLocalInstallFolder() + "/osgi/shared")).getBytes());
            }
        }
    }

    /**
     * Creates the "com.ibm.domino.osgi.sharedlib" fragment jar in the local working environment
     */
    private void createSharedLibJar(@NotNull OsgiRunConfiguration configuration) throws IOException {
        Path jarPath = Paths.get(configuration.getWorkingDir(), "domino", "eclipse", "plugins", ndPlatform.getSystemFragmentFileName());
        Files.createDirectories(jarPath.getParent());
        try(OutputStream os = Files.newOutputStream(jarPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            try(JarOutputStream jos = new JarOutputStream(os)) {
                ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
                jos.putNextEntry(manifestEntry);

                Manifest manifest = new Manifest();
                Attributes attrs = manifest.getMainAttributes();
                attrs.putValue("Manifest-Version", "1.0");
                attrs.putValue("Bundle-SymbolicName", "com.ibm.domino.osgi.sharedlib");
                attrs.putValue("Export-Package", "lotus.domino, com.ibm.domino.http.bootstrap.osgi");
                attrs.putValue("Bundle-Name", "Domino/OSGI Shared library bundle");
                attrs.putValue("Bundle-Version", "1.0.0");
                attrs.putValue("Bundle-ManifestVersion", "2");
                attrs.putValue("Fragment-Host", "system.bundle; extension:=framework");
                manifest.write(jos);
            }
        }
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

    private Collection<String> computeOsgiBundles(INotesDominoPlatform ndPlatform, String remotePath) throws IOException {
        Path plugins = Paths.get(remotePath).resolve("plugins");
        if(Files.isDirectory(plugins)) {
            return Files.find(plugins, 1, (path, attrs) -> {
                    if(path.equals(plugins)) {
                        return false;
                    } if(attrs.isRegularFile() && path.getFileName().toString().toLowerCase().endsWith(".jar")) {
                        return true;
                    } else if(attrs.isDirectory()) {
                        return true;
                    } else {
                        return false;
                    }
                })
                .map(Path::toAbsolutePath)
                .map(path -> LaunchUtil.toLocalPath(path.toString(), ndPlatform))
                .map(LaunchUtil::fixPathSeparators)
                .map(url -> "reference:file:" + url) // TODO fix this
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Converts the IntelliJ OpenAPI module object to a filesystem path.
     *
     * @param module the OpenAPI module to convert
     * @return the path to the directory of the module
     */
    private Path toLocalPath(Module module) {
        // Is it safe to assume that this will always point to a ".iml" file?
        Path modFile = Paths.get(module.getModuleFilePath());
        return modFile.getParent();
    }

    /**
     * Converts a local {@link Path} object to a "reference:file" format suitable for use in a bundle list.
     *
     * @param localPath the local machine path to the file
     * @param localSharedDir the local shared directory
     * @param remoteMappedPath the path on the remote machine to the local shared directory
     * @return a "reference:file" format string
     */
    private String toReferenceUrl(Path localPath, String localSharedDir, String remoteMappedPath) {
        String path = localPath.toString();
        String uri = LaunchUtil.toJunctionPath(path, ndPlatform, localSharedDir, remoteMappedPath);
        return "reference:file:" + uri;
    }
}
