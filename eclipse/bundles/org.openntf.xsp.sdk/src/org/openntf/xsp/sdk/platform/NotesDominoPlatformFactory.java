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
package org.openntf.xsp.sdk.platform;

import org.openntf.xsp.sdk.commons.platform.INotesDominoPlatform;

public class NotesDominoPlatformFactory {

	private static INotesDominoPlatform NOTES_PLATFORM;
	private static INotesDominoPlatform DOMINO_HTTP_PLATFORM;
	private static INotesDominoPlatform DOMINO_DOTS_PLATFORM;

		
	public static INotesDominoPlatform getNotesPlatform() {
		if(NOTES_PLATFORM == null) {
			NOTES_PLATFORM = new EclipseNotesPlatform();
		}
		
		return NOTES_PLATFORM;
	}

	public static INotesDominoPlatform getDominoHttpPlatform() {
		if(DOMINO_HTTP_PLATFORM == null) {
			DOMINO_HTTP_PLATFORM = new DominoHttpPlatformEclipse();
		}
		
		return DOMINO_HTTP_PLATFORM;
	}

	public static INotesDominoPlatform getDominoDotsPlatform() {
		if(DOMINO_DOTS_PLATFORM == null) {
			DOMINO_DOTS_PLATFORM = new DominoDotsPlatformEclipse();
		}
		
		return DOMINO_DOTS_PLATFORM;
	}
	
	
	
	
}
