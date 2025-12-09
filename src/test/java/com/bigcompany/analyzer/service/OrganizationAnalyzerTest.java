package com.bigcompany.analyzer.service;

import com.bigcompany.analyzer.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationAnalyzerTest {

    @Test
    @DisplayName("Should identify underpaid manager (earning less than 20% above subordinate average)")
    void shouldIdentifyUnderpaidManager() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 55000, null));
        employees.put("2", new Employee("2", "Sub", "One", 50000, "1"));
        employees.put("3", new Employee("3", "Sub", "Two", 50000, "1"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        List<SalaryAnalysisResult> underpaid = analyzer.getUnderpaidManagers();

        assertEquals(1, underpaid.size());
        assertEquals("CEO Boss", underpaid.get(0).manager().getFullName());
        assertEquals(5000, underpaid.get(0).getDeviation(), 0.01);
    }

    @Test
    @DisplayName("Should identify overpaid manager (earning more than 50% above subordinate average)")
    void shouldIdentifyOverpaidManager() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Sub", "One", 50000, "1"));
        employees.put("3", new Employee("3", "Sub", "Two", 50000, "1"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        List<SalaryAnalysisResult> overpaid = analyzer.getOverpaidManagers();

        assertEquals(1, overpaid.size());
        assertEquals("CEO Boss", overpaid.get(0).manager().getFullName());
        assertEquals(125000, overpaid.get(0).getDeviation(), 0.01);
    }

    @Test
    @DisplayName("Should identify manager within salary range")
    void shouldIdentifyManagerWithinRange() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 65000, null));
        employees.put("2", new Employee("2", "Sub", "One", 50000, "1"));
        employees.put("3", new Employee("3", "Sub", "Two", 50000, "1"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);

        assertTrue(analyzer.getUnderpaidManagers().isEmpty());
        assertTrue(analyzer.getOverpaidManagers().isEmpty());
    }

    @Test
    @DisplayName("Should detect employees with reporting line longer than 4")
    void shouldDetectLongReportingLines() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "VP", "One", 150000, "1"));
        employees.put("3", new Employee("3", "Dir", "Two", 120000, "2"));
        employees.put("4", new Employee("4", "Mgr", "Three", 100000, "3"));
        employees.put("5", new Employee("5", "Lead", "Four", 80000, "4"));
        employees.put("6", new Employee("6", "Emp", "Five", 60000, "5"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        List<ReportingLineResult> longLines = analyzer.getEmployeesWithLongReportingLines();

        assertEquals(1, longLines.size());
        assertEquals("Emp Five", longLines.get(0).employee().getFullName());
        assertEquals(5, longLines.get(0).reportingLineLength());
        assertEquals(1, longLines.get(0).excessLength());
    }

    @Test
    @DisplayName("Should not flag employees with reporting line of exactly 4")
    void shouldNotFlagReportingLineOfExactlyFour() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "VP", "One", 150000, "1"));
        employees.put("3", new Employee("3", "Dir", "Two", 120000, "2"));
        employees.put("4", new Employee("4", "Mgr", "Three", 100000, "3"));
        employees.put("5", new Employee("5", "Lead", "Four", 80000, "4"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        List<ReportingLineResult> longLines = analyzer.getEmployeesWithLongReportingLines();

        assertTrue(longLines.isEmpty());
    }

    @Test
    @DisplayName("Should find CEO")
    void shouldFindCeo() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Sub", "One", 50000, "1"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        Optional<Employee> ceo = analyzer.findCeo();

        assertTrue(ceo.isPresent());
        assertEquals("CEO Boss", ceo.get().getFullName());
    }

    @Test
    @DisplayName("Should get direct subordinates")
    void shouldGetDirectSubordinates() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Sub", "One", 50000, "1"));
        employees.put("3", new Employee("3", "Sub", "Two", 60000, "1"));
        employees.put("4", new Employee("4", "Sub", "Three", 40000, "2"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        List<Employee> ceoSubordinates = analyzer.getDirectSubordinates("1");

        assertEquals(2, ceoSubordinates.size());
    }

    @Test
    @DisplayName("Should throw exception for circular reference")
    void shouldThrowExceptionForCircularReference() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "One", "Person", 50000, "2"));
        employees.put("2", new Employee("2", "Two", "Person", 50000, "1"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);

        assertThrows(IllegalStateException.class, analyzer::analyzeReportingLines);
    }

    @Test
    @DisplayName("Should throw exception for missing manager")
    void shouldThrowExceptionForMissingManager() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Emp", "One", 50000, "999"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);

        assertThrows(IllegalStateException.class, analyzer::analyzeReportingLines);
    }

    @Test
    @DisplayName("Should handle employee with no subordinates")
    void shouldHandleEmployeeWithNoSubordinates() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        List<SalaryAnalysisResult> results = analyzer.analyzeManagerSalaries();

        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should calculate correct minimum expected salary (20% above average)")
    void shouldCalculateCorrectMinExpectedSalary() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 100000, null));
        employees.put("2", new Employee("2", "Sub", "One", 40000, "1"));
        employees.put("3", new Employee("3", "Sub", "Two", 60000, "1"));

        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);
        List<SalaryAnalysisResult> results = analyzer.analyzeManagerSalaries();

        assertEquals(1, results.size());
        assertEquals(50000, results.get(0).averageSubordinateSalary(), 0.01);
        assertEquals(60000, results.get(0).minExpectedSalary(), 0.01);
        assertEquals(75000, results.get(0).maxExpectedSalary(), 0.01);
    }
}
