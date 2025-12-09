package com.bigcompany.analyzer.service;

import com.bigcompany.analyzer.config.AnalyzerConfig;
import com.bigcompany.analyzer.model.Employee;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Core analysis engine for organizational structure validation.
 * 
 * <p>This class performs two main types of analysis:
 * <ul>
 *   <li><b>Salary Analysis:</b> Validates that managers earn between 20% and 50% more
 *       than the average salary of their direct subordinates</li>
 *   <li><b>Reporting Line Analysis:</b> Identifies employees with more than 4 managers
 *       between them and the CEO</li>
 * </ul>
 * 
 * <h2>Business Rules</h2>
 * <ul>
 *   <li>Minimum manager salary = subordinate average × 1.20 (20% above)</li>
 *   <li>Maximum manager salary = subordinate average × 1.50 (50% above)</li>
 *   <li>Maximum reporting line length = 4 managers to CEO</li>
 * </ul>
 * 
 * <h2>Thread Safety</h2>
 * <p>This class is thread-safe for read operations after construction.
 * 
 * @see SalaryAnalysisResult
 * @see ReportingLineResult
 */
public final class OrganizationAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(OrganizationAnalyzer.class.getName());

    private final AnalyzerConfig config;
    private final Map<String, Employee> employeesById;
    private final Map<String, List<Employee>> subordinatesByManagerId;

    // Cached results for performance
    private volatile List<SalaryAnalysisResult> cachedSalaryResults;
    private volatile List<ReportingLineResult> cachedReportingLineResults;

    /**
     * Creates an analyzer with default configuration.
     * 
     * @param employees map of employee ID to Employee
     */
    public OrganizationAnalyzer(Map<String, Employee> employees) {
        this(employees, AnalyzerConfig.defaults());
    }

    /**
     * Creates an analyzer with custom configuration.
     * 
     * @param employees map of employee ID to Employee
     * @param config analysis configuration
     */
    public OrganizationAnalyzer(Map<String, Employee> employees, AnalyzerConfig config) {
        Objects.requireNonNull(employees, "Employees map cannot be null");
        Objects.requireNonNull(config, "Config cannot be null");

        this.config = config;
        this.employeesById = Collections.unmodifiableMap(new HashMap<>(employees));
        this.subordinatesByManagerId = buildSubordinatesMap(employees);

        LOGGER.fine(() -> String.format("Initialized analyzer with %d employees, config: %s",
                employees.size(), config));
    }

    private Map<String, List<Employee>> buildSubordinatesMap(Map<String, Employee> employees) {
        return employees.values().stream()
                .filter(e -> !e.isCeo())
                .collect(Collectors.groupingBy(
                        Employee::getManagerId,
                        Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)
                ));
    }

    /**
     * Analyzes all managers for salary compliance.
     * 
     * @return list of salary analysis results for all managers
     */
    public List<SalaryAnalysisResult> analyzeManagerSalaries() {
        if (cachedSalaryResults != null) {
            return cachedSalaryResults;
        }

        List<SalaryAnalysisResult> results = new ArrayList<>();

        for (Employee employee : employeesById.values()) {
            List<Employee> subordinates = subordinatesByManagerId.get(employee.getId());

            if (subordinates == null || subordinates.isEmpty()) {
                continue;
            }

            double avgSubordinateSalary = calculateAverageSalary(subordinates);
            double minExpected = config.calculateMinExpectedSalary(avgSubordinateSalary);
            double maxExpected = config.calculateMaxExpectedSalary(avgSubordinateSalary);

            SalaryAnalysisResult.SalaryStatus status;
            if (employee.getSalary() < minExpected) {
                status = SalaryAnalysisResult.SalaryStatus.UNDERPAID;
            } else if (employee.getSalary() > maxExpected) {
                status = SalaryAnalysisResult.SalaryStatus.OVERPAID;
            } else {
                status = SalaryAnalysisResult.SalaryStatus.WITHIN_RANGE;
            }

            results.add(new SalaryAnalysisResult(
                    employee,
                    avgSubordinateSalary,
                    employee.getSalary(),
                    minExpected,
                    maxExpected,
                    status
            ));
        }

        cachedSalaryResults = Collections.unmodifiableList(results);
        return cachedSalaryResults;
    }

    /**
     * Returns managers who earn less than the minimum required salary.
     * 
     * @return list of underpaid manager results
     */
    public List<SalaryAnalysisResult> getUnderpaidManagers() {
        return analyzeManagerSalaries().stream()
                .filter(r -> r.status() == SalaryAnalysisResult.SalaryStatus.UNDERPAID)
                .toList();
    }

    /**
     * Returns managers who earn more than the maximum allowed salary.
     * 
     * @return list of overpaid manager results
     */
    public List<SalaryAnalysisResult> getOverpaidManagers() {
        return analyzeManagerSalaries().stream()
                .filter(r -> r.status() == SalaryAnalysisResult.SalaryStatus.OVERPAID)
                .toList();
    }

    /**
     * Analyzes all employees for reporting line depth.
     * 
     * @return list of reporting line results for all non-CEO employees
     */
    public List<ReportingLineResult> analyzeReportingLines() {
        if (cachedReportingLineResults != null) {
            return cachedReportingLineResults;
        }

        List<ReportingLineResult> results = new ArrayList<>();
        int maxAllowed = config.getMaxReportingLineLength();

        for (Employee employee : employeesById.values()) {
            if (employee.isCeo()) {
                continue;
            }

            int lineLength = calculateReportingLineLength(employee);
            int excess = Math.max(0, lineLength - maxAllowed);

            results.add(new ReportingLineResult(
                    employee,
                    lineLength,
                    maxAllowed,
                    excess
            ));
        }

        cachedReportingLineResults = Collections.unmodifiableList(results);
        return cachedReportingLineResults;
    }

    /**
     * Returns employees with reporting lines that exceed the maximum allowed depth.
     * 
     * @return list of employees with long reporting lines
     */
    public List<ReportingLineResult> getEmployeesWithLongReportingLines() {
        return analyzeReportingLines().stream()
                .filter(ReportingLineResult::isTooLong)
                .toList();
    }

    /**
     * Runs complete analysis and returns statistics.
     * 
     * @return analysis statistics
     */
    public AnalysisStatistics runAnalysis() {
        Instant start = Instant.now();

        List<SalaryAnalysisResult> salaryResults = analyzeManagerSalaries();
        List<ReportingLineResult> reportingResults = analyzeReportingLines();

        int maxDepth = reportingResults.stream()
                .mapToInt(ReportingLineResult::reportingLineLength)
                .max()
                .orElse(0);

        Duration executionTime = Duration.between(start, Instant.now());

        AnalysisStatistics stats = AnalysisStatistics.builder()
                .totalEmployees(employeesById.size())
                .totalManagers(salaryResults.size())
                .underpaidManagerCount((int) salaryResults.stream()
                        .filter(r -> r.status() == SalaryAnalysisResult.SalaryStatus.UNDERPAID).count())
                .overpaidManagerCount((int) salaryResults.stream()
                        .filter(r -> r.status() == SalaryAnalysisResult.SalaryStatus.OVERPAID).count())
                .longReportingLineCount((int) reportingResults.stream()
                        .filter(ReportingLineResult::isTooLong).count())
                .maxReportingLineDepth(maxDepth)
                .executionTime(executionTime)
                .build();

        LOGGER.info(() -> "Analysis completed: " + stats);
        return stats;
    }

    private double calculateAverageSalary(List<Employee> employees) {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    private int calculateReportingLineLength(Employee employee) {
        int length = 0;
        String currentManagerId = employee.getManagerId();

        Set<String> visited = new HashSet<>();
        visited.add(employee.getId());

        while (currentManagerId != null && !currentManagerId.isBlank()) {
            if (visited.contains(currentManagerId)) {
                throw new IllegalStateException("Circular reference detected for employee: " + employee.getId());
            }

            Employee manager = employeesById.get(currentManagerId);
            if (manager == null) {
                throw new IllegalStateException("Manager not found: " + currentManagerId +
                        " for employee: " + employee.getId());
            }

            visited.add(currentManagerId);
            length++;
            currentManagerId = manager.getManagerId();
        }

        return length;
    }

    /**
     * Finds the CEO (employee with no manager).
     * 
     * @return Optional containing the CEO, or empty if not found
     */
    public Optional<Employee> findCeo() {
        return employeesById.values().stream()
                .filter(Employee::isCeo)
                .findFirst();
    }

    /**
     * Gets direct subordinates of a manager.
     * 
     * @param managerId the manager's employee ID
     * @return list of direct subordinates (empty if none)
     */
    public List<Employee> getDirectSubordinates(String managerId) {
        return subordinatesByManagerId.getOrDefault(managerId, Collections.emptyList());
    }

    /**
     * Returns the total number of employees.
     */
    public int getEmployeeCount() {
        return employeesById.size();
    }

    /**
     * Returns the configuration used by this analyzer.
     */
    public AnalyzerConfig getConfig() {
        return config;
    }
}
