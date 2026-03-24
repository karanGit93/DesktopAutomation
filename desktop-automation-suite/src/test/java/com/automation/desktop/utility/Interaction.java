package com.automation.desktop.utility;

import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.xml.sax.Locator;

import com.automation.desktop.base.BaseSetup;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;

/**
 * Interaction — the single entry-point for every UI action in the framework.
 * All interactions (click, type, scroll, drag, keyboard, element queries, OS operations)
 * go through {@code performAction(...)}.
 *
 * <pre>
 *   performAction(driver, XPATH, CLICK, locator);                          // click
 *   performAction(driver, NAME, SET, locator, "hello");                    // set value
 *   performAction(driver, srcStrategy, srcLoc, tgtStrategy, tgtLoc, DRAG_AND_DROP); // drag
 *   performAction(driver, DISMISS);                                        // driver-level
 *   performAction(XPATH, CLICK, locator);                                  // convenience (uses appDriver)
 * </pre>
 */
public class Interaction extends BaseSetup {
	
	// ========== ENUMS ==========

	/** Strategy used to locate a UI element. */
	public enum LocatorStrategy {
		NAME,
		XPATH,
		TAGNAME
	}

	/** Every possible action/interaction type. Grouped by category — see inline comments. */
	public enum InteractionType {
		// --- Click variants ---
		CLICK("Clicking element"),
		ACTION_CLICK("Action class click (Double click) on element."),
		CLICK_DROPDOWN("Clicking on dropdown element"),
		RIGHT_END_CLICK("Click on the Right End of the Element"),
		CO_ORDINATE_CLICK("Click using element co-ordinate"),
		COORDINATE_CLICK_XY("Click on Element using both X and Y coordinate"),
		DOUBLE_CLICK_WITH_COORDINATE("Double click with coordinate"),
		SHIFT_CLICK_WITH_COORDINATE("Shift + Click with coordinate for multi-select"),
		ABSOLUTE_COORDINATE_CLICK("Click using cordinates"),

		// --- Key-press variants ---
		TAB("Pressing tab"),
		TAB_OFF("Performing tab off"),
		ENTER("Pressing enter"),
		ESCAPE("Pressing escape"),
		DELETE("Pressing Delete"),
		SPACEBAR("Pressing spacebar"),
		LEFT("Pressing left"),
		RIGHT_ARROW("Pressing right arrow"),
		DOWN_ARROW("Pressing down arrow Key"),
		UP_ARROW("Pressing up arrow Key"),
		PAGE_DOWN("Page Down"),
		PAGE_UP("Page Up"),
		CONTROL_SPACE("This simulates Ctrl + Space on the target element"),

		// --- Value / text ---
		SET("Setting value"),
		SET_WITHOUT_CLEAR("Setting value without clearing"),
		SEND_KEYS("Sending keys to element"),
		CLEAR("Clearing element"),
		CLIPBOARD_COPY_PASTE("Copy text in clipboard and paste."),

		// --- Element queries ---
		GET_TEXT("Getting element text"),
		GET_TAG_NAME("Getting element tag name"),
		GET_ATTRIBUTE("Getting element attribute"),
		IS_DISPLAYED("Checking if element is displayed"),
		IS_ENABLED("Checking if element is enabled"),
		IS_SELECTED("Checking if element is selected"),
		IS_VISIBLE("Check the visibility of element"),
		IS_CHECKED("Check if checkbox is checked using Toggle.ToggleState attribute"),
		IS_UNCHECKED("Check if checkbox is unchecked using Toggle.ToggleState attribute"),
		IS_ELEMENT_PRESENT("Check if element is present - returns boolean"),
		IS_ELEMENT_PRESENT_WITH_SCROLL("Scroll and check if element is present - returns boolean"),

		// --- Mouse / hover ---
		MOUSE_RIGHT_CLICK("Mouse right click"),
		HOVER("Hover over element"),
		HOVER_WITH_COORDINATE("Hover over element at specified coordinate to get tooltip"),

		// --- Scroll ---
		SCROLL_DOWN("Scroll down the element"),
		SCROLL_UP("Scroll up the element"),
		SCROLL_AND_SELECT("Scroll to top, then scroll down until element is visible and select it"),
		SCROLL_AND_SELECT_WITH_END_DETECTION("Scroll to top, then scroll down using scrollbar end detection until element is visible and select it"),

		// --- Drag-and-drop ---
		DRAG_AND_DROP("Drag and Drop element"),
		DRAG_DROP_WITH_COORDINATE("Drag and Drop element with coordinate"),
		DRAG_AND_DROP_ISIDE_ELEMENT("Drag and Drop inside element"),
		DRAG_AND_DROP_FROM_BOTTOM("Drag and Drop element from bottom"),
		DRAG_AND_DROP_TESTCASE("Drag and Drop testcase"),

		// --- Driver-level (no locator needed) ---
		DISMISS("Dismiss / close by pressing ESC key (driver-level, no locator)"),
		SELECT_ALL("Select all content using Ctrl+A (driver-level, no locator)"),
		SELECT_ALL_AND_DELETE("Select all content and delete it using Ctrl+A then Delete (driver-level, no locator)"),
		DRIVER_SCROLL_PAGE_DOWN("Scroll page down via Actions on driver (no locator)"),
		CONTEXT_CLICK_AT_COORDINATE("Context-click at coordinate on element (raw Actions)"),
		UNDO("Undo last action using Ctrl+Z (driver-level, no locator)"),
		REDO("Redo last undone action using Ctrl+Y (driver-level, no locator)"),
		OPEN_SYSTEM_MENU("Open window system menu using Alt+Space (driver-level, no locator)"),
		MAXIMIZE_WINDOW("Maximize the current window using Alt+Space then X (driver-level, no locator)"),
		MINIMIZE_WINDOW("Minimize the current window using Alt+Space then N (driver-level, no locator)"),
		RESTORE_WINDOW("Restore the current window using Alt+Space then R (driver-level, no locator)"),

		// --- Element queries (count) ---
		GET_ELEMENT_COUNT("Return the count of elements matching a locator"),

		// --- Window management (driver-level) ---
		SWITCH_TO_WINDOW("Switch driver focus to a window by handle (pass handle as value)"),
		GET_WINDOW_HANDLES("Return all available window handles as Set<String>"),
		GET_WINDOW_TITLE("Return the title of the currently focused window"),
		GET_WINDOW_SIZE("Return the current window dimensions"),
		MINIMIZE_ALL_WINDOWS("Minimize all desktop windows (toggle desktop via Shell.Application)"),
		CLOSE_FILE_EXPLORER("Close all File Explorer windows without killing the shell"),

		// --- OS / System operations (driver-level, value = process name or path) ---
		KILL_PROCESS("Kill a Windows process by name (pass process name as value, e.g. 'chrome.exe')"),
		IS_PROCESS_RUNNING("Check if a Windows process is running (pass process name as value) - returns boolean"),
		DELETE_FILE("Delete a specific file (pass file path as value) - returns boolean"),
		DELETE_FOLDER("Delete a folder and all its contents recursively (pass folder path as value) - returns boolean"),
		DELETE_FILES_IN_FOLDER("Delete all files in a folder but keep the folder (pass folder path as value) - returns count");

		private final String description;

		InteractionType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	// ========== CONSOLIDATED PUBLIC API METHODS ==========

	/** Single-element action with default timeout, no value. */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			InteractionType type,
			String locator) {
		return performAction(driver, strategy, type, locator, Optional.empty(), Optional.of(Waits.getDefaultWaitTime()));
	}

