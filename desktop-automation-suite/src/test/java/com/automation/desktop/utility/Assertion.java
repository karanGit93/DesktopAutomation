package com.automation.desktop.utility;

import static org.testng.Assert.*;

import java.util.Objects;

import org.testng.asserts.SoftAssert;

/**
 * Centralized assertion wrapper for the Desktop automation framework.
 * <p>
 * All assertions flow through this class to provide unified logging to:
 * <ul>
 *   <li>Console / TestNG Reporter</li>
 *   <li>Allure report (via TestExecutionLogger)</li>
 *   <li>PDF report (via TestReportStore bridge)</li>
 * </ul>
 * <p>
 * <b>Hard asserts</b> — fail immediately and stop the test.<br>
 * <b>Soft asserts</b> — accumulate failures; call {@link #AssertAll()} at test end to flush.
 *
 * @see TestExecutionLogger#logAssertionResult(String, String, String, boolean)
 */
public class Assertion {

    // ========== SOFT ASSERT STATE ==========

    /** Thread-safe SoftAssert instance — per-thread isolation for parallel execution. */
    private static ThreadLocal<SoftAssert> softAssert = ThreadLocal.withInitial(SoftAssert::new);

    // ==========================================================================
    //  SECTION 1: HARD ASSERTS — TEXT
    // ==========================================================================

    /**
     * Assert two strings are equal (after trimming whitespace).
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the expected value
     */
    public static void AssertText(String actualValue, String expectedValue) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        try {
            assertEquals(actual, expected);
            TestExecutionLogger.logAssertionResult("Text Assertion", expected, actual, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Text Assertion", expected, actual, false);
            throw e;
        }
    }

    /**
     * Assert two strings are equal with a descriptive message.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the expected value
     * @param outputMessage descriptive context shown on failure
     */
    public static void AssertText(String actualValue, String expectedValue, String outputMessage) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        try {
            assertEquals(actual, expected, outputMessage);
            TestExecutionLogger.logAssertionResult("Text Assertion", expected, actual, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Text Assertion", expected, actual, false);
            throw e;
        }
    }

    /**
     * Assert two strings are NOT equal.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the value that should NOT match
     */
    public static void AssertNotEquals(String actualValue, String expectedValue) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        try {
            assertNotEquals(actual, expected);
            TestExecutionLogger.logAssertionResult("Not-Equals Assertion", "not [" + expected + "]", actual, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Not-Equals Assertion", "not [" + expected + "]", actual, false);
            throw e;
        }
    }

    /**
     * Assert two strings are NOT equal with a descriptive message.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the value that should NOT match
     * @param outputMessage descriptive context shown on failure
     */
    public static void AssertNotEquals(String actualValue, String expectedValue, String outputMessage) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        try {
            assertNotEquals(actual, expected, outputMessage);
            TestExecutionLogger.logAssertionResult("Not-Equals Assertion", "not [" + expected + "]", actual, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Not-Equals Assertion", "not [" + expected + "]", actual, false);
            throw e;
        }
    }

