package org.openntf.xsp.sdk.platform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.openntf.xsp.sdk.exceptions.XPagesSDKError;

public abstract class AbstractNotesDominoPlatform implements INotesDominoPlatform {

	protected static final String INIVAR_INSTALLFOLDER = "NotesProgram";
	protected static final String INIVAR_DATAFOLDER = "Directory";
		
	private Properties notesIniProperties;
	
	public AbstractNotesDominoPlatform() {
	}
	
	protected Properties getNotesIniProperties() {
		if(notesIniProperties == null) {
			Properties props = new Properties();
			
			try {
				loadNotesIniVars(getNotesIniFilePath(), props);
			} catch (IOException e) {
				throw new XPagesSDKError("Unable to find notes.ini file for " + getName() + " : " + getNotesIniFilePath());
			}
			
			notesIniProperties = props;
		}
		
		return notesIniProperties;
	}
	
	protected String getNotesIniProperty(String propertyName, String defaultValue) {
		return getNotesIniProperties().getProperty(propertyName, defaultValue);
	}

	@Override
	public String getLocalInstallFolder() {
		return getNotesIniProperty(INIVAR_INSTALLFOLDER, "");
	}

	@Override
	public String getLocalDataFolder() {
		return getNotesIniProperty(INIVAR_DATAFOLDER, "");
	}

	private static void loadNotesIniVars(String fileLocation, Properties props) throws IOException {
		// TODO Error handling
		BufferedReader br = null;

		try {

			String line;

			br = new BufferedReader(new FileReader(fileLocation));

			while ((line = br.readLine()) != null) {
				if(line.contains("=")) {
					String iniParamName = StringUtils.substringBefore(line, "=");
					String paramValue= StringUtils.substringAfter(line, "=");

					props.put(iniParamName, paramValue);
				}
			}

		} finally {
			try {
				if (null != br) 
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