	/** Single-element action with a value (SET, GET_ATTRIBUTE, SEND_KEYS, coordinate-based, etc.). */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			String value) {
		return performAction(driver, strategy, type, locator, Optional.ofNullable(value), Optional.of(Waits.getDefaultWaitTime()));
	}

	/** Single-element action with custom timeout (seconds), no value. */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			int timeoutInSeconds) {
		return performAction(driver, strategy, type, locator, Optional.empty(), Optional.of(timeoutInSeconds));
	}

	/** Single-element action with Optional timeout (legacy delegation target). */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			Optional<Integer> timeoutInSeconds) {
		return performAction(driver, strategy, type, locator, Optional.empty(), timeoutInSeconds);
	}

	/** Single-element action with X and Y coordinate values. */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			String xValue,
			String yValue,
			Optional<Integer> timeoutInSeconds) {

		validateInputs(driver, strategy, type, locator);
		TestExecutionLogger.logStep("Starting to perform action on :" + type + " with locator type: " + strategy
				+ " and locator: " + locator);

		WindowsElement element = findElementSafely(driver, strategy, locator, timeoutInSeconds);

		try {
			switch (type) {
				case COORDINATE_CLICK_XY:
					coOrdinateClickXY(element, Integer.parseInt(xValue), Integer.parseInt(yValue));
					return null;

				case CONTEXT_CLICK_AT_COORDINATE:
					contextClickAtCoordinate(element, Integer.parseInt(xValue), Integer.parseInt(yValue));
					return null;

				default:
					throw new IllegalArgumentException(
							"Unsupported interaction type for (x, y) overload: " + type);
			}
		} catch (Exception e) {
			String error = String.format("Failed to perform %s: %s", type.getDescription(), e.getMessage());
			TestExecutionLogger.error(error);
			throw new RuntimeException(error, e);
		}
	}

	/** CORE single-element overload — all simpler overloads delegate here. */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			Optional<String> value,
			Optional<Integer> timeoutInSeconds) {

		TestExecutionLogger.logStep("Starting to perform action: " + type
				+ " with locator type: " + strategy
				+ " and locator: " + locator
				+ (value.isPresent() ? " and value: " + value.get() : "")
				+ " and using timeout of: " + timeoutInSeconds.orElse(Waits.getDefaultWaitTime()) + " seconds");

		validateInputs(driver, strategy, type, locator);

		// --- Interactions that do their own element search (scroll-based / presence checks) ---
		if (type == InteractionType.IS_ELEMENT_PRESENT) {
			return isElementPresentCheck(driver, strategy, locator);
		}
		if (type == InteractionType.IS_ELEMENT_PRESENT_WITH_SCROLL) {
			Integer maxRetryOverride = parseOptionalInt(value.orElse(null));
			return isElementPresentWithScrollCheck(driver, strategy, locator, maxRetryOverride);
		}
		if (type == InteractionType.SCROLL_AND_SELECT) {
			return wrapScrollAction(() -> scrollAndSelect(driver, strategy, locator), type);
		}
		if (type == InteractionType.SCROLL_AND_SELECT_WITH_END_DETECTION) {
			return wrapScrollAction(() -> scrollAndSelectWithEndDetection(driver, strategy, locator), type);
		}

		// --- Standard path: find element, then interact ---
		WindowsElement element = findElementSafely(driver, strategy, locator, timeoutInSeconds);

		try {
			return executeInteraction(element, type, locator, strategy, value.orElse(null));
		} catch (Exception e) {
			String error = String.format("Failed to perform %s: %s", type.getDescription(), e.getMessage());
			TestExecutionLogger.error(error + " Exception: " + e.getMessage());
			throw new RuntimeException(error, e);
		}
	}

	// ========== DUAL-ELEMENT OVERLOADS (DRAG & DROP) ==========

	/** Two-element action with default timeout. */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type) {
		return performAction(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator,
				type, Optional.of(Waits.getDefaultWaitTime()));
	}

	/** Two-element action with Y-offset and default timeout. */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type,
			String y) {
		return performAction(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator,
				type, y, Optional.of(Waits.getDefaultWaitTime()));
	}

	/** Two-element action with custom timeout (no Y-offset). */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type,
			Optional<Integer> timeoutInSeconds) {

		validateDualInputs(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator);
		TestExecutionLogger.logStep(String.format("Performing %s from [%s: %s] to [%s: %s]",
				type.getDescription(), sourceStrategy, sourceLocator, targetStrategy, targetLocator));

		WindowsElement source = findElementSafely(driver, sourceStrategy, sourceLocator, timeoutInSeconds);
		WindowsElement target = findElementSafely(driver, targetStrategy, targetLocator, timeoutInSeconds);

		return executeDualInteraction(source, target, type, null);
	}

	/** Two-element action with Y-offset and custom timeout. */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type,
			String y,
			Optional<Integer> timeoutInSeconds) {

		validateDualInputs(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator);
		TestExecutionLogger.logStep(String.format("Performing %s from [%s: %s] to [%s: %s]",
				type.getDescription(), sourceStrategy, sourceLocator, targetStrategy, targetLocator));

		WindowsElement source = findElementSafely(driver, sourceStrategy, sourceLocator, timeoutInSeconds);
		WindowsElement target = findElementSafely(driver, targetStrategy, targetLocator, timeoutInSeconds);

		return executeDualInteraction(source, target, type, y);
	}

	// ========== DRIVER-LEVEL ACTIONS (NO LOCATOR) ==========

	/** Driver-level action — no locator needed (e.g. DISMISS, SELECT_ALL, UNDO, REDO). */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			InteractionType type) {

		if (driver == null) {
			throw new IllegalArgumentException("Driver cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("Interaction type cannot be null");
		}
		TestExecutionLogger.logStep("Performing driver-level action: " + type.getDescription());

		try {
			switch (type) {
				case DISMISS:
					robotKeyPress(KeyEvent.VK_ESCAPE);
					TestExecutionLogger.info("DISMISS: Pressed ESC key via Robot.");
					return null;

				case SELECT_ALL:
					robotCtrlKey(KeyEvent.VK_A);
					TestExecutionLogger.info("SELECT_ALL: Pressed Ctrl+A via Robot.");
					return null;

				case SELECT_ALL_AND_DELETE:
					robotCtrlKey(KeyEvent.VK_A);
					robotKeyPress(KeyEvent.VK_DELETE);
					TestExecutionLogger.info("SELECT_ALL_AND_DELETE: Pressed Ctrl+A then Delete via Robot.");
					return null;

				case DRIVER_SCROLL_PAGE_DOWN:
					new Actions(driver).sendKeys(Keys.PAGE_DOWN).perform();
					TestExecutionLogger.info("DRIVER_SCROLL_PAGE_DOWN: Sent PAGE_DOWN via Actions on driver.");
					return null;

				case UNDO:
					robotCtrlKey(KeyEvent.VK_Z);
					TestExecutionLogger.info("UNDO: Pressed Ctrl+Z via Robot.");
					return null;

				case REDO:
					robotCtrlKey(KeyEvent.VK_Y);
					TestExecutionLogger.info("REDO: Pressed Ctrl+Y via Robot.");
					return null;

				case OPEN_SYSTEM_MENU:
					robotAltKey(KeyEvent.VK_SPACE);
					TestExecutionLogger.info("OPEN_SYSTEM_MENU: Pressed Alt+Space via Robot.");
					return null;

				case MAXIMIZE_WINDOW:
					robotAltKey(KeyEvent.VK_SPACE);
					Thread.sleep(300);
					new Actions(driver).sendKeys("x").perform();
					TestExecutionLogger.info("MAXIMIZE_WINDOW: Maximized window using Alt+Space then X.");
					return null;

				case MINIMIZE_WINDOW:
					robotAltKey(KeyEvent.VK_SPACE);
					Thread.sleep(300);
					new Actions(driver).sendKeys("n").perform();
					TestExecutionLogger.info("MINIMIZE_WINDOW: Minimized window using Alt+Space then N.");
					return null;

				case RESTORE_WINDOW:
					robotAltKey(KeyEvent.VK_SPACE);
					Thread.sleep(300);
					new Actions(driver).sendKeys("r").perform();
					TestExecutionLogger.info("RESTORE_WINDOW: Restored window using Alt+Space then R.");
					return null;

				default:
					throw new IllegalArgumentException("Unsupported driver-level interaction type: " + type
							+ ". Use an overload that accepts a locator for element-based interactions.");
			}
		} catch (Exception e) {
			String error = String.format("Failed to perform driver-level %s: %s",
					type.getDescription(), e.getMessage());
			TestExecutionLogger.error(error);
			throw new RuntimeException(error, e);
		}
	}

	/** Driver-level action with a value but no locator (OS/system ops, window management). */
	public static Object performAction(
			WindowsDriver<WindowsElement> driver,
			InteractionType type,
			String value) {

		if (driver == null) {
			throw new IllegalArgumentException("Driver cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("Interaction type cannot be null");
		}
		TestExecutionLogger.logStep("Performing driver-level action: " + type.getDescription()
				+ (value != null ? " with value: " + value : ""));

		try {
			switch (type) {

				// --- Element query (count) ---

				case GET_ELEMENT_COUNT:
					requireValue(value, "GET_ELEMENT_COUNT");
					// value is treated as an XPath locator by default
					int count = driver.findElementsByXPath(value).size();
					TestExecutionLogger.info("GET_ELEMENT_COUNT: Found " + count + " elements matching: " + value);
					return count;

				// --- Window management ---

				case SWITCH_TO_WINDOW:
					requireValue(value, "SWITCH_TO_WINDOW");
					driver.switchTo().window(value);
					TestExecutionLogger.info("SWITCH_TO_WINDOW: Switched to window handle: " + value);
					return null;

				case GET_WINDOW_HANDLES:
					Set<String> handles = driver.getWindowHandles();
					TestExecutionLogger.info("GET_WINDOW_HANDLES: Found " + handles.size() + " window handles.");
					return handles;

				case GET_WINDOW_TITLE:
					String title = driver.getTitle();
					TestExecutionLogger.info("GET_WINDOW_TITLE: Current window title: " + title);
					return title;

				case GET_WINDOW_SIZE:
					org.openqa.selenium.Dimension size = driver.manage().window().getSize();
					TestExecutionLogger.info("GET_WINDOW_SIZE: " + size.getWidth() + "x" + size.getHeight());
					return size;

				case MINIMIZE_ALL_WINDOWS:
					executeCommand("powershell -command \"(New-Object -ComObject Shell.Application).ToggleDesktop()\"");
					TestExecutionLogger.info("MINIMIZE_ALL_WINDOWS: Toggled desktop (minimized all windows).");
					return null;

				case CLOSE_FILE_EXPLORER:
					executeCommand("powershell -command \"(New-Object -ComObject Shell.Application).Windows() | ForEach-Object { $_.Quit() }\"");
					TestExecutionLogger.info("CLOSE_FILE_EXPLORER: Closed all File Explorer windows.");
					return null;

				// --- OS / System operations ---

				case KILL_PROCESS:
					requireValue(value, "KILL_PROCESS");
					return killProcessByName(value);

				case IS_PROCESS_RUNNING:
					requireValue(value, "IS_PROCESS_RUNNING");
					return checkIfProcessRunning(value);

				case DELETE_FILE:
					requireValue(value, "DELETE_FILE");
					return deleteFileByPath(value);

				case DELETE_FOLDER:
					requireValue(value, "DELETE_FOLDER");
					return deleteFolderByPath(value);

				case DELETE_FILES_IN_FOLDER:
					requireValue(value, "DELETE_FILES_IN_FOLDER");
					return deleteFilesInFolder(value);

				default:
					// Fall through to the no-value overload for types that don't need a value
					return performAction(driver, type);
			}
		} catch (Exception e) {
			String error = String.format("Failed to perform %s: %s",
					type.getDescription(), e.getMessage());
			TestExecutionLogger.error(error);
			throw new RuntimeException(error, e);
		}
	}

	// ========== DEFAULT DRIVER (appDriver) OVERLOADS ==========
	// Mirror every overload above but omit the driver param, using appDriver automatically.

	/** @see #performAction(WindowsDriver, LocatorStrategy, InteractionType, String) */
	public static Object performAction(
			LocatorStrategy strategy,
			InteractionType type,
			String locator) {
		return performAction(appDriver, strategy, type, locator);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, InteractionType, String, String) */
	public static Object performAction(
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			String value) {
		return performAction(appDriver, strategy, type, locator, value);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, InteractionType, String, int) */
	public static Object performAction(
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			int timeoutInSeconds) {
		return performAction(appDriver, strategy, type, locator, timeoutInSeconds);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, InteractionType, String, Optional) */
	public static Object performAction(
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			Optional<Integer> timeoutInSeconds) {
		return performAction(appDriver, strategy, type, locator, timeoutInSeconds);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, InteractionType, String, String, String, Optional) */
	public static Object performAction(
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			String xValue,
			String yValue,
			Optional<Integer> timeoutInSeconds) {
		return performAction(appDriver, strategy, type, locator, xValue, yValue, timeoutInSeconds);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, InteractionType, String, Optional, Optional) */
	public static Object performAction(
			LocatorStrategy strategy,
			InteractionType type,
			String locator,
			Optional<String> value,
			Optional<Integer> timeoutInSeconds) {
		return performAction(appDriver, strategy, type, locator, value, timeoutInSeconds);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, String, LocatorStrategy, String, InteractionType) */
	public static Object performAction(
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type) {
		return performAction(appDriver, sourceStrategy, sourceLocator, targetStrategy, targetLocator, type);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, String, LocatorStrategy, String, InteractionType, String) */
	public static Object performAction(
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type,
			String y) {
		return performAction(appDriver, sourceStrategy, sourceLocator, targetStrategy, targetLocator, type, y);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, String, LocatorStrategy, String, InteractionType, Optional) */
	public static Object performAction(
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type,
			Optional<Integer> timeoutInSeconds) {
		return performAction(appDriver, sourceStrategy, sourceLocator, targetStrategy, targetLocator,
				type, timeoutInSeconds);
	}

	/** @see #performAction(WindowsDriver, LocatorStrategy, String, LocatorStrategy, String, InteractionType, String, Optional) */
	public static Object performAction(
			LocatorStrategy sourceStrategy,
			String sourceLocator,
			LocatorStrategy targetStrategy,
			String targetLocator,
			InteractionType type,
			String y,
			Optional<Integer> timeoutInSeconds) {
		return performAction(appDriver, sourceStrategy, sourceLocator, targetStrategy, targetLocator,
				type, y, timeoutInSeconds);
	}

	/** @see #performAction(WindowsDriver, InteractionType) */
	public static Object performAction(InteractionType type) {
		return performAction(appDriver, type);
	}

	/** @see #performAction(WindowsDriver, InteractionType, String) */
	public static Object performAction(InteractionType type, String value) {
		return performAction(appDriver, type, value);
	}

	// ========== INPUT VALIDATION ==========

	private static void validateInputs(WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy, InteractionType type, String locator) {
		if (driver == null) {
			TestExecutionLogger.error("Driver cannot be null");
			throw new IllegalArgumentException("Driver cannot be null");
		}
		if (strategy == null) {
			TestExecutionLogger.error("Locator strategy cannot be null");
			throw new IllegalArgumentException("Locator strategy cannot be null");
		}
		if (type == null) {
			TestExecutionLogger.error("Interaction type cannot be null");
			throw new IllegalArgumentException("Interaction type cannot be null");
		}
		if (locator == null || locator.isEmpty()) {
			TestExecutionLogger.error("Locator cannot be null or empty");
			throw new IllegalArgumentException("Locator cannot be null or empty");
		}
	}

	private static void validateDualInputs(WindowsDriver<WindowsElement> driver,
			LocatorStrategy srcStrategy, String srcLocator,
			LocatorStrategy tgtStrategy, String tgtLocator) {
		if (driver == null)
			throw new IllegalArgumentException("Driver cannot be null");
		if (srcStrategy == null || srcLocator == null || srcLocator.isEmpty())
			throw new IllegalArgumentException("Source locator or strategy cannot be null or empty");
		if (tgtStrategy == null || tgtLocator == null || tgtLocator.isEmpty())
			throw new IllegalArgumentException("Target locator or strategy cannot be null or empty");
	}

	private static void requireValue(String value, String interactionName) {
		if (value == null || value.isEmpty()) {
			TestExecutionLogger.error(interactionName + ": value cannot be null or empty");
			throw new IllegalArgumentException("Value cannot be null or empty for " + interactionName);
		}
	}

	// ========== ELEMENT FINDING ==========

	/** Find a single element with wait, wrapping exceptions into RuntimeException. */
	private static WindowsElement findElementSafely(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			String locator,
			Optional<Integer> timeoutInSeconds) {
		try {
			return findElement(driver, strategy, locator, timeoutInSeconds);
		} catch (NoSuchElementException e) {
			String error = String.format("Element not found with %s: %s", strategy, locator);
			TestExecutionLogger.error(error + " Exception: " + e.getMessage());
			throw new RuntimeException(error, e);
		} catch (TimeoutException e) {
			String error = String.format("Timeout waiting for element with %s: %s", strategy, locator);
			TestExecutionLogger.error(error + " Exception: " + e.getMessage());
			throw new RuntimeException(error, e);
		} catch (IllegalArgumentException e) {
			String error = String.format("Invalid argument: %s", e.getMessage());
			TestExecutionLogger.error(error + " Exception: " + e.getMessage());
			throw new RuntimeException(error, e);
		} catch (Exception e) {
			String error = String.format("Failed to find element with %s: %s", strategy, e.getMessage());
			TestExecutionLogger.error(error + " Exception: " + e.getMessage());
			throw new RuntimeException(error, e);
		}
	}

	private static WindowsElement findElement(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			String locator,
			Optional<Integer> timeoutInSeconds) {

		TestExecutionLogger.info("Finding element with locator type: " + strategy + " and locator: " + locator
				+ " with timeout: " + timeoutInSeconds.orElse(Waits.getDefaultWaitTime()) + " seconds");
		try {
			WindowsElement element = null;
			switch (strategy) {
				case NAME:
					element = Waits.waitForElementByName(driver, locator, timeoutInSeconds);
					break;
				case XPATH:
					element = Waits.waitForElementByXpath(driver, locator, timeoutInSeconds);
					break;
				case TAGNAME:
					element = Waits.waitForElementByTagName(driver, locator, timeoutInSeconds);
					break;
			}

			if (element == null) {
				TestExecutionLogger.error("Element not found with " + strategy + ": " + locator);
				throw new NoSuchElementException("Element not found with " + strategy + ": " + locator);
			} else {
				TestExecutionLogger.info("Element found successfully with " + strategy + ": " + locator);
			}

			if (!element.isDisplayed()) {
				TestExecutionLogger.error("Element is not displayed: " + locator);
				throw new NoSuchElementException("Element is not displayed: " + locator);
			}

			return element;
		} catch (TimeoutException e) {
			TestExecutionLogger.error("Timeout waiting for element or failed to locate the element with "
					+ strategy + ": " + locator + " Exception: " + e.getMessage());
			throw new RuntimeException("Timeout waiting for element: " + locator, e);
		}
	}

	/** Find all matching elements without waiting (used for presence checks during scrolling). */
	private static List<WindowsElement> findAllElements(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			String locator) {
		switch (strategy) {
			case NAME:
				return driver.findElementsByName(locator);
			case XPATH:
				return driver.findElementsByXPath(locator);
			case TAGNAME:
				return driver.findElementsByTagName(locator);
			default:
				throw new IllegalArgumentException("Unsupported locator strategy: " + strategy);
		}
	}

	// ========== CORE DISPATCHER — SINGLE ELEMENT ==========

	/** Central switch for all single-element interactions. Add new InteractionType cases here. */
	private static Object executeInteraction(
			WindowsElement element,
			InteractionType type,
			String locator,
			LocatorStrategy strategy,
			String value) {

		switch (type) {

			// --- Click Variants ---

			case CLICK:
				clickableCheck(element);
				TestExecutionLogger.info("Clicked on element with " + strategy + ": " + locator);
				return null;

			case ACTION_CLICK:
				actionClick(element);
				return null;

			case CLICK_DROPDOWN:
				new Actions(appDriver).moveToElement(element).click().perform();
				TestExecutionLogger.info("Clicked on dropdown element: " + locator);
				return null;

			case RIGHT_END_CLICK:
				clickRightEnd(element);
				TestExecutionLogger.info("Right end clicked on element: " + locator);
				return null;

			case CO_ORDINATE_CLICK:
				coOrdinateClick(element, Integer.parseInt(value));
				return null;

			case COORDINATE_CLICK_XY:
				// When called via the core overload (value = "x,y" CSV)
				requireValue(value, "COORDINATE_CLICK_XY");
				String[] xy = value.split(",");
				coOrdinateClickXY(element, Integer.parseInt(xy[0].trim()), Integer.parseInt(xy[1].trim()));
				return null;

			case DOUBLE_CLICK_WITH_COORDINATE:
				doubleClickWithCoordinate(element, value);
				return null;

			case SHIFT_CLICK_WITH_COORDINATE:
				shiftClickWithCoordinate(element, Integer.parseInt(value));
				TestExecutionLogger.info("Performed SHIFT + Click with coordinate on element: "
						+ locator + " at Y offset: " + value);
				return null;

			case ABSOLUTE_COORDINATE_CLICK:
				coOrdinateClick(element, Integer.parseInt(value));
				return null;

			// --- Key-Press Variants ---

			case TAB:
				pressKey(element, Keys.TAB, "TAB", locator);
				return null;

			case TAB_OFF:
				element.sendKeys(Keys.SHIFT, Keys.TAB);
				TestExecutionLogger.info("Performed TAB_OFF on element: " + locator);
				return null;

			case ENTER:
				pressKey(element, Keys.ENTER, "ENTER", locator);
				return null;

			case ESCAPE:
				pressKey(element, Keys.ESCAPE, "ESCAPE", locator);
				return null;

			case DELETE:
				pressKey(element, Keys.DELETE, "DELETE", locator);
				return null;

			case SPACEBAR:
				pressKey(element, Keys.SPACE, "SPACEBAR", locator);
				return null;

			case LEFT:
				pressKey(element, Keys.LEFT, "LEFT", locator);
				return null;

			case RIGHT_ARROW:
				pressKey(element, Keys.ARROW_RIGHT, "RIGHT_ARROW", locator);
				return null;

			case DOWN_ARROW:
				pressKey(element, Keys.ARROW_DOWN, "DOWN_ARROW", locator);
				return null;

			case UP_ARROW:
				pressKey(element, Keys.ARROW_UP, "UP_ARROW", locator);
				return null;

			case PAGE_DOWN:
				pressKey(element, Keys.PAGE_DOWN, "PAGE_DOWN", locator);
				return null;

			case PAGE_UP:
				pressKey(element, Keys.PAGE_UP, "PAGE_UP", locator);
				return null;

			case CONTROL_SPACE:
				new Actions(appDriver).sendKeys(Keys.chord(Keys.CONTROL, Keys.SPACE)).perform();
				TestExecutionLogger.info("Simulated Ctrl + Space on the target element: " + locator);
				return null;

			// --- Value / Text ---

			case SET:
				requireValue(value, "SET");
				setElementValueWithRetry(element, value);
				TestExecutionLogger.info("Set value in element: " + locator + " - " + value);
				return null;

			case SET_WITHOUT_CLEAR:
				setElementValueWithoutClear(element, value);
				TestExecutionLogger.info("Set value in element: " + locator + " - " + value);
				return null;

			case SEND_KEYS:
				requireValue(value, "SEND_KEYS");
				try {
					element.sendKeys(value);
					TestExecutionLogger.info("Keys sent successfully: " + value);
				} catch (Exception e) {
					TestExecutionLogger.error("Failed to send keys: " + value + " Exception: " + e.getMessage());
					throw new RuntimeException("Failed to send keys: " + value, e);
				}
				return null;

			case CLEAR:
				try {
					element.clear();
					TestExecutionLogger.info("Element cleared successfully");
				} catch (Exception e) {
					TestExecutionLogger.error("Failed to clear element: " + locator
							+ " Exception: " + e.getMessage());
					throw new RuntimeException("Failed to clear element", e);
				}
				return null;

			case CLIPBOARD_COPY_PASTE:
				StringSelection stringSelection = new StringSelection(value);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
				TestExecutionLogger.info("Copied '" + value + "' to clipboard.");
				element.sendKeys(Keys.chord(Keys.CONTROL, "v"));
				TestExecutionLogger.info("Pasted '" + value + "' into element: " + locator);
				return null;

			// --- Element Queries ---

			case IS_DISPLAYED:
				boolean isDisplayed = element.isDisplayed();
				TestExecutionLogger.info("Checked if element is displayed: " + locator + " - " + isDisplayed);
				return isDisplayed;

			case IS_ENABLED:
				boolean isEnabled = element.isEnabled();
				TestExecutionLogger.info("Checked if element is enabled: " + locator + " - " + isEnabled);
				return isEnabled;

			case IS_SELECTED:
				boolean isSelected = element.isSelected();
				TestExecutionLogger.info("Checked if element is selected: " + locator + " - " + isSelected);
				return isSelected;

			case GET_TEXT:
				String text = element.getText();
				TestExecutionLogger.info("Got text from element: " + locator + " - " + text);
				return text;

			case GET_TAG_NAME:
				String tagName = element.getTagName();
				if (tagName == null || tagName.isEmpty()) {
					TestExecutionLogger.error("Element tag name is empty: " + locator);
					throw new IllegalArgumentException("Element tag name is empty: " + locator);
				}
				TestExecutionLogger.info("Got tag name from element: " + locator + " - " + tagName);
				return tagName;

			case GET_ATTRIBUTE:
				requireValue(value, "GET_ATTRIBUTE");
				TestExecutionLogger.info("Getting attribute '" + value + "' from element: " + locator);
				return element.getAttribute(value);

			case IS_VISIBLE:
				TestExecutionLogger.info(element.getTagName()
						+ ": Checked if element is visible: " + locator + " - " + value);
				return isVisible(element, Integer.parseInt(value));

			case IS_CHECKED:
				String toggleChecked = element.getAttribute("Toggle.ToggleState");
				TestExecutionLogger.info("Checkbox Toggle.ToggleState for " + locator + ": " + toggleChecked);
				boolean checked = "1".equals(toggleChecked);
				TestExecutionLogger.info("IS_CHECKED result: " + checked);
				return checked;

			case IS_UNCHECKED:
				String toggleUnchecked = element.getAttribute("Toggle.ToggleState");
				TestExecutionLogger.info("Checkbox Toggle.ToggleState for " + locator + ": " + toggleUnchecked);
				boolean unchecked = "0".equals(toggleUnchecked);
				TestExecutionLogger.info("IS_UNCHECKED result: " + unchecked);
				return unchecked;

			// --- Mouse / Hover ---

			case MOUSE_RIGHT_CLICK:
				mouseRightClick(element, value);
				return null;

			case HOVER:
				hoverOnElement(element, locator);
				return null;

			case HOVER_WITH_COORDINATE:
				hoverWithCoordinate(element, Integer.parseInt(value));
				TestExecutionLogger.info("Performed hover with coordinate on element: "
						+ locator + " at Y offset: " + value);
				return null;

			// --- Scroll ---

			case SCROLL_DOWN:
				mouseScroll(5);
				TestExecutionLogger.info("Performed SCROLL_DOWN using mouse wheel.");
				return null;

			case SCROLL_UP:
				mouseScroll(-5);
				TestExecutionLogger.info("Performed SCROLL_UP using mouse wheel.");
				return null;

			// --- Context-click at coordinate ---

			case CONTEXT_CLICK_AT_COORDINATE:
				requireValue(value, "CONTEXT_CLICK_AT_COORDINATE");
				contextClickAtCoordinate(element, Integer.parseInt(value));
				return null;

			// --- Default ---

			default:
				TestExecutionLogger.error("Unsupported interaction type: " + type);
				throw new IllegalArgumentException("Unsupported interaction type: " + type);
		}
	}

	// ========== CORE DISPATCHER — DUAL ELEMENT (DRAG & DROP) ==========

	private static Object executeDualInteraction(
			WindowsElement source,
			WindowsElement target,
			InteractionType type,
			String yOffset) {

		switch (type) {
			case DRAG_AND_DROP:
				dragAndDrop(source, target);
				TestExecutionLogger.info("Dragged element from source to target.");
				return null;

			case DRAG_AND_DROP_ISIDE_ELEMENT:
				dragAndDrop(source, target, Integer.parseInt(yOffset));
				TestExecutionLogger.info("Dragged element from source to target (inside element).");
				return null;

			case DRAG_AND_DROP_FROM_BOTTOM:
				dragAndDropFromBottom(source, Integer.parseInt(yOffset), target, 35);
				TestExecutionLogger.info("Dragged element from source to target (from bottom).");
				return null;

			case DRAG_AND_DROP_TESTCASE:
				dragAndDropFromBottom(source, Integer.parseInt(yOffset), target, 20);
				TestExecutionLogger.info("Dragged testcase from source to target.");
				return null;

			default:
				TestExecutionLogger.error("Unsupported dual-element interaction: " + type);
				throw new IllegalArgumentException("Unsupported dual-element interaction: " + type);
		}
	}

	// ========== HELPERS — CLICK ==========

	private static void clickableCheck(WindowsElement element) {
		WebDriverWait wait = new WebDriverWait(appDriver, 60);
		wait.until(ExpectedConditions.elementToBeClickable(element));
		element.click();
	}

	private static void actionClick(WindowsElement element) {
		Waits.waitForFixedDuration(5000);
		WebDriverWait wait = new WebDriverWait(appDriver, 60);
		wait.until(ExpectedConditions.elementToBeClickable(element));

		Actions actions = new Actions(appDriver);
		actions.moveToElement(element)
				.pause(Duration.ofMillis(500))
				.doubleClick()
				.build()
				.perform();
		TestExecutionLogger.info("Action-click performed on element.");
	}

	private static void clickRightEnd(WindowsElement element) {
		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();
		new Actions(appDriver).moveToElement(element, width - 6, height / 2).click().perform();
	}

	// ========== HELPERS — COORDINATE ==========

	private static void coOrdinateClick(WindowsElement element, int y) {
		int offsetX = element.getSize().getWidth() / 2;
		Actions actions = new Actions(appDriver);
		actions.moveToElement(element, offsetX, y).click().perform();
	}

	private static void coOrdinateClickXY(WindowsElement element, int x, int y) {
		int width = element.getSize().getWidth();
		TestExecutionLogger.info("Width of the element is " + width);
		int height = element.getSize().getHeight();
		TestExecutionLogger.info("Height of the element is " + height);

		Actions actions = new Actions(appDriver);
		actions.moveToElement(element, x, y).click().perform();
	}

	private static void doubleClickWithCoordinate(WindowsElement element, String value) {
		int width = element.getSize().getWidth();
		int offsetY = Integer.parseInt(value);
		int offsetX = width / 2;

		TestExecutionLogger.info("Initiating Double click using co-ordinate x=" + offsetX
				+ " : y=" + value + " on element.");
		Actions actions = new Actions(appDriver);
		actions.moveToElement(element, offsetX, offsetY)
				.doubleClick()
				.perform();
		TestExecutionLogger.info("Performed Double click using co-ordinate x=" + offsetX
				+ " : y=" + value + " on element.");
	}

	private static void shiftClickWithCoordinate(WindowsElement element, int y) {
		int offsetX = element.getSize().getWidth() / 2;

		TestExecutionLogger.info("Initiating SHIFT + Click using co-ordinate x=" + offsetX
				+ " : y=" + y + " on element.");
		Actions actions = new Actions(appDriver);
		actions.keyDown(Keys.SHIFT)
				.moveToElement(element, offsetX, y)
				.click()
				.keyUp(Keys.SHIFT)
				.perform();
		TestExecutionLogger.info("Performed SHIFT + Click using co-ordinate x=" + offsetX
				+ " : y=" + y + " on element.");
	}

	/** Context-click (right-click) at a Y-coordinate offset on an element. */
	private static void contextClickAtCoordinate(WindowsElement element, int yOffset) {
		org.openqa.selenium.Point location = element.getLocation();
		int elementX = location.getX();
		int elementY = location.getY();
		int elementWidth = element.getSize().getWidth();
		int clickX = elementX + (elementWidth / 2);
		int clickY = elementY + yOffset;

		Actions actions = new Actions(appDriver);
		actions.moveToElement(element, clickX - 75, clickY)
				.contextClick()
				.perform();
		actions.moveByOffset(-clickX + 75, -clickY).perform();
		TestExecutionLogger.info("Context-clicked at coordinate offset y=" + yOffset + " on element.");
	}

	/** Context-click with both x and y pixel offsets. */
	private static void contextClickAtCoordinate(WindowsElement element, int xOffset, int yOffset) {
		org.openqa.selenium.Point location = element.getLocation();
		int elementX = location.getX();
		int elementY = location.getY();
		int elementWidth = element.getSize().getWidth();
		int clickX = elementX + (elementWidth / 2);
		int clickY = elementY + yOffset;

		Actions actions = new Actions(appDriver);
		actions.moveToElement(element, clickX - xOffset, clickY)
				.contextClick()
				.perform();
		actions.moveByOffset(-(clickX - xOffset), -clickY).perform();
		TestExecutionLogger.info("Context-clicked at coordinate (xOffset=" + xOffset
				+ ", yOffset=" + yOffset + ") on element.");
	}

	// ========== HELPERS — MOUSE / HOVER ==========

	private static void mouseRightClick(WindowsElement element, String value) {
		int offsetX = element.getSize().getWidth() / 2;
		int offsetY = Integer.parseInt(value);
		Actions actions = new Actions(appDriver);
		actions.moveToElement(element, offsetX, offsetY)
				.contextClick()
				.perform();
	}

	private static void hoverOnElement(WindowsElement element, String locator) {
		try {
			org.openqa.selenium.Point elementLocation = element.getLocation();
			org.openqa.selenium.Dimension elementSize = element.getSize();
			int centerX = elementLocation.getX() + (elementSize.getWidth() / 2);
			int centerY = elementLocation.getY() + (elementSize.getHeight() / 2);

			java.awt.Robot robot = new java.awt.Robot();
			robot.mouseMove(centerX, centerY);
			robot.delay(500);
			TestExecutionLogger.info("Hovered over element at coordinates ("
					+ centerX + ", " + centerY + "): " + locator);
		} catch (Exception e) {
			TestExecutionLogger.error("Failed to perform HOVER: " + e.getMessage());
			throw new RuntimeException("Failed to perform HOVER", e);
		}
	}

	private static void hoverWithCoordinate(WindowsElement element, int y) {
		int offsetX = element.getSize().getWidth() / 2;

		TestExecutionLogger.info("Initiating hover using co-ordinate x=" + offsetX
				+ " : y=" + y + " on element.");
		Actions actions = new Actions(appDriver);
		actions.moveToElement(element, offsetX, y).perform();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		TestExecutionLogger.info("Performed hover using co-ordinate x=" + offsetX
				+ " : y=" + y + " on element.");
	}

	// ========== HELPERS — KEY-PRESS ==========

	/** Send a single key to an element and log it. */
	private static void pressKey(WindowsElement element, CharSequence key,
			String keyName, String locator) {
		element.sendKeys(key);
		TestExecutionLogger.info("Pressed " + keyName + " on element: " + locator);
	}

	// ========== HELPERS — ROBOT KEY-PRESS ==========

	/** Press and release a single key via java.awt.Robot. */
	private static void robotKeyPress(int keyEvent) {
		try {
			java.awt.Robot robot = new java.awt.Robot();
			robot.keyPress(keyEvent);
			robot.keyRelease(keyEvent);
		} catch (java.awt.AWTException e) {
			TestExecutionLogger.error("Robot key press failed: " + e.getMessage());
			throw new RuntimeException("Robot key press failed", e);
		}
	}

	/** Press Ctrl + another key via java.awt.Robot. */
	private static void robotCtrlKey(int keyEvent) {
		try {
			java.awt.Robot robot = new java.awt.Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(keyEvent);
			robot.keyRelease(keyEvent);
			robot.keyRelease(KeyEvent.VK_CONTROL);
		} catch (java.awt.AWTException e) {
			TestExecutionLogger.error("Robot Ctrl+key press failed: " + e.getMessage());
			throw new RuntimeException("Robot Ctrl+key press failed", e);
		}
	}

	/** Press Alt + another key via java.awt.Robot. */
	private static void robotAltKey(int keyEvent) {
		try {
			java.awt.Robot robot = new java.awt.Robot();
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(keyEvent);
			robot.keyRelease(keyEvent);
			robot.keyRelease(KeyEvent.VK_ALT);
		} catch (java.awt.AWTException e) {
			TestExecutionLogger.error("Robot Alt+key press failed: " + e.getMessage());
			throw new RuntimeException("Robot Alt+key press failed", e);
		}
	}

	// ========== HELPERS — SCROLL ==========

	/** Scroll using mouse wheel. Positive = down, negative = up. */
	private static void mouseScroll(int clicks) {
		try {
			java.awt.Robot robot = new java.awt.Robot();
			robot.delay(500);
			robot.mouseWheel(clicks);
		} catch (Exception e) {
			TestExecutionLogger.error("Failed to perform mouse scroll: " + e.getMessage());
			throw new RuntimeException("Failed to perform mouse scroll", e);
		}
	}

	/** Scroll aggressively to the top of the page. */
	private static void scrollToTop(java.awt.Robot robot) {
		TestExecutionLogger.info("Scrolling to top of the page...");
		for (int i = 0; i < 10; i++) {
			robot.delay(200);
			robot.mouseWheel(-10);
		}
		TestExecutionLogger.info("Reached top of the page.");
		Waits.waitForFixedDuration(1000);
	}

	/** Wrap a scroll-based action in standard error handling. */
	private static Object wrapScrollAction(Runnable action, InteractionType type) {
		try {
			action.run();
			return null;
		} catch (Exception e) {
			String error = String.format("Failed to perform %s: %s", type.getDescription(), e.getMessage());
			TestExecutionLogger.error(error + " Exception: " + e.getMessage());
			throw new RuntimeException(error, e);
		}
	}

	// ========== HELPERS — DRAG AND DROP ==========

	private static void dragAndDrop(WindowsElement source, WindowsElement target) {
		int offsetX = target.getSize().getWidth() / 2;
		Actions actions = new Actions(appDriver);
		actions.clickAndHold(source)
				.moveToElement(target, offsetX, 20)
				.release()
				.perform();
	}

	private static void dragAndDrop(WindowsElement source, WindowsElement target, int y) {
		int offsetX = target.getSize().getWidth() / 2;
		Actions actions = new Actions(appDriver);
		actions.clickAndHold(source)
				.moveToElement(target, offsetX, y)
				.release()
				.perform();
	}

	private static void dragAndDropFromBottom(WindowsElement source, int sourceY,
			WindowsElement target, int targetY) {
		int sourceOffsetX = source.getSize().getWidth() / 2;
		int targetOffsetX = target.getSize().getWidth() / 2;

		Actions actions = new Actions(appDriver);
		actions.moveToElement(source, sourceOffsetX, sourceY)
				.clickAndHold()
				.moveToElement(target, targetOffsetX, targetY)
				.release()
				.perform();
	}

	// ========== HELPERS — VALUE SETTING ==========

	private static void setElementValueWithRetry(WebElement element, String value) {
		try {
			element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
			element.sendKeys(Keys.DELETE);
			TestExecutionLogger.info("Element cleared successfully");
			Waits.waitForFixedDuration(2000);
			element.click();
			Thread.sleep(500);
			element.sendKeys(value);
			Waits.waitForFixedDuration(1000);
			TestExecutionLogger.info(String.format("Value set: %s", value));
		} catch (Exception e) {
			TestExecutionLogger.error("Failed to set element value: " + value
					+ ". Exception: " + e.getMessage());
			throw new RuntimeException("Failed to set element value: " + value, e);
		}
	}

	private static void setElementValueWithoutClear(WebElement element, String value) {
		try {
			element.sendKeys(value);
			Waits.waitForFixedDuration(1000);
			TestExecutionLogger.info(String.format("Value set: %s", value));
		} catch (Exception e) {
			TestExecutionLogger.error("Failed to set element value: " + value
					+ ". Exception: " + e.getMessage());
			throw new RuntimeException("Failed to set element value: " + value, e);
		}
	}

	// ========== HELPERS — VISIBILITY ==========

	private static boolean isVisible(WindowsElement element, Integer timeoutInSeconds) {
		if (element == null) {
			TestExecutionLogger.warning("Element is null. Cannot check visibility.");
			return false;
		}

		int timeout = (timeoutInSeconds != null) ? timeoutInSeconds : 10;
		long endTime = System.currentTimeMillis() + timeout * 1000L;

		while (System.currentTimeMillis() < endTime) {
			try {
				String isOffscreen = element.getAttribute("IsOffscreen");
				TestExecutionLogger.info("IsOffscreen attribute: " + isOffscreen);
				if ("false".equalsIgnoreCase(isOffscreen)) {
					return true;
				}

				String boundingRect = element.getAttribute("BoundingRectangle");
				TestExecutionLogger.info("BoundingRectangle: " + boundingRect);
				if (boundingRect != null && !boundingRect.trim().equals("[]")) {
					return true;
				}
			} catch (Exception e) {
				TestExecutionLogger.info("Exception while checking visibility: " + e.getMessage());
			}
			Waits.waitForFixedDuration(500);
		}

		TestExecutionLogger.warning("Element did not become visible within " + timeout + " seconds.");
		return false;
	}

	// ========== HELPERS — SCROLL AND SELECT ==========

	private static void scrollAndSelect(WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy, String locator) {
		int maxScrollAttempts = 25;
		int scrollAttempt = 0;

		try {
			java.awt.Robot robot = new java.awt.Robot();

			// Step 1: Scroll to top
			scrollToTop(robot);

			// Step 2: Scroll down until element is found and visible
			TestExecutionLogger.info("SCROLL_AND_SELECT: Now scrolling down to find element: " + locator);
			while (scrollAttempt < maxScrollAttempts) {
				scrollAttempt++;

				try {
					WindowsElement element = findElement(driver, strategy, locator, Optional.of(2));
					if (element != null) {
						String isOffscreen = element.getAttribute("IsOffscreen");
						if ("false".equalsIgnoreCase(isOffscreen)) {
							TestExecutionLogger.info("SCROLL_AND_SELECT: Element found and visible on attempt "
									+ scrollAttempt + ". Clicking...");
							Waits.waitForFixedDuration(500);
							element.click();
							TestExecutionLogger.info("SCROLL_AND_SELECT: Successfully clicked on element: "
									+ locator);
							return;
						} else {
							TestExecutionLogger.info(
									"SCROLL_AND_SELECT: Element found but off-screen, scrolling more...");
						}
					}
				} catch (Exception e) {
					TestExecutionLogger.info("SCROLL_AND_SELECT: Element not found yet on attempt "
							+ scrollAttempt + ", continuing to scroll...");
				}

				TestExecutionLogger.info("SCROLL_AND_SELECT: Scrolling down (attempt "
						+ scrollAttempt + " of " + maxScrollAttempts + ")");
				robot.delay(300);
				robot.mouseWheel(5);
				Waits.waitForFixedDuration(500);
			}

			TestExecutionLogger.error("SCROLL_AND_SELECT: Max scroll attempts reached. Element not found: "
					+ locator);
			throw new RuntimeException("SCROLL_AND_SELECT: Failed to find and select element after "
					+ maxScrollAttempts + " scroll attempts: " + locator);

		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			TestExecutionLogger.error("SCROLL_AND_SELECT: Failed to scroll and select element: "
					+ locator + ". Exception: " + e.getMessage());
			throw new RuntimeException("Failed to scroll and select element: " + locator, e);
		}
	}

	// ========== HELPERS — SCROLL AND SELECT WITH END DETECTION ==========

	/** Scroll to top, then scroll down to find and click element using scrollbar end detection. */
	private static void scrollAndSelectWithEndDetection(WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy, String locator) {
		int maxScrollAttempts = 30;
		int scrollAttempt = 0;
		String scrollBarXPath = "//ScrollBar[@Name='Vertical Scroll Bar']";

		double minScrollValue = 0;
		double maxScrollValue = 0;

		boolean elementFound = false;
		boolean endOfPageReached = false;
		boolean maxAttemptsReached = false;
		WindowsElement foundElement = null;

		try {
			java.awt.Robot robot = new java.awt.Robot();

			// Get reference to scrollbar
			boolean hasScrollBar = false;
			try {
				driver.findElementByXPath(scrollBarXPath);
				hasScrollBar = true;
				TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: Found vertical scrollbar.");
			} catch (Exception e) {
				TestExecutionLogger.warning("SCROLL_AND_SELECT_WITH_END_DETECTION: Could not find scrollbar. "
						+ "Will use scroll attempts without position verification.");
			}

			// ===== STEP 1: SCROLL TO TOP =====
			TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: ===== STEP 1: Scrolling to top =====");
			scrollToTopWithDetection(robot, driver, scrollBarXPath, hasScrollBar);
			Waits.waitForFixedDuration(500);

			// ===== STEP 2: GET SCROLLBAR RANGE =====
			TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: ===== STEP 2: Getting scrollbar range =====");
			if (hasScrollBar) {
				try {
					WindowsElement scrollBar = driver.findElementByXPath(scrollBarXPath);
					String minValueStr = scrollBar.getAttribute("RangeValue.Minimum");
					String maxValueStr = scrollBar.getAttribute("RangeValue.Maximum");
					String currentValueStr = scrollBar.getAttribute("RangeValue.Value");

					TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: Raw scrollbar - Min="
							+ minValueStr + ", Max=" + maxValueStr + ", Current=" + currentValueStr);

					if (minValueStr != null && maxValueStr != null && currentValueStr != null
							&& !minValueStr.isEmpty() && !maxValueStr.isEmpty()
							&& !currentValueStr.isEmpty()) {
						minScrollValue = Double.parseDouble(minValueStr);
						maxScrollValue = Double.parseDouble(maxValueStr);
						TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: STEP 2 COMPLETE - Range: "
								+ minScrollValue + " to " + maxScrollValue);
					} else {
						TestExecutionLogger.warning("SCROLL_AND_SELECT_WITH_END_DETECTION: "
								+ "Scrollbar values null/empty. Will rely on stuck detection.");
						hasScrollBar = false;
					}
				} catch (Exception ex) {
					TestExecutionLogger.warning("SCROLL_AND_SELECT_WITH_END_DETECTION: Error getting scrollbar: "
							+ ex.getMessage());
					hasScrollBar = false;
				}
			}
			Waits.waitForFixedDuration(500);

			// ===== STEP 3: SCROLL DOWN UNTIL CONDITION MET =====
			TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: ===== STEP 3: Scrolling to find: "
					+ locator + " =====");

			double lastScrollPosition = -999;
			int consecutiveNoChangeCount = 0;

			scrollLoop:
			while (scrollAttempt < maxScrollAttempts) {
				scrollAttempt++;
				TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: --- Attempt "
						+ scrollAttempt + " of " + maxScrollAttempts + " ---");

				// Get current scroll position
				double currentPosition = -1;
				if (hasScrollBar) {
					try {
						WindowsElement scrollBar = driver.findElementByXPath(scrollBarXPath);
						String currentValueStr = scrollBar.getAttribute("RangeValue.Value");
						if (currentValueStr != null && !currentValueStr.isEmpty()) {
							currentPosition = Double.parseDouble(currentValueStr);
						}
					} catch (Exception ex) {
						TestExecutionLogger.warning("SCROLL_AND_SELECT_WITH_END_DETECTION: "
								+ "Error getting scroll position: " + ex.getMessage());
					}
				}

				// End detection: reached max
				if (hasScrollBar && maxScrollValue > 0 && currentPosition >= 0
						&& currentPosition >= maxScrollValue) {
					endOfPageReached = true;
					TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: ***** END OF PAGE *****");
				}

				// End detection: stuck
				if (scrollAttempt > 1 && currentPosition == lastScrollPosition) {
					consecutiveNoChangeCount++;
					if (consecutiveNoChangeCount >= 3) {
						endOfPageReached = true;
						TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: "
								+ "***** END OF PAGE - Scroll stuck at " + currentPosition + " *****");
					}
				} else {
					consecutiveNoChangeCount = 0;
				}
				lastScrollPosition = currentPosition;

				// Try to find element
				try {
					WindowsElement element = findElement(driver, strategy, locator, Optional.of(2));
					if (element != null) {
						String isOffscreen = element.getAttribute("IsOffscreen");
						if ("false".equalsIgnoreCase(isOffscreen)) {
							elementFound = true;
							foundElement = element;
							TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: "
									+ "***** ELEMENT FOUND AND VISIBLE *****");
							break scrollLoop;
						}
					}
				} catch (Exception e) {
					TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: "
							+ "Element not found on this attempt");
				}

				if (endOfPageReached) {
					TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: "
							+ "***** EXITING - End of page reached, element not found *****");
					break scrollLoop;
				}

				robot.delay(300);
				robot.mouseWheel(5);
				Waits.waitForFixedDuration(500);
			}

			if (scrollAttempt >= maxScrollAttempts && !elementFound && !endOfPageReached) {
				maxAttemptsReached = true;
			}

			// ===== STEP 4: PERFORM ACTION BASED ON RESULT =====
			TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: ===== STEP 4: elementFound="
					+ elementFound + ", endOfPage=" + endOfPageReached
					+ ", maxAttempts=" + maxAttemptsReached + " =====");

			if (elementFound && foundElement != null) {
				Waits.waitForFixedDuration(500);
				foundElement.click();
				TestExecutionLogger.info("SCROLL_AND_SELECT_WITH_END_DETECTION: SUCCESS - Clicked: " + locator);
			} else if (endOfPageReached) {
				TestExecutionLogger.error("SCROLL_AND_SELECT_WITH_END_DETECTION: FAILURE - End of page. Not found: "
						+ locator);
				throw new RuntimeException("SCROLL_AND_SELECT_WITH_END_DETECTION: Element not found after scrolling to end: "
						+ locator);
			} else if (maxAttemptsReached) {
				TestExecutionLogger.error("SCROLL_AND_SELECT_WITH_END_DETECTION: FAILURE - Max attempts ("
						+ maxScrollAttempts + "). Not found: " + locator);
				throw new RuntimeException("SCROLL_AND_SELECT_WITH_END_DETECTION: Element not found after "
						+ maxScrollAttempts + " attempts: " + locator);
			} else {
				TestExecutionLogger.error("SCROLL_AND_SELECT_WITH_END_DETECTION: FAILURE - Unknown state. "
						+ "Not found: " + locator);
				throw new RuntimeException("SCROLL_AND_SELECT_WITH_END_DETECTION: Element not found (unknown state): "
						+ locator);
			}

		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			TestExecutionLogger.error("SCROLL_AND_SELECT_WITH_END_DETECTION: Exception: " + e.getMessage());
			throw new RuntimeException("Failed to scroll and select element: " + locator, e);
		}
	}

	/** Scroll to top with scrollbar position detection. */
	private static void scrollToTopWithDetection(java.awt.Robot robot,
			WindowsDriver<WindowsElement> driver, String scrollBarXPath, boolean hasScrollBar) {
		boolean reachedTop = false;
		double previousTopScrollValue = -1;
		int stuckAtTopCounter = 0;
		int scrollToTopAttempt = 0;
		int maxScrollToTopAttempts = 50;

		while (!reachedTop && scrollToTopAttempt < maxScrollToTopAttempts) {
			scrollToTopAttempt++;
			robot.delay(200);
			robot.mouseWheel(-10);
			Waits.waitForFixedDuration(300);

			if (hasScrollBar) {
				try {
					WindowsElement scrollBar = driver.findElementByXPath(scrollBarXPath);
					String currentValueStr = scrollBar.getAttribute("RangeValue.Value");
					String minValueStr = scrollBar.getAttribute("RangeValue.Minimum");

					if (currentValueStr == null || minValueStr == null
							|| currentValueStr.isEmpty() || minValueStr.isEmpty()) {
						stuckAtTopCounter++;
						if (stuckAtTopCounter >= 5) {
							reachedTop = true;
							TestExecutionLogger.info("SCROLL_TO_TOP: Assuming at top (values unavailable)");
						}
						continue;
					}

					double currentScrollValue = Double.parseDouble(currentValueStr);
					double minScrollValue = Double.parseDouble(minValueStr);

					if (currentScrollValue <= minScrollValue) {
						reachedTop = true;
						TestExecutionLogger.info("SCROLL_TO_TOP: Reached top (value: "
								+ currentScrollValue + " <= min: " + minScrollValue + ")");
					}

					if (currentScrollValue == previousTopScrollValue) {
						stuckAtTopCounter++;
						if (stuckAtTopCounter >= 3) {
							reachedTop = true;
							TestExecutionLogger.info("SCROLL_TO_TOP: Reached top (stopped at: "
									+ currentScrollValue + ")");
						}
					} else {
						stuckAtTopCounter = 0;
					}
					previousTopScrollValue = currentScrollValue;
				} catch (Exception ex) {
					stuckAtTopCounter++;
					if (stuckAtTopCounter >= 5) {
						reachedTop = true;
						TestExecutionLogger.info("SCROLL_TO_TOP: Assuming at top (errors occurred)");
					}
				}
			} else {
				scrollToTop(robot);
				reachedTop = true;
			}
		}

		if (!reachedTop) {
			TestExecutionLogger.warning("SCROLL_TO_TOP: Max attempts reached ("
					+ maxScrollToTopAttempts + "). Assuming at top.");
		}
	}

	// ========== HELPERS — ELEMENT PRESENCE ==========

	private static boolean isElementPresentCheck(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			String locator) {

		TestExecutionLogger.info("IS_ELEMENT_PRESENT: Checking for element with "
				+ strategy + ": " + locator);

		try {
			List<WindowsElement> elements = findAllElements(driver, strategy, locator);
			int matchCount = (elements != null) ? elements.size() : 0;

			if (matchCount == 0) {
				TestExecutionLogger.error("IS_ELEMENT_PRESENT: No element found with "
						+ strategy + ": " + locator);
				return false;
			} else if (matchCount == 1) {
				return checkSingleElementVisibility(elements.get(0), locator);
			} else {
				return checkMultipleElementsVisibility(elements, matchCount, locator);
			}
		} catch (Exception e) {
			TestExecutionLogger.error("IS_ELEMENT_PRESENT: Failed to find element. Exception: "
					+ e.getMessage());
			return false;
		}
	}

	private static boolean checkSingleElementVisibility(WindowsElement element, String locator) {
		try {
			String isOffscreenValue = element.getAttribute("IsOffscreen");
			boolean isOnScreen = "false".equalsIgnoreCase(isOffscreenValue);

			if (isOnScreen) {
				TestExecutionLogger.info("IS_ELEMENT_PRESENT: Element found and visible. "
						+ "Match count: 1. IsOffscreen: " + isOffscreenValue);
				return true;
			} else {
				TestExecutionLogger.error("IS_ELEMENT_PRESENT: Element found but NOT in focus (off-screen). "
						+ "IsOffscreen: " + isOffscreenValue
						+ ". Use IS_ELEMENT_PRESENT_WITH_SCROLL or SCROLL_AND_SELECT instead.");
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean checkMultipleElementsVisibility(List<WindowsElement> elements,
			int matchCount, String locator) {
		TestExecutionLogger.warning("IS_ELEMENT_PRESENT: Multiple elements found! Match count: "
				+ matchCount + ". Consider making locator more specific.");

		int visibleCount = 0;
		for (int i = 0; i < Math.min(matchCount, 5); i++) {
			try {
				WindowsElement elem = elements.get(i);
				String name = elem.getAttribute("Name");
				String automationId = elem.getAttribute("AutomationId");
				String className = elem.getAttribute("ClassName");
				String isOffscreenValue = elem.getAttribute("IsOffscreen");
				boolean isOnScreen = "false".equalsIgnoreCase(isOffscreenValue);

				if (isOnScreen) {
					visibleCount++;
				}

				TestExecutionLogger.warning("IS_ELEMENT_PRESENT: Element " + (i + 1)
						+ " - Name: " + name
						+ ", AutomationId: " + automationId
						+ ", ClassName: " + className
						+ ", IsOffscreen: " + isOffscreenValue);
			} catch (Exception e) {
				// Ignore
			}
		}
		if (matchCount > 5) {
			TestExecutionLogger.warning("IS_ELEMENT_PRESENT: ... and "
					+ (matchCount - 5) + " more elements");
		}

		if (visibleCount > 0) {
			TestExecutionLogger.info("IS_ELEMENT_PRESENT: Found " + visibleCount
					+ " visible element(s) out of " + matchCount + " total.");
			return true;
		} else {
			TestExecutionLogger.error("IS_ELEMENT_PRESENT: Multiple elements found but NONE visible. "
					+ "Total: " + matchCount
					+ ". Use IS_ELEMENT_PRESENT_WITH_SCROLL or SCROLL_AND_SELECT instead.");
			return false;
		}
	}

	// ========== HELPERS — ELEMENT PRESENCE WITH SCROLL ==========

	private static boolean isElementPresentWithScrollCheck(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			String locator,
			Integer maxRetryOverride) {

		TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Starting search for element with "
				+ strategy + ": " + locator);

		int maxScrollAttempts = (maxRetryOverride != null) ? maxRetryOverride : 25;
		TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Max scroll attempts: "
				+ maxScrollAttempts
				+ (maxRetryOverride != null ? " (custom)" : " (default)"));

		int scrollAttempt = 0;

		try {
			java.awt.Robot robot = new java.awt.Robot();

			// Step 1: Scroll to top
			scrollToTop(robot);

			// Step 2: Check without scrolling first
			TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Checking without scrolling...");
			Boolean initialResult = tryIsElementPresent(driver, strategy, locator);
			if (initialResult != null && initialResult) {
				TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Element already visible.");
				return true;
			}

			// Check if element exists but is off-screen
			List<WindowsElement> elementsBeforeScroll = findAllElements(driver, strategy, locator);
			if (elementsBeforeScroll != null && !elementsBeforeScroll.isEmpty()) {
				try {
					String isOffscreenValue = elementsBeforeScroll.get(0).getAttribute("IsOffscreen");
					if ("true".equalsIgnoreCase(isOffscreenValue)) {
						TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: "
								+ "Element in DOM but off-screen. Scrolling...");
					}
				} catch (Exception e) {
					// Ignore
				}
			}

			// Step 3: Scroll down until element is found or max attempts reached
			TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Scrolling down...");
			while (scrollAttempt < maxScrollAttempts) {
				scrollAttempt++;
				TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Scrolling (attempt "
						+ scrollAttempt + " of " + maxScrollAttempts + ")");
				robot.delay(300);
				robot.mouseWheel(5);
				Waits.waitForFixedDuration(500);

				Boolean scrollResult = tryIsElementPresent(driver, strategy, locator);
				if (scrollResult != null && scrollResult) {
					TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Element now visible.");
					return true;
				}
			}

			// Max attempts reached — final check
			List<WindowsElement> elementsAfterScroll = findAllElements(driver, strategy, locator);
			if (elementsAfterScroll != null && !elementsAfterScroll.isEmpty()) {
				try {
					String isOffscreenValue = elementsAfterScroll.get(0).getAttribute("IsOffscreen");
					if ("true".equalsIgnoreCase(isOffscreenValue)) {
						TestExecutionLogger.error("IS_ELEMENT_PRESENT_WITH_SCROLL: "
								+ "Element in DOM but could NOT be brought into focus after "
								+ maxScrollAttempts + " attempts. Locator: " + strategy + ": " + locator);
						return false;
					}
				} catch (Exception e) {
					// Ignore
				}
			}

			TestExecutionLogger.error("IS_ELEMENT_PRESENT_WITH_SCROLL: Element NOT FOUND after "
					+ maxScrollAttempts + " attempts. Locator: " + strategy + ": " + locator);
			return false;

		} catch (Exception e) {
			TestExecutionLogger.error("IS_ELEMENT_PRESENT_WITH_SCROLL: Failed. Exception: "
					+ e.getMessage());
			return false;
		}
	}

	/** Quick presence check: true = found & visible, false = found but off-screen, null = error. */
	private static Boolean tryIsElementPresent(
			WindowsDriver<WindowsElement> driver,
			LocatorStrategy strategy,
			String locator) {
		try {
			List<WindowsElement> elements = findAllElements(driver, strategy, locator);
			int matchCount = (elements != null) ? elements.size() : 0;

			if (matchCount == 0) {
				return false;
			} else if (matchCount == 1) {
				WindowsElement element = elements.get(0);
				String isOffscreenValue = element.getAttribute("IsOffscreen");
				boolean isOnScreen = "false".equalsIgnoreCase(isOffscreenValue);

				if (isOnScreen) {
					TestExecutionLogger.info("IS_ELEMENT_PRESENT_WITH_SCROLL: Element found and visible. "
							+ "Match count: 1. IsOffscreen: " + isOffscreenValue);
					return true;
				}
				return false;
			} else {
				int visibleCount = 0;
				for (int i = 0; i < Math.min(matchCount, 3); i++) {
					try {
						WindowsElement elem = elements.get(i);
						String name = elem.getAttribute("Name");
						String automationId = elem.getAttribute("AutomationId");
						String isOffscreenValue = elem.getAttribute("IsOffscreen");
						boolean isOnScreen = "false".equalsIgnoreCase(isOffscreenValue);

						if (isOnScreen) {
							visibleCount++;
						}

						TestExecutionLogger.warning("IS_ELEMENT_PRESENT_WITH_SCROLL: Element " + (i + 1)
								+ " - Name: " + name
								+ ", AutomationId: " + automationId
								+ ", IsOffscreen: " + isOffscreenValue);
					} catch (Exception e) {
						// Ignore
					}
				}

				if (visibleCount > 0) {
					TestExecutionLogger.warning("IS_ELEMENT_PRESENT_WITH_SCROLL: Multiple elements! "
							+ "Count: " + matchCount + ". Visible: " + visibleCount);
					return true;
				}
				return false;
			}
		} catch (Exception e) {
			return null;
		}
	}

	// ========== UTILITY ==========

	/** Parse an optional integer from a string, returning null on failure. */
	private static Integer parseOptionalInt(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		try {
			int parsed = Integer.parseInt(value);
			if (parsed < 0) {
				TestExecutionLogger.warning("Negative value '" + value + "'. Using default.");
				return null;
			}
			return parsed;
		} catch (NumberFormatException e) {
			TestExecutionLogger.warning("Invalid integer '" + value + "'. Using default.");
			return null;
		}
	}

	// ========== HELPERS — OS / SYSTEM ==========

	/** Execute a system command and return its output. */
	private static String executeCommand(String command) {
		StringBuilder output = new StringBuilder();
		try {
			Process process = Runtime.getRuntime().exec(command);
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append(System.lineSeparator());
				}
			}
			process.waitFor();
		} catch (Exception e) {
			TestExecutionLogger.error("Failed to execute command: " + command + " — " + e.getMessage());
			throw new RuntimeException("Command execution failed: " + command, e);
		}
		return output.toString().trim();
	}

	/** Kill a process by name using taskkill. Returns true on success. */
	private static boolean killProcessByName(String processName) {
		try {
			String cmd = "taskkill /IM " + processName + " /F";
			String result = executeCommand(cmd);
			TestExecutionLogger.info("KILL_PROCESS: Killed '" + processName + "'. Output: " + result);
			return true;
		} catch (Exception e) {
			TestExecutionLogger.error("KILL_PROCESS: Failed to kill '" + processName + "': " + e.getMessage());
			return false;
		}
	}

	/** Check if a process is currently running by name. */
	private static boolean checkIfProcessRunning(String processName) {
		try {
			String result = executeCommand("tasklist /FI \"IMAGENAME eq " + processName + "\"");
			boolean running = result.toLowerCase().contains(processName.toLowerCase());
			TestExecutionLogger.info("IS_PROCESS_RUNNING: '" + processName + "' running = " + running);
			return running;
		} catch (Exception e) {
			TestExecutionLogger.error("IS_PROCESS_RUNNING: Error checking '" + processName + "': " + e.getMessage());
			return false;
		}
	}

	/** Delete a single file by absolute path. Returns true if deleted or did not exist. */
	private static boolean deleteFileByPath(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			TestExecutionLogger.info("DELETE_FILE: File does not exist (nothing to delete): " + filePath);
			return true;
		}
		boolean deleted = file.delete();
		if (deleted) {
			TestExecutionLogger.info("DELETE_FILE: Successfully deleted file: " + filePath);
		} else {
			TestExecutionLogger.error("DELETE_FILE: Failed to delete file: " + filePath);
		}
		return deleted;
	}

	/** Delete a folder and all its contents recursively. Returns true if deleted or did not exist. */
	private static boolean deleteFolderByPath(String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists()) {
			TestExecutionLogger.info("DELETE_FOLDER: Folder does not exist (nothing to delete): " + folderPath);
			return true;
		}
		boolean deleted = deleteFolderRecursively(folder);
		if (deleted) {
			TestExecutionLogger.info("DELETE_FOLDER: Successfully deleted folder: " + folderPath);
		} else {
			TestExecutionLogger.error("DELETE_FOLDER: Failed to fully delete folder: " + folderPath);
		}
		return deleted;
	}

	/** Recursively delete a directory and all its contents. */
	private static boolean deleteFolderRecursively(File folder) {
		File[] contents = folder.listFiles();
		if (contents != null) {
			for (File file : contents) {
				if (file.isDirectory()) {
					deleteFolderRecursively(file);
				} else {
					file.delete();
				}
			}
		}
		return folder.delete();
	}

	/** Delete all files inside a folder (keeps the folder). Returns true if all files deleted. */
	private static boolean deleteFilesInFolder(String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists() || !folder.isDirectory()) {
			TestExecutionLogger.info("DELETE_FILES_IN_FOLDER: Folder does not exist or is not a directory: " + folderPath);
			return true;
		}
		boolean allDeleted = true;
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					if (!file.delete()) {
						TestExecutionLogger.error("DELETE_FILES_IN_FOLDER: Failed to delete: " + file.getAbsolutePath());
						allDeleted = false;
					}
				}
			}
		}
		if (allDeleted) {
			TestExecutionLogger.info("DELETE_FILES_IN_FOLDER: Successfully deleted all files in: " + folderPath);
		}
		return allDeleted;
	}
}
