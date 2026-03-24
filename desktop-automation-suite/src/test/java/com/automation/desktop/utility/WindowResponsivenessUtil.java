package com.automation.desktop.utility;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

public class WindowResponsivenessUtil {

    /**
     * Checks if the given window handle (HWND) is responsive using SendMessageTimeout.
     * Returns true if the window is responding to WM_NULL within the timeout.
     */
    public static boolean isWindowResponsive(HWND hwnd) {
        final int SMTO_ABORTIFHUNG = 0x0002;
        final int WM_NULL = 0x0000;
        final int TIMEOUT_MS = 1000;

        DWORDByReference result = new DWORDByReference();

        LRESULT response = User32.INSTANCE.SendMessageTimeout(
            hwnd,
            WM_NULL,
            new WPARAM(0),
            new LPARAM(0),
            SMTO_ABORTIFHUNG,
            TIMEOUT_MS,
            result
        );

        return response.intValue() != 0;
    }

    /**
     * Waits until a window with the specified title becomes responsive.
     * Times out after maxWaitInSeconds and throws RuntimeException.
     */
    public static HWND waitUntilDesktopAppIsResponsive(String windowTitle, int maxWaitInSeconds) {
        long startTime = System.currentTimeMillis();
        HWND hwnd = null;

        while ((System.currentTimeMillis() - startTime) / 1000 < maxWaitInSeconds) {
            hwnd = User32.INSTANCE.FindWindow(null, windowTitle);

            if (hwnd != null) {
                boolean responsive = isWindowResponsive(hwnd);
                TestExecutionLogger.info("📶 Found HWND: " + hwnd + " | Responsive: " + responsive);
                if (responsive) {
                    TestExecutionLogger.info("✅ " + windowTitle + " window is responsive.");
                    return hwnd;
                } else {
                    TestExecutionLogger.info("⚠️ HWND found, but not responsive. Waiting...");
                }
            } else {
                TestExecutionLogger.info("❌ Window not found yet.");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        if (hwnd != null) {
            TestExecutionLogger.info("⚠️ " + windowTitle + " window found but unresponsive. Proceeding with HWND.");
            return hwnd; // Proceed with hwnd anyway
        }

        throw new RuntimeException("❌ " + windowTitle + " window not found after " + maxWaitInSeconds + " seconds.");
    }

    /**
     * Returns the title of the current foreground (active) window.
     */
    public static String getForegroundWindowTitle() {
        HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        if (hwnd == null) {
            return null;
        }

        char[] buffer = new char[1024];
        User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
        return Native.toString(buffer).trim();
    }

    /**
     * Utility method to list all open window titles (for debugging).
     */
    public static void listAllWindows() {
        User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
            public boolean callback(HWND hwnd, Pointer data) {
                char[] buffer = new char[1024];
                User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
                String windowText = Native.toString(buffer).trim();
                if (!windowText.isEmpty()) {
                    TestExecutionLogger.info("🪟 Window Title: " + windowText);
                }
                return true;
            }
        }, null);
    }

    /**
     * Finds a window by partial title match and returns its HWND. Returns null if not found.
     */
    public static HWND findWindowByPartialTitle(String partialTitle) {
        final HWND[] match = new HWND[1];
        User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
            public boolean callback(HWND hwnd, Pointer data) {
                char[] buffer = new char[1024];
                User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
                String title = Native.toString(buffer).trim();
                if (title.contains(partialTitle)) {
                    match[0] = hwnd;
                    return false; // stop
                }
                return true;
            }
        }, null);
        return match[0];
    }

    /**
     * Maximizes a window by its exact title using Windows API.
     * Uses User32.ShowWindow with SW_MAXIMIZE command.
     * 
     * @param windowTitle The exact title of the window to maximize
     * @return true if the window was found and maximized, false otherwise
     */
    public static boolean maximizeWindowByTitle(String windowTitle) {
        final int SW_MAXIMIZE = 3;
        
        HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, SW_MAXIMIZE);
            TestExecutionLogger.info("✅ Maximized window with title: " + windowTitle);
            return true;
        } else {
            TestExecutionLogger.warning("❌ Window not found with title: " + windowTitle);
            return false;
        }
    }

    /**
     * Maximizes a window by partial title match using Windows API.
     * 
     * @param partialTitle Partial title of the window to maximize
     * @return true if the window was found and maximized, false otherwise
     */
    public static boolean maximizeWindowByPartialTitle(String partialTitle) {
        final int SW_MAXIMIZE = 3;
        
        HWND hwnd = findWindowByPartialTitle(partialTitle);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, SW_MAXIMIZE);
            TestExecutionLogger.info("✅ Maximized window containing title: " + partialTitle);
            return true;
        } else {
            TestExecutionLogger.warning("❌ Window not found containing title: " + partialTitle);
            return false;
        }
    }

    /**
     * Brings a window to foreground and maximizes it by title.
     * 
     * @param windowTitle The exact title of the window
     * @return true if successful, false otherwise
     */
    public static boolean bringToFrontAndMaximize(String windowTitle) {
        final int SW_MAXIMIZE = 3;
        
        HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd != null) {
            User32.INSTANCE.SetForegroundWindow(hwnd);
            User32.INSTANCE.ShowWindow(hwnd, SW_MAXIMIZE);
            TestExecutionLogger.info("✅ Brought to front and maximized window: " + windowTitle);
            return true;
        } else {
            TestExecutionLogger.warning("❌ Window not found: " + windowTitle);
            return false;
        }
    }
}
