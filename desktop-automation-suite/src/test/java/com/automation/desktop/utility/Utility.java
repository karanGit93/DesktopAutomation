package com.automation.desktop.utility;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.automation.desktop.base.BaseSetup;

import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Utility extends BaseSetup {

	static int ScreenshotCount = 0;
	static String screenshotName = null;

	/** JPEG compression quality (0.0–1.0). 0.5 gives ~70-80% size reduction. */
	private static final float JPEG_QUALITY = 0.5f;

	/**
	 * Take a JPEG-compressed screenshot from a WebDriver instance.
	 * Used as an Allure @Attachment returning raw bytes.
	 */
	@Attachment(value = "Screenshot", type = "image/jpeg")
	public static byte[] takeScreenshot(WebDriver driver) {
		try {
			byte[] pngBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
			return compressToJpeg(pngBytes, JPEG_QUALITY);
		} catch (Exception e) {
			TestExecutionLogger.warning("JPEG compression failed, falling back to PNG: " + e.getMessage());
			return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
		}
	}

	/**
	 * Take a JPEG-compressed screenshot from the appDriver and attach to Allure.
	 */
	public static void takeScreenshot() {
		try {
			ScreenshotCount++;
			TestExecutionLogger.info("Capturing Screenshot...");
			byte[] pngBytes = ((TakesScreenshot) appDriver).getScreenshotAs(OutputType.BYTES);
			byte[] jpegBytes = compressToJpeg(pngBytes, JPEG_QUALITY);
			TestExecutionLogger.info("Attaching Screenshot to Allure Report...");
			screenshotName = "Screenshot " + ScreenshotCount + ".jpg";
			Allure.addAttachment(screenshotName, "image/jpeg", new ByteArrayInputStream(jpegBytes), ".jpg");
			TestExecutionLogger.info("Screenshot " + ScreenshotCount + " Attached Successfully (JPEG compressed).");
		} catch (Exception e) {
			TestExecutionLogger.warning("Error while taking screenshot: " + e.getMessage());
		}
	}

	/**
	 * Compress PNG bytes to JPEG — delegates to BaseSetup.compressToJpeg().
	 * Kept as a convenience alias within the Utility class.
	 */

	/**
	 * Creates a folder at the specified base path with the given folder name.
	 * 
	 * @param basePath    The base directory path
	 * @param folderName  The name of the folder to create
	 * @return The full path of the created/existing folder
	 */
	public static String createFolder(String basePath, String folderName) {
		String folderPath = basePath + folderName + File.separator;
		File folder = new File(folderPath);
		if (!folder.exists()) {
			boolean created = folder.mkdirs();
			if (created) {
				TestExecutionLogger.info(folderName + " folder created successfully at: " + folderPath);
			} else {
				TestExecutionLogger.error("Failed to create " + folderName + " folder at: " + folderPath);
			}
		} else {
			TestExecutionLogger.info(folderName + " folder already exists at: " + folderPath);
		}
		return folderPath;
	}

	/**
	 * Verifies the number of PDF files in a directory and their names.
	 * 
	 * @param directoryPath      The path to the directory containing PDF files
	 * @param expectedPdfFiles   Array of expected PDF file names
	 * @return true if all verifications pass, false otherwise
	 */
	public static boolean verifyPdfFiles(String directoryPath, String[] expectedPdfFiles) {
		boolean allVerificationsPassed = true;
		
		File exportDir = new File(directoryPath);
		
		// Verify directory exists
		if (!exportDir.exists()) {
			TestExecutionLogger.error("Export directory does not exist: " + directoryPath);
			Assertion.AssertBoolean(false);
			return false;
		}
		TestExecutionLogger.info("Export directory exists: " + directoryPath);
		
		// Get all PDF files in the directory
		File[] pdfFiles = exportDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
		
		// Verify number of PDF files created
		int expectedPdfCount = expectedPdfFiles.length;
		int actualPdfCount = pdfFiles != null ? pdfFiles.length : 0;
		TestExecutionLogger.info("Expected PDF count: " + expectedPdfCount + ", Actual PDF count: " + actualPdfCount);
		
		if (actualPdfCount != expectedPdfCount) {
			TestExecutionLogger.error("PDF count mismatch! Expected: " + expectedPdfCount + ", Actual: " + actualPdfCount);
			allVerificationsPassed = false;
		}
		Assertion.AssertText(String.valueOf(actualPdfCount), String.valueOf(expectedPdfCount), "Number of PDF files should match");
		
		// Verify each expected PDF file exists
		for (String expectedPdfName : expectedPdfFiles) {
			File expectedPdfFile = new File(directoryPath + File.separator + expectedPdfName);
			if (expectedPdfFile.exists()) {
				TestExecutionLogger.info("PDF file exists: " + expectedPdfName);
			} else {
				TestExecutionLogger.error("PDF file NOT found: " + expectedPdfName);
				allVerificationsPassed = false;
			}
			Assertion.AssertBoolean(expectedPdfFile.exists());
		}
		
		// Log all actual PDF file names found
		logPdfFileNames(directoryPath);
		
		return allVerificationsPassed;
	}

	/**
	 * Gets the count of PDF files in a directory.
	 * 
	 * @param directoryPath The path to the directory
	 * @return The number of PDF files found
	 */
	public static int getPdfFileCount(String directoryPath) {
		File exportDir = new File(directoryPath);
		if (!exportDir.exists()) {
			TestExecutionLogger.warning("Directory does not exist: " + directoryPath);
			return 0;
		}
		
		File[] pdfFiles = exportDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
		int count = pdfFiles != null ? pdfFiles.length : 0;
		TestExecutionLogger.info("PDF file count in " + directoryPath + ": " + count);
		return count;
	}

	/**
	 * Gets the names of all PDF files in a directory.
	 * 
	 * @param directoryPath The path to the directory
	 * @return List of PDF file names
	 */
	public static List<String> getPdfFileNames(String directoryPath) {
		List<String> pdfFileNames = new ArrayList<>();
		File exportDir = new File(directoryPath);
		
		if (!exportDir.exists()) {
			TestExecutionLogger.warning("Directory does not exist: " + directoryPath);
			return pdfFileNames;
		}
		
		File[] pdfFiles = exportDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
		if (pdfFiles != null) {
			for (File pdfFile : pdfFiles) {
				pdfFileNames.add(pdfFile.getName());
			}
		}
		return pdfFileNames;
	}

	/**
	 * Logs all PDF file names found in a directory.
	 * 
	 * @param directoryPath The path to the directory
	 */
	public static void logPdfFileNames(String directoryPath) {
		TestExecutionLogger.info("=== PDF Files in Directory: " + directoryPath + " ===");
		List<String> pdfFileNames = getPdfFileNames(directoryPath);
		if (pdfFileNames.isEmpty()) {
			TestExecutionLogger.info("No PDF files found.");
		} else {
			for (String fileName : pdfFileNames) {
				TestExecutionLogger.info("PDF File: " + fileName);
			}
		}
	}

	/**
	 * Verifies if a specific PDF file exists in a directory.
	 * 
	 * @param directoryPath The path to the directory
	 * @param pdfFileName   The name of the PDF file to check
	 * @return true if the file exists, false otherwise
	 */
	public static boolean verifyPdfFileExists(String directoryPath, String pdfFileName) {
		File pdfFile = new File(directoryPath + File.separator + pdfFileName);
		boolean exists = pdfFile.exists();
		if (exists) {
			TestExecutionLogger.info("PDF file exists: " + pdfFileName);
		} else {
			TestExecutionLogger.error("PDF file NOT found: " + pdfFileName);
		}
		return exists;
	}

	/**
	 * Checks if a file's last modified timestamp is within the specified number of minutes from now.
	 * 
	 * @param filePath      The full path to the file
	 * @param minutesAgo    The number of minutes to check against (e.g., 1 for less than a minute ago)
	 * @return true if the file was modified within the specified time, false otherwise
	 */
	public static boolean isFileModifiedWithinMinutes(String filePath, int minutesAgo) {
		File file = new File(filePath);
		if (!file.exists()) {
			TestExecutionLogger.error("File does not exist: " + filePath);
			return false;
		}
		
		long lastModifiedTime = file.lastModified();
		long currentTime = System.currentTimeMillis();
		long timeDifferenceMillis = currentTime - lastModifiedTime;
		long timeDifferenceMinutes = timeDifferenceMillis / (60 * 1000);
		
		boolean isWithinTime = timeDifferenceMinutes < minutesAgo;
		
		TestExecutionLogger.info("File: " + file.getName());
		TestExecutionLogger.info("Last modified: " + new java.util.Date(lastModifiedTime));
		TestExecutionLogger.info("Current time: " + new java.util.Date(currentTime));
		TestExecutionLogger.info("Time difference: " + timeDifferenceMinutes + " minute(s)");
		
		if (isWithinTime) {
			TestExecutionLogger.info("File was modified within " + minutesAgo + " minute(s) - PASSED");
		} else {
			TestExecutionLogger.error("File was NOT modified within " + minutesAgo + " minute(s) - FAILED");
		}
		
		return isWithinTime;
	}

	/**
	 * Verifies that a file's timestamp is less than a minute old and asserts the result.
	 * 
	 * @param filePath The full path to the file
	 */
	public static void assertFileModifiedWithinOneMinute(String filePath) {
		boolean isRecent = isFileModifiedWithinMinutes(filePath, 1);
		if (!isRecent) {
			TestExecutionLogger.error("File timestamp verification FAILED - File was not modified within 1 minute");
		}
		Assertion.AssertBoolean(isRecent);
	}

	/**
	 * Deletes all PDF files in a directory.
	 * 
	 * @param directoryPath The path to the directory
	 * @return The number of PDF files deleted
	 */
	public static int deleteAllPdfFiles(String directoryPath) {
		int deletedCount = 0;
		File exportDir = new File(directoryPath);
		
		if (!exportDir.exists()) {
			TestExecutionLogger.warning("Directory does not exist: " + directoryPath);
			return 0;
		}
		
		File[] pdfFiles = exportDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
		if (pdfFiles != null) {
			for (File pdfFile : pdfFiles) {
				String fileName = pdfFile.getName();
				if (pdfFile.delete()) {
					TestExecutionLogger.info("Deleted PDF file: " + fileName);
					deletedCount++;
				} else {
					TestExecutionLogger.error("Failed to delete PDF file: " + fileName);
				}
			}
		}
		TestExecutionLogger.info("Total PDF files deleted from " + directoryPath + ": " + deletedCount);
		return deletedCount;
	}

	/**
	 * Deletes a specific PDF file.
	 * 
	 * @param filePath The full path to the PDF file
	 * @return true if the file was deleted, false otherwise
	 */
	public static boolean deletePdfFile(String filePath) {
		File pdfFile = new File(filePath);
		if (!pdfFile.exists()) {
			TestExecutionLogger.warning("PDF file does not exist: " + filePath);
			return false;
		}
		
		if (pdfFile.delete()) {
			TestExecutionLogger.info("Deleted PDF file: " + pdfFile.getName());
			return true;
		} else {
			TestExecutionLogger.error("Failed to delete PDF file: " + pdfFile.getName());
			return false;
		}
	}

	/**
	 * Deletes a folder and all its contents (including subfolders).
	 * 
	 * @param directoryPath The path to the directory to delete
	 * @return true if the folder was deleted successfully, false otherwise
	 */
	public static boolean deleteFolder(String directoryPath) {
		File folder = new File(directoryPath);
		if (!folder.exists()) {
			TestExecutionLogger.warning("Folder does not exist: " + directoryPath);
			return false;
		}
		
		return deleteFolderRecursively(folder);
	}

	private static boolean deleteFolderRecursively(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteFolderRecursively(file);
				} else {
					if (file.delete()) {
						TestExecutionLogger.info("Deleted file: " + file.getName());
					} else {
						TestExecutionLogger.error("Failed to delete file: " + file.getName());
					}
				}
			}
		}
		
		if (folder.delete()) {
			TestExecutionLogger.info("Deleted folder: " + folder.getAbsolutePath());
			return true;
		} else {
			TestExecutionLogger.error("Failed to delete folder: " + folder.getAbsolutePath());
			return false;
		}
	}

	/**
	 * Closes all File Explorer windows without affecting the Windows desktop shell.
	 * Uses PowerShell to close only the explorer windows, not the entire explorer.exe process.
	 * 
	 * @return true if the operation completed successfully, false otherwise
	 */
	public static boolean closeFileExplorerWindows() {
		try {
			// PowerShell command to close only File Explorer windows (not the desktop shell)
			// This iterates through Shell.Application windows and closes them
			String powershellCommand = "powershell -Command \"(New-Object -ComObject Shell.Application).Windows() | ForEach-Object { $_.Quit() }\"";
			
			Process process = Runtime.getRuntime().exec(powershellCommand);
			int exitCode = process.waitFor();
			
			if (exitCode == 0) {
				TestExecutionLogger.info("File Explorer windows closed successfully");
				return true;
			} else {
				TestExecutionLogger.warning("Failed to close File Explorer windows. Exit code: " + exitCode);
				return false;
			}
		} catch (Exception e) {
			TestExecutionLogger.warning("Error while closing File Explorer windows: " + e.getMessage());
			return false;
		}
	}

}