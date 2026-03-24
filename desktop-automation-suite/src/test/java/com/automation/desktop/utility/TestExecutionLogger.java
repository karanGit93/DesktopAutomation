package com.automation.desktop.utility;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.testng.Reporter;

import com.automation.desktop.base.ApplicationLaunch;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestExecutionLogger {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int skippedTests = 0;
    private static int currentTestSteps = 0;

    static {
        try {
            System.setProperty("file.encoding", "UTF-8");
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public enum LogLevel {
        INFO("ℹ️"),
        ERROR("❌"),
        WARNING("⚠️"),
        SUCCESS("✅"),
        START("⚡"),
        SKIP("⏭️"),
        TEST("🧪"),
        STEP("📝");

        private final String icon;

        LogLevel(String icon) {
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }
    }

    /** Base logging method — does not call info() again */
    private static void logMessage(LogLevel level, String message) {
        String formattedMessage = String.format("[%s] %s %s", getCurrentTime(), level.getIcon(), message);
        System.out.println(formattedMessage);
        Reporter.log(formattedMessage);

        // Map log level to Allure status
        Status allureStatus;
        switch (level) {
            case ERROR:
                allureStatus = Status.FAILED;
                break;
            case WARNING:
                allureStatus = Status.BROKEN;
                break;
            case SKIP:
                allureStatus = Status.SKIPPED;
                break;
            default:
                allureStatus = Status.PASSED;
        }

        Allure.step(formattedMessage, allureStatus);
    }

    // Public helper methods
    public static void info(String message) {
        logMessage(LogLevel.INFO, message);
    }

    public static void error(String message) {
        logMessage(LogLevel.ERROR, message);
    }

    public static void warning(String message) {
        logMessage(LogLevel.WARNING, message);
    }

    public static void logTestStart(String testName, String description) {
        totalTests++;
        currentTestSteps = 0;
        String message = String.format("%n----------------------------------------------------------New Test Execution Started----------------------------------------------------------%nStarting test: %s%s",
                testName,
                (description != null && !description.isEmpty())
                        ? String.format("%n%s Description: %s", LogLevel.INFO.getIcon(), description)
                        : "");
        logMessage(LogLevel.START, message);
    }

    public static void logTestSuccess(String testName) {
        passedTests++;
        logMessage(LogLevel.SUCCESS, String.format("Test passed: %s", testName));
    }

    public static void logTestFailure(String testName, String error) {
        failedTests++;
        logMessage(LogLevel.ERROR, String.format("Test failed: %s%nError: %s and reference Screenshot: %s", testName, error, Utility.screenshotName));
        
    }

    public static void logTestSkipped(String testName, String reason) {
        skippedTests++;
        String message = String.format("Test skipped: %s%s",
                testName,
                (reason != null && !reason.isEmpty())
                        ? String.format("%nReason: %s", reason)
                        : "");
        logMessage(LogLevel.SKIP, message);
    }

    public static void logAssertionResult(String assertionType, String expected, String actual, boolean passed) {
        LogLevel level = passed ? LogLevel.SUCCESS : LogLevel.ERROR;
        String result = passed ? "PASSED" : "FAILED";
        String message = String.format("%s - %s | Expected: %s | Actual: %s", assertionType, result, expected, actual);
        logMessage(level, message);

        // Bridge to PDF report store
        String testName = TestReportStore.getCurrentTestName();
        if (testName != null) {
            TestReportStore.addStep(testName, message);
        }
    }

    public static void logStep(String stepDescription) {
        currentTestSteps++;
        logMessage(LogLevel.STEP, String.format("Step %d: %s", currentTestSteps, stepDescription));
        // Bridge to PDF report store — every UI step now flows into the PDF
        String testName = TestReportStore.getCurrentTestName();
        if (testName != null) {
            TestReportStore.addStep(testName, "Step " + currentTestSteps + ": " + stepDescription);
        }
    }

    public static void logSummary() {
        double successRate = totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0;
        String summary = String.format("""
                
                ============= Test Execution Summary =============
                Total Tests: %d
                Passed: %d %s
                Failed: %d %s
                Skipped: %d %s
                ================================================
                Success Rate: %.2f%%
                ================================================
                Test Cycle ID: %s
                Test Cycle Name: %s
                ================================================
                """,
                totalTests,
                passedTests, LogLevel.SUCCESS.getIcon(),
                failedTests, LogLevel.ERROR.getIcon(),
                skippedTests, LogLevel.SKIP.getIcon(),
                successRate
        );

        logMessage(LogLevel.INFO, summary);
    }

    private static String getCurrentTime() {
        return LocalDateTime.now().format(formatter);
    }
}
