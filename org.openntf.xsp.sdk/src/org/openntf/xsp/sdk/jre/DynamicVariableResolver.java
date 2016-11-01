package org.openntf.xsp.sdk.jre;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.openntf.xsp.sdk.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.platform.NotesDominoPlatformFactory;

public class DynamicVariableResolver implements IDynamicVariableResolver {

	public String resolveValue(IDynamicVariable variable, String arg1) throws CoreException {
		String varName = variable.getName().toLowerCase(Locale.US);

		INotesDominoPlatform ndPlatform;

		if (varName.startsWith("notes_")) {
			ndPlatform = NotesDominoPlatformFactory.getNotesPlatform();
		} else if (varName.startsWith("domino_")) {
			ndPlatform = NotesDominoPlatformFactory.getDominoHttpPlatform();
		} else if (varName.startsWith("dots_")) {
			ndPlatform = NotesDominoPlatformFactory.getDominoDotsPlatform();
		} else {
			return "";
		}

		if (varName.endsWith("_install")) {
			return ndPlatform.getRemoteInstallFolder();
		} else if (varName.endsWith("_rcp_data")) {
			return ndPlatform.getRemoteWorkspaceFolder();
		} else if (varName.endsWith("_rcp_base")) {
			try {
				return ndPlatform.getRcpBase();
			} catch (Exception e) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
		} else if (varName.endsWith("_rcp_target")) {
			return ndPlatform.getRemoteRcpTargetFolder();
		} else if (varName.endsWith("_shared_target")) {
			return ndPlatform.getRemoteRcpSharedFolder();
		}
		
		return "";

	}
}
