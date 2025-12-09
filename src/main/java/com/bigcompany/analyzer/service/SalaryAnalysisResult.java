package com.bigcompany.analyzer.service;

import com.bigcompany.analyzer.model.Employee;

/**
 * Result of salary analysis for a single manager.
 * 
 * <p>Contains the analysis details including:
 * <ul>
 *   <li>The manager being analyzed</li>
 *   <li>Average salary of their direct subordinates</li>
 *   <li>Expected salary range (min 20% above, max 50% above average)</li>
 *   <li>Compliance status (UNDERPAID, WITHIN_RANGE, or OVERPAID)</li>
 * </ul>
 * 
 * @param manager The manager employee being analyzed
 * @param averageSubordinateSalary Average salary of direct subordinates
 * @param actualSalary The manager's current salary
 * @param minExpectedSalary Minimum acceptable salary (avg × 1.20)
 * @param maxExpectedSalary Maximum acceptable salary (avg × 1.50)
 * @param status Whether the manager is UNDERPAID, WITHIN_RANGE, or OVERPAID
 */
public record SalaryAnalysisResult(
        Employee manager,
        double averageSubordinateSalary,
        double actualSalary,
        double minExpectedSalary,
        double maxExpectedSalary,
        SalaryStatus status) {

    public enum SalaryStatus {
        UNDERPAID,
        WITHIN_RANGE,
        OVERPAID
    }

    public double getDeviation() {
        return switch (status) {
            case UNDERPAID -> minExpectedSalary - actualSalary;
            case OVERPAID -> actualSalary - maxExpectedSalary;
            case WITHIN_RANGE -> 0;
        };
    }

    public boolean isCompliant() {
        return status == SalaryStatus.WITHIN_RANGE;
    }
}
