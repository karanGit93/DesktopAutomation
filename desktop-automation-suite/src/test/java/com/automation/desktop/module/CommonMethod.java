package com.automation.desktop.module;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base32;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.w3c.dom.Document;

import com.automation.desktop.base.ApplicationLaunch;
import com.automation.desktop.testdata.DataHandler;
import com.automation.desktop.utility.Interaction;
import com.automation.desktop.utility.Interaction.InteractionType;
import com.automation.desktop.utility.Interaction.LocatorStrategy;
import com.automation.desktop.utility.TestExecutionLogger;

import io.appium.java_client.windows.WindowsElement;

import com.automation.desktop.utility.Waits;

public class CommonMethod extends ApplicationLaunch {

	/**
	 * This method will click on dropdown values, available on connections windows
	 * Eg. For Environment dropdown pass:- Sandbox or'Production/Developer Edition'
	 * or Prerelease Eg. For Encryption dropdown pass: 'Java Key Store (JKS)' or
	 * 'Private Key'
	 * 
	 * @param Name_Locator
	 */
	public static void clickOnDropdown(String nameLocator) {
		try {
			var dropDownOptionName = appDriver.findElementByName(nameLocator);
			new Actions(appDriver).moveToElement(dropDownOptionName).click().perform();
		} catch (Exception e) {
			System.err.println("Error while clicking dropdown option: " + nameLocator);
			e.printStackTrace();
		}
	}

	public static void CloseTestCaseButton() {
		String closeButton = "//Text[contains(@Name,'TestCase_')]/following-sibling::ToolBar//Button[8]";
		Interaction.performAction(appDriver, LocatorStrategy.XPATH, InteractionType.CLICK, closeButton);
	}

	public static void CloseTestCase() {
		// Try to find Button[8] first
		List<WindowsElement> button8 = appDriver.findElements(By.xpath("//Pane[contains(@Name,'TestCase_')]//ToolBar//Button[8]"));

		WindowsElement buttonToClick;
		if (!button8.isEmpty()) {
		    buttonToClick = button8.get(0); // Use Button[8]
		} else {
		    buttonToClick = appDriver.findElement(By.xpath("//Pane[contains(@Name,'TestCase_')]//ToolBar//Button[7]"));
		}

		// Click the right button
		buttonToClick.click();

	}
	
	public static void headerplusButton() {
		// Try to find Button[8] first
		List<WindowsElement> buttons = appDriver.findElements(
		        By.xpath("//Pane[contains(@Name,'TestCase_')]//ToolBar//Button[8]")
		);

		WindowsElement buttonToClick;

		if (buttons.size() >= 8) {
		    // If 8th exists, click the 7th
			buttonToClick = appDriver.findElement(
			        By.xpath("//Pane[contains(@Name,'TestCase_')]//ToolBar//Button[7]"));
		} else if (buttons.size() >= 7) {
		    // Else click the 6th
			buttonToClick = appDriver.findElement(
			        By.xpath("//Pane[contains(@Name,'TestCase_')]//ToolBar//Button[7]"));
		} else {
		    throw new RuntimeException("Not enough buttons available to click");
		}
		buttonToClick.click();
	}

	public static void launchDesktopApp() {
		try {
			launchWindowsApplicationDriver();
			minimizeAllWindows();
			launchApplication();
		}catch (Exception e) {
			Waits.waitForFixedDuration(10000);
		}
	}

	public static void pageDown() {
		Actions actions = new Actions(appDriver);
		actions.sendKeys(Keys.PAGE_DOWN).perform();
	}

	@SuppressWarnings("deprecation")
	public static void killIfRunning(String processName) {
		try {
			Process check = Runtime.getRuntime().exec("tasklist");
			BufferedReader reader = new BufferedReader(new InputStreamReader(check.getInputStream()));
			String line;
			boolean isRunning = false;

			while ((line = reader.readLine()) != null) {
				if (line.toLowerCase().contains(processName.toLowerCase())) {
					isRunning = true;
					break;
				}
			}

			if (isRunning) {
				Process kill = Runtime.getRuntime().exec("taskkill /F /IM " + processName);
				kill.waitFor();
				TestExecutionLogger.info(processName + " was running and has been terminated.");
			} else {
				TestExecutionLogger.warning(processName + " is not running.");
			}
		} catch (Exception e) {
			TestExecutionLogger.error("Error handling " + processName + ": " + e.getMessage());
		}
	}

