package com.bigcompany.analyzer.service;

import com.bigcompany.analyzer.model.Employee;

/**
 * Result of reporting line analysis for a single employee.
 * 
 * <p>Contains information about how many managers exist between an employee
 * and the CEO, and whether this exceeds the maximum allowed depth.
 * 
 * @param employee The employee being analyzed
 * @param reportingLineLength Number of managers between this employee and CEO
 * @param maxAllowedLength Maximum allowed reporting line length (currently 4)
 * @param excessLength How many levels above the maximum (0 if within limit)
 */
public record ReportingLineResult(
        Employee employee,
        int reportingLineLength,
        int maxAllowedLength,
        int excessLength) {

    public boolean isTooLong() {
        return excessLength > 0;
    }
}
