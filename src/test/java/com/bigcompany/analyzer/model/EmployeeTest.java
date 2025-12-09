package com.bigcompany.analyzer.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    @DisplayName("Should create employee with valid data")
    void shouldCreateEmployeeWithValidData() {
        Employee employee = new Employee("1", "John", "Doe", 50000, "2");

        assertEquals("1", employee.getId());
        assertEquals("John", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
        assertEquals("John Doe", employee.getFullName());
        assertEquals(50000, employee.getSalary());
        assertEquals("2", employee.getManagerId());
        assertFalse(employee.isCeo());
    }

    @Test
    @DisplayName("Should identify CEO when manager ID is null")
    void shouldIdentifyCeoWhenManagerIdIsNull() {
        Employee ceo = new Employee("1", "Jane", "CEO", 200000, null);

        assertTrue(ceo.isCeo());
    }

    @Test
    @DisplayName("Should identify CEO when manager ID is blank")
    void shouldIdentifyCeoWhenManagerIdIsBlank() {
        Employee ceo = new Employee("1", "Jane", "CEO", 200000, "   ");

        assertTrue(ceo.isCeo());
    }

    @Test
    @DisplayName("Should throw exception for null ID")
    void shouldThrowExceptionForNullId() {
        assertThrows(NullPointerException.class, () ->
                new Employee(null, "John", "Doe", 50000, "2"));
    }

    @Test
    @DisplayName("Should throw exception for null first name")
    void shouldThrowExceptionForNullFirstName() {
        assertThrows(NullPointerException.class, () ->
                new Employee("1", null, "Doe", 50000, "2"));
    }

    @Test
    @DisplayName("Should throw exception for null last name")
    void shouldThrowExceptionForNullLastName() {
        assertThrows(NullPointerException.class, () ->
                new Employee("1", "John", null, 50000, "2"));
    }

    @Test
    @DisplayName("Should throw exception for negative salary")
    void shouldThrowExceptionForNegativeSalary() {
        assertThrows(IllegalArgumentException.class, () ->
                new Employee("1", "John", "Doe", -1000, "2"));
    }

    @Test
    @DisplayName("Should be equal when IDs match")
    void shouldBeEqualWhenIdsMatch() {
        Employee e1 = new Employee("1", "John", "Doe", 50000, "2");
        Employee e2 = new Employee("1", "Jane", "Smith", 60000, "3");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when IDs differ")
    void shouldNotBeEqualWhenIdsDiffer() {
        Employee e1 = new Employee("1", "John", "Doe", 50000, "2");
        Employee e2 = new Employee("2", "John", "Doe", 50000, "2");

        assertNotEquals(e1, e2);
    }
}
