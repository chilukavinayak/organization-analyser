package com.bigcompany.analyzer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Application Integration Tests")
class ApplicationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should return success exit code for valid data with no issues")
    void shouldReturnSuccessForValidDataWithNoIssues() throws IOException {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,CEO,Boss,70000,
                2,Emp,One,50000,1
                3,Emp,Two,50000,1
                """;
        Path csvFile = createTempCsv(csv);

        Application app = new Application();
        int exitCode = app.run(new String[]{csvFile.toString()});

        assertEquals(Application.EXIT_SUCCESS, exitCode);
    }

    @Test
    @DisplayName("Should return issues found exit code when issues exist")
    void shouldReturnIssuesFoundWhenIssuesExist() throws IOException {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,CEO,Boss,200000,
                2,Emp,One,50000,1
                3,Emp,Two,50000,1
                """;
        Path csvFile = createTempCsv(csv);

        Application app = new Application();
        int exitCode = app.run(new String[]{csvFile.toString()});

        assertEquals(Application.EXIT_ISSUES_FOUND, exitCode);
    }

    @Test
    @DisplayName("Should return invalid args for missing file argument")
    void shouldReturnInvalidArgsForMissingFile() {
        Application app = new Application();
        int exitCode = app.run(new String[]{});

        assertEquals(Application.EXIT_INVALID_ARGS, exitCode);
    }

    @Test
    @DisplayName("Should return invalid args for non-existent file")
    void shouldReturnInvalidArgsForNonExistentFile() {
        Application app = new Application();
        int exitCode = app.run(new String[]{"/non/existent/file.csv"});

        assertEquals(Application.EXIT_INVALID_ARGS, exitCode);
    }

    @Test
    @DisplayName("Should return parse error for invalid CSV")
    void shouldReturnParseErrorForInvalidCsv() throws IOException {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,CEO,Boss,not_a_number,
                """;
        Path csvFile = createTempCsv(csv);

        Application app = new Application();
        int exitCode = app.run(new String[]{csvFile.toString()});

        assertEquals(Application.EXIT_PARSE_ERROR, exitCode);
    }

    @Test
    @DisplayName("Should return validation error for circular reference")
    void shouldReturnValidationErrorForCircularReference() throws IOException {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,One,Person,50000,2
                2,Two,Person,50000,1
                """;
        Path csvFile = createTempCsv(csv);

        Application app = new Application();
        int exitCode = app.run(new String[]{csvFile.toString()});

        assertEquals(Application.EXIT_VALIDATION_ERROR, exitCode);
    }

    @Test
    @DisplayName("Should return success for help option")
    void shouldReturnSuccessForHelpOption() {
        Application app = new Application();
        int exitCode = app.run(new String[]{"--help"});

        assertEquals(Application.EXIT_SUCCESS, exitCode);
    }

    @Test
    @DisplayName("Should return success for version option")
    void shouldReturnSuccessForVersionOption() {
        Application app = new Application();
        int exitCode = app.run(new String[]{"--version"});

        assertEquals(Application.EXIT_SUCCESS, exitCode);
    }

    @Test
    @DisplayName("Should return success for validate-only with valid data")
    void shouldReturnSuccessForValidateOnlyWithValidData() throws IOException {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,CEO,Boss,200000,
                2,Emp,One,50000,1
                """;
        Path csvFile = createTempCsv(csv);

        Application app = new Application();
        int exitCode = app.run(new String[]{csvFile.toString(), "--validate-only"});

        assertEquals(Application.EXIT_SUCCESS, exitCode);
    }

    @Test
    @DisplayName("Should return invalid args for unknown option")
    void shouldReturnInvalidArgsForUnknownOption() throws IOException {
        String csv = """
                Id,firstName,lastName,salary,managerId
                1,CEO,Boss,200000,
                """;
        Path csvFile = createTempCsv(csv);

        Application app = new Application();
        int exitCode = app.run(new String[]{csvFile.toString(), "--unknown-option"});

        assertEquals(Application.EXIT_INVALID_ARGS, exitCode);
    }

    private Path createTempCsv(String content) throws IOException {
        Path csvFile = tempDir.resolve("test.csv");
        Files.writeString(csvFile, content);
        return csvFile;
    }
}
