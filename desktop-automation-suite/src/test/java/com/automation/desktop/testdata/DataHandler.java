package com.automation.desktop.testdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import com.automation.desktop.base.BaseSetup;

public class DataHandler extends BaseSetup {
    
    /**
     * Read Config.properties file value
     */
	
	
	protected static String CONFIG_FILE_PATH = "./src/test/resources/config.properties";
    
    public static Properties loadPropertiesOne() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
        	configProp = new Properties();
        	configProp.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configProp;
    }
  
    public static String getProperty(String key) {
        if (configProp == null) {
            loadPropertiesOne(); // Load config only when needed
        }
        return configProp.getProperty(key);
    }
    
    public static String readFileAsString(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath))).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null; // or throw custom exception if preferred
        }
    }
    
    /**
     * Read Assertion validation message
     */
    
    protected static String ASSERTION_FILE_PATH = "./src/test/resources/assertionErrorMessage.properties";
  
	public static Properties loadPropertiesTwo() {
		try (FileInputStream fis = new FileInputStream(ASSERTION_FILE_PATH)) {
			assertProp = new Properties();
			assertProp.load(fis);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return assertProp;
	}
  
	public static String getAssertProperty(String key) {
		if (assertProp == null) {
			loadPropertiesTwo(); // Load config only when needed
		}
		return assertProp.getProperty(key);
	}

}