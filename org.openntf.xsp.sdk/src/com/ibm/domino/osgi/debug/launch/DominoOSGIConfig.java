 /*
 * © Copyright IBM Corp. 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package com.ibm.domino.osgi.debug.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.StringTokenizer;

import com.ibm.domino.osgi.debug.utils.StringUtil;

/**
 * @author dtaieb
 * Hold information about the Domino OSGi configuration
 */
public class DominoOSGIConfig {

	private int iReturnCode;	//Return Code from the Config Dialog
	private String dominoBinPath;	//Domino Directory path
	private String dominoDataPath;	//Domino Data path
	private String workspaceRelativePath;
	private AbstractDominoOSGILaunchConfiguration launchConfiguration;

	/**
	 * @param launchConfiguration 
	 * @param workspaceRelativePath
	 */
	public DominoOSGIConfig( AbstractDominoOSGILaunchConfiguration launchConfiguration, String workspaceRelativePath ) {
		this.launchConfiguration = launchConfiguration;
		this.workspaceRelativePath = workspaceRelativePath;
	}

	/**
	 * @return
	 */
	public int getReturnCode() {
		return iReturnCode;
	}

	/**
	 * @param returnCode
	 */
	public void setReturnCode(int returnCode) {
		iReturnCode = returnCode;
	}
	
	/**
	 * @return
	 */
	public String getDominoBinPath() {
		return dominoBinPath;
	}

	/**
	 * @param path
	 * @throws DominoOSGIConfigException
	 */
	public void setDirectory(String binPath, String dataPath) throws DominoOSGIConfigException {
		dominoBinPath = binPath;
		dominoDataPath = dataPath;
				
		if ( binPath == null || binPath.length() == 0 ){
			//Try to find a default value using the NotesBin env variable
			binPath = System.getenv("notesbin");
		}
		
		File bin = null;
		if ( binPath != null && binPath.length() > 0 ){
			//Check if it is valid
			bin = new File( binPath );
			if ( !bin.exists() || !bin.isDirectory() ){
				throw new DominoOSGIConfigException( MessageFormat.format( "Path does not exist or is not a directory: {0}", binPath ) );
			}
		}
		
		if ( bin != null && ( dataPath == null || dataPath.length() == 0 ) ){
			//Look for notes.ini
			File notesIni = new File( bin, "notes.ini" );
			if ( notesIni.exists() ){
				//Get the data directory from the notes.ini
				BufferedReader br = null;
				try{
					try{
						br = new BufferedReader( new FileReader( notesIni ));
						String line = null;
						while ( ( line = br.readLine() ) != null ){
							StringTokenizer st = new StringTokenizer( line, "=" );
							if ( st.countTokens() == 2 ){
								String key = st.nextToken();
								if ( "Directory".equalsIgnoreCase( key )){
									dominoDataPath = st.nextToken().trim();
									break;
								}
							}
						}
					}finally{
						if ( br != null ){
							br.close();
						}
					}
				}catch( IOException ex ){
					throw new DominoOSGIConfigException( ex );
				}
			}
		}		
	}

	/**
	 * @return
	 */
	public String getDominoDataPath() {
		return dominoDataPath;
	}

	public void isValid() throws DominoOSGIConfigException {
		if ( StringUtil.isEmpty( dominoBinPath ) ){
			throw new DominoOSGIConfigException("Domino Bin Path is empty");
		}
		if ( StringUtil.isEmpty( dominoDataPath )){
			throw new DominoOSGIConfigException("Domino Data Path is empty");
		}
		
		File bin = new File( dominoBinPath );
		if ( !bin.exists() || !bin.isDirectory() ){
			throw new DominoOSGIConfigException("Domino Bin Path does not exist or is not a valid directory");
		}
		
		File data = new File( dominoDataPath );
		if ( !data.exists() || !data.isDirectory() ){
			throw new DominoOSGIConfigException("Domino Data Path does not exist or is not a valid directory");
		}
		
		File dominoData = new File( data, "domino");
		if ( !dominoData.exists() || !dominoData.isDirectory() ){
			throw new DominoOSGIConfigException("Domino Data Path is not a valid data directory: domino subdirectory is missing");
		}
	}

	/**
	 * @return
	 */
	public String getWorkspacePath() {
		return getDominoDataPath()+ "/" + workspaceRelativePath;
	}

	/**
	 * @return
	 */
	public String[] getProfiles() {
		return launchConfiguration.getProfiles();
	}

	/**
	 * @param selectedProfile
	 */
	public void setProfile(String selectedProfile) {
		launchConfiguration.setProfile( selectedProfile );
	}

}
