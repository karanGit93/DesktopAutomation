package com.automation.desktop.utility;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

import com.automation.desktop.base.ApplicationLaunch;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class TestCaseReporting extends ApplicationLaunch {
	
	private static String dateTime = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    
	public static String getDisplayResolution() {
		String os = System.getProperty("os.name").toLowerCase();
		int width = 0;
		int height = 0;
		int scalePercent = 100;
		
		try {
			if (os.contains("win")) {
				// Get actual physical resolution using wmic
				ProcessBuilder resolutionBuilder = new ProcessBuilder("wmic", "path", 
						"Win32_VideoController", "get", "CurrentHorizontalResolution,CurrentVerticalResolution");
				resolutionBuilder.redirectErrorStream(true);
				Process resProcess = resolutionBuilder.start();
				
				java.io.BufferedReader resReader = new java.io.BufferedReader(
						new java.io.InputStreamReader(resProcess.getInputStream()));
				String line;
				
				while ((line = resReader.readLine()) != null) {
					line = line.trim();
					if (!line.isEmpty() && !line.contains("CurrentHorizontalResolution")) {
						String[] parts = line.split("\\s+");
						if (parts.length >= 2) {
							try {
								width = Integer.parseInt(parts[0]);
								height = Integer.parseInt(parts[1]);
								break; // Found valid resolution, exit loop
							} catch (NumberFormatException e) {
								// Skip invalid lines
							}
						}
					}
				}
				resProcess.waitFor();
				
				// Try multiple methods to get DPI scale percentage
				scalePercent = getDPIScale();
				
			} else {
				// For Mac/Linux, use Toolkit as fallback
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				width = (int) screenSize.getWidth();
				height = (int) screenSize.getHeight();
			}
		} catch (Exception e) {
			// Fallback to Toolkit
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			width = (int) screenSize.getWidth();
			height = (int) screenSize.getHeight();
		}
		
		if (scalePercent != 100) {
			return width + "x" + height + " (" + scalePercent + "% scale)";
		}
		return width + "x" + height;
	}
	
	private static int getDPIScale() {
		int scalePercent = 100;
		
		// Method 1: Check HKCU\Control Panel\Desktop\WindowMetrics - AppliedDPI
		scalePercent = checkRegistryDPI("HKCU\\Control Panel\\Desktop\\WindowMetrics", "AppliedDPI");
		if (scalePercent != 100) return scalePercent;
		
		// Method 2: Check HKCU\Control Panel\Desktop - LogPixels
		scalePercent = checkRegistryDPI("HKCU\\Control Panel\\Desktop", "LogPixels");
		if (scalePercent != 100) return scalePercent;
		
		// Method 3: Check HKLM\SOFTWARE\Microsoft\Windows NT\CurrentVersion\FontDPI - LogPixels
		scalePercent = checkRegistryDPI("HKLM\\SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\FontDPI", "LogPixels");
		if (scalePercent != 100) return scalePercent;
		
		// Method 4: Check Windows 10/11 DPI Awareness registry
		scalePercent = checkRegistryDPI("HKCU\\Control Panel\\Desktop\\PerMonitorSettings", "DpiValue");
		if (scalePercent != 100) return scalePercent;
		
		// Method 5: Check System DPI via Win32_DesktopMonitor
		scalePercent = getSystemDPIFromWMI();
		if (scalePercent != 100) return scalePercent;
		
		// Method 6: Check Display Settings via PowerShell
		scalePercent = getDPIFromPowerShell();
		if (scalePercent != 100) return scalePercent;
		
		// Method 7: Check Windows Registry Display Settings
		scalePercent = checkDisplaySettingsRegistry();
		if (scalePercent != 100) return scalePercent;
		
		// Method 8: Try PowerShell command to get DPI
		try {
			ProcessBuilder psBuilder = new ProcessBuilder("powershell", "-Command", 
				"Add-Type -AssemblyName System.Windows.Forms; [System.Windows.Forms.SystemInformation]::PrimaryMonitorSize.Width / [System.Windows.Forms.Screen]::PrimaryScreen.Bounds.Width * 100");
			psBuilder.redirectErrorStream(true);
			Process psProcess = psBuilder.start();
			
			java.io.BufferedReader psReader = new java.io.BufferedReader(
					new java.io.InputStreamReader(psProcess.getInputStream()));
			String line;
			
			while ((line = psReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && line.matches("\\d+(\\.\\d+)?")) {
					try {
						double scale = Double.parseDouble(line);
						scalePercent = (int) Math.round(scale);
						break;
					} catch (NumberFormatException e) {
						// Continue to next method
					}
				}
			}
			psProcess.waitFor();
			
			if (scalePercent != 100) return scalePercent;
		} catch (Exception e) {
			// Continue to next method
		}
		
		// Method 9: Compare Toolkit resolution with WMIC resolution
		try {
			Dimension toolkitSize = Toolkit.getDefaultToolkit().getScreenSize();
			int toolkitWidth = (int) toolkitSize.getWidth();
			int toolkitHeight = (int) toolkitSize.getHeight();
			
			// Get WMIC resolution again for comparison
			ProcessBuilder wmicBuilder = new ProcessBuilder("wmic", "path", 
					"Win32_VideoController", "get", "CurrentHorizontalResolution,CurrentVerticalResolution");
			wmicBuilder.redirectErrorStream(true);
			Process wmicProcess = wmicBuilder.start();
			
			java.io.BufferedReader wmicReader = new java.io.BufferedReader(
					new java.io.InputStreamReader(wmicProcess.getInputStream()));
			String line;
			int wmicWidth = 0, wmicHeight = 0;
			
			while ((line = wmicReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && !line.contains("CurrentHorizontalResolution")) {
					String[] parts = line.split("\\s+");
					if (parts.length >= 2) {
						try {
							wmicWidth = Integer.parseInt(parts[0]);
							wmicHeight = Integer.parseInt(parts[1]);
							break;
						} catch (NumberFormatException e) {
							// Skip invalid lines
						}
					}
				}
			}
			wmicProcess.waitFor();
			
			// Calculate scale based on resolution difference
			if (wmicWidth > 0 && wmicHeight > 0 && toolkitWidth > 0 && toolkitHeight > 0) {
				double scaleX = (double) wmicWidth / toolkitWidth;
				double scaleY = (double) wmicHeight / toolkitHeight;
				double avgScale = (scaleX + scaleY) / 2.0;
				scalePercent = (int) Math.round(avgScale * 100);
			}
		} catch (Exception e) {
			// Use default scale
		}
		
		return scalePercent;
	}
	
	private static int getSystemDPIFromWMI() {
		try {
			ProcessBuilder wmicBuilder = new ProcessBuilder("wmic", "path", 
					"Win32_DesktopMonitor", "get", "PixelsPerXLogicalInch");
			wmicBuilder.redirectErrorStream(true);
			Process wmicProcess = wmicBuilder.start();
			
			java.io.BufferedReader wmicReader = new java.io.BufferedReader(
					new java.io.InputStreamReader(wmicProcess.getInputStream()));
			String line;
			
			while ((line = wmicReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && !line.contains("PixelsPerXLogicalInch") && line.matches("\\d+")) {
					try {
						int dpi = Integer.parseInt(line);
						return (dpi * 100) / 96; // 96 DPI = 100%
					} catch (NumberFormatException e) {
						// Skip invalid lines
					}
				}
			}
			wmicProcess.waitFor();
		} catch (Exception e) {
			// Continue to next method
		}
		return 100;
	}
	
	private static int getDPIFromPowerShell() {
		try {
			// PowerShell command to get DPI from Windows Display Settings
			String psCommand = "Add-Type -AssemblyName System.Windows.Forms; " +
					"$dpi = [System.Windows.Forms.Screen]::PrimaryScreen.Bounds.Width; " +
					"$scale = [System.Windows.Forms.SystemInformation]::PrimaryMonitorSize.Width / $dpi * 100; " +
					"Write-Output $scale";
			
			ProcessBuilder psBuilder = new ProcessBuilder("powershell", "-Command", psCommand);
			psBuilder.redirectErrorStream(true);
			Process psProcess = psBuilder.start();
			
			java.io.BufferedReader psReader = new java.io.BufferedReader(
					new java.io.InputStreamReader(psProcess.getInputStream()));
			String line;
			
			while ((line = psReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty() && line.matches("\\d+(\\.\\d+)?")) {
					try {
						double scale = Double.parseDouble(line);
						return (int) Math.round(scale);
					} catch (NumberFormatException e) {
						// Continue
					}
				}
			}
			psProcess.waitFor();
		} catch (Exception e) {
			// Continue to next method
		}
		return 100;
	}
	
	private static int checkDisplaySettingsRegistry() {
		try {
			// Check Windows 10/11 Display Configuration in registry
			ProcessBuilder regBuilder = new ProcessBuilder("reg", "query", 
					"HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\ImmersiveShell\\BrandConfiguration", 
					"/v", "DesiredDPI");
			regBuilder.redirectErrorStream(true);
			Process regProcess = regBuilder.start();
			
			java.io.BufferedReader regReader = new java.io.BufferedReader(
					new java.io.InputStreamReader(regProcess.getInputStream()));
			String line;
			
			while ((line = regReader.readLine()) != null) {
				if (line.contains("DesiredDPI")) {
					String[] parts = line.trim().split("\\s+");
					if (parts.length >= 3) {
						try {
							String dpiValue = parts[parts.length - 1];
							int dpi;
							if (dpiValue.startsWith("0x")) {
								dpi = Integer.decode(dpiValue);
							} else {
								dpi = Integer.parseInt(dpiValue);
							}
							return (dpi * 100) / 96;
						} catch (NumberFormatException e) {
							// Continue
						}
					}
				}
			}
			regProcess.waitFor();
			
			// Also try checking NVIDIA/AMD display settings if available
			String[] additionalPaths = {
					"HKCU\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\ThemeManager",
					"HKLM\\SYSTEM\\CurrentControlSet\\Hardware Profiles\\Current\\Software\\Fonts"
			};
			
			for (String path : additionalPaths) {
				ProcessBuilder additionalRegBuilder = new ProcessBuilder("reg", "query", path);
				additionalRegBuilder.redirectErrorStream(true);
				Process additionalRegProcess = additionalRegBuilder.start();
				
				java.io.BufferedReader additionalRegReader = new java.io.BufferedReader(
						new java.io.InputStreamReader(additionalRegProcess.getInputStream()));
				
				while ((line = additionalRegReader.readLine()) != null) {
					if (line.contains("LogPixels") || line.contains("DPI")) {
						// Extract DPI value if found
						String[] parts = line.trim().split("\\s+");
						if (parts.length >= 3) {
							try {
								String dpiValue = parts[parts.length - 1];
								int dpi;
								if (dpiValue.startsWith("0x")) {
									dpi = Integer.decode(dpiValue);
								} else {
									dpi = Integer.parseInt(dpiValue);
								}
								int scale = (dpi * 100) / 96;
								if (scale != 100) return scale;
							} catch (NumberFormatException e) {
								// Continue
							}
						}
					}
				}
				additionalRegProcess.waitFor();
			}
			
		} catch (Exception e) {
			// Continue to next method
		}
		return 100;
	}
	
	private static int checkRegistryDPI(String regPath, String valueName) {
		try {
			ProcessBuilder regBuilder = new ProcessBuilder("reg", "query", regPath, "/v", valueName);
			regBuilder.redirectErrorStream(true);
			Process regProcess = regBuilder.start();
			
			java.io.BufferedReader regReader = new java.io.BufferedReader(
					new java.io.InputStreamReader(regProcess.getInputStream()));
			String line;
			
			while ((line = regReader.readLine()) != null) {
				if (line.contains(valueName)) {
					String[] parts = line.trim().split("\\s+");
					if (parts.length >= 3) {
						try {
							String dpiValue = parts[parts.length - 1];
							int dpi;
							if (dpiValue.startsWith("0x")) {
								dpi = Integer.decode(dpiValue);
							} else {
								dpi = Integer.parseInt(dpiValue);
							}
							// 96 DPI = 100%, 144 DPI = 150%, 192 DPI = 200%
							return (dpi * 100) / 96;
						} catch (NumberFormatException e) {
							// Continue to next registry location
						}
					}
				}
			}
			regProcess.waitFor();
		} catch (Exception e) {
			// Continue to next method
		}
		return 100; // Default scale if not found
	}
	
	public static String getOperatingSystem() {
		return System.getProperty("os.name");
	}
	
	public static String getOperatingSystemVersion() {
		return System.getProperty("os.version");
	}
	
	public static String getDeviceModel() {
		String os = System.getProperty("os.name").toLowerCase();
		String deviceModel = "Unknown";
		
		try {
			ProcessBuilder processBuilder;
			if (os.contains("win")) {
				processBuilder = new ProcessBuilder("wmic", "csproduct", "get", "name");
			} else if (os.contains("mac")) {
				processBuilder = new ProcessBuilder("sysctl", "-n", "hw.model");
			} else {
				// Linux
				processBuilder = new ProcessBuilder("cat", "/sys/devices/virtual/dmi/id/product_name");
			}
			
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			
			java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(process.getInputStream()));
			String line;
			StringBuilder output = new StringBuilder();
			
			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty() && !line.trim().equalsIgnoreCase("Name")) {
					output.append(line.trim());
				}
			}
			
			process.waitFor();
			deviceModel = output.toString().trim();
			
			if (deviceModel.isEmpty()) {
				deviceModel = System.getProperty("os.name") + " " + System.getProperty("os.arch");
			}
		} catch (Exception e) {
			deviceModel = System.getProperty("os.name") + " " + System.getProperty("os.arch");
		}
		
		return deviceModel;
	}
	
	public static String getChromeBrowserVersion() {
		String os = System.getProperty("os.name").toLowerCase();
		String chromeVersion = "Unknown";
		
		try {
			ProcessBuilder processBuilder;
			if (os.contains("win")) {
				processBuilder = new ProcessBuilder("reg", "query", 
						"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon", "/v", "version");
			} else if (os.contains("mac")) {
				processBuilder = new ProcessBuilder("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", "--version");
			} else {
				// Linux
				processBuilder = new ProcessBuilder("google-chrome", "--version");
			}
			
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			
			java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(process.getInputStream()));
			String line;
			
			while ((line = reader.readLine()) != null) {
				if (os.contains("win")) {
					// Windows registry output format: "    version    REG_SZ    xxx.x.xxxx.xxx"
					if (line.contains("version")) {
						String[] parts = line.trim().split("\\s+");
						if (parts.length >= 3) {
							chromeVersion = parts[parts.length - 1];
						}
					}
				} else {
					// Mac/Linux output format: "Google Chrome xxx.x.xxxx.xxx"
					if (line.contains("Google Chrome")) {
						chromeVersion = line.replace("Google Chrome", "").trim();
					}
				}
			}
			
			process.waitFor();
		} catch (Exception e) {
			chromeVersion = "Unable to detect";
		}
		
		return chromeVersion;
	}
	
	public static String getTestingTool() {
		String testingTool = System.getenv("Application");
		if (testingTool == null || testingTool.isEmpty()) {
			testingTool = "Unknown";
		}
		return testingTool;
	}
	
	public static String getTestingToolVersion() {
		String testingToolVersion = System.getenv("version");
		if (testingToolVersion == null || testingToolVersion.isEmpty()) {
			testingToolVersion = "Unknown";
		}
		return testingToolVersion;
	}
	
	public static void createTestCycle() throws Exception {
		String testCycle_Name = "Desktop_TestCycle_" + dateTime;
		String testingTool = getTestingTool();
		String testingToolVersion = getTestingToolVersion();
		String browser = "Chrome";
		String browserVersion = getChromeBrowserVersion();
		String deviceModel = getDeviceModel();
		String operatingSystem = getOperatingSystem();
		String operatingSystemVersion = getOperatingSystemVersion();
		String displayResolution = getDisplayResolution();
	}
}