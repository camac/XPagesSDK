package org.openntf.xsp.sdk.intellij;

import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.util.JavaParametersUtil;
import org.jetbrains.annotations.NotNull;
import org.openntf.xsp.sdk.intellij.ui.LaunchDialog;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.SelectedBundle;

import java.util.List;

public class DominoRunner implements FrameworkRunner {
    @Override
    public JavaParameters createJavaParameters(@NotNull OsgiRunConfiguration osgiRunConfiguration, @NotNull List<SelectedBundle> list) throws ExecutionException {

        for(SelectedBundle bundle : list) {

        }

        showDialog(osgiRunConfiguration);
        
        return createNoopJavaParameters(osgiRunConfiguration);
    }

    @Override
    public void dispose() {

    }

    // *******************************************************************************
    // * Process
    // *******************************************************************************

    private void showDialog(@NotNull OsgiRunConfiguration osgiRunConfiguration) {
        LaunchDialog dialog = new LaunchDialog(osgiRunConfiguration);
        dialog.pack();
        dialog.setVisible(true);
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
}
