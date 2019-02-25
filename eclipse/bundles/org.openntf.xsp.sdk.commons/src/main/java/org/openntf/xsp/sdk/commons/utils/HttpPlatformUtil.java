package org.openntf.xsp.sdk.commons.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public enum HttpPlatformUtil {
    ;


    public static void loadNotesIniVars(String fileLocation, Properties props) throws IOException {
        // TODO Error handling
        try(BufferedReader br = new BufferedReader(new FileReader(fileLocation))) {
            String line;
            while ((line = br.readLine()) != null) {
                int index = line.indexOf('=');
                if(index > -1) {

                    String iniParamName = line.substring(0, index);
                    String paramValue = line.substring(index+1);

                    props.put(iniParamName, paramValue);
                }
            }

        }

    }
}
