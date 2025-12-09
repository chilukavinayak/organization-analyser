package com.bigcompany.analyzer;

import com.bigcompany.analyzer.model.Employee;
import com.bigcompany.analyzer.parser.CsvParseException;
import com.bigcompany.analyzer.parser.EmployeeCsvParser;
import com.bigcompany.analyzer.service.OrganizationAnalyzer;
import com.bigcompany.analyzer.service.TestResultFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data-driven test suite for the Organization Analyzer.
 * 
 * <p>This test class uses a file-based approach where each test case is defined by:
 * <ul>
 *   <li>{@code input.csv} - The employee data to analyze</li>
 *   <li>{@code expected.txt} - The expected analysis output</li>
 * </ul>
 * 
 * <h2>Test Case Structure</h2>
 * <p>Test cases are organized in {@code src/test/resources/testcases/} with each 
 * subdirectory representing a single test case. The directory name becomes the test name.
 * 
 * <h2>Expected Output Format</h2>
 * <p>For success cases, the expected.txt follows this format:
 * <pre>
 * UNDERPAID_MANAGERS:
 * Name|ActualSalary|MinExpected|Deviation
 * OVERPAID_MANAGERS:
 * Name|ActualSalary|MaxExpected|Deviation
 * LONG_REPORTING_LINES:
 * Name|LineLength|Excess
 * </pre>
 * 
 * <p>For error cases, prefix with ERROR: or PARSE_ERROR:
 * <pre>
 * ERROR:Circular reference detected
 * PARSE_ERROR:Invalid salary
 * </pre>
 * 
 * <h2>Adding New Test Cases</h2>
 * <ol>
 *   <li>Create a new directory under {@code testcases/}</li>
 *   <li>Add {@code input.csv} with employee data</li>
 *   <li>Add {@code expected.txt} with expected output</li>
 *   <li>Run tests - the new case is automatically discovered</li>
 * </ol>
 * 
 * @see TestResultFormatter
 * @see OrganizationAnalyzer
 */
@DisplayName("Data-Driven Organization Analyzer Tests")
class DataDrivenOrganizationAnalyzerTest {

    private static final String TESTCASES_DIR = "testcases";

    static Stream<String> testCaseProvider() throws IOException, URISyntaxException {
        Path testcasesPath = Paths.get(
                DataDrivenOrganizationAnalyzerTest.class.getClassLoader()
                        .getResource(TESTCASES_DIR).toURI()
        );

        return Files.list(testcasesPath)
                .filter(Files::isDirectory)
                .map(path -> path.getFileName().toString())
                .sorted();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testCaseProvider")
    @DisplayName("Test case: ")
    void runTestCase(String testCaseName) throws IOException, URISyntaxException {
        Path testCaseDir = Paths.get(
                getClass().getClassLoader().getResource(TESTCASES_DIR + "/" + testCaseName).toURI()
        );

        Path inputFile = testCaseDir.resolve("input.csv");
        Path expectedFile = testCaseDir.resolve("expected.txt");

        assertTrue(Files.exists(inputFile), "Input file must exist: " + inputFile);
        assertTrue(Files.exists(expectedFile), "Expected file must exist: " + expectedFile);

        String expectedContent = Files.readString(expectedFile).trim();

        if (expectedContent.startsWith("ERROR:")) {
            runErrorTestCase(inputFile, expectedContent);
        } else if (expectedContent.startsWith("PARSE_ERROR:")) {
            runParseErrorTestCase(inputFile, expectedContent);
        } else {
            runSuccessTestCase(inputFile, expectedContent);
        }
    }

    private void runSuccessTestCase(Path inputFile, String expectedContent) {
        EmployeeCsvParser parser = new EmployeeCsvParser();
        Map<String, Employee> employees = parser.parseFile(inputFile);
        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);

        String actualContent = TestResultFormatter.formatResults(analyzer).trim();

        assertEquals(expectedContent, actualContent,
                "Output mismatch for input file: " + inputFile);
    }

    private void runErrorTestCase(Path inputFile, String expectedContent) {
        String expectedError = expectedContent.substring("ERROR:".length());

        EmployeeCsvParser parser = new EmployeeCsvParser();
        Map<String, Employee> employees = parser.parseFile(inputFile);
        OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            analyzer.analyzeReportingLines();
        });

        assertTrue(exception.getMessage().contains(expectedError),
                "Expected error containing: " + expectedError + ", but got: " + exception.getMessage());
    }

    private void runParseErrorTestCase(Path inputFile, String expectedContent) {
        String expectedError = expectedContent.substring("PARSE_ERROR:".length());

        EmployeeCsvParser parser = new EmployeeCsvParser();

        CsvParseException exception = assertThrows(CsvParseException.class, () -> {
            parser.parseFile(inputFile);
        });

        assertTrue(exception.getMessage().contains(expectedError),
                "Expected error containing: " + expectedError + ", but got: " + exception.getMessage());
    }
}
