package com.bigcompany.analyzer.model;

import java.util.Objects;

/**
 * Immutable representation of an employee in the organization.
 * 
 * <p>Each employee has a unique ID, name, salary, and an optional manager reference.
 * The CEO is identified by having no manager (null or blank managerId).
 * 
 * <p>This class is immutable and thread-safe. Validation is performed in the constructor
 * to ensure no invalid employee instances can be created.
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Create a regular employee
 * Employee emp = new Employee("123", "John", "Doe", 50000, "100");
 * 
 * // Create the CEO (no manager)
 * Employee ceo = new Employee("100", "Jane", "Smith", 200000, null);
 * assertTrue(ceo.isCeo());
 * }</pre>
 * 
 * @author Organization Analyzer Team
 */
public class Employee {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final double salary;
    private final String managerId;

    public Employee(String id, String firstName, String lastName, double salary, String managerId) {
        this.id = Objects.requireNonNull(id, "Employee ID cannot be null");
        this.firstName = Objects.requireNonNull(firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(lastName, "Last name cannot be null");
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        this.salary = salary;
        this.managerId = managerId;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public double getSalary() {
        return salary;
    }

    public String getManagerId() {
        return managerId;
    }

    public boolean isCeo() {
        return managerId == null || managerId.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Employee{id='%s', name='%s %s', salary=%.2f, managerId='%s'}",
                id, firstName, lastName, salary, managerId != null ? managerId : "N/A");
    }
}
