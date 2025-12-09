package com.bigcompany.analyzer.validation;

import com.bigcompany.analyzer.model.Employee;

import java.util.*;
import java.util.logging.Logger;

/**
 * Validates organizational data integrity before analysis.
 * 
 * <p>Performs the following validations:
 * <ul>
 *   <li>Exactly one CEO exists (employee with no manager)</li>
 *   <li>All manager references are valid</li>
 *   <li>No circular reporting relationships</li>
 *   <li>All employees are reachable from CEO</li>
 *   <li>No orphaned employees</li>
 * </ul>
 */
public final class OrganizationDataValidator {

    private static final Logger LOGGER = Logger.getLogger(OrganizationDataValidator.class.getName());

    private final Map<String, Employee> employees;

    public OrganizationDataValidator(Map<String, Employee> employees) {
        this.employees = Objects.requireNonNull(employees, "Employees map cannot be null");
    }

    /**
     * Validates the organizational data.
     * 
     * @return validation result containing any errors or warnings
     */
    public ValidationResult validate() {
        ValidationResult.Builder result = ValidationResult.builder();

        if (employees.isEmpty()) {
            result.addError("No employees found in the data");
            return result.build();
        }

        validateCeo(result);
        validateManagerReferences(result);
        validateNoCircularReferences(result);
        validateConnectivity(result);
        validateDataQuality(result);

        ValidationResult validationResult = result.build();
        
        if (validationResult.isValid()) {
            LOGGER.info("Data validation passed successfully");
        } else {
            LOGGER.warning("Data validation failed with " + validationResult.getErrorCount() + " error(s)");
        }

        return validationResult;
    }

    private void validateCeo(ValidationResult.Builder result) {
        List<Employee> ceos = employees.values().stream()
                .filter(Employee::isCeo)
                .toList();

        if (ceos.isEmpty()) {
            result.addError("No CEO found (employee with no manager)");
        } else if (ceos.size() > 1) {
            result.addError("Multiple CEOs found: %s",
                    ceos.stream()
                            .map(e -> e.getId() + " (" + e.getFullName() + ")")
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(""));
        }
    }

    private void validateManagerReferences(ValidationResult.Builder result) {
        for (Employee employee : employees.values()) {
            if (!employee.isCeo()) {
                String managerId = employee.getManagerId();
                if (!employees.containsKey(managerId)) {
                    result.addError("Employee %s (%s) references non-existent manager: %s",
                            employee.getId(), employee.getFullName(), managerId);
                }
            }
        }
    }

    private void validateNoCircularReferences(ValidationResult.Builder result) {
        for (Employee employee : employees.values()) {
            if (hasCircularReference(employee)) {
                result.addError("Circular reference detected starting from employee %s (%s)",
                        employee.getId(), employee.getFullName());
            }
        }
    }

    private boolean hasCircularReference(Employee employee) {
        Set<String> visited = new HashSet<>();
        visited.add(employee.getId());

        String currentManagerId = employee.getManagerId();
        while (currentManagerId != null && !currentManagerId.isBlank()) {
            if (visited.contains(currentManagerId)) {
                return true;
            }
            visited.add(currentManagerId);

            Employee manager = employees.get(currentManagerId);
            if (manager == null) {
                break;
            }
            currentManagerId = manager.getManagerId();
        }

        return false;
    }

    private void validateConnectivity(ValidationResult.Builder result) {
        Optional<Employee> ceo = employees.values().stream()
                .filter(Employee::isCeo)
                .findFirst();

        if (ceo.isEmpty()) {
            return;
        }

        Set<String> reachableFromCeo = new HashSet<>();
        collectReachableEmployees(ceo.get().getId(), reachableFromCeo);

        for (Employee employee : employees.values()) {
            if (!reachableFromCeo.contains(employee.getId())) {
                result.addWarning("Employee %s (%s) is not connected to the CEO hierarchy",
                        employee.getId(), employee.getFullName());
            }
        }
    }

    private void collectReachableEmployees(String managerId, Set<String> reachable) {
        reachable.add(managerId);
        
        for (Employee employee : employees.values()) {
            if (managerId.equals(employee.getManagerId()) && !reachable.contains(employee.getId())) {
                collectReachableEmployees(employee.getId(), reachable);
            }
        }
    }

    private void validateDataQuality(ValidationResult.Builder result) {
        for (Employee employee : employees.values()) {
            if (employee.getSalary() == 0) {
                result.addWarning("Employee %s (%s) has zero salary",
                        employee.getId(), employee.getFullName());
            }

            if (employee.getSalary() > 10_000_000) {
                result.addWarning("Employee %s (%s) has unusually high salary: %.2f",
                        employee.getId(), employee.getFullName(), employee.getSalary());
            }
        }
    }
}
