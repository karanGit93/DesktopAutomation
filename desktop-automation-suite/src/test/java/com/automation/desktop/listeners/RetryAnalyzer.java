package com.automation.desktop.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import com.automation.desktop.utility.TestExecutionLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int DEFAULT_MAX_RETRY_COUNT = 2;
    private static final int SPECIAL_MAX_RETRY_COUNT = 6;
    
    private static final Map<String, Boolean> willBeRetried = new ConcurrentHashMap<>();
    
    private static int totalRetryAttempts = 0;

    @Override
    public boolean retry(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        int maxRetryCount = getMaxRetryCountForMethod(methodName);
        
        if (retryCount < maxRetryCount) {
            retryCount++;
            totalRetryAttempts++; 
            willBeRetried.put(methodName, Boolean.TRUE);
            TestExecutionLogger.info("⟳ Retry scheduled for test '" + methodName + 
                "' - Attempt " + retryCount + " of " + maxRetryCount);
            return true;
        }
        
        willBeRetried.put(methodName, Boolean.FALSE);
        TestExecutionLogger.info("❌ Test '" + methodName + 
            "' - All " + maxRetryCount + " retries exhausted");
        return false;
    }

    public static boolean isRetryAvailable(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        
        if (result.getStatus() == ITestResult.SUCCESS) {
            willBeRetried.remove(methodName);
            TestExecutionLogger.info("✅ Test '" + methodName + "' PASSED");
            return false;
        }
        
        Boolean willRetry = willBeRetried.get(methodName);
        
        if (willRetry == null) {
            IRetryAnalyzer retryAnalyzer = result.getMethod().getRetryAnalyzer(result);
            if (retryAnalyzer == null) {
                TestExecutionLogger.info("📋 Test '" + methodName + "' - No retry analyzer");
                return false;
            }
            TestExecutionLogger.info("⚠️ Test '" + methodName + "' - Not in tracking, assuming dependency skip");
            return false;
        }
        
        if (willRetry) {
            TestExecutionLogger.info("🔄 Test '" + methodName + "' - Retry pending");
            return true;
        } else {
            willBeRetried.remove(methodName); // Clean up
            TestExecutionLogger.info("🏁 Test '" + methodName + "' - Final attempt");
            return false;
        }
    }

    public static boolean isFinalAttempt(ITestResult result) {
        return !isRetryAvailable(result);
    }

    public int getRetryCount() {
        return retryCount;
    }

    public static int getMaxRetryCount() {
        return DEFAULT_MAX_RETRY_COUNT; // Return default for backwards compatibility
    }
    
    public static boolean useMaxRetryCount(String testName) {
        return testName.equals("");
    }
    
    private int getMaxRetryCountForMethod(String methodName) {
        // Check if this test method should use maximum retry count
        if (useMaxRetryCount(methodName)) {
            return SPECIAL_MAX_RETRY_COUNT;
        }
        return DEFAULT_MAX_RETRY_COUNT;
    }
    
    public static void clearRetryTracking(String methodName) {
        willBeRetried.remove(methodName);
    }
    
    public static int getTotalRetryAttempts() {
        return totalRetryAttempts;
    }
    
    public static void resetAll() {
        willBeRetried.clear();
        totalRetryAttempts = 0;
    }
    

    
}