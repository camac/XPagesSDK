package org.openntf.xsp.sdk.intellij;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.impl.AbstractFrameworkInstanceManager;
import org.osmorc.run.ui.SelectedBundle;

import java.util.Collection;
import java.util.Collections;

public class DominoInstanceManager extends AbstractFrameworkInstanceManager {
    @NotNull
    @Override
    public Collection<SelectedBundle> getFrameworkBundles(@NotNull FrameworkInstanceDefinition frameworkInstanceDefinition, @NotNull FrameworkBundleType frameworkBundleType) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String checkValidity(@NotNull FrameworkInstanceDefinition instance) {
        // Always valid, since we're not really running a framework
        return null;
    }
}
