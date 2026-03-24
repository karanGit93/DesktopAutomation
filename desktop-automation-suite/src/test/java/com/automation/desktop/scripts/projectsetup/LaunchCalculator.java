package com.automation.desktop.scripts.projectsetup;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.automation.desktop.base.ApplicationLaunch;
import com.automation.desktop.listeners.RetryAnalyzer;
import com.automation.desktop.module.CommonMethod;
import com.automation.desktop.utility.TestExecutionLogger;

import io.qameta.allure.Step;

public class LaunchCalculator extends ApplicationLaunch {

    @BeforeTest(alwaysRun = true)
    public void setup() {
        launchWindowsApplicationDriver();
        isWinAppDriverProcessRunning();
        minimizeAllWindows();
        CommonMethod.killIfRunning("Calculator");

    }

    @Test(priority = 1, alwaysRun = true, retryAnalyzer = RetryAnalyzer.class, description = "Verify Calculator application is getting launched successfully")
    @Step("Verify Calculator application is getting launched successfully")
    public void LaunchCalculatorApp() {
        try {
            launchApplication();
        } catch (Exception e) {
            e.printStackTrace();
        
    }
}

    @AfterTest(alwaysRun = true)
    @Step("Closing WindowsDriver driver instance")
    public static void End() {
    	try {
    		endSession(appDriver);
    		endWinAppDriver();
    	}catch (Exception e) {
			TestExecutionLogger.info("Instance already closed.");
		}
    }
    
}
