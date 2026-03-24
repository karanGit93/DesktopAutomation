package com.automation.desktop.base;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.automation.desktop.module.CommonMethod;
import com.automation.desktop.utility.TestExecutionLogger;
import com.automation.desktop.utility.Waits;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;
import io.qameta.allure.Step;

/**
 * AppSessionManager — Manages the Desktop application session lifecycle.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Launch desktop app with retry logic (8 attempts)</li>
 *   <li>Reconnect to the desktop app window after restart/reload</li>
 *   <li>End the desktop app session</li>
 *   <li>Wait for the desktop application process to appear</li>
 *   <li>Maximize / ensure-maximized window state</li>
 * </ul>
 *
 * <p>Bug fix: {@link #reconnect()} now waits <em>after</em> a failed attempt
 * (the original code had a sleep inside the try-block before the reconnect).</p>
 */
public final class AppSessionManager {

    // ========== CONSTANTS ==========

    static final String DESKTOP_APPLICATION_DIRECTORY = "";
    static final String DESKTOP_APP_EXE = DESKTOP_APPLICATION_DIRECTORY + "Microsoft.WindowsCalculator_8wekyb3d8bbwe!App";
    static final String EXECUTION_ENV = System.getProperty("execution.env", "local");

    private AppSessionManager() { /* utility class */ }

    // ========== INNER CLASS: CAPABILITIES ==========

    /**
     * AppCapabilities — Builds DesiredCapabilities for desktop application and Root sessions.
     */
    public static final class AppCapabilities {
        private static final int LOCAL_WAIT_FOR_APP_LAUNCH = 15;
        private static final int SERVER_WAIT_FOR_APP_LAUNCH = 20;

        private AppCapabilities() { }

        public static DesiredCapabilities getDesktopAppCapabilities() {
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("app", DESKTOP_APP_EXE);
            caps.setCapability("appWorkingDir", DESKTOP_APPLICATION_DIRECTORY);

            if (EXECUTION_ENV.equalsIgnoreCase("server")) {
                TestExecutionLogger.info("Setting wait time for server execution environment.");
                caps.setCapability("ms:waitForAppLaunch", SERVER_WAIT_FOR_APP_LAUNCH);
            } else {
                TestExecutionLogger.info("Setting wait time for local execution environment.");
                caps.setCapability("ms:waitForAppLaunch", LOCAL_WAIT_FOR_APP_LAUNCH);
            }
            TestExecutionLogger.info("Desktop app capabilities set: " + caps.asMap().toString());
            return caps;
        }

        public static String getExecutionEnv() {
            return EXECUTION_ENV;
        }

        public static DesiredCapabilities getRootCapabilities() {
            DesiredCapabilities rootCaps = new DesiredCapabilities();
            rootCaps.setCapability("app", "Root");
            return rootCaps;
        }
    }

    // ========== LAUNCH ==========

    /**
     * Launch desktop application with up to 8 retry attempts.
     *
     * @param appDriverRef  will be set on the static BaseSetup.appDriver field
     *                      (passed via caller in ApplicationLaunch facade)
     * @return the created WindowsDriver, or throws Error on exhaustion
     */
    @SuppressWarnings("deprecation")
    @Step("Launching desktop application using WinApp driver capabilities")
    public static WindowsDriver<WindowsElement> launch() {
        int maxRetries = 8;
        int attempt = 0;
        Exception lastException = null;
        CommonMethod.killIfRunning(DESKTOP_APP_EXE);
        TestExecutionLogger.info("Starting winapp driver on server 127.0.0.1 with port 4723");

        while (attempt < maxRetries) {
            try {
                CommonMethod.killIfRunning(DESKTOP_APP_EXE);
                attempt++;
                TestExecutionLogger.info("Launching desktop application — Attempt " + attempt + " of " + maxRetries);
                WindowsDriver<WindowsElement> driver = new WindowsDriver<>(
                        new URL(WinAppDriverService.getUrl()),
                        AppCapabilities.getDesktopAppCapabilities());
                TestExecutionLogger.info("AppDriver instance created: " + driver);
                TestExecutionLogger.info("Desktop application launched successfully on attempt " + attempt);
                return driver;
            } catch (MalformedURLException e) {
                throw new Error("MalformedURLException: " + e.getMessage());
            } catch (Exception e) {
                lastException = e;
                TestExecutionLogger.error("Launch attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    TestExecutionLogger.info("Retrying in 2 seconds...");
                    Waits.waitForFixedDuration(2000);
                }
            }
        }

        throw new Error("Failed to launch desktop application after " + maxRetries + " retries: "
                + (lastException != null ? lastException.getMessage() : "unknown"));
    }

