package org.openntf.xsp.sdk.intellij;

import com.intellij.execution.configurations.RuntimeConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.impl.DefaultOsgiRunConfigurationChecker;
import org.osmorc.run.OsgiRunConfiguration;

public class DominoOsgiRunConfigurationChecker extends DefaultOsgiRunConfigurationChecker {
    @Override
    protected void checkFrameworkSpecifics(@NotNull OsgiRunConfiguration runConfiguration) throws RuntimeConfigurationException {
        super.checkFrameworkSpecifics(runConfiguration);
    }
}
