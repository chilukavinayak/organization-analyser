package com.bigcompany.analyzer.parser;

import com.bigcompany.analyzer.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeCsvParserTest {

    private EmployeeCsvParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new EmployeeCsvParser();
    }

    @Test
    @DisplayName("Should parse valid CSV file")
    void shouldParseValidCsvFile() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                124,Martin,Chekov,45000,123
                125,Bob,Ronstad,47000,123
                """;

        Path csvFile = createTempCsvFile(csvContent);
        Map<String, Employee> employees = parser.parseFile(csvFile);

        assertEquals(3, employees.size());

        Employee ceo = employees.get("123");
        assertNotNull(ceo);
        assertEquals("Joe", ceo.getFirstName());
        assertEquals("Doe", ceo.getLastName());
        assertEquals(60000, ceo.getSalary());
        assertTrue(ceo.isCeo());

        Employee emp124 = employees.get("124");
        assertNotNull(emp124);
        assertEquals("Martin", emp124.getFirstName());
        assertEquals("123", emp124.getManagerId());
    }

    @Test
    @DisplayName("Should throw exception for empty file")
    void shouldThrowExceptionForEmptyFile() throws IOException {
        Path csvFile = createTempCsvFile("");

        assertThrows(CsvParseException.class, () -> parser.parseFile(csvFile));
    }

    @Test
    @DisplayName("Should throw exception for duplicate employee ID")
    void shouldThrowExceptionForDuplicateEmployeeId() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                123,Jane,Smith,50000,
                """;

        Path csvFile = createTempCsvFile(csvContent);

        CsvParseException exception = assertThrows(CsvParseException.class,
                () -> parser.parseFile(csvFile));
        assertTrue(exception.getMessage().contains("Duplicate employee ID"));
    }

    @Test
    @DisplayName("Should throw exception for invalid salary")
    void shouldThrowExceptionForInvalidSalary() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,invalid,
                """;

        Path csvFile = createTempCsvFile(csvContent);

        CsvParseException exception = assertThrows(CsvParseException.class,
                () -> parser.parseFile(csvFile));
        assertTrue(exception.getMessage().contains("Invalid salary"));
    }

    @Test
    @DisplayName("Should throw exception for missing columns")
    void shouldThrowExceptionForMissingColumns() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe
                """;

        Path csvFile = createTempCsvFile(csvContent);

        assertThrows(CsvParseException.class, () -> parser.parseFile(csvFile));
    }

    @Test
    @DisplayName("Should throw exception for empty employee ID")
    void shouldThrowExceptionForEmptyEmployeeId() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                ,Joe,Doe,50000,
                """;

        Path csvFile = createTempCsvFile(csvContent);

        CsvParseException exception = assertThrows(CsvParseException.class,
                () -> parser.parseFile(csvFile));
        assertTrue(exception.getMessage().contains("Employee ID cannot be empty"));
    }

    @Test
    @DisplayName("Should skip blank lines")
    void shouldSkipBlankLines() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123,Joe,Doe,60000,
                
                124,Martin,Chekov,45000,123
                """;

        Path csvFile = createTempCsvFile(csvContent);
        Map<String, Employee> employees = parser.parseFile(csvFile);

        assertEquals(2, employees.size());
    }

    @Test
    @DisplayName("Should handle whitespace in values")
    void shouldHandleWhitespaceInValues() throws IOException {
        String csvContent = """
                Id,firstName,lastName,salary,managerId
                123 , Joe , Doe , 60000 ,
                """;

        Path csvFile = createTempCsvFile(csvContent);
        Map<String, Employee> employees = parser.parseFile(csvFile);

        Employee employee = employees.get("123");
        assertEquals("Joe", employee.getFirstName());
        assertEquals("Doe", employee.getLastName());
    }

    private Path createTempCsvFile(String content) throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, content);
        return csvFile;
    }
}
