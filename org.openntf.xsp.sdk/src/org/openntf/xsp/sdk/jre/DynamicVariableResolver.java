package org.openntf.xsp.sdk.jre;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.openntf.xsp.sdk.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.platform.NotesDominoPlatformFactory;
import org.openntf.xsp.sdk.utils.StringUtil;

import com.ibm.domino.osgi.debug.launch.LaunchUtils;

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

		String value = ndPlatform.resolveVariable(varName);
		if (!StringUtil.isEmpty(value)) {
			// On Windows need to use 8.3 format for some arguments 
			value = value.replace("Program Files (x86)", "Progra~2");
		}
		
		return LaunchUtils.fixPathSeparators(value);
		
	}
}
