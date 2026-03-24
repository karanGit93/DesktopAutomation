package com.automation.desktop.base;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.automation.desktop.utility.TestExecutionLogger;
import com.automation.desktop.utility.Waits;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;
import io.qameta.allure.Step;

/**
 * WinAppDriverService — Manages the WinAppDriver process lifecycle.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Start/stop the WinAppDriver.exe process</li>
 *   <li>Check whether the process is running</li>
 *   <li>Generic driver session teardown (closeApp + quit)</li>
 * </ul>
 *
 * <p>Bug fix: {@link #endSession(WindowsDriver)} uses try/finally to ensure
 * {@code quit()} is always called even if {@code closeApp()} throws.</p>
 */
public final class WinAppDriverService {

    // ========== CONSTANTS ==========

    static final String WINAPP_DRIVER_PATH = "C:\\Program Files\\Windows Application Driver\\WinAppDriver.exe";
    static final String WINAPP_DRIVER_URL = "http://127.0.0.1:4723";
    private static final String WINAPP_DRIVER_PROCESS = "WinAppDriver.exe";

    private WinAppDriverService() { /* utility class */ }

    // ========== LIFECYCLE ==========

    /**
     * Launch the WinAppDriver process.
     */
	public static void launch() {
        try {
            TestExecutionLogger.info("Starting WinAppDriver from path: " + WINAPP_DRIVER_PATH);
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", WINAPP_DRIVER_PATH);
            builder.inheritIO(); // Optional: inherit IO to see WinAppDriver logs in console
            Process process = builder.start();
            TestExecutionLogger.info("WinAppDriver opening command triggered successfully.");
            Waits.waitForFixedDuration(2500);
        } catch (IOException e) {
            TestExecutionLogger.error("Failed to start WinAppDriver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Force-kill WinAppDriver processes.
     */
    @SuppressWarnings("deprecation")
    public static void stop() {
        try {
            TestExecutionLogger.info("Closing WinAppDriver");
            Runtime.getRuntime().exec("taskkill /F /IM WinAppDriver.exe");
            TestExecutionLogger.info("WinAppDriver closed successfully.");
        } catch (IOException e) {
            TestExecutionLogger.error("Failed to close WinAppDriver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if WinAppDriver.exe is currently running.
     */
    public static boolean isRunning() {
        try {
            @SuppressWarnings("deprecation")
            Process process = Runtime.getRuntime().exec("tasklist");
            java.io.BufferedReader reader =
                    new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(WINAPP_DRIVER_PROCESS)) {
                    TestExecutionLogger.info("WinAppDriver process is running.");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            TestExecutionLogger.error("Error checking WinAppDriver process: " + e.getMessage());
            return false;
        }
    }

    /**
     * Return the WinAppDriver URL.
     */
    public static String getUrl() {
        return WINAPP_DRIVER_URL;
    }

    // ========== SESSION TEARDOWN ==========

    /**
     * Close and quit a driver session safely.
     *
     * <p>Bug fix: uses try/finally so {@code quit()} is always called
     * even if {@code closeApp()} throws an exception.</p>
     *
     * @param sessionDriver the driver session to tear down
     */
    @Step("Closing driver instance")
    public static void endSession(WindowsDriver<WindowsElement> sessionDriver) {
        Waits.waitForFixedDuration(1000);
        TestExecutionLogger.info("Closing the application session");

        if (sessionDriver == null) {
            TestExecutionLogger.warning("Session driver is null, cannot close the application.");
            return;
        }
        if (sessionDriver.getSessionId() == null || sessionDriver.getSessionId().toString().isEmpty()) {
            TestExecutionLogger.warning("Session driver is not active, cannot close the application.");
            return;
        }

        try {
            TestExecutionLogger.info("Closing all the open application");
            sessionDriver.closeApp();
        } catch (Exception e) {
            TestExecutionLogger.warning("closeApp() failed: " + e.getMessage());
        } finally {
            try {
                if (sessionDriver.getSessionId() != null) {
                    TestExecutionLogger.info("Closing all additional application along with session driver");
                    sessionDriver.quit();
                }
            } catch (Exception e) {
                TestExecutionLogger.warning("quit() failed: " + e.getMessage());
            }
        }

        TestExecutionLogger.info("Application session closed successfully.");
    }
}
