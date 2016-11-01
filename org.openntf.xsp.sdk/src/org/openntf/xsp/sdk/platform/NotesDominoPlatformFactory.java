package org.openntf.xsp.sdk.platform;

public class NotesDominoPlatformFactory {

	private static INotesDominoPlatform NOTES_PLATFORM;
	private static INotesDominoPlatform DOMINO_HTTP_PLATFORM;
	private static INotesDominoPlatform DOMINO_DOTS_PLATFORM;

		
	public static INotesDominoPlatform getNotesPlatform() {
		if(NOTES_PLATFORM == null) {
			NOTES_PLATFORM = new NotesPlatform();
		}
		
		return NOTES_PLATFORM;
	}

	public static INotesDominoPlatform getDominoHttpPlatform() {
		if(DOMINO_HTTP_PLATFORM == null) {
			DOMINO_HTTP_PLATFORM = new DominoHttpPlatform();
		}
		
		return DOMINO_HTTP_PLATFORM;
	}

	public static INotesDominoPlatform getDominoDotsPlatform() {
		if(DOMINO_DOTS_PLATFORM == null) {
			DOMINO_DOTS_PLATFORM = new DominoDotsPlatform();
		}
		
		return DOMINO_DOTS_PLATFORM;
	}
	
	
	
	
}
