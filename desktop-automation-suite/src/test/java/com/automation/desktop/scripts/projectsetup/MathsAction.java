package com.automation.desktop.scripts.projectsetup;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.automation.desktop.base.ApplicationLaunch;
import com.automation.desktop.locator.calculatorbuttons.CalculatorMainScreen;
import com.automation.desktop.utility.Interaction;
import com.automation.desktop.utility.Interaction.InteractionType;
import com.automation.desktop.utility.Interaction.LocatorStrategy;
import com.automation.desktop.utility.TestExecutionLogger;

import io.qameta.allure.Step;

public class MathsAction extends ApplicationLaunch {
	
	@BeforeTest(alwaysRun = true)
	public void setup() {
		launchWindowsApplicationDriver();
		launchApplication();
	}
	
	@Test(priority=1, description="Perform addition action")
	@Step("Perform addition action")
	public void sumOfTwoNumbers() {
		Interaction.performAction(LocatorStrategy.NAME, InteractionType.CLICK, CalculatorMainScreen.NUMBER_ONE_NAME);
		Interaction.performAction(LocatorStrategy.NAME, InteractionType.CLICK, CalculatorMainScreen.NUMBER_PLUS_NAME);
		Interaction.performAction(LocatorStrategy.NAME, InteractionType.CLICK, CalculatorMainScreen.NUMBER_TWO_NAME);
		Interaction.performAction(LocatorStrategy.NAME, InteractionType.CLICK, CalculatorMainScreen.NUMBER_EQUALS_NAME);
		String sumOutput = (String) Interaction.performAction(LocatorStrategy.XPATH, InteractionType.GET_TEXT, CalculatorMainScreen.RESULT_XPATH);
		sumOutput = sumOutput.replace("Display is ", "").trim();
		TestExecutionLogger.info("Sum of numbers is " + sumOutput);

	}
	
	@AfterTest(alwaysRun = true)
	public void tearDown() {
		endSession(appDriver);
		endWinAppDriver();
	}
}
