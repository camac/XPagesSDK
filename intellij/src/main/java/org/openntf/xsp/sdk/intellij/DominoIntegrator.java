package org.openntf.xsp.sdk.intellij;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openntf.xsp.sdk.intellij.ui.DominoRunPropertiesEditor;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkIntegrator;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

public class DominoIntegrator extends AbstractFrameworkIntegrator {
    public static final String FRAMEWORK_NAME = "Domino OSGi Framework";

    public DominoIntegrator() {
        super(new DominoInstanceManager(), new DominoOsgiRunConfigurationChecker());
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return FRAMEWORK_NAME;
    }

    @NotNull
    @Override
    public FrameworkRunner createFrameworkRunner() {
        return new DominoRunner();
    }

    @Nullable
    @Override
    public FrameworkRunPropertiesEditor createRunPropertiesEditor() {
        return new DominoRunPropertiesEditor();
    }
}
