package com.automation.desktop.base;

import static org.testng.Assert.assertTrue;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;

import com.automation.desktop.utility.TestExecutionLogger;
import com.automation.desktop.utility.Waits;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

/**
 * DesktopSession — Manages the root desktop session and dialog handling.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Start/end the root desktop driver session</li>
 *   <li>Close unnamed popup dialogs without touching appDriver</li>
 *   <li>Attempt dialog close via Close button or ESC key</li>
 * </ul>
 *
 * <p>Bug fixes:
 * <ul>
 *   <li>{@link #closeDialogs(WindowsDriver)} returns a boolean instead of
 *       throwing RuntimeException, letting the caller decide the outcome.</li>
 *   <li>{@link #attemptDialogClose(WindowsDriver, WindowsElement)} no longer
 *       creates a new root session internally (avoiding session churn).</li>
 * </ul>
 */
public final class DesktopSession {

    private DesktopSession() { /* utility class */ }

    // ========== ROOT SESSION LIFECYCLE ==========

    /**
     * Start a new root desktop session via WinAppDriver.
     *
     * <p>Bug fix: if a rootDriver already exists and has an active session,
     * it is closed first to prevent driver leaks.</p>
     *
     * @param existingRoot  current rootDriver (may be null)
     * @return newly created root WindowsDriver
     */
    @SuppressWarnings("deprecation")
    @Step("Connecting to root apps of desktop launched by desktop application")
    public static WindowsDriver<WindowsElement> startRootSession(WindowsDriver<WindowsElement> existingRoot) {
        // Prevent root driver leak
        if (existingRoot != null && existingRoot.getSessionId() != null) {
            try {
                existingRoot.quit();
                TestExecutionLogger.info("Cleaned up previous root session before starting new one.");
            } catch (Exception e) {
                TestExecutionLogger.warning("Could not clean previous root session: " + e.getMessage());
            }
        }

        try {
            WindowsDriver<WindowsElement> root = new WindowsDriver<>(
                    new URL(WinAppDriverService.getUrl()),
                    AppSessionManager.AppCapabilities.getRootCapabilities());

            try {
   //             assertTrue(root.findElementByName("desktop application").isDisplayed());
            } catch (Exception e) {
                Allure.step("An Exception occurred but has been handled to avoid false failure");
  //              assertTrue(root.findElementByName("desktop application").isDisplayed());
            }

            Waits.waitForFixedDuration(2000);
            return root;

        } catch (MalformedURLException e) {
            Allure.step("MalformedURLException occurred while connecting to root app driver");
            e.printStackTrace();
            throw new RuntimeException("Failed to start root session", e);
        }
    }

    /**
     * End the root desktop session.
     */
    public static void endRootSession(WindowsDriver<WindowsElement> rootDriver) {
        WinAppDriverService.endSession(rootDriver);
    }

    // ========== DIALOG MANAGEMENT ==========

    /**
     * Scan for and close unnamed popup dialogs without touching appDriver.
     *
     * <p>Bug fix: returns {@code true} if any popup was closed (instead of
     * throwing RuntimeException). The caller can decide whether to fail the test.</p>
     *
     * @param rootDriver  the root desktop driver to scan with
     * @return true if any popup was closed, false otherwise
     */
    public static boolean closeDialogs(WindowsDriver<WindowsElement> rootDriver) {
        boolean popupClosed = false;

        try {
            System.out.println("🔍 Scanning for windows...");

            for (int attempt = 1; attempt <= 5; attempt++) {
                List<WindowsElement> dialogs = rootDriver.findElementsByXPath(
                        "//Window[contains(@LocalizedControlType,'dialog')]");
                TestExecutionLogger.info("🪟 Total windows found: " + dialogs.size());

                if (dialogs.isEmpty()) {
                    TestExecutionLogger.info("✅ No dialogs found.");
                    break;
                }

                boolean foundVisibleDialog = false;

                for (WindowsElement dialog : dialogs) {
                    try {
                        String name = dialog.getAttribute("Name");
                        String type = dialog.getAttribute("LocalizedControlType");
                        String isOffscreen = dialog.getAttribute("IsOffscreen");
                        String isEnabled = dialog.getAttribute("IsEnabled");

                        if ("true".equalsIgnoreCase(isOffscreen) || "false".equalsIgnoreCase(isEnabled)) {
                            TestExecutionLogger.info("🚫 Skipping hidden or inactive dialog: '" + name + "'");
                            continue;
                        }

                        TestExecutionLogger.info("🧩 Handling dialog: Name='" + name + "', Type='" + type + "'");
                        foundVisibleDialog = true;
                        popupClosed = true;

                        attemptDialogClose(rootDriver, dialog);
                        Waits.waitForFixedDuration(200);

                    } catch (Exception e) {
                        TestExecutionLogger.info("⚠️ Could not handle a dialog: " + e.getMessage());
                    }
                }

                if (!foundVisibleDialog) {
                    TestExecutionLogger.info("✅ No visible dialogs to handle.");
                    break;
                }
            }

        } catch (Exception e) {
            TestExecutionLogger.info("❌ Failed to scan/close popups: " + e.getMessage());
        }

        if (popupClosed) {
            TestExecutionLogger.info("✅ Popup check complete. Popups were closed.");
        } else {
            TestExecutionLogger.info("✅ No popups needed closing.");
        }

        return popupClosed;
    }

    /**
     * Attempt to close a dialog via the Close button, falling back to ESC key.
     *
     * <p>Bug fix: no longer creates a new root session internally — uses the
     * passed rootDriver directly, avoiding session churn.</p>
     *
     * @param rootDriver  the root desktop driver
     * @param dialog      the dialog element to close
     */
    public static void attemptDialogClose(WindowsDriver<WindowsElement> rootDriver, WindowsElement dialog) throws Exception {
        try {
            rootDriver.findElement(By.xpath(
                    "//Window[contains(@LocalizedControlType,'dialog')]//Button[@Name='Close']")).click();
            TestExecutionLogger.info("✅ Closed dialog using Close button.");
            // Also send ESC as a safety measure
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
        } catch (NoSuchElementException e) {
            // Fallback to ESC key
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_ESCAPE);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            TestExecutionLogger.info("🔘 Sent ESC key to close dialog.");
        }
    }
}
