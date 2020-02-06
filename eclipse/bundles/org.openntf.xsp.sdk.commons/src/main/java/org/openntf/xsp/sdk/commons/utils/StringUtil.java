 /*
 * � Copyright IBM Corp. 2012
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

package org.openntf.xsp.sdk.commons.utils;

 /**
 * @author dtaieb
 *
 */
public class StringUtil {

	/**
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		if ( s == null ){
			return true;
		}
		return s.length() == 0 ;
	}
	
	public static String getNonNullString(String test){
	    return test == null ? "" : test;
	}

	public static String prunePath(String path) {
		if (CommonUtils.isNotEmpty(path)) {
			if (path.endsWith("/") || path.endsWith("\\")) {
				return path.substring(0, path.length() - 1);
			}
		}
		return path;
	}
	
}
