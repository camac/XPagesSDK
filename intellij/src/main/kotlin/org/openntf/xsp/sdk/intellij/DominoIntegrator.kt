package org.openntf.xsp.sdk.intellij

import org.openntf.xsp.sdk.intellij.ui.DominoRunPropertiesEditor
import org.osmorc.frameworkintegration.FrameworkRunner
import org.osmorc.frameworkintegration.impl.AbstractFrameworkIntegrator
import org.osmorc.run.ui.FrameworkRunPropertiesEditor

class DominoIntegrator : AbstractFrameworkIntegrator(DominoInstanceManager(), DominoOsgiRunConfigurationChecker()) {
    val FRAMEWORK_NAME = "Domino OSGi Framework"

    override fun getDisplayName(): String {
        return FRAMEWORK_NAME
    }

    override fun createFrameworkRunner(): FrameworkRunner {
        return DominoRunner()
    }

    override fun createRunPropertiesEditor(): FrameworkRunPropertiesEditor? {
        return DominoRunPropertiesEditor()
    }
}