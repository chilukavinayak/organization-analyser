package com.bigcompany.analyzer.validation;

import com.bigcompany.analyzer.model.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrganizationDataValidator Tests")
class OrganizationDataValidatorTest {

    @Test
    @DisplayName("Should pass validation for valid organization")
    void shouldPassValidationForValidOrganization() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Manager", "One", 100000, "1"));
        employees.put("3", new Employee("3", "Employee", "Two", 50000, "2"));

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertTrue(result.isValid());
        assertEquals(0, result.getErrorCount());
    }

    @Test
    @DisplayName("Should fail validation for empty organization")
    void shouldFailValidationForEmptyOrganization() {
        Map<String, Employee> employees = new HashMap<>();

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("No employees")));
    }

    @Test
    @DisplayName("Should fail validation when no CEO exists")
    void shouldFailValidationWhenNoCeoExists() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "Employee", "One", 50000, "2"));
        employees.put("2", new Employee("2", "Employee", "Two", 50000, "1"));

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("No CEO")));
    }

    @Test
    @DisplayName("Should fail validation when multiple CEOs exist")
    void shouldFailValidationWhenMultipleCeosExist() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "One", 200000, null));
        employees.put("2", new Employee("2", "CEO", "Two", 200000, null));

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Multiple CEOs")));
    }

    @Test
    @DisplayName("Should fail validation for non-existent manager reference")
    void shouldFailValidationForNonExistentManagerReference() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Employee", "One", 50000, "999"));

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("non-existent manager")));
    }

    @Test
    @DisplayName("Should fail validation for circular reference")
    void shouldFailValidationForCircularReference() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Manager", "One", 100000, "3"));
        employees.put("3", new Employee("3", "Manager", "Two", 100000, "2"));

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Circular reference")));
    }

    @Test
    @DisplayName("Should warn for zero salary employees")
    void shouldWarnForZeroSalaryEmployees() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 200000, null));
        employees.put("2", new Employee("2", "Intern", "One", 0, "1"));

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("zero salary")));
    }

    @Test
    @DisplayName("Should warn for unusually high salary")
    void shouldWarnForUnusuallyHighSalary() {
        Map<String, Employee> employees = new HashMap<>();
        employees.put("1", new Employee("1", "CEO", "Boss", 50_000_000, null));

        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult result = validator.validate();

        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("unusually high")));
    }
}
