package org.openntf.xsp.sdk.platform;

public enum Target {
	NOTES(),
	DOMINO_HTTP(),
	DOMINO_DOTS();
	
	Target() {
		
	}
	
	public INotesDominoPlatform platform() {
		switch(this) {
		case NOTES: 
			return NotesDominoPlatformFactory.getNotesPlatform();
		case DOMINO_HTTP: 
			return NotesDominoPlatformFactory.getDominoHttpPlatform();
		case DOMINO_DOTS: 
			return NotesDominoPlatformFactory.getDominoDotsPlatform();
		default:
			return null;
		}
	}
	
}
