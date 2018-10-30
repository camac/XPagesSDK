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

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.openntf.xsp.sdk.platform.INotesDominoPlatform;
import org.openntf.xsp.sdk.platform.NotesDominoPlatformFactory;

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
		
		return LaunchUtils.fixPathSeparators(value);
		
	}
}
