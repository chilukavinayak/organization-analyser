package com.bigcompany.analyzer;

import com.bigcompany.analyzer.config.AnalyzerConfig;
import com.bigcompany.analyzer.model.Employee;
import com.bigcompany.analyzer.parser.CsvParseException;
import com.bigcompany.analyzer.parser.EmployeeCsvParser;
import com.bigcompany.analyzer.service.AnalysisReportPrinter;
import com.bigcompany.analyzer.service.AnalysisStatistics;
import com.bigcompany.analyzer.service.OrganizationAnalyzer;
import com.bigcompany.analyzer.validation.OrganizationDataValidator;
import com.bigcompany.analyzer.validation.ValidationResult;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Main application entry point for the Organization Analyzer.
 * 
 * <p>This application reads employee data from a CSV file and produces a report
 * analyzing salary compliance and reporting line depth.
 * 
 * <h2>Usage</h2>
 * <pre>
 * java -jar organization-analyzer.jar &lt;csv-file-path&gt; [options]
 * 
 * Options:
 *   --validate-only    Only validate data, don't run analysis
 *   --strict           Fail on validation warnings
 *   --help             Show this help message
 * </pre>
 * 
 * <h2>Exit Codes</h2>
 * <ul>
 *   <li>0 - Success (no issues found)</li>
 *   <li>1 - Analysis found issues</li>
 *   <li>2 - Invalid arguments or file not found</li>
 *   <li>3 - Parse error in input file</li>
 *   <li>4 - Data validation failed</li>
 *   <li>5 - Internal error</li>
 * </ul>
 */
