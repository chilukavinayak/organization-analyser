package com.bigcompany.analyzer.parser;

import com.bigcompany.analyzer.model.Employee;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for reading employee data from CSV files.
 * 
 * <p>Parses CSV files with the following format:
 * <pre>
 * Id,firstName,lastName,salary,managerId
 * 123,Joe,Doe,60000,
 * 124,Martin,Chekov,45000,123
 * </pre>
 * 
 * <h2>Validation</h2>
 * <p>The parser performs comprehensive validation:
 * <ul>
 *   <li>Header row must have at least 5 columns</li>
 *   <li>Employee ID must be non-empty and unique</li>
 *   <li>First and last names must be non-empty</li>
 *   <li>Salary must be a valid non-negative number</li>
 *   <li>Blank lines are skipped</li>
 *   <li>Whitespace is trimmed from all values</li>
 * </ul>
 * 
 * <h2>Error Handling</h2>
 * <p>Throws {@link CsvParseException} with detailed error messages including
 * line numbers for debugging.
 * 
 * @see CsvParseException
 * @see Employee
 */
public class EmployeeCsvParser {

    private static final int EXPECTED_COLUMNS = 5;
    private static final int COL_ID = 0;
    private static final int COL_FIRST_NAME = 1;
    private static final int COL_LAST_NAME = 2;
    private static final int COL_SALARY = 3;
    private static final int COL_MANAGER_ID = 4;

    public Map<String, Employee> parseFile(Path filePath) {
        Map<String, Employee> employees = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new CsvParseException("CSV file is empty");
            }

            validateHeader(headerLine);

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                Employee employee = parseLine(line, lineNumber);
                if (employees.containsKey(employee.getId())) {
                    throw new CsvParseException(lineNumber, "Duplicate employee ID: " + employee.getId());
                }
                employees.put(employee.getId(), employee);
            }
        } catch (IOException e) {
            throw new CsvParseException("Failed to read CSV file: " + filePath, e);
        }

        return employees;
    }

    private void validateHeader(String headerLine) {
        String[] headers = headerLine.split(",");
        if (headers.length < EXPECTED_COLUMNS) {
            throw new CsvParseException("Invalid header: expected at least " + EXPECTED_COLUMNS + " columns");
        }
    }

    private Employee parseLine(String line, int lineNumber) {
        String[] parts = line.split(",", -1);

        if (parts.length < EXPECTED_COLUMNS) {
            throw new CsvParseException(lineNumber,
                    "Expected " + EXPECTED_COLUMNS + " columns but found " + parts.length);
        }

        String id = parts[COL_ID].trim();
        String firstName = parts[COL_FIRST_NAME].trim();
        String lastName = parts[COL_LAST_NAME].trim();
        String salaryStr = parts[COL_SALARY].trim();
        String managerId = parts[COL_MANAGER_ID].trim();

        if (id.isEmpty()) {
            throw new CsvParseException(lineNumber, "Employee ID cannot be empty");
        }

        if (firstName.isEmpty()) {
            throw new CsvParseException(lineNumber, "First name cannot be empty");
        }

        if (lastName.isEmpty()) {
            throw new CsvParseException(lineNumber, "Last name cannot be empty");
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            throw new CsvParseException(lineNumber, "Invalid salary value: " + salaryStr, e);
        }

        String managerIdValue = managerId.isEmpty() ? null : managerId;

        return new Employee(id, firstName, lastName, salary, managerIdValue);
    }
}
