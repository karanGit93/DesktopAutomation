package com.automation.desktop.base;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.automation.desktop.listeners.RetryAnalyzer;
import com.automation.desktop.module.CommonMethod;
import com.automation.desktop.utility.TestCaseReporting;
import com.automation.desktop.utility.TestExecutionLogger;
import com.automation.desktop.utility.Waits;
import com.automation.desktop.utility.WindowResponsivenessUtil;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;
import io.qameta.allure.Step;

/**
 * ApplicationLaunch — Facade that preserves every public static method
 * signature while delegating to focused helper classes.
 *
 * <p>Delegates to:
 * <ul>
 *   <li>{@link WinAppDriverService} — WinAppDriver process lifecycle</li>
 *   <li>{@link AppSessionManager} — Desktop app session lifecycle</li>
 *   <li>{@link DesktopSession} — Root desktop session &amp; dialog management</li>
 * </ul>
 *
 * <p>All 100+ test scripts continue to compile unchanged because every
 * existing static method is preserved as a thin delegate.</p>
 */
public class ApplicationLaunch extends BaseSetup {

	public static void launchWindowsApplicationDriver() {
		WinAppDriverService.launch();
	}

	@SuppressWarnings("deprecation")
	public static void endWinAppDriver() {
		WinAppDriverService.stop();
	}

	public static boolean isWinAppDriverProcessRunning() {
		return WinAppDriverService.isRunning();
	}

	@SuppressWarnings("deprecation")
	public static void launchApplication() {
		appDriver = AppSessionManager.launch();
	}

	public static void reconnectToApplication() {
		windowTitle = AppSessionManager.reconnect(appDriver);
	}

	public static void windowMaximize() {
		AppSessionManager.maximize(appDriver);
	}

	public static void ensureWindowMaximized(String methodName) {
		AppSessionManager.ensureMaximized(appDriver, methodName);
	}

	@SuppressWarnings("deprecation")
	public static void minimizeAllWindows() {
		AppSessionManager.minimizeAllWindows();
	}

	public static void waitForDesktopApp() throws IOException {
		AppSessionManager.waitForAppProcess("appName");
	}

	public static void endSession(WindowsDriver<WindowsElement> sessionDriver) {
		WinAppDriverService.endSession(sessionDriver);
	}

	@SuppressWarnings("deprecation")
	public static void rootAppDriver() {
		rootDriver = DesktopSession.startRootSession(rootDriver);
	}

	public static void closeUnnamedPopupsWithoutTouchingAppDriver() {
		rootAppDriver();
		try {
			boolean popupClosed = DesktopSession.closeDialogs(rootDriver);
			if (popupClosed) {
				throw new RuntimeException("❗ Popup(s) detected and closed. Failing test to ensure clean execution.");
			}
		} finally {
			endSession(rootDriver);
		}
	}

	public static void attemptDialogCloseOrPressEsc(WindowsElement dialog) throws Exception {
		DesktopSession.attemptDialogClose(rootDriver, dialog);
	}

//	@AfterMethod(alwaysRun = true)
//	public void tearDown(Method method, ITestResult result) {
//		TestExecutionLogger.info("Application Launch class teardown started...");
//		String methodName = null;
//		try {
//			methodName = method.getName();
//		} catch (Exception e) {
//			// handle exception
//		}
//
//		// Step 1: Window maximize check
//		TestExecutionLogger.info("Step 1 : Checking window maximize state for test: " + methodName);
//		try {
//			ensureWindowMaximized(methodName);
//		} catch (Exception e) {
//			TestExecutionLogger.error("Window maximize failed for: " + methodName + " — " + e.getMessage());
//		}
//
//		// Step 2: Handle popups if test failed
//		TestExecutionLogger.info("Step 2 : Handling popups for test: " + methodName);
//		try {
//			if (!shouldSkipPopupHandling(methodName) && result.getStatus() == ITestResult.FAILURE) {
//
//				// 2a. Check window responsiveness
//				TestExecutionLogger.info("Step 2a : Checking window responsiveness for test: " + methodName);
//				try {
//					String title = WindowResponsivenessUtil.getForegroundWindowTitle();
//					WindowResponsivenessUtil.waitUntilDesktopAppIsResponsive(title, 120);
//					TestExecutionLogger.info("Window is responsive for test: " + methodName);
//				} catch (RuntimeException e) {
//					TestExecutionLogger.error("Window not responsive for test: " + methodName + " — " + e.getMessage());
//				}
//
//				// 2b. Close all popups safely
//				TestExecutionLogger.info("Step 2b : Closing unnamed popups for test: " + methodName);
//				closeUnnamedPopupsWithoutTouchingAppDriver();
//
//				// 2c. Optional: additional cleanup
//				TestExecutionLogger.info("Step 2c : Additional wait after popup handling for test: " + methodName);
//				Waits.waitForFixedDuration(2000);
//			}
//		} catch (Exception e) {
//			TestExecutionLogger.error("Unexpected error during popup handling in tearDown for test: "
//					+ methodName + " — " + e.getMessage());
//		}
//	}

//	@BeforeMethod()
//	public void setup(Method method) {
//		if (AppSessionManager.EXECUTION_ENV.equalsIgnoreCase("local")) {
//			String testName = method.getName();
//			if (!skipProjectCreation(testName))  {
//				
//			}
//		}
//	}

	public static boolean skipProjectCreation(String testName) {
		return testName.equals("quitTestCase");
	}

	private boolean shouldSkipPopupHandling(String methodName) {
		return methodName.equals("End");
	}

	public String testCaseExecutionStatus(ITestResult result) {
		String status;
		switch (result.getStatus()) {
			case ITestResult.SUCCESS:
				status = "PASS";
				break;
			case ITestResult.FAILURE:
				status = "FAIL";
				break;
			case ITestResult.SKIP:
				status = "SKIP";
				break;
			default:
				status = "SKIP";
		}
		return status;
	}
}