    // ========== RECONNECT ==========

    /**
     * Reconnect to the running desktop application window (after restart/reload).
     *
     * <p>Bug fix: the wait now happens <em>before</em> each attempt (matching
     * the original intent) and a post-failure wait was added so the next attempt
     * doesn't fire immediately after an exception.</p>
     *
     * @param appDriver the existing appDriver to reconnect through
     * @return the window title after successful reconnection
     */
    @Step("Trying to reconnect with windows app.")
    public static String reconnect(WindowsDriver<WindowsElement> appDriver) {
        int maxRetries = 5;
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                TestExecutionLogger.info("Reconnecting to desktop application window.");
                if (AppCapabilities.getExecutionEnv().equalsIgnoreCase("server")) {
                    TestExecutionLogger.info("Server environment — longer wait time.");
                    Waits.waitForFixedDuration(45000);
                } else {
                    TestExecutionLogger.info("Local environment — standard wait time.");
                    Waits.waitForFixedDuration(30000);
                }
                attempt++;
                TestExecutionLogger.info("Reconnect attempt " + attempt + " of " + maxRetries);
                TestExecutionLogger.info("Getting window handles.");
                Set<String> handles = appDriver.getWindowHandles();
                System.out.println(handles);

                if (handles == null || handles.isEmpty()) {
                    TestExecutionLogger.warning("No window handles found on attempt " + attempt);
                    if (attempt < maxRetries) {
                        Waits.waitForFixedDuration(1000);
                    }
                    continue;
                }

                String windowTitle = null;
                for (String handle : handles) {
                    TestExecutionLogger.info("Switching to window handle: " + handle);
                    appDriver.switchTo().window(handle);
                    TestExecutionLogger.info("Window Title: " + appDriver.getTitle());
                    windowTitle = appDriver.getTitle();
                }

                TestExecutionLogger.info("Reconnected successfully on attempt " + attempt);
                return windowTitle;

            } catch (Exception e) {
                lastException = e;
                TestExecutionLogger.warning("Reconnect attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    TestExecutionLogger.info("Retrying reconnection in 5 seconds...");
                    Waits.waitForFixedDuration(5000);
                }
            }
        }

        throw new RuntimeException("Failed to reconnect after " + maxRetries + " retries: "
                + (lastException != null ? lastException.getMessage() : "No window handles found"));
    }

    // ========== SESSION END ==========

    /**
     * End the desktop application session. Delegates to WinAppDriverService.
     */
    public static void endAppSession(WindowsDriver<WindowsElement> appDriver) {
        WinAppDriverService.endSession(appDriver);
    }

    // ========== PROCESS WAIT ==========

    /**
     * Wait for desktop application to appear in the task list (up to 5 minutes).
     */
    @SuppressWarnings("deprecation")
    public static void waitForAppProcess(String appProcessName) throws IOException {
        boolean isRunning = false;
        long startTime = System.currentTimeMillis();
        long timeout = 5 * 60 * 1000; // 5 minutes

        while (!isRunning && (System.currentTimeMillis() - startTime < timeout)) {
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(appProcessName)) {
                    isRunning = true;
                    System.out.println("✅ " + appProcessName + " is running!");
                    break;
                }
            }
            reader.close();

            if (!isRunning) {
                System.out.println("⏳ Waiting for " + appProcessName + " to start...");
                Waits.waitForFixedDuration(3000);
            }
        }

        if (!isRunning) {
            throw new RuntimeException("❌ Timeout: " + appProcessName + " did not start within 5 minutes.");
        }
    }

    // ========== WINDOW MANAGEMENT ==========

    public static void maximize(WindowsDriver<WindowsElement> appDriver) {
        appDriver.manage().window().maximize();
    }

    /**
     * Ensure the window is maximized; maximize if it isn't.
     */
    public static void ensureMaximized(WindowsDriver<WindowsElement> appDriver, String methodName) {
        Dimension currentSize = appDriver.manage().window().getSize();
        Dimension screenSize = new Dimension(
                (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());

        if (!currentSize.equals(screenSize)) {
            appDriver.manage().window().maximize();
            TestExecutionLogger.info("Window was NOT maximized after test: " + methodName + ". Maximized now.");
        } else {
            TestExecutionLogger.info("Window is already maximized after test: " + methodName);
        }
    }

    /**
     * Minimize all desktop windows using PowerShell toggle-desktop.
     */
    @SuppressWarnings("deprecation")
    public static void minimizeAllWindows() {
        try {
            String command = "powershell -command \"(New-Object -ComObject Shell.Application).ToggleDesktop()\"";
            Runtime.getRuntime().exec(command);
            TestExecutionLogger.info("All windows minimized (toggle desktop).");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
