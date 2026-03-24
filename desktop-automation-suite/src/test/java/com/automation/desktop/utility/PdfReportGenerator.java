package com.automation.desktop.utility;

import com.automation.desktop.base.ApplicationLaunch;
import com.automation.desktop.base.BaseSetup;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

/**
 * PdfReportGenerator — Produces a professional 3-section PDF test execution report.
 *
 * <p>Section 1: Executive Summary (counts, percentages, pass rate, overhead, cycle info)<br>
 * Section 2: Test Index Table (package.class.method | status | duration)<br>
 * Section 3: Detailed Logs (Option B — every test in execution order, full step detail,
 *            inline screenshots attached directly below the step that triggered them)</p>
 *
 * @see TestReportStore
 */
public class PdfReportGenerator {

    // ========== CONSTANTS ==========

    private static final String REPORT_DIR = "Report";
    private static final String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    private static final String REPORT_PATH = REPORT_DIR + File.separator + "TestExecutionReport_" + timestamp + ".pdf";

    // --- Colors ---
    private static final BaseColor GREEN = new BaseColor(34, 139, 34);
    private static final BaseColor RED = new BaseColor(220, 20, 60);
    private static final BaseColor ORANGE = new BaseColor(255, 140, 0);
    private static final BaseColor LIGHT_GREEN = new BaseColor(230, 255, 230);
    private static final BaseColor LIGHT_RED = new BaseColor(255, 230, 230);
    private static final BaseColor LIGHT_ORANGE = new BaseColor(255, 240, 220);
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);
    private static final BaseColor HEADER_BG = new BaseColor(41, 65, 122);

    // --- Fonts ---
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, HEADER_BG);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font SECTION_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, HEADER_BG);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
    private static final Font LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font VALUE_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font STEP_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font ERROR_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, RED);
    private static final Font STATUS_PASS_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, GREEN);
    private static final Font STATUS_FAIL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, RED);
    private static final Font STATUS_SKIP_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, ORANGE);
    private static final Font TEST_NAME_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);

    // ========== PUBLIC API ==========

    public static void generateReport(int passed, int failed, int skipped) {
        try {
            File reportFolder = new File(REPORT_DIR);
            if (!reportFolder.exists()) {
                reportFolder.mkdirs();
            }

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, new FileOutputStream(REPORT_PATH));
            document.open();

            Collection<TestReportStore.TestResultData> allResults = TestReportStore.getAllResults();
            int total = passed + failed + skipped;

            addSummarySection(document, total, passed, failed, skipped);
            document.newPage();
            addIndexSection(document, allResults);
            document.newPage();
            addDetailSection(document, allResults);

            document.close();
            System.out.println("📄 PDF Report Generated at: " + REPORT_PATH);

        } catch (Exception e) {
            System.err.println("❌ Failed to generate PDF report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== SECTION 1 — EXECUTIVE SUMMARY ==========

    private static void addSummarySection(Document doc, int total, int passed, int failed, int skipped) throws DocumentException {
        // Title
        Paragraph title = new Paragraph("Test Execution Report", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph genDate = new Paragraph("Generated: " + new java.util.Date(), SUBTITLE_FONT);
        genDate.setAlignment(Element.ALIGN_CENTER);
        doc.add(genDate);
        doc.add(Chunk.NEWLINE);

        // --- Separator ---
        addSeparator(doc);
        doc.add(Chunk.NEWLINE);

        // --- Summary Table ---
        doc.add(new Paragraph("Executive Summary", SECTION_FONT));
        doc.add(Chunk.NEWLINE);

        double passPct = total > 0 ? (passed * 100.0 / total) : 0;
        double failPct = total > 0 ? (failed * 100.0 / total) : 0;
        double skipPct = total > 0 ? (skipped * 100.0 / total) : 0;

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(65);
        table.setWidths(new float[]{1.5f, 2f});

        addSummaryRow(table, "Total Tests", String.valueOf(total), LIGHT_GRAY);
        addSummaryRow(table, "Passed", passed + " (" + String.format("%.1f", passPct) + "%)", LIGHT_GREEN);
        addSummaryRow(table, "Failed", failed + " (" + String.format("%.1f", failPct) + "%)", LIGHT_RED);
        addSummaryRow(table, "Skipped", skipped + " (" + String.format("%.1f", skipPct) + "%)", LIGHT_ORANGE);
        addSummaryRow(table, "Pass Rate", String.format("%.1f%%", passPct), LIGHT_GRAY);

        // Fixed wait overhead
        double waitSeconds = BaseSetup.totalFixedWaitTime / 1000.0;
        addSummaryRow(table, "Fixed Wait Overhead", String.format("%.1fs", waitSeconds), LIGHT_GRAY);

        doc.add(table);
        doc.add(Chunk.NEWLINE);

        // --- Cycle Info ---
        addSeparator(doc);
        doc.add(Chunk.NEWLINE);
    }

    // ========== SECTION 2 — TEST INDEX TABLE ==========

    private static void addIndexSection(Document doc, Collection<TestReportStore.TestResultData> results) throws DocumentException {
        doc.add(new Paragraph("Test Index", SECTION_FONT));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3f, 2f, 1.5f, 1.5f});

        // Header row
        addHeaderCell(table, "Test");
        addHeaderCell(table, "Class");
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Duration");

        int row = 0;
        for (TestReportStore.TestResultData result : results) {
            BaseColor rowBg = (row % 2 == 0) ? BaseColor.WHITE : LIGHT_GRAY;

            // Test method name
            PdfPCell nameCell = new PdfPCell(new Phrase(safe(result.methodName, result.testName), VALUE_FONT));
            nameCell.setBackgroundColor(rowBg);
            nameCell.setPadding(5);
            table.addCell(nameCell);

            // Class name (with package tooltip in the name)
            String classDisplay = safe(result.className, "—");
            if (result.packageName != null && !result.packageName.isEmpty()) {
                classDisplay = result.packageName + "." + result.className;
            }
            PdfPCell classCell = new PdfPCell(new Phrase(classDisplay, VALUE_FONT));
            classCell.setBackgroundColor(rowBg);
            classCell.setPadding(5);
            table.addCell(classCell);

            // Status (color-coded)
            Font statusFont = getStatusFont(result.status);
            BaseColor statusBg = getStatusBackground(result.status);
            PdfPCell statusCell = new PdfPCell(new Phrase(safe(result.status), statusFont));
            statusCell.setBackgroundColor(statusBg);
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setPadding(5);
            table.addCell(statusCell);

            // Duration
            String duration = result.getDurationSeconds() > 0
                    ? String.format("%.1fs", result.getDurationSeconds())
                    : "—";
            PdfPCell durationCell = new PdfPCell(new Phrase(duration, VALUE_FONT));
            durationCell.setBackgroundColor(rowBg);
            durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            durationCell.setPadding(5);
            table.addCell(durationCell);

            row++;
        }

        doc.add(table);
    }

    // ========== SECTION 3 — DETAILED TEST LOGS (OPTION B) ==========

    private static void addDetailSection(Document doc, Collection<TestReportStore.TestResultData> results) throws DocumentException {
        doc.add(new Paragraph("Detailed Test Logs", SECTION_FONT));
        doc.add(Chunk.NEWLINE);

        int testNumber = 0;
        for (TestReportStore.TestResultData result : results) {
            testNumber++;

            // --- Test header box ---
            PdfPTable headerTable = new PdfPTable(3);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{4f, 1.5f, 1.5f});
            headerTable.setSpacingBefore(8);

            // Test name
            PdfPCell nameCell = new PdfPCell(new Phrase(testNumber + ". " + result.testName, TEST_NAME_FONT));
            nameCell.setBorderColor(BaseColor.LIGHT_GRAY);
            nameCell.setPadding(6);
            nameCell.setBackgroundColor(LIGHT_GRAY);
            headerTable.addCell(nameCell);

            // Status
            Font statusFont = getStatusFont(result.status);
            BaseColor statusBg = getStatusBackground(result.status);
            PdfPCell statusCell = new PdfPCell(new Phrase(safe(result.status), statusFont));
            statusCell.setBackgroundColor(statusBg);
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            statusCell.setPadding(6);
            statusCell.setBorderColor(BaseColor.LIGHT_GRAY);
            headerTable.addCell(statusCell);

            // Duration
            String duration = result.getDurationSeconds() > 0
                    ? String.format("%.1fs", result.getDurationSeconds())
                    : "—";
            PdfPCell durationCell = new PdfPCell(new Phrase(duration, VALUE_FONT));
            durationCell.setBackgroundColor(LIGHT_GRAY);
            durationCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            durationCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            durationCell.setPadding(6);
            durationCell.setBorderColor(BaseColor.LIGHT_GRAY);
            headerTable.addCell(durationCell);

            doc.add(headerTable);

            // --- Class info ---
            if (result.className != null) {
                String classInfo = safe(result.packageName, "") + "." + safe(result.className, "");
                doc.add(new Paragraph("    Class: " + classInfo, SUBTITLE_FONT));
            }

            // --- Step list ---
            if (!result.steps.isEmpty()) {
                PdfPTable stepsTable = new PdfPTable(1);
                stepsTable.setWidthPercentage(98);
                stepsTable.setSpacingBefore(4);

                int stepNum = 0;
                for (TestReportStore.StepEntry step : result.steps) {
                    stepNum++;

                    // Choose font: error steps get red, normal steps get gray
                    Font font = (step.description.startsWith("❌") || step.description.contains("FAILED"))
                            ? ERROR_FONT : STEP_FONT;

                    PdfPCell stepCell = new PdfPCell(new Phrase("  " + stepNum + ". " + step.description, font));
                    stepCell.setBorder(Rectangle.NO_BORDER);
                    stepCell.setPaddingLeft(15);
                    stepCell.setPaddingTop(2);
                    stepCell.setPaddingBottom(2);
                    stepsTable.addCell(stepCell);

                    // --- Inline screenshot directly below this step ---
                    if (step.screenshotPath != null) {
                        try {
                            Image img = Image.getInstance(step.screenshotPath);
                            img.scaleToFit(450, 280);
                            img.setAlignment(Element.ALIGN_CENTER);

                            PdfPCell imgCell = new PdfPCell(img, false);
                            imgCell.setBorder(Rectangle.NO_BORDER);
                            imgCell.setPaddingLeft(20);
                            imgCell.setPaddingTop(4);
                            imgCell.setPaddingBottom(8);
                            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            stepsTable.addCell(imgCell);
                        } catch (Exception e) {
                            PdfPCell errCell = new PdfPCell(new Phrase(
                                    "  [Screenshot not found: " + step.screenshotPath + "]", STEP_FONT));
                            errCell.setBorder(Rectangle.NO_BORDER);
                            errCell.setPaddingLeft(20);
                            stepsTable.addCell(errCell);
                        }
                    }
                }

                doc.add(stepsTable);
            }

            // --- Error message block (if present) ---
            if (result.errorMessage != null && !result.errorMessage.isEmpty()) {
                PdfPTable errorTable = new PdfPTable(1);
                errorTable.setWidthPercentage(96);
                errorTable.setSpacingBefore(4);

                PdfPCell errorCell = new PdfPCell(new Phrase("Error: " + result.errorMessage, ERROR_FONT));
                errorCell.setBackgroundColor(LIGHT_RED);
                errorCell.setPadding(6);
                errorCell.setBorderColor(RED);
                errorTable.addCell(errorCell);

                doc.add(errorTable);
            }

            doc.add(Chunk.NEWLINE);
        }
    }

    // ========== PRIVATE HELPERS ==========

    /** Add a horizontal separator line. */
    private static void addSeparator(Document doc) throws DocumentException {
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setFixedHeight(1);
        line.addCell(cell);
        doc.add(line);
    }

    /** Add a label-value row to the summary table. */
    private static void addSummaryRow(PdfPTable table, String label, String value, BaseColor bg) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBackgroundColor(bg);
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, VALUE_FONT));
        valueCell.setBackgroundColor(bg);
        valueCell.setPadding(6);
        table.addCell(valueCell);
    }

    /** Add a header cell (dark background, white text). */
    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    /** Get the status font based on result status. */
    private static Font getStatusFont(String status) {
        if ("PASSED".equalsIgnoreCase(status)) return STATUS_PASS_FONT;
        if ("FAILED".equalsIgnoreCase(status)) return STATUS_FAIL_FONT;
        if ("SKIPPED".equalsIgnoreCase(status)) return STATUS_SKIP_FONT;
        return VALUE_FONT;
    }

    /** Get the status background color. */
    private static BaseColor getStatusBackground(String status) {
        if ("PASSED".equalsIgnoreCase(status)) return LIGHT_GREEN;
        if ("FAILED".equalsIgnoreCase(status)) return LIGHT_RED;
        if ("SKIPPED".equalsIgnoreCase(status)) return LIGHT_ORANGE;
        return LIGHT_GRAY;
    }

    /** Null-safe string: returns value or fallback. */
    private static String safe(String value) {
        return value != null ? value : "—";
    }

    /** Null-safe string: returns value or fallback. */
    private static String safe(String value, String fallback) {
        return (value != null && !value.isEmpty()) ? value : fallback;
    }
}
