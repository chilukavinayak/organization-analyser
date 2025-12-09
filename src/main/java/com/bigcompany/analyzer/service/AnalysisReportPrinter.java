package com.bigcompany.analyzer.service;

import com.bigcompany.analyzer.config.AnalyzerConfig;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Formats and prints analysis reports to console or other output streams.
 * 
 * <p>Produces human-readable reports with clear sections for:
 * <ul>
 *   <li>Underpaid managers</li>
 *   <li>Overpaid managers</li>
 *   <li>Employees with long reporting lines</li>
 *   <li>Summary statistics</li>
 * </ul>
 */
public final class AnalysisReportPrinter {

    private static final int LINE_WIDTH = 80;
    private static final String SEPARATOR = "=".repeat(LINE_WIDTH);
    private static final String SECTION_SEPARATOR = "-".repeat(LINE_WIDTH);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PrintStream out;

    /**
     * Creates a printer that outputs to System.out.
     */
    public AnalysisReportPrinter() {
        this(System.out);
    }

    /**
     * Creates a printer that outputs to the specified stream.
     * 
     * @param out the output stream
     */
    public AnalysisReportPrinter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "Output stream cannot be null");
    }

    /**
     * Prints the full analysis report including all sections and statistics.
     * 
     * @param analyzer the organization analyzer with results
     */
    public void printFullReport(OrganizationAnalyzer analyzer) {
        Objects.requireNonNull(analyzer, "Analyzer cannot be null");

        AnalysisStatistics stats = analyzer.runAnalysis();

        printHeader(stats);
        printConfigurationSummary(analyzer.getConfig());
        printUnderpaidManagers(analyzer.getUnderpaidManagers());
        printOverpaidManagers(analyzer.getOverpaidManagers());
        printLongReportingLines(analyzer.getEmployeesWithLongReportingLines());
        printStatistics(stats);
        printFooter(stats);
    }

    private void printHeader(AnalysisStatistics stats) {
        out.println();
        out.println(SEPARATOR);
        out.println(center("ORGANIZATION STRUCTURE ANALYSIS REPORT"));
        out.println(SEPARATOR);
        out.printf("  Analysis Date: %s%n", 
                stats.getAnalysisTimestamp().atZone(java.time.ZoneId.systemDefault())
                        .format(TIMESTAMP_FORMAT));
        out.printf("  Total Employees: %d%n", stats.getTotalEmployees());
        out.println();
    }

    private void printConfigurationSummary(AnalyzerConfig config) {
        out.println(SECTION_SEPARATOR);
        out.println("CONFIGURATION");
        out.println(SECTION_SEPARATOR);
        out.printf("  Minimum manager salary: %.0f%% above subordinate average%n",
                config.getMinSalaryPercentageAboveSubordinates() * 100);
        out.printf("  Maximum manager salary: %.0f%% above subordinate average%n",
                config.getMaxSalaryPercentageAboveSubordinates() * 100);
        out.printf("  Maximum reporting line depth: %d managers to CEO%n",
                config.getMaxReportingLineLength());
        out.println();
    }

    private void printUnderpaidManagers(List<SalaryAnalysisResult> underpaid) {
        out.println(SECTION_SEPARATOR);
        out.println("MANAGERS EARNING LESS THAN REQUIRED");
        out.println(SECTION_SEPARATOR);

        if (underpaid.isEmpty()) {
            out.println("  ✓ No issues found.");
        } else {
            out.printf("  %-25s | %15s | %15s | %12s%n",
                    "Manager Name", "Current Salary", "Min Required", "Underpaid By");
            out.println("  " + "-".repeat(74));

            for (SalaryAnalysisResult result : underpaid) {
                out.printf("  %-25s | %,15.2f | %,15.2f | %,12.2f%n",
                        truncate(result.manager().getFullName(), 25),
                        result.actualSalary(),
                        result.minExpectedSalary(),
                        result.getDeviation());
            }
        }
        out.println();
    }

    private void printOverpaidManagers(List<SalaryAnalysisResult> overpaid) {
        out.println(SECTION_SEPARATOR);
        out.println("MANAGERS EARNING MORE THAN ALLOWED");
        out.println(SECTION_SEPARATOR);

        if (overpaid.isEmpty()) {
            out.println("  ✓ No issues found.");
        } else {
            out.printf("  %-25s | %15s | %15s | %12s%n",
                    "Manager Name", "Current Salary", "Max Allowed", "Overpaid By");
            out.println("  " + "-".repeat(74));

            for (SalaryAnalysisResult result : overpaid) {
                out.printf("  %-25s | %,15.2f | %,15.2f | %,12.2f%n",
                        truncate(result.manager().getFullName(), 25),
                        result.actualSalary(),
                        result.maxExpectedSalary(),
                        result.getDeviation());
            }
        }
        out.println();
    }

    private void printLongReportingLines(List<ReportingLineResult> longLines) {
        out.println(SECTION_SEPARATOR);
        out.println("EMPLOYEES WITH REPORTING LINE TOO LONG");
        out.println(SECTION_SEPARATOR);

        if (longLines.isEmpty()) {
            out.println("  ✓ No issues found.");
        } else {
            out.printf("  %-30s | %15s | %10s%n",
                    "Employee Name", "Line Length", "Excess");
            out.println("  " + "-".repeat(62));

            for (ReportingLineResult result : longLines) {
                out.printf("  %-30s | %15d | %10d%n",
                        truncate(result.employee().getFullName(), 30),
                        result.reportingLineLength(),
                        result.excessLength());
            }
        }
        out.println();
    }

    private void printStatistics(AnalysisStatistics stats) {
        out.println(SECTION_SEPARATOR);
        out.println("SUMMARY STATISTICS");
        out.println(SECTION_SEPARATOR);
        out.printf("  Total Employees:              %d%n", stats.getTotalEmployees());
        out.printf("  Total Managers:               %d%n", stats.getTotalManagers());
        out.printf("  Underpaid Managers:           %d%n", stats.getUnderpaidManagerCount());
        out.printf("  Overpaid Managers:            %d%n", stats.getOverpaidManagerCount());
        out.printf("  Long Reporting Lines:         %d%n", stats.getLongReportingLineCount());
        out.printf("  Max Reporting Depth:          %d%n", stats.getMaxReportingLineDepth());
        out.printf("  Salary Compliance Rate:       %.1f%%%n", stats.getComplianceRate());
        out.printf("  Total Issues Found:           %d%n", stats.getTotalIssues());
        out.printf("  Execution Time:               %d ms%n", stats.getExecutionTime().toMillis());
        out.println();
    }

    private void printFooter(AnalysisStatistics stats) {
        out.println(SEPARATOR);
        if (stats.hasIssues()) {
            out.println(center("⚠ ANALYSIS COMPLETE - " + stats.getTotalIssues() + " ISSUE(S) FOUND"));
        } else {
            out.println(center("✓ ANALYSIS COMPLETE - NO ISSUES FOUND"));
        }
        out.println(SEPARATOR);
        out.println();
    }

    private String center(String text) {
        int padding = (LINE_WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
