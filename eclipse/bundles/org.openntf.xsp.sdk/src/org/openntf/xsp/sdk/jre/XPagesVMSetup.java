/**
 * Copyright © 2011-2018 Nathan T. Freeman, Serdar Basegmez, Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.sdk.jre;

import java.io.File;

import org.eclipse.jdt.internal.launching.StandardVM;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

public class XPagesVMSetup {
	public final static String NOTES_VM_ID = "org.openntf.xsp.notes.jre";
	public final static String DOMINO_VM_ID = "org.openntf.xsp.domino.jre";
	public final static String NOTES_VM_NAME = "XPages Notes JRE";
	public final static String DOMINO_VM_NAME = "XPages Domino JRE";
	public final static String NOTES_VM_ARGS = "-Djava.library.path=${notes_install} -Xj9";
	public final static String DOMINO_VM_ARGS = "-Djava.library.path=${domino_install} -Xj9";

	public XPagesVMSetup() {

	}

	public static void setupNotesJRE(String jvmPath) {
		// check that the appropriate JRE is configured for this version of Notes
		// System.out.println("JRE Setup Activated!");
		String vmID = NOTES_VM_ID;
		IVMInstallType installType = JavaRuntime.getVMInstallType("org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType");
		IVMInstall install = installType.findVMInstall(vmID);

		if (install == null) {
			install = installType.createVMInstall(vmID);
			install.setName(NOTES_VM_NAME);
			install.setInstallLocation(new File(jvmPath));
			((StandardVM) install).setVMArgs(NOTES_VM_ARGS);
			JavaRuntime.fireVMAdded(install);
		}
	}

	public static void setupDominoJRE(String jvmPath) {
		// check that the appropriate JRE is configured for this version of Notes
		// System.out.println("JRE Setup Activated: " + jvmPath);
		String vmID = DOMINO_VM_ID;
		IVMInstallType installType = JavaRuntime.getVMInstallType("org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType");
		IVMInstall install = installType.findVMInstall(vmID);

		if (install == null) {
			install = installType.createVMInstall(vmID);
			install.setName(DOMINO_VM_NAME);
			install.setInstallLocation(new File(jvmPath));
			((StandardVM) install).setVMArgs(DOMINO_VM_ARGS);
			JavaRuntime.fireVMAdded(install);
		}
	}
}
