package com.automation.desktop.utility;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.automation.desktop.base.BaseSetup;

import io.appium.java_client.functions.ExpectedCondition;
import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;

/**
 * Waits — centralized wait utility for the framework.
 * Provides fluent-wait element finding, enabled-element waiting, and fixed-duration pauses.
 * Timeout defaults: 20s (local), 25s (server) — controlled by {@code -Dexecution.env}.
 */
public class Waits extends BaseSetup {

	// ========== CONFIGURATION ==========

	private static final int DEFAULT_TIMEOUT;
	private static final int POLLING_INTERVAL_SECONDS = 2;

	static {
		String executionEnv = System.getProperty("execution.env", "local");
		DEFAULT_TIMEOUT = executionEnv.equalsIgnoreCase("server") ? 25 : 20;
	}

	/** Returns the environment-aware default wait timeout (in seconds). */
	public static int getDefaultWaitTime() {
		return DEFAULT_TIMEOUT;
	}

	// ========== ELEMENT WAIT ==========

	/** Wait for element by Name. */
	public static WindowsElement waitForElementByName(WindowsDriver<WindowsElement> driver,
			String locator, Optional<Integer> timeoutInSeconds) {
		return waitForElement(driver, locator, "name", timeoutInSeconds, d -> d.findElementByName(locator));
	}

	/** Wait for element by XPath. */
	public static WindowsElement waitForElementByXpath(WindowsDriver<WindowsElement> driver,
			String locator, Optional<Integer> timeoutInSeconds) {
		return waitForElement(driver, locator, "XPath", timeoutInSeconds, d -> d.findElementByXPath(locator));
	}

	/** Wait for element by TagName. */
	public static WindowsElement waitForElementByTagName(WindowsDriver<WindowsElement> driver,
			String locator, Optional<Integer> timeoutInSeconds) {
		return waitForElement(driver, locator, "tag name", timeoutInSeconds, d -> d.findElementByTagName(locator));
	}

	// ========== ENABLED ELEMENT WAIT ==========

	/** Wait until element located by XPath is enabled. */
	public static void waitForEnabledElementByXpath(WindowsDriver<WindowsElement> driver,
			String locator, Optional<Integer> timeoutInSeconds) {
		waitForEnabledElement(driver, driver.findElement(By.xpath(locator)), locator, "XPath", timeoutInSeconds);
	}

	/** Wait until element located by Name is enabled. */
	public static void waitForEnabledElementByName(WindowsDriver<WindowsElement> driver,
			String locator, Optional<Integer> timeoutInSeconds) {
		waitForEnabledElement(driver, driver.findElementByName(locator), locator, "name", timeoutInSeconds);
	}

	/** Wait until element located by TagName is enabled. */
	public static void waitForEnabledElementByTagName(WindowsDriver<WindowsElement> driver,
			String locator, Optional<Integer> timeoutInSeconds) {
		waitForEnabledElement(driver, driver.findElementByTagName(locator), locator, "tag name", timeoutInSeconds);
	}

	// ========== FIXED DURATION WAIT ==========

	/**
	 * Pause execution for a fixed duration.
	 * Accumulates total fixed wait time across the entire test run for diagnostic purposes
	 * (see {@code BaseSetup.totalFixedWaitTime}).
	 */
	public static void waitForFixedDuration(int timeInMilliSeconds) {
		try {
			totalFixedWaitTime += timeInMilliSeconds;
			TestExecutionLogger.info("Fixed wait: " + timeInMilliSeconds + "ms"
					+ " | Cumulative fixed wait: " + totalFixedWaitTime + "ms");
			Thread.sleep(timeInMilliSeconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			TestExecutionLogger.warning("waitForFixedDuration interrupted after " + timeInMilliSeconds + "ms");
		}
	}

	// ========== PRIVATE HELPERS ==========

	/** Core FluentWait logic — all waitForElementBy* methods delegate here. */
	private static WindowsElement waitForElement(WindowsDriver<WindowsElement> driver,
			String locator, String locatorType, Optional<Integer> timeoutInSeconds,
			Function<WindowsDriver<WindowsElement>, WindowsElement> finder) {
		int timeout = timeoutInSeconds.orElse(DEFAULT_TIMEOUT);
		TestExecutionLogger.info("Waiting for element by " + locatorType + ": " + locator
				+ " (timeout: " + timeout + "s, polling: " + POLLING_INTERVAL_SECONDS + "s)");

		FluentWait<WindowsDriver<WindowsElement>> wait = new FluentWait<>(driver)
				.withTimeout(Duration.ofSeconds(timeout))
				.pollingEvery(Duration.ofSeconds(POLLING_INTERVAL_SECONDS))
				.ignoring(Exception.class);

		return wait.until(finder);
	}

	/** Core enabled-wait logic — all waitForEnabledElementBy* methods delegate here. */
	private static void waitForEnabledElement(WindowsDriver<WindowsElement> driver,
			WindowsElement element, String locator, String locatorType,
			Optional<Integer> timeoutInSeconds) {
		int timeout = timeoutInSeconds.orElse(15);
		if (timeout > 300) {
			TestExecutionLogger.warning("waitForEnabled timeout=" + timeout
					+ "s seems too high. Did you pass milliseconds instead of seconds?");
		}
		TestExecutionLogger.info("Waiting for element by " + locatorType + ": " + locator
				+ " to be enabled (timeout: " + timeout + "s)");
		WebDriverWait wait = new WebDriverWait(driver, timeout);
		wait.until((ExpectedCondition<Boolean>) d -> element.isEnabled());
	}
}