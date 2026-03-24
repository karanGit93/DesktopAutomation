package com.automation.desktop.utility;

import java.util.*;

/**
 * TestReportStore — In-memory data store for PDF test execution reports.
 *
 * <p>Holds per-test metadata, ordered step entries (each optionally carrying an
 * inline screenshot path), timing, and final status. Data is consumed by
 * {@link PdfReportGenerator} at suite end.</p>
 *
 * <p>LinkedHashMap preserves execution order so the PDF renders tests
 * sequentially (Option B).</p>
 */
public class TestReportStore {

    // ========== STATE ==========

    /** Execution-ordered map: methodName → TestResultData */
    private static final Map<String, TestResultData> testResults = new LinkedHashMap<>();
    private static String currentTestName = null;

    // ========== SUITE LIFECYCLE ==========

    /** Reset all stored data before a new suite run. */
    public static void reset() {
        testResults.clear();
        currentTestName = null;
    }

    // ========== CURRENT TEST TRACKING ==========

    /** Set the active test name; creates a new entry if first occurrence. */
    public static void setCurrentTestName(String testName) {
        currentTestName = testName;
        if (!testResults.containsKey(testName)) {
            testResults.put(testName, new TestResultData(testName));
        }
    }

    public static String getCurrentTestName() {
        return currentTestName;
    }

    // ========== METADATA ==========

    /** Store package, class, and method names extracted from ITestResult. */
    public static void setMetadata(String testName, String packageName, String className, String methodName) {
        TestResultData data = testResults.get(testName);
        if (data != null) {
            data.packageName = packageName;
            data.className = className;
            data.methodName = methodName;
        }
    }

    // ========== TIMING ==========

    /** Record test start time (epoch millis). */
    public static void setStartTime(String testName, long startTimeMs) {
        TestResultData data = testResults.get(testName);
        if (data != null) {
            data.startTimeMs = startTimeMs;
        }
    }

    /** Record test end time (epoch millis). */
    public static void setEndTime(String testName, long endTimeMs) {
        TestResultData data = testResults.get(testName);
        if (data != null) {
            data.endTimeMs = endTimeMs;
        }
    }

    // ========== STEPS ==========

    /** Add a step entry (no screenshot). */
    public static void addStep(String testName, String description) {
        TestResultData data = testResults.get(testName);
        if (data != null) {
            data.steps.add(new StepEntry(description));
        }
    }

    /** Attach a screenshot path to the most recent step of the given test. */
    public static void attachScreenshotToLastStep(String testName, String screenshotPath) {
        TestResultData data = testResults.get(testName);
        if (data != null && !data.steps.isEmpty()) {
            data.steps.get(data.steps.size() - 1).screenshotPath = screenshotPath;
        }
    }

    // ========== STATUS & ERROR ==========

    public static void setTestResult(String testName, String result) {
        TestResultData data = testResults.get(testName);
        if (data != null) {
            data.status = result;
        }
    }

    public static void setErrorMessage(String testName, String errorMessage) {
        TestResultData data = testResults.get(testName);
        if (data != null) {
            data.errorMessage = errorMessage;
        }
    }

    // ========== SCREENSHOTS (backward-compatible) ==========

    /** Add a screenshot and also attach it to the most recent step for inline PDF rendering. */
    public static void addScreenshot(String testName, String path) {
        TestResultData data = testResults.get(testName);
        if (data != null) {
            data.screenshots.add(path);
            // Also attach to most recent step so PdfReportGenerator renders it inline
            if (!data.steps.isEmpty()) {
                data.steps.get(data.steps.size() - 1).screenshotPath = path;
            }
        }
    }

    // ========== QUERY ==========

    /** Return all results in execution order (LinkedHashMap guarantees this). */
    public static Collection<TestResultData> getAllResults() {
        return testResults.values();
    }

    // ========== INNER CLASSES ==========

    /**
     * StepEntry — A single logged step, optionally carrying an inline screenshot.
     *
     * <p>When {@code screenshotPath} is non-null, PdfReportGenerator renders the
     * image directly below this step's text line.</p>
     */
    public static class StepEntry {
        public final String description;
        public String screenshotPath; // null = no screenshot for this step

        public StepEntry(String description) {
            this.description = description;
        }
    }

    /**
     * TestResultData — All data for a single test method's execution.
     *
     * <p>Public static so {@link PdfReportGenerator} can read fields directly.</p>
     */
    public static class TestResultData {
        public final String testName;
        public String packageName;
        public String className;
        public String methodName;
        public String status;
        public String errorMessage;
        public long startTimeMs;
        public long endTimeMs;
        public final List<StepEntry> steps = new ArrayList<>();
        public final List<String> screenshots = new ArrayList<>();

        public TestResultData(String testName) {
            this.testName = testName;
        }

        /** Duration in seconds, or 0 if timing not captured. */
        public double getDurationSeconds() {
            if (endTimeMs > 0 && startTimeMs > 0) {
                return (endTimeMs - startTimeMs) / 1000.0;
            }
            return 0;
        }
    }
}
