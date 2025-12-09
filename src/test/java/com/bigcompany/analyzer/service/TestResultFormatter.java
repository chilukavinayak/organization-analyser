package com.bigcompany.analyzer.service;

import java.util.List;
import java.util.stream.Collectors;

public class TestResultFormatter {

    public static String formatResults(OrganizationAnalyzer analyzer) {
        StringBuilder sb = new StringBuilder();

        sb.append("UNDERPAID_MANAGERS:\n");
        for (SalaryAnalysisResult result : analyzer.getUnderpaidManagers()) {
            sb.append(String.format("%s|%.2f|%.2f|%.2f%n",
                    result.manager().getFullName(),
                    result.actualSalary(),
                    result.minExpectedSalary(),
                    result.getDeviation()));
        }

        sb.append("OVERPAID_MANAGERS:\n");
        for (SalaryAnalysisResult result : analyzer.getOverpaidManagers()) {
            sb.append(String.format("%s|%.2f|%.2f|%.2f%n",
                    result.manager().getFullName(),
                    result.actualSalary(),
                    result.maxExpectedSalary(),
                    result.getDeviation()));
        }

        sb.append("LONG_REPORTING_LINES:\n");
        for (ReportingLineResult result : analyzer.getEmployeesWithLongReportingLines()) {
            sb.append(String.format("%s|%d|%d%n",
                    result.employee().getFullName(),
                    result.reportingLineLength(),
                    result.excessLength()));
        }

        return sb.toString();
    }
}
