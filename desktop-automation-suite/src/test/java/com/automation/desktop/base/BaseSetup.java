package com.automation.desktop.base;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.automation.desktop.utility.TestReportStore;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;

/**
 * BaseSetup — Top-level field holder for all framework and test state.
 *
 * <p>Organized into logical sections for readability.
 * Extended by ApplicationLaunch → CommonMethod → test scripts.</p>
 */
public class BaseSetup {

	// ═══════════════════════════════════════════════════════════════
	// §1  GENERIC FRAMEWORK STATE
	// ═══════════════════════════════════════════════════════════════

	/** Master pass/fail flag used across assertions. */
	public static boolean flag = true;

	/** Primary desktop application driver (WinAppDriver session). */
	protected static WindowsDriver<WindowsElement> appDriver = null;

	/** Root desktop driver for dialog/popup management. */
	protected static WindowsDriver<WindowsElement> rootDriver = null;

	/** Loaded from config.properties at suite start. */
	protected static Properties configProp;

	/** Loaded from assertionErrorMessage.properties. */
	protected static Properties assertProp;

	/** Current set of window handles (used during reconnection). */
	protected static Set<String> windowHandles = null;

	/** Title of the currently active window. */
	protected static String windowTitle = null;
	
	/** Cumulative fixed-wait time in milliseconds (reported in PDF). */
	public static int totalFixedWaitTime = 0;

	// ═══════════════════════════════════════════════════════════════
	// §3  SCREENSHOT SUPPORT (for PDF reports)
	// ═══════════════════════════════════════════════════════════════

	/** JPEG compression quality for PDF report screenshots. */
	private static final float SCREENSHOT_QUALITY = 0.5f;

	/**
	 * Take a JPEG-compressed screenshot and save to disk for PDF reports.
	 * Also attaches the path to the current test's report data.
	 *
	 * @param testName  Base name for the screenshot file
	 * @return Absolute path of the saved screenshot, or null on failure
	 */
	public static String takeScreenshot(String testName) {
		try {
			if (appDriver == null) {
				System.out.println("No driver found for screenshot");
				return null;
			}

			File screenshotsDir = new File("screenshots");
			if (!screenshotsDir.exists()) {
				screenshotsDir.mkdir();
			}

			String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String fileName = testName + "_" + timestamp + ".jpg";

			// Capture PNG bytes and compress to JPEG
			byte[] pngBytes = ((TakesScreenshot) appDriver).getScreenshotAs(OutputType.BYTES);
			byte[] jpegBytes = compressToJpeg(pngBytes, SCREENSHOT_QUALITY);

			File targetFile = new File(screenshotsDir, fileName);
			java.nio.file.Files.write(targetFile.toPath(), jpegBytes);

			String fullPath = targetFile.getAbsolutePath();
			TestReportStore.addScreenshot(testName.contains("_") ? testName.substring(0, testName.lastIndexOf('_')) : testName, fullPath);
			System.out.println("Screenshot saved (JPEG compressed): " + fullPath);

			return fullPath;

		} catch (Exception e) {
			System.out.println("Failed to capture screenshot: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Compress PNG bytes to JPEG with the specified quality.
	 *
	 * @param pngBytes  Raw PNG screenshot bytes
	 * @param quality   JPEG quality (0.0 to 1.0)
	 * @return Compressed JPEG bytes
	 */
	protected static byte[] compressToJpeg(byte[] pngBytes, float quality) throws Exception {
		BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));

		// Convert to RGB (JPEG doesn't support alpha channel)
		BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		rgbImage.createGraphics().drawImage(image, 0, 0, java.awt.Color.WHITE, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
		ImageWriteParam param = writer.getDefaultWriteParam();
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality);

		try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
			writer.setOutput(ios);
			writer.write(null, new IIOImage(rgbImage, null, null), param);
		} finally {
			writer.dispose();
		}

		return baos.toByteArray();
	}
}