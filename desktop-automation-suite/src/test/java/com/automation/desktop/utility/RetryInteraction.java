package com.automation.desktop.utility;

import java.util.Optional;

import com.automation.desktop.utility.Interaction.InteractionType;
import com.automation.desktop.utility.Interaction.LocatorStrategy;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;

/**
 * RetryInteraction - A wrapper class for Interaction that provides automatic retry functionality.
 * 
 * Use this class for potentially flaky operations like clicks, dropdown selections, etc.
 * Use regular Interaction for stable operations like getting text or checking element visibility.
 * 
 * Example usage:
 * - RetryInteraction.performAction(driver, LocatorStrategy.XPATH, InteractionType.CLICK, locator);
 * - RetryInteraction.performAction(driver, LocatorStrategy.NAME, InteractionType.CLICK_DROPDOWN, locator);
 * 
 * @author Automation Framework
 */
public class RetryInteraction {

    // Default retry configuration
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_RETRY_DELAY_MS = 2000;
    
    // Configurable retry settings
    private static int maxRetries = DEFAULT_MAX_RETRIES;
    private static int retryDelayMs = DEFAULT_RETRY_DELAY_MS;

    /**
     * Configure the retry settings globally
     * @param retries Maximum number of retry attempts
     * @param delayMs Delay in milliseconds between retries
     */
    public static void configure(int retries, int delayMs) {
        maxRetries = retries;
        retryDelayMs = delayMs;
        TestExecutionLogger.info("RetryInteraction configured with maxRetries=" + retries + ", delayMs=" + delayMs);
    }

    /**
     * Reset retry settings to default values
     */
    public static void resetToDefaults() {
        maxRetries = DEFAULT_MAX_RETRIES;
        retryDelayMs = DEFAULT_RETRY_DELAY_MS;
    }

    /**
     * Perform action with retry - basic version
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy strategy,
            InteractionType type,
            String locator) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, strategy, type, locator),
            type.getDescription(), locator);
    }

    /**
     * Perform action with retry - with value parameter
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy strategy,
            InteractionType type,
            String locator,
            String value) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, strategy, type, locator, value),
            type.getDescription(), locator);
    }

    /**
     * Perform action with retry - with timeout parameter
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy strategy,
            InteractionType type,
            String locator,
            int timeoutInSeconds) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, strategy, type, locator, timeoutInSeconds),
            type.getDescription(), locator);
    }

    /**
     * Perform action with retry - with Optional timeout
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy strategy,
            InteractionType type,
            String locator,
            Optional<Integer> timeoutInSeconds) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, strategy, type, locator, timeoutInSeconds),
            type.getDescription(), locator);
    }

    /**
     * Perform action with retry - with Optional value and Optional timeout
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy strategy,
            InteractionType type,
            String locator,
            Optional<String> value,
            Optional<Integer> timeoutInSeconds) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, strategy, type, locator, value, timeoutInSeconds),
            type.getDescription(), locator);
    }

    /**
     * Perform action with retry - drag and drop with source and target
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy sourceStrategy,
            String sourceLocator,
            LocatorStrategy targetStrategy,
            String targetLocator,
            InteractionType type) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator, type),
            type.getDescription(), sourceLocator + " -> " + targetLocator);
    }

    /**
     * Perform action with retry - drag and drop with y coordinate
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy sourceStrategy,
            String sourceLocator,
            LocatorStrategy targetStrategy,
            String targetLocator,
            InteractionType type,
            String y) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator, type, y),
            type.getDescription(), sourceLocator + " -> " + targetLocator);
    }

    /**
     * Perform action with retry - drag and drop with Optional timeout
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy sourceStrategy,
            String sourceLocator,
            LocatorStrategy targetStrategy,
            String targetLocator,
            InteractionType type,
            Optional<Integer> timeoutInSeconds) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator, type, timeoutInSeconds),
            type.getDescription(), sourceLocator + " -> " + targetLocator);
    }

    /**
     * Perform action with retry - drag and drop with y coordinate and Optional timeout
     */
    public static Object performAction(
            WindowsDriver<WindowsElement> driver,
            LocatorStrategy sourceStrategy,
            String sourceLocator,
            LocatorStrategy targetStrategy,
            String targetLocator,
            InteractionType type,
            String y,
            Optional<Integer> timeoutInSeconds) {
        return performActionWithRetry(() -> 
            Interaction.performAction(driver, sourceStrategy, sourceLocator, targetStrategy, targetLocator, type, y, timeoutInSeconds),
            type.getDescription(), sourceLocator + " -> " + targetLocator);
    }

    /**
     * Core retry logic - wraps any action with retry capability
     */
    private static Object performActionWithRetry(ActionSupplier action, String actionDescription, String locator) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    TestExecutionLogger.info(String.format("[RetryInteraction] Attempt %d of %d: %s on [%s]", 
                        attempt, maxRetries, actionDescription, locator));
                }
                
                Object result = action.execute();
                
                if (attempt > 1) {
                    TestExecutionLogger.info(String.format("[RetryInteraction] Action succeeded on attempt %d: %s on [%s]", 
                        attempt, actionDescription, locator));
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                TestExecutionLogger.warning(String.format("[RetryInteraction] Attempt %d of %d failed: %s on [%s]. Error: %s", 
                    attempt, maxRetries, actionDescription, locator, e.getMessage()));
                
                if (attempt < maxRetries) {
                    TestExecutionLogger.info(String.format("[RetryInteraction] Waiting %dms before retry...", retryDelayMs));
                    Waits.waitForFixedDuration(retryDelayMs);
                }
            }
        }
        
        // All retries exhausted
        String errorMessage = String.format("[RetryInteraction] Action failed after %d attempts: %s on [%s]", 
            maxRetries, actionDescription, locator);
        TestExecutionLogger.error(errorMessage);
        
        throw new RuntimeException(errorMessage, lastException);
    }

    /**
     * Functional interface for action execution
     */
    @FunctionalInterface
    private interface ActionSupplier {
        Object execute() throws Exception;
    }
}