    /**
     * Assert actual text contains the expected substring.
     *
     * @param actualValue   the full text from the application
     * @param expectedValue the substring expected within actualValue
     */
    public static void AssertTextContains(String actualValue, String expectedValue) {
        try {
            assertTrue(actualValue != null && actualValue.contains(expectedValue),
                    "Expected text to contain [" + expectedValue + "] but was [" + actualValue + "]");
            TestExecutionLogger.logAssertionResult("Contains Assertion", expectedValue, actualValue, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Contains Assertion", expectedValue, String.valueOf(actualValue), false);
            throw e;
        }
    }

    /**
     * Assert actual text contains the expected substring with a descriptive message.
     *
     * @param actualValue   the full text from the application
     * @param expectedValue the substring expected within actualValue
     * @param outputMessage descriptive context shown on failure
     */
    public static void AssertTextContains(String actualValue, String expectedValue, String outputMessage) {
        try {
            assertTrue(actualValue != null && actualValue.contains(expectedValue), outputMessage);
            TestExecutionLogger.logAssertionResult("Contains Assertion", expectedValue, actualValue, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Contains Assertion", expectedValue, String.valueOf(actualValue), false);
            throw e;
        }
    }

    /**
     * Assert two strings are equal ignoring case.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the expected value
     */
    public static void AssertTextIgnoreCase(String actualValue, String expectedValue) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        boolean match = (actual == null && expected == null)
                || (actual != null && actual.equalsIgnoreCase(expected));
        try {
            assertTrue(match, "Expected (ignore case): [" + expected + "] but was: [" + actual + "]");
            TestExecutionLogger.logAssertionResult("IgnoreCase Assertion", expected, actual, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("IgnoreCase Assertion", expected, actual, false);
            throw e;
        }
    }

    /**
     * Assert actual text starts with the expected prefix.
     *
     * @param actualValue   the full text from the application
     * @param expectedPrefix the prefix expected at the start
     */
    public static void AssertStartsWith(String actualValue, String expectedPrefix) {
        try {
            assertTrue(actualValue != null && actualValue.startsWith(expectedPrefix),
                    "Expected to start with [" + expectedPrefix + "] but was [" + actualValue + "]");
            TestExecutionLogger.logAssertionResult("StartsWith Assertion", expectedPrefix, actualValue, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("StartsWith Assertion", expectedPrefix, String.valueOf(actualValue), false);
            throw e;
        }
    }

    /**
     * Assert actual text ends with the expected suffix.
     *
     * @param actualValue    the full text from the application
     * @param expectedSuffix the suffix expected at the end
     */
    public static void AssertEndsWith(String actualValue, String expectedSuffix) {
        try {
            assertTrue(actualValue != null && actualValue.endsWith(expectedSuffix),
                    "Expected to end with [" + expectedSuffix + "] but was [" + actualValue + "]");
            TestExecutionLogger.logAssertionResult("EndsWith Assertion", expectedSuffix, actualValue, true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("EndsWith Assertion", expectedSuffix, String.valueOf(actualValue), false);
            throw e;
        }
    }

    /**
     * Assert actual text does NOT contain the unexpected substring.
     *
     * @param actualValue     the full text from the application
     * @param unexpectedValue the substring that should NOT be present
     */
    public static void AssertTextNotContains(String actualValue, String unexpectedValue) {
        try {
            assertTrue(actualValue == null || !actualValue.contains(unexpectedValue),
                    "Expected text NOT to contain [" + unexpectedValue + "] but was [" + actualValue + "]");
            TestExecutionLogger.logAssertionResult("NotContains Assertion", "not contain [" + unexpectedValue + "]", String.valueOf(actualValue), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("NotContains Assertion", "not contain [" + unexpectedValue + "]", String.valueOf(actualValue), false);
            throw e;
        }
    }

    /**
     * Assert that a string is null or empty.
     *
     * @param actualValue the value to check
     * @param message     descriptive context (e.g. "Input field should be empty")
     */
    public static void AssertEmpty(String actualValue, String message) {
        boolean isEmpty = (actualValue == null || actualValue.trim().isEmpty());
        try {
            assertTrue(isEmpty, message);
            TestExecutionLogger.logAssertionResult("Empty Assertion [" + message + "]", "empty", String.valueOf(actualValue), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Empty Assertion [" + message + "]", "empty", String.valueOf(actualValue), false);
            throw e;
        }
    }

    /**
     * Assert that a string is NOT null and NOT empty.
     *
     * @param actualValue the value to check
     * @param message     descriptive context (e.g. "Name field should have a value")
     */
    public static void AssertNotEmpty(String actualValue, String message) {
        boolean isNotEmpty = (actualValue != null && !actualValue.trim().isEmpty());
        try {
            assertTrue(isNotEmpty, message);
            TestExecutionLogger.logAssertionResult("NotEmpty Assertion [" + message + "]", "not empty", String.valueOf(actualValue), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("NotEmpty Assertion [" + message + "]", "not empty", String.valueOf(actualValue), false);
            throw e;
        }
    }

    /**
     * Assert that actual text matches the given regex pattern.
     *
     * @param actualValue the value to test
     * @param regex       the regular expression pattern
     */
    public static void AssertMatches(String actualValue, String regex) {
        boolean matches = (actualValue != null && actualValue.matches(regex));
        try {
            assertTrue(matches, "Expected [" + actualValue + "] to match regex [" + regex + "]");
            TestExecutionLogger.logAssertionResult("Regex Assertion", regex, String.valueOf(actualValue), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Regex Assertion", regex, String.valueOf(actualValue), false);
            throw e;
        }
    }

    // ==========================================================================
    //  SECTION 2: HARD ASSERTS — BOOLEAN
    // ==========================================================================

    /**
     * Assert condition is true.
     *
     * @param condition the boolean condition to verify
     */
    public static void AssertBoolean(Boolean condition) {
        try {
            assertTrue(condition != null && condition);
            TestExecutionLogger.logAssertionResult("Boolean Assertion", "true", String.valueOf(condition), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Boolean Assertion", "true", String.valueOf(condition), false);
            throw e;
        }
    }

    /**
     * Assert condition is true with a descriptive message.
     *
     * @param condition the boolean condition to verify
     * @param message   descriptive context shown on failure (e.g. "Element should be visible")
     */
    public static void AssertBoolean(boolean condition, String message) {
        try {
            assertTrue(condition, message);
            TestExecutionLogger.logAssertionResult("Boolean Assertion [" + message + "]", "true", String.valueOf(condition), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Boolean Assertion [" + message + "]", "true", String.valueOf(condition), false);
            throw e;
        }
    }

    /**
     * Assert an Object equals Boolean.TRUE (safe for mixed-type returns from Interaction).
     *
     * @param object the object to verify (typically from performAction return)
     */
    public static void AssertBoolean(Object object) {
        try {
            assertTrue(Boolean.TRUE.equals(object));
            TestExecutionLogger.logAssertionResult("Boolean Assertion", "true", String.valueOf(object), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Boolean Assertion", "true", String.valueOf(object), false);
            throw e;
        }
    }

    /**
     * Assert condition is false.
     *
     * @param condition the boolean condition to verify
     */
    public static void AssertBooleanFalse(Boolean condition) {
        try {
            assertFalse(condition != null && condition);
            TestExecutionLogger.logAssertionResult("Boolean Assertion", "false", String.valueOf(condition), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Boolean Assertion", "false", String.valueOf(condition), false);
            throw e;
        }
    }

    /**
     * Assert condition is false with a descriptive message.
     *
     * @param condition the boolean condition to verify
     * @param message   descriptive context shown on failure (e.g. "Element should be disabled")
     */
    public static void AssertBooleanFalse(boolean condition, String message) {
        try {
            assertFalse(condition, message);
            TestExecutionLogger.logAssertionResult("Boolean Assertion [" + message + "]", "false", String.valueOf(condition), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Boolean Assertion [" + message + "]", "false", String.valueOf(condition), false);
            throw e;
        }
    }

    // ==========================================================================
    //  SECTION 3: HARD ASSERTS — NULL CHECKS
    // ==========================================================================

    /**
     * Assert that an object is null.
     *
     * @param object  the object to verify
     * @param message descriptive context (e.g. "Error dialog should not appear")
     */
    public static void AssertNull(Object object, String message) {
        try {
            assertNull(object, message);
            TestExecutionLogger.logAssertionResult("Null Assertion [" + message + "]", "null", String.valueOf(object), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Null Assertion [" + message + "]", "null", String.valueOf(object), false);
            throw e;
        }
    }

    /**
     * Assert that an object is NOT null.
     *
     * @param object  the object to verify
     * @param message descriptive context (e.g. "Driver should be initialized")
     */
    public static void AssertNotNull(Object object, String message) {
        try {
            assertNotNull(object, message);
            TestExecutionLogger.logAssertionResult("NotNull Assertion [" + message + "]", "not null", String.valueOf(object), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("NotNull Assertion [" + message + "]", "not null", String.valueOf(object), false);
            throw e;
        }
    }

    // ==========================================================================
    //  SECTION 4: HARD ASSERTS — NUMERIC
    // ==========================================================================

    /**
     * Assert that actual is greater than expected.
     *
     * @param actual   the actual numeric value
     * @param expected the value that actual should exceed
     */
    public static void AssertGreaterThan(int actual, int expected) {
        boolean passed = actual > expected;
        try {
            assertTrue(passed, "Expected [" + actual + "] to be greater than [" + expected + "]");
            TestExecutionLogger.logAssertionResult("GreaterThan Assertion", ">" + expected, String.valueOf(actual), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("GreaterThan Assertion", ">" + expected, String.valueOf(actual), false);
            throw e;
        }
    }

    /**
     * Assert that actual is less than expected.
     *
     * @param actual   the actual numeric value
     * @param expected the value that actual should be below
     */
    public static void AssertLessThan(int actual, int expected) {
        boolean passed = actual < expected;
        try {
            assertTrue(passed, "Expected [" + actual + "] to be less than [" + expected + "]");
            TestExecutionLogger.logAssertionResult("LessThan Assertion", "<" + expected, String.valueOf(actual), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("LessThan Assertion", "<" + expected, String.valueOf(actual), false);
            throw e;
        }
    }

    /**
     * Assert two integers are equal.
     *
     * @param actual   the actual numeric value
     * @param expected the expected numeric value
     */
    public static void AssertEquals(int actual, int expected) {
        try {
            assertEquals(actual, expected);
            TestExecutionLogger.logAssertionResult("Numeric Equals Assertion", String.valueOf(expected), String.valueOf(actual), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("Numeric Equals Assertion", String.valueOf(expected), String.valueOf(actual), false);
            throw e;
        }
    }

    /**
     * Assert that actual is greater than or equal to expected.
     *
     * @param actual   the actual numeric value
     * @param expected the threshold value
     */
    public static void AssertGreaterThanOrEqual(int actual, int expected) {
        boolean passed = actual >= expected;
        try {
            assertTrue(passed, "Expected [" + actual + "] to be >= [" + expected + "]");
            TestExecutionLogger.logAssertionResult("GreaterOrEqual Assertion", ">=" + expected, String.valueOf(actual), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("GreaterOrEqual Assertion", ">=" + expected, String.valueOf(actual), false);
            throw e;
        }
    }

    /**
     * Assert that actual is less than or equal to expected.
     *
     * @param actual   the actual numeric value
     * @param expected the threshold value
     */
    public static void AssertLessThanOrEqual(int actual, int expected) {
        boolean passed = actual <= expected;
        try {
            assertTrue(passed, "Expected [" + actual + "] to be <= [" + expected + "]");
            TestExecutionLogger.logAssertionResult("LessOrEqual Assertion", "<=" + expected, String.valueOf(actual), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("LessOrEqual Assertion", "<=" + expected, String.valueOf(actual), false);
            throw e;
        }
    }

    // ==========================================================================
    //  SECTION 4b: HARD ASSERTS — LIST / COLLECTION
    // ==========================================================================

    /**
     * Assert that a list has the expected size.
     *
     * @param list         the list to check
     * @param expectedSize the expected number of items
     * @param message      descriptive context (e.g. "Dropdown should have 5 options")
     */
    public static void AssertListSize(java.util.List<?> list, int expectedSize, String message) {
        int actualSize = (list != null) ? list.size() : 0;
        try {
            assertEquals(actualSize, expectedSize, message);
            TestExecutionLogger.logAssertionResult("ListSize Assertion [" + message + "]", String.valueOf(expectedSize), String.valueOf(actualSize), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("ListSize Assertion [" + message + "]", String.valueOf(expectedSize), String.valueOf(actualSize), false);
            throw e;
        }
    }

    /**
     * Assert that a list contains the expected item.
     *
     * @param list     the list to search
     * @param expected the item expected to be in the list
     * @param message  descriptive context
     */
    public static void AssertListContains(java.util.List<?> list, Object expected, String message) {
        boolean contains = (list != null && list.contains(expected));
        try {
            assertTrue(contains, message);
            TestExecutionLogger.logAssertionResult("ListContains Assertion [" + message + "]", String.valueOf(expected), String.valueOf(list), true);
        } catch (AssertionError e) {
            TestExecutionLogger.logAssertionResult("ListContains Assertion [" + message + "]", String.valueOf(expected), String.valueOf(list), false);
            throw e;
        }
    }

    // ==========================================================================
    //  SECTION 5: HARD ASSERT — FORCED FAILURE
    // ==========================================================================

    /**
     * Force-fail the test with a descriptive message.
     * Logs the failure first, then throws via {@code fail()}.
     * Screenshot is handled by onTestFailure() listener — not here.
     *
     * @param message the failure reason
     */
    public static void FailureMessage(String message) {
        TestExecutionLogger.logAssertionResult("Forced Failure", "N/A", message, false);
        fail(message);
    }

    // ==========================================================================
    //  SECTION 6: SOFT ASSERTS — TEXT
    // ==========================================================================

    /**
     * Soft-assert two strings are equal (after trimming).
     * Failure is accumulated — call {@link #AssertAll()} to flush.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the expected value
     */
    public static void SoftAssertText(String actualValue, String expectedValue) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        boolean passed = Objects.equals(actual, expected);
        TestExecutionLogger.logAssertionResult("Soft Text Assertion", expected, actual, passed);
        softAssert.get().assertEquals(actual, expected);
    }

    /**
     * Soft-assert two strings are equal with a descriptive message.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the expected value
     * @param outputMessage descriptive context
     */
    public static void SoftAssertText(String actualValue, String expectedValue, String outputMessage) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        boolean passed = Objects.equals(actual, expected);
        TestExecutionLogger.logAssertionResult("Soft Text Assertion [" + outputMessage + "]", expected, actual, passed);
        softAssert.get().assertEquals(actual, expected, outputMessage);
    }

    /**
     * Soft-assert two strings are NOT equal.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the value that should NOT match
     */
    public static void SoftAssertNotEquals(String actualValue, String expectedValue) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        boolean passed = !Objects.equals(actual, expected);
        TestExecutionLogger.logAssertionResult("Soft Not-Equals Assertion", "not [" + expected + "]", actual, passed);
        softAssert.get().assertNotEquals(actual, expected);
    }

    /**
     * Soft-assert actual text contains the expected substring.
     *
     * @param actualValue   the full text from the application
     * @param expectedValue the substring expected within actualValue
     */
    public static void SoftAssertTextContains(String actualValue, String expectedValue) {
        boolean passed = actualValue != null && actualValue.contains(expectedValue);
        TestExecutionLogger.logAssertionResult("Soft Contains Assertion", expectedValue, String.valueOf(actualValue), passed);
        softAssert.get().assertTrue(passed,
                "Expected text to contain [" + expectedValue + "] but was [" + actualValue + "]");
    }

    /**
     * Soft-assert two strings are equal ignoring case.
     *
     * @param actualValue   the value obtained from the application
     * @param expectedValue the expected value
     */
    public static void SoftAssertTextIgnoreCase(String actualValue, String expectedValue) {
        String actual = actualValue != null ? actualValue.trim() : null;
        String expected = expectedValue != null ? expectedValue.trim() : null;
        boolean passed = (actual == null && expected == null)
                || (actual != null && actual.equalsIgnoreCase(expected));
        TestExecutionLogger.logAssertionResult("Soft IgnoreCase Assertion", expected, actual, passed);
        softAssert.get().assertTrue(passed,
                "Expected (ignore case): [" + expected + "] but was: [" + actual + "]");
    }

    /**
     * Soft-assert actual text starts with the expected prefix.
     *
     * @param actualValue    the full text from the application
     * @param expectedPrefix the prefix expected at the start
     */
    public static void SoftAssertStartsWith(String actualValue, String expectedPrefix) {
        boolean passed = actualValue != null && actualValue.startsWith(expectedPrefix);
        TestExecutionLogger.logAssertionResult("Soft StartsWith Assertion", expectedPrefix, String.valueOf(actualValue), passed);
        softAssert.get().assertTrue(passed,
                "Expected to start with [" + expectedPrefix + "] but was [" + actualValue + "]");
    }

    /**
     * Soft-assert actual text ends with the expected suffix.
     *
     * @param actualValue    the full text from the application
     * @param expectedSuffix the suffix expected at the end
     */
    public static void SoftAssertEndsWith(String actualValue, String expectedSuffix) {
        boolean passed = actualValue != null && actualValue.endsWith(expectedSuffix);
        TestExecutionLogger.logAssertionResult("Soft EndsWith Assertion", expectedSuffix, String.valueOf(actualValue), passed);
        softAssert.get().assertTrue(passed,
                "Expected to end with [" + expectedSuffix + "] but was [" + actualValue + "]");
    }

    /**
     * Soft-assert actual text does NOT contain the unexpected substring.
     *
     * @param actualValue     the full text from the application
     * @param unexpectedValue the substring that should NOT be present
     */
    public static void SoftAssertTextNotContains(String actualValue, String unexpectedValue) {
        boolean passed = actualValue == null || !actualValue.contains(unexpectedValue);
        TestExecutionLogger.logAssertionResult("Soft NotContains Assertion", "not contain [" + unexpectedValue + "]", String.valueOf(actualValue), passed);
        softAssert.get().assertTrue(passed,
                "Expected text NOT to contain [" + unexpectedValue + "] but was [" + actualValue + "]");
    }

    /**
     * Soft-assert that a string is null or empty.
     *
     * @param actualValue the value to check
     * @param message     descriptive context
     */
    public static void SoftAssertEmpty(String actualValue, String message) {
        boolean passed = (actualValue == null || actualValue.trim().isEmpty());
        TestExecutionLogger.logAssertionResult("Soft Empty Assertion [" + message + "]", "empty", String.valueOf(actualValue), passed);
        softAssert.get().assertTrue(passed, message);
    }

    /**
     * Soft-assert that a string is NOT null and NOT empty.
     *
     * @param actualValue the value to check
     * @param message     descriptive context
     */
    public static void SoftAssertNotEmpty(String actualValue, String message) {
        boolean passed = (actualValue != null && !actualValue.trim().isEmpty());
        TestExecutionLogger.logAssertionResult("Soft NotEmpty Assertion [" + message + "]", "not empty", String.valueOf(actualValue), passed);
        softAssert.get().assertTrue(passed, message);
    }

    /**
     * Soft-assert that actual text matches the given regex pattern.
     *
     * @param actualValue the value to test
     * @param regex       the regular expression pattern
     */
    public static void SoftAssertMatches(String actualValue, String regex) {
        boolean passed = (actualValue != null && actualValue.matches(regex));
        TestExecutionLogger.logAssertionResult("Soft Regex Assertion", regex, String.valueOf(actualValue), passed);
        softAssert.get().assertTrue(passed,
                "Expected [" + actualValue + "] to match regex [" + regex + "]");
    }

    // ==========================================================================
    //  SECTION 7: SOFT ASSERTS — BOOLEAN
    // ==========================================================================

    /**
     * Soft-assert condition is true.
     *
     * @param condition the boolean condition to verify
     * @param message   descriptive context (e.g. "Element should be enabled")
     */
    public static void SoftAssertTrue(Boolean condition, String message) {
        boolean passed = condition != null && condition;
        TestExecutionLogger.logAssertionResult("Soft Boolean Assertion [" + message + "]", "true", String.valueOf(condition), passed);
        softAssert.get().assertTrue(passed, message);
    }

    /**
     * Soft-assert condition is false.
     *
     * @param condition the boolean condition to verify
     * @param message   descriptive context (e.g. "Element should be disabled")
     */
    public static void SoftAssertFalse(Boolean condition, String message) {
        boolean passed = condition == null || !condition;
        TestExecutionLogger.logAssertionResult("Soft Boolean Assertion [" + message + "]", "false", String.valueOf(condition), passed);
        softAssert.get().assertFalse(condition != null && condition, message);
    }

    // ==========================================================================
    //  SECTION 8: SOFT ASSERTS — NULL CHECKS
    // ==========================================================================

    /**
     * Soft-assert that an object is null.
     *
     * @param object  the object to verify
     * @param message descriptive context
     */
    public static void SoftAssertNull(Object object, String message) {
        boolean passed = (object == null);
        TestExecutionLogger.logAssertionResult("Soft Null Assertion [" + message + "]", "null", String.valueOf(object), passed);
        softAssert.get().assertNull(object, message);
    }

    /**
     * Soft-assert that an object is NOT null.
     *
     * @param object  the object to verify
     * @param message descriptive context
     */
    public static void SoftAssertNotNull(Object object, String message) {
        boolean passed = (object != null);
        TestExecutionLogger.logAssertionResult("Soft NotNull Assertion [" + message + "]", "not null", String.valueOf(object), passed);
        softAssert.get().assertNotNull(object, message);
    }

    // ==========================================================================
    //  SECTION 8b: SOFT ASSERTS — NUMERIC
    // ==========================================================================

    /**
     * Soft-assert two integers are equal.
     *
     * @param actual   the actual numeric value
     * @param expected the expected numeric value
     */
    public static void SoftAssertEquals(int actual, int expected) {
        boolean passed = (actual == expected);
        TestExecutionLogger.logAssertionResult("Soft Numeric Equals", String.valueOf(expected), String.valueOf(actual), passed);
        softAssert.get().assertEquals(actual, expected);
    }

    /**
     * Soft-assert that actual is greater than expected.
     *
     * @param actual   the actual numeric value
     * @param expected the value that actual should exceed
     */
    public static void SoftAssertGreaterThan(int actual, int expected) {
        boolean passed = actual > expected;
        TestExecutionLogger.logAssertionResult("Soft GreaterThan Assertion", ">" + expected, String.valueOf(actual), passed);
        softAssert.get().assertTrue(passed,
                "Expected [" + actual + "] to be greater than [" + expected + "]");
    }

    /**
     * Soft-assert that actual is less than expected.
     *
     * @param actual   the actual numeric value
     * @param expected the value that actual should be below
     */
    public static void SoftAssertLessThan(int actual, int expected) {
        boolean passed = actual < expected;
        TestExecutionLogger.logAssertionResult("Soft LessThan Assertion", "<" + expected, String.valueOf(actual), passed);
        softAssert.get().assertTrue(passed,
                "Expected [" + actual + "] to be less than [" + expected + "]");
    }

    /**
     * Soft-assert that actual is greater than or equal to expected.
     *
     * @param actual   the actual numeric value
     * @param expected the threshold value
     */
    public static void SoftAssertGreaterThanOrEqual(int actual, int expected) {
        boolean passed = actual >= expected;
        TestExecutionLogger.logAssertionResult("Soft GreaterOrEqual Assertion", ">=" + expected, String.valueOf(actual), passed);
        softAssert.get().assertTrue(passed,
                "Expected [" + actual + "] to be >= [" + expected + "]");
    }

    /**
     * Soft-assert that actual is less than or equal to expected.
     *
     * @param actual   the actual numeric value
     * @param expected the threshold value
     */
    public static void SoftAssertLessThanOrEqual(int actual, int expected) {
        boolean passed = actual <= expected;
        TestExecutionLogger.logAssertionResult("Soft LessOrEqual Assertion", "<=" + expected, String.valueOf(actual), passed);
        softAssert.get().assertTrue(passed,
                "Expected [" + actual + "] to be <= [" + expected + "]");
    }

    // ==========================================================================
    //  SECTION 8c: SOFT ASSERTS — LIST / COLLECTION
    // ==========================================================================

    /**
     * Soft-assert that a list has the expected size.
     *
     * @param list         the list to check
     * @param expectedSize the expected number of items
     * @param message      descriptive context
     */
    public static void SoftAssertListSize(java.util.List<?> list, int expectedSize, String message) {
        int actualSize = (list != null) ? list.size() : 0;
        boolean passed = (actualSize == expectedSize);
        TestExecutionLogger.logAssertionResult("Soft ListSize Assertion [" + message + "]", String.valueOf(expectedSize), String.valueOf(actualSize), passed);
        softAssert.get().assertEquals(actualSize, expectedSize, message);
    }

    /**
     * Soft-assert that a list contains the expected item.
     *
     * @param list     the list to search
     * @param expected the item expected to be in the list
     * @param message  descriptive context
     */
    public static void SoftAssertListContains(java.util.List<?> list, Object expected, String message) {
        boolean passed = (list != null && list.contains(expected));
        TestExecutionLogger.logAssertionResult("Soft ListContains Assertion [" + message + "]", String.valueOf(expected), String.valueOf(list), passed);
        softAssert.get().assertTrue(passed, message);
    }

    // ==========================================================================
    //  SECTION 9: SOFT ASSERT FLUSH
    // ==========================================================================

    /**
     * Flush all accumulated soft assertion failures.
     * Throws {@code AssertionError} if any soft assertions failed during the test.
     * Automatically resets the ThreadLocal SoftAssert to prevent cross-test contamination.
     * <p>
     * Called automatically from {@code ApplicationLaunch.tearDown()} — no manual call needed.
     */
    public static void AssertAll() {
        try {
            softAssert.get().assertAll();
        } finally {
            softAssert.remove();
        }
    }
}
