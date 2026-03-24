package com.automation.desktop.listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.ITestListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FullSeparatorListener implements IInvokedMethodListener, ITestListener {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private boolean executionStarted = false;

    // ===== Invoked method logging =====
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        // Print global starting banner once
        if (!executionStarted) {
            System.out.println("\n========== STARTING TEST CASE EXECUTION ==========\n");
            executionStarted = true;
        }
        printMethodLog("STARTING", method, testResult);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        printMethodLog("ENDING", method, testResult);
    }

    // ===== TestNG finish hook =====
    @Override
    public void onFinish(ITestContext context) {
        System.out.println("\n========== TEST CASE EXECUTION COMPLETE ==========\n");
    }

    // ===== Utility methods =====
    private void printMethodLog(String status, IInvokedMethod method, ITestResult testResult) {
        String type = getMethodType(method);
        String methodName = getSafeMethodName(method);

        System.out.println("\n========== " + status + " " + type + " ==========");
        System.out.println("Method: " + methodName
                + " | Test: " + (testResult.getMethod() != null ? testResult.getMethod().getMethodName() : "N/A")
                + " | Time: " + sdf.format(new Date()));
        System.out.println("====================================\n");
    }

    private String getMethodType(IInvokedMethod method) {
        if (method.isTestMethod()) return "TEST";

        if (method.isConfigurationMethod() && method.getTestMethod() != null) {
            if (method.getTestMethod().isBeforeMethodConfiguration()) return "BEFORE_METHOD";
            if (method.getTestMethod().isAfterMethodConfiguration()) return "AFTER_METHOD";
            if (method.getTestMethod().isBeforeClassConfiguration()) return "BEFORE_CLASS";
            if (method.getTestMethod().isAfterClassConfiguration()) return "AFTER_CLASS";
            if (method.getTestMethod().isBeforeTestConfiguration()) return "BEFORE_TEST";
            if (method.getTestMethod().isAfterTestConfiguration()) return "AFTER_TEST";
            if (method.getTestMethod().isBeforeSuiteConfiguration()) return "BEFORE_SUITE";
            if (method.getTestMethod().isAfterSuiteConfiguration()) return "AFTER_SUITE";
        }
        return "CONFIGURATION";
    }

    private String getSafeMethodName(IInvokedMethod method) {
        if (method.getTestMethod() != null) {
            return method.getTestMethod().getMethodName();
        }
        return "UNKNOWN_METHOD";
    }
}