	public static void killWinAppDriverIfRunning() {
		killIfRunning("WinAppDriver.exe");
	}

	public static void pressESC() {
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_ESCAPE);
			robot.keyRelease(KeyEvent.VK_ESCAPE);
			System.out.println("Esc key pressed");
		} catch (Exception e) {
			// handle exception
		}
	}

	public static void copyPasteFile(String from, String to) {
		Path source = Paths.get(from);
		Path destination = Paths.get(to);

		try {
			// Make sure the parent directory of the destination exists
			Path parentDir = destination.getParent();
			if (parentDir != null && !Files.exists(parentDir)) {
				Files.createDirectories(parentDir);
			}

			// Copy the file (overwrites if exists)
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("file copied successfully from " + from + " to " + to);

		} catch (IOException e) {
			System.err.println("Error copying file: " + e.getMessage());
		}
	}

	public static void doubleClickWithCoordinate(String value) {
		Interaction.performAction(appDriver, LocatorStrategy.XPATH, InteractionType.DOUBLE_CLICK_WITH_COORDINATE,
				"//Pane[@Name='All Tests']//Pane//Pane", value);
	}

	public static void pressCtrlA() {

		try {
			Robot robot = new Robot();

			// Press CTRL + A
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_A);

			// Release A then CTRL
			robot.keyRelease(KeyEvent.VK_A);
			robot.keyRelease(KeyEvent.VK_CONTROL);

		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	public static void pressCtrlADelete() {

		try {
			Robot robot = new Robot();

			// Press CTRL + A
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_A);

			// Release A then CTRL
			robot.keyRelease(KeyEvent.VK_A);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			
			robot.keyPress(KeyEvent.VK_DELETE);
			robot.keyRelease(KeyEvent.VK_DELETE);

		} catch (AWTException e) {
			e.printStackTrace();
	}
	}

	public static void ClickWithCoordinate(String value) {
		ApplicationLaunch.rootAppDriver();
		Interaction.performAction(rootDriver, LocatorStrategy.XPATH, InteractionType.CO_ORDINATE_CLICK,
				"//Pane[@Name='All Tests']//Pane//Pane", value);
		ApplicationLaunch.endSession(rootDriver);
	}
	
	public static void deleteImportedProject(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try {
					removeReadOnlyIfExists(file);
					Files.delete(file);
					System.out.println("Deleted file: " + file);
				} catch (IOException e) {
					System.err.println("❌ Failed to delete file: " + file + " - " + e.getMessage());
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				try {
					removeReadOnlyIfExists(dir);
					Files.delete(dir);
					System.out.println("Deleted directory: " + dir);
				} catch (IOException e) {
					System.err.println("❌ Failed to delete directory: " + dir + " - " + e.getMessage());
				}
				return FileVisitResult.CONTINUE;
			}

			private void removeReadOnlyIfExists(Path path) {
				try {
					DosFileAttributeView attrView = Files.getFileAttributeView(path, DosFileAttributeView.class);
					if (attrView != null && attrView.readAttributes().isReadOnly()) {
						attrView.setReadOnly(false);
					}
				} catch (IOException e) {
					System.err.println("⚠️ Could not clear read-only on: " + path + " - " + e.getMessage());
				}
			}
		});
	}

	public static String generateBase32Secret(int byteLength) {
		SecureRandom secureRandom = new SecureRandom();
		byte[] buffer = new byte[byteLength]; // e.g., 10 for 80-bit key
		secureRandom.nextBytes(buffer);

		Base32 base32 = new Base32();
		return base32.encodeToString(buffer).replace("=", ""); // remove padding if needed
	}

	public static void unSelect() {
		try {
			WindowsElement element = appDriver.findElement(By.xpath("//Pane[@Name='All Tests']//Pane//Pane"));

			if (element.isDisplayed()) {
				int width = element.getSize().getWidth();
				int offsetX = width / 2;

				Actions actions = new Actions(appDriver);
				actions.moveToElement(element, offsetX, 25).click().perform();
				Waits.waitForFixedDuration(2000);
				actions.keyDown(Keys.CONTROL).sendKeys("z").pause(Duration.ofMillis(300)).sendKeys("y")
						.keyUp(Keys.CONTROL).perform();

				System.out.println("✅ Element clicked and Ctrl +Z, +Y performed.");
			} else {
				System.out.println("ℹ️ Element is not displayed. Skipping click.");
			}
		} catch (NoSuchElementException e) {
			System.out.println("ℹ️ Element not found. Skipping action.");
		} catch (Exception e) {
			System.out.println("⚠️ Unexpected error: " + e.getMessage());
		}
	}
	
	public static void doubleclick(String Locator,int value) {
		var element = appDriver.findElement(By.xpath(Locator));
		// Get element location and size
		Point location = element.getLocation();
		int elementX = location.getX();
		int elementY = location.getY();
		int elementWidth = element.getSize().getWidth();
		// Compute horizontal center and Y coordinate 20px above top
		int clickX = elementX + (elementWidth / 2); // horizontal center
		int clickY = elementY + value;                 // 20px above top
		Actions actions = new Actions(appDriver);
		// Move to absolute coordinate and click
		actions.moveToElement(element,clickX-75, clickY)
		       .contextClick()
		       .perform();
		// Reset cursor back (important for next moveByOffset)
		actions.moveByOffset(-clickX, -clickY).perform();
	}
	
	public static String getDriverVersion(String driverPath) {
	    try {
	        Process process = new ProcessBuilder(driverPath, "--version").start();
	        BufferedReader reader = new BufferedReader(
	                new InputStreamReader(process.getInputStream()));
	        return reader.readLine();
	    } catch (Exception e) {
	        return "Unable to get driver version";
	    }
	}
	
	public static boolean waitForFolderAndFileExistance(
            String parentPath,
            String folderName,
            String fileName,
            int timeoutSec) {

        File folder = new File(parentPath, folderName);
        File targetFile = new File(folder, fileName);

        long endTime = System.currentTimeMillis() + (timeoutSec * 1000L);

        // 1️⃣ Wait for folder
        while (System.currentTimeMillis() < endTime) {
            if (folder.exists() && folder.isDirectory()) {
                System.out.println("Folder found: " + folder.getAbsolutePath());
                break;
            }
            Waits.waitForFixedDuration(1000);
        }

        if (!folder.exists()) {
            System.out.println("Timeout waiting for folder: " + folder.getAbsolutePath());
            return false;
        }

        // 2️⃣ Wait for file inside folder
        while (System.currentTimeMillis() < endTime) {
            if (targetFile.exists() && targetFile.isFile()) {
                System.out.println("File found: " + targetFile.getAbsolutePath());
                return true;
            }
            Waits.waitForFixedDuration(1000);
        }

        System.out.println("Timeout waiting for file: " + targetFile.getAbsolutePath());
        return false;
    }
	
	 /**
	  * Checks if a specific process is currently running
	  * @param processName The name of the process to check (e.g., "msedge.exe")
	  * @return true if the process is running, false otherwise
	  */
	 @SuppressWarnings("deprecation")
	 public static boolean isProcessRunning(String processName) {
		 try {
			 Process check = Runtime.getRuntime().exec("tasklist");
			 BufferedReader reader = new BufferedReader(new InputStreamReader(check.getInputStream()));
			 String line;
			 
			 while ((line = reader.readLine()) != null) {
				 if (line.toLowerCase().contains(processName.toLowerCase())) {
					 return true;
				 }
			 }
			 reader.close();
		 } catch (Exception e) {
			 TestExecutionLogger.info("Error checking process " + processName + ": " + e.getMessage());
		 }
		 return false;
	 }
	 
	 public static void readFile(String path, String expectedValues) {
			
			List<String> lines = null;
			try {
				lines = Files.readAllLines(
					    Path.of(path),
					    StandardCharsets.UTF_8
					);
			} catch (IOException e) {
				TestExecutionLogger.error("File not found " + e.getMessage());
				e.printStackTrace();
			}
				String content = String.join(System.lineSeparator(), lines);		
				String expectedText = expectedValues;
//				System.out.println(content);
				if (content.contains(expectedText)) {
					TestExecutionLogger.info("ASSERTION PASSED: Text found - " + expectedText);
				} else {
				    Assert.fail("ASSERTION FAILED: Text not found - " + expectedText);
				}
		}
}
