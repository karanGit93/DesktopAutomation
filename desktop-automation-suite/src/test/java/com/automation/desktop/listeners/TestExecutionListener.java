package com.automation.desktop.listeners;

import org.testng.*;

import com.automation.desktop.base.BaseSetup;
import com.automation.desktop.utility.PdfReportGenerator;
import com.automation.desktop.utility.TestExecutionLogger;
import com.automation.desktop.utility.TestReportStore;
import com.automation.desktop.utility.Utility;

/**
 * TestExecutionListener - Handles test execution events and reporting.
 * 
 * Counts only FINAL results (not intermediate retry attempts):
 * - Passed: Tests that passed (on 1st attempt or after retry)
 * - Failed: Tests that failed after all retries exhausted
 * - Skipped: Tests skipped due to dependency failure
 * - Retried: Total retry attempts (tracked separately)
 */
public class TestExecutionListener implements ITestListener, ISuiteListener {

    private int passedCount = 0;
    private int failedCount = 0;
    private int skippedCount = 0;

    /** ============ SUITE LEVEL HOOKS ============ */
    @Override
    public void onStart(ISuite suite) {
        TestExecutionLogger.info("==== Test Suite Started: " + suite.getName() + " ====");
        TestReportStore.reset();
        RetryAnalyzer.resetAll(); // Reset retry tracking
    }

    @Override
    public void onFinish(ISuite suite) {
        int retriedCount = RetryAnalyzer.getTotalRetryAttempts();
        int totalTests = passedCount + failedCount + skippedCount;
        
        double passPercent = totalTests > 0 ? (passedCount * 100.0 / totalTests) : 0;
        double failPercent = totalTests > 0 ? (failedCount * 100.0 / totalTests) : 0;
        double skipPercent = totalTests > 0 ? (skippedCount * 100.0 / totalTests) : 0;

        TestExecutionLogger.info("====== Final Execution Summary ======");
        TestExecutionLogger.info("Total Tests: " + totalTests);
        TestExecutionLogger.info("Passed: " + passedCount + " (" + String.format("%.2f", passPercent) + "%)");
        TestExecutionLogger.info("Failed: " + failedCount + " (" + String.format("%.2f", failPercent) + "%)");
        TestExecutionLogger.info("Skipped: " + skippedCount + " (" + String.format("%.2f", skipPercent) + "%)");
        TestExecutionLogger.info("Retried: " + retriedCount + " (retry attempts, not counted in total)");
        TestExecutionLogger.info("====================================");
        TestExecutionLogger.info("======================================✅ All tests finished. Generating consolidated PDF report...=================================");
        PdfReportGenerator.generateReport(passedCount, failedCount, skippedCount);
    }

    /** ============ TEST LEVEL HOOKS ============ */
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        TestExecutionLogger.info("==== Test Case Started: " + testName + " ====");
        String description = result.getMethod().getDescription();
        description = description != null ? description : "No description provided";

        TestReportStore.setCurrentTestName(testName);
        TestExecutionLogger.logTestStart(testName, description);
        TestReportStore.addStep(testName, "Test started: " + description);

        // Populate class/package metadata from ITestResult
        Class<?> testClass = result.getTestClass().getRealClass();
        TestReportStore.setMetadata(testName,
                testClass.getPackage() != null ? testClass.getPackage().getName() : "",
                testClass.getSimpleName(),
                testName);

        // Record start time
        TestReportStore.setStartTime(testName, result.getStartMillis());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        TestExecutionLogger.logTestSuccess(testName);
        TestReportStore.addStep(testName, "Test passed successfully");
        TestReportStore.setTestResult(testName, "PASSED");
        TestReportStore.setEndTime(testName, result.getEndMillis());
        passedCount++;
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String errorMessage = result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown error";

        // Check if this failure will be retried - if so, don't count it yet
        if (RetryAnalyzer.isRetryAvailable(result)) {
            // This is an intermediate failure - will be retried, don't count
            TestExecutionLogger.info("⚠️ Test '" + testName + "' failed but will be retried - not counting yet");
            TestReportStore.addStep(testName, "Test failed (will retry): " + errorMessage);
            Utility.takeScreenshot();
            return;
        }

        // Final failure - count it
        TestExecutionLogger.logTestFailure(testName, errorMessage);
        TestReportStore.addStep(testName, "Test failed: " + errorMessage);
        Utility.takeScreenshot();
        
        String screenshotPath = BaseSetup.takeScreenshot(testName + "_failed");
        if (screenshotPath != null) {
            TestReportStore.addScreenshot(testName, screenshotPath);
        }

        TestReportStore.setTestResult(testName, "FAILED");
        TestReportStore.setErrorMessage(testName, errorMessage);
        TestReportStore.setEndTime(testName, result.getEndMillis());
        failedCount++;
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String reason = result.getThrowable() != null ? result.getThrowable().getMessage() : "Test skipped";

        // Check if this skip is due to retry mechanism (not a genuine skip)
        if (result.wasRetried()) {
            // This is a retry-related skip - don't count
            TestExecutionLogger.info("⏭️ Test '" + testName + "' skipped (retry-related) - not counting");
            return;
        }

        // Genuine skip (dependency failure)
        TestExecutionLogger.logTestSkipped(testName, reason);
        TestReportStore.addStep(testName, "Test skipped: " + reason);

        String screenshotPath = BaseSetup.takeScreenshot(testName + "_skipped");
        if (screenshotPath != null) {
            TestReportStore.addScreenshot(testName, screenshotPath);
        }

        TestReportStore.setTestResult(testName, "SKIPPED");
        TestReportStore.setErrorMessage(testName, reason);
        TestReportStore.setEndTime(testName, result.getEndMillis());
        skippedCount++;
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}

    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
}