public final class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    // Exit codes
    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_ISSUES_FOUND = 1;
    public static final int EXIT_INVALID_ARGS = 2;
    public static final int EXIT_PARSE_ERROR = 3;
    public static final int EXIT_VALIDATION_ERROR = 4;
    public static final int EXIT_INTERNAL_ERROR = 5;

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) {
        configureLogging();

        int exitCode = new Application().run(args);
        System.exit(exitCode);
    }

    /**
     * Runs the application with the given arguments.
     * 
     * @param args command line arguments
     * @return exit code
     */
    public int run(String[] args) {
        try {
            return doRun(args);
        } catch (Exception e) {
            LOGGER.severe("Unexpected error: " + e.getMessage());
            System.err.println("Internal error: " + e.getMessage());
            e.printStackTrace(System.err);
            return EXIT_INTERNAL_ERROR;
        }
    }

    private int doRun(String[] args) {
        // Parse arguments
        CommandLineArgs cliArgs = parseArguments(args);
        if (cliArgs == null) {
            return EXIT_INVALID_ARGS;
        }

        if (cliArgs.showHelp) {
            printHelp();
            return EXIT_SUCCESS;
        }

        if (cliArgs.showVersion) {
            System.out.println("Organization Analyzer v" + VERSION);
            return EXIT_SUCCESS;
        }

        // Validate file path
        Path csvPath = Path.of(cliArgs.filePath);
        if (!Files.exists(csvPath)) {
            System.err.println("Error: File not found: " + cliArgs.filePath);
            return EXIT_INVALID_ARGS;
        }

        if (!Files.isReadable(csvPath)) {
            System.err.println("Error: Cannot read file: " + cliArgs.filePath);
            return EXIT_INVALID_ARGS;
        }

        // Load configuration
        AnalyzerConfig config = AnalyzerConfig.load();
        LOGGER.info("Using configuration: " + config);

        // Parse CSV file
        Map<String, Employee> employees;
        try {
            LOGGER.info("Parsing file: " + csvPath);
            EmployeeCsvParser parser = new EmployeeCsvParser();
            employees = parser.parseFile(csvPath);
            LOGGER.info("Successfully loaded " + employees.size() + " employees");
        } catch (CsvParseException e) {
            System.err.println("Error parsing CSV file: " + e.getMessage());
            return EXIT_PARSE_ERROR;
        }

        // Validate data
        OrganizationDataValidator validator = new OrganizationDataValidator(employees);
        ValidationResult validationResult = validator.validate();

        if (!validationResult.isValid()) {
            System.err.println("Data validation failed:");
            for (String error : validationResult.getErrors()) {
                System.err.println("  ERROR: " + error);
            }
            return EXIT_VALIDATION_ERROR;
        }

        if (validationResult.hasWarnings()) {
            System.err.println("Data validation warnings:");
            for (String warning : validationResult.getWarnings()) {
                System.err.println("  WARNING: " + warning);
            }
            if (cliArgs.strictMode) {
                System.err.println("Strict mode enabled - failing due to warnings");
                return EXIT_VALIDATION_ERROR;
            }
        }

        if (cliArgs.validateOnly) {
            System.out.println("Validation passed. " + employees.size() + " employees loaded.");
            return EXIT_SUCCESS;
        }

        // Run analysis
        try {
            OrganizationAnalyzer analyzer = new OrganizationAnalyzer(employees, config);
            AnalysisReportPrinter printer = new AnalysisReportPrinter();
            printer.printFullReport(analyzer);

            AnalysisStatistics stats = analyzer.runAnalysis();
            return stats.hasIssues() ? EXIT_ISSUES_FOUND : EXIT_SUCCESS;

        } catch (IllegalStateException e) {
            System.err.println("Error analyzing organization structure: " + e.getMessage());
            return EXIT_VALIDATION_ERROR;
        }
    }

    private CommandLineArgs parseArguments(String[] args) {
        CommandLineArgs result = new CommandLineArgs();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case "--help", "-h" -> result.showHelp = true;
                case "--version", "-v" -> result.showVersion = true;
                case "--validate-only" -> result.validateOnly = true;
                case "--strict" -> result.strictMode = true;
                default -> {
                    if (arg.startsWith("-")) {
                        System.err.println("Unknown option: " + arg);
                        printUsage();
                        return null;
                    }
                    if (result.filePath == null) {
                        result.filePath = arg;
                    } else {
                        System.err.println("Unexpected argument: " + arg);
                        printUsage();
                        return null;
                    }
                }
            }
        }

        if (!result.showHelp && !result.showVersion && result.filePath == null) {
            System.err.println("Error: CSV file path is required");
            printUsage();
            return null;
        }

        return result;
    }

    private void printUsage() {
        System.err.println("Usage: java -jar organization-analyzer.jar <csv-file> [options]");
        System.err.println("Run with --help for more information.");
    }

    private void printHelp() {
        System.out.println("Organization Analyzer v" + VERSION);
        System.out.println();
        System.out.println("Analyzes organizational structure for salary compliance and reporting line depth.");
        System.out.println();
        System.out.println("USAGE:");
        System.out.println("  java -jar organization-analyzer.jar <csv-file> [options]");
        System.out.println();
        System.out.println("ARGUMENTS:");
        System.out.println("  <csv-file>         Path to the CSV file containing employee data");
        System.out.println();
        System.out.println("OPTIONS:");
        System.out.println("  --help, -h         Show this help message");
        System.out.println("  --version, -v      Show version information");
        System.out.println("  --validate-only    Only validate data, don't run analysis");
        System.out.println("  --strict           Fail on validation warnings");
        System.out.println();
        System.out.println("EXIT CODES:");
        System.out.println("  0    Success (no issues found)");
        System.out.println("  1    Analysis completed with issues found");
        System.out.println("  2    Invalid arguments or file not found");
        System.out.println("  3    Parse error in input file");
        System.out.println("  4    Data validation failed");
        System.out.println("  5    Internal error");
        System.out.println();
        System.out.println("CONFIGURATION:");
        System.out.println("  Configuration can be set via analyzer.properties or system properties:");
        System.out.println("  -Danalyzer.salary.min-percentage=0.20");
        System.out.println("  -Danalyzer.salary.max-percentage=0.50");
        System.out.println("  -Danalyzer.reporting-line.max-length=4");
        System.out.println();
        System.out.println("EXAMPLES:");
        System.out.println("  java -jar organization-analyzer.jar employees.csv");
        System.out.println("  java -jar organization-analyzer.jar data.csv --validate-only");
        System.out.println("  java -Danalyzer.salary.min-percentage=0.25 -jar organization-analyzer.jar data.csv");
    }

    private static void configureLogging() {
        try (InputStream is = Application.class.getClassLoader()
                .getResourceAsStream("logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load logging configuration");
        }
    }

    private static class CommandLineArgs {
        String filePath;
        boolean showHelp;
        boolean showVersion;
        boolean validateOnly;
        boolean strictMode;
    }
}
