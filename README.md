# Organization Analyzer

A **production-grade** Java application that analyzes organizational structure to identify salary compliance issues and overly long reporting lines.

## Problem Statement

BIG COMPANY needs to ensure:
1. Every manager earns **at least 20% more** than the average salary of their direct subordinates
2. Every manager earns **no more than 50% more** than the average salary of their direct subordinates
3. No employee has **more than 4 managers** between them and the CEO

## Business Rules Explained

### Rule 1: Minimum Manager Salary (20% above team average)

**Purpose:** Prevents underpaid managers

**Formula:**
```
Manager Salary ≥ 1.20 × Average(Subordinate Salaries)
```

**Example:**
```
Subordinates earn: 40,000; 50,000; 60,000
Average = (40,000 + 50,000 + 60,000) / 3 = 50,000

Manager must earn at least: 1.20 × 50,000 = 60,000

✗ Manager earns 55,000 → UNDERPAID (needs 5,000 more)
✓ Manager earns 65,000 → OK
```

### Rule 2: Maximum Manager Salary (50% above team average)

**Purpose:** Prevents excessive pay gaps between managers and their teams

**Formula:**
```
Manager Salary ≤ 1.50 × Average(Subordinate Salaries)
```

**Example:**
```
Same subordinates: average = 50,000
Manager must earn at most: 1.50 × 50,000 = 75,000

✗ Manager earns 80,000 → OVERPAID (by 5,000)
✓ Manager earns 72,000 → OK
```

### Combined Salary Band

Rules 1 & 2 together create an acceptable salary range for managers:

```
Using average subordinate salary of 50,000:

Minimum salary: 60,000  (20% above average)
Maximum salary: 75,000  (50% above average)

Valid range: 60,000 - 75,000
```

### Rule 3: Maximum Reporting Line Depth (≤ 4 managers to CEO)

**Purpose:** Keeps organizational hierarchy flat and efficient

**Why this matters:**
- Communication becomes slow in deep hierarchies
- Decision making becomes inefficient
- Company structure becomes too vertical

**Allowed Structure (4 managers):**
```
CEO
 └─ Manager 1
     └─ Manager 2
         └─ Manager 3
             └─ Manager 4
                 └─ Employee  ✓ (4 managers above - OK)
```

**Not Allowed (5 managers):**
```
CEO
 └─ Manager 1
     └─ Manager 2
         └─ Manager 3
             └─ Manager 4
                 └─ Manager 5
                     └─ Employee  ✗ (5 managers above - TOO DEEP)
```

### Rules Summary

| Rule | Description | Purpose |
|------|-------------|---------|
| 1 | Manager earns ≥ 20% more than team average | Prevents underpaid managers |
| 2 | Manager earns ≤ 50% more than team average | Prevents excessive pay gaps |
| 3 | Depth ≤ 4 managers between employee & CEO | Keeps hierarchy flat and efficient |

## Quick Start

```bash
# Build
mvn clean package

# Run with sample data
java -jar target/organization-analyzer-1.0.0.jar src/main/resources/employees.csv

# Show help
java -jar target/organization-analyzer-1.0.0.jar --help
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (optional, for containerized deployment)

## Installation

```bash
# Clone the repository
git clone https://github.com/interviewdeck-io/organization-analyzer.git
cd organization-analyzer

# Build the project
mvn clean package

# Run tests
mvn test
```

## Usage

### Command Line Interface

```
Usage: java -jar organization-analyzer.jar <csv-file> [options]

Arguments:
  <csv-file>         Path to the CSV file containing employee data

Options:
  --help, -h         Show help message
  --version, -v      Show version information
  --validate-only    Only validate data, don't run analysis
  --strict           Fail on validation warnings
```

### Examples

```bash
# Basic usage
java -jar organization-analyzer-1.0.0.jar employees.csv

# Validate data only (no analysis)
java -jar organization-analyzer-1.0.0.jar employees.csv --validate-only

# Strict mode - fail on warnings
java -jar organization-analyzer-1.0.0.jar employees.csv --strict

# Custom configuration via system properties
java -Danalyzer.salary.min-percentage=0.25 \
     -Danalyzer.salary.max-percentage=0.60 \
     -jar organization-analyzer-1.0.0.jar employees.csv
```

### Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success - no issues found |
| 1 | Analysis completed with issues found |
| 2 | Invalid arguments or file not found |
| 3 | Parse error in input file |
| 4 | Data validation failed |
| 5 | Internal error |

## Configuration

Configuration can be set via `analyzer.properties` file or system properties:

| Property | Default | Description |
|----------|---------|-------------|
| `analyzer.salary.min-percentage` | 0.20 | Minimum % above subordinate average (20%) |
| `analyzer.salary.max-percentage` | 0.50 | Maximum % above subordinate average (50%) |
| `analyzer.reporting-line.max-length` | 4 | Maximum managers between employee and CEO |

**Example analyzer.properties:**
```properties
analyzer.salary.min-percentage=0.20
analyzer.salary.max-percentage=0.50
analyzer.reporting-line.max-length=4
```

**Override via command line:**
```bash
java -Danalyzer.salary.min-percentage=0.25 -jar organization-analyzer-1.0.0.jar data.csv
```

## Sample Output

```
================================================================================
                     ORGANIZATION STRUCTURE ANALYSIS REPORT
================================================================================
  Analysis Date: 2025-12-09 12:45:21
  Total Employees: 80

--------------------------------------------------------------------------------
CONFIGURATION
--------------------------------------------------------------------------------
  Minimum manager salary: 20% above subordinate average
  Maximum manager salary: 50% above subordinate average
  Maximum reporting line depth: 4 managers to CEO

--------------------------------------------------------------------------------
MANAGERS EARNING LESS THAN REQUIRED
--------------------------------------------------------------------------------
  ✓ No issues found.

--------------------------------------------------------------------------------
MANAGERS EARNING MORE THAN ALLOWED
--------------------------------------------------------------------------------
  Manager Name              |  Current Salary |     Max Allowed |  Overpaid By
  --------------------------------------------------------------------------
  John Smith                |      250,000.00 |      172,500.00 |    77,500.00
  Sarah Johnson             |      120,000.00 |      108,500.00 |    11,500.00

--------------------------------------------------------------------------------
EMPLOYEES WITH REPORTING LINE TOO LONG
--------------------------------------------------------------------------------
  Employee Name                  |     Line Length |     Excess
  --------------------------------------------------------------
  Richard Kim                    |               5 |          1
  Karen Cox                      |               5 |          1

--------------------------------------------------------------------------------
SUMMARY STATISTICS
--------------------------------------------------------------------------------
  Total Employees:              80
  Total Managers:               37
  Underpaid Managers:           0
  Overpaid Managers:            37
  Long Reporting Lines:         10
  Max Reporting Depth:          5
  Salary Compliance Rate:       0.0%
  Total Issues Found:           47
  Execution Time:               3 ms

================================================================================
                    ⚠ ANALYSIS COMPLETE - 47 ISSUE(S) FOUND
================================================================================
```

## Architecture

### Class Diagram

![Class Diagram](docs/class-diagram.png)

The PlantUML source is available at [docs/class-diagram.puml](docs/class-diagram.puml).

### Package Structure

```
com.bigcompany.analyzer
├── config/              # Configuration management
│   └── AnalyzerConfig   # Externalized configuration
├── model/               # Domain entities (immutable)
│   └── Employee         # Core employee representation
├── parser/              # Data access layer
│   ├── EmployeeCsvParser      # CSV file parsing
│   └── CsvParseException      # Parse error handling
├── validation/          # Data validation layer
│   ├── OrganizationDataValidator  # Data integrity checks
│   └── ValidationResult           # Validation results
├── service/             # Business logic layer
│   ├── OrganizationAnalyzer   # Core analysis engine
│   ├── AnalysisStatistics     # Execution metrics
│   ├── SalaryAnalysisResult   # Salary analysis (record)
│   ├── ReportingLineResult    # Reporting line (record)
│   └── AnalysisReportPrinter  # Report formatting
└── Application          # CLI entry point
```

### Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Immutable Classes** | Thread-safe, prevents invalid state |
| **Java Records** | Reduces boilerplate for data carriers |
| **Builder Pattern** | Used for ValidationResult and AnalysisStatistics |
| **Configuration Externalization** | Allows runtime customization without code changes |
| **Data Validation Layer** | Validates data integrity before analysis |
| **Caching in Analyzer** | Thread-safe caching of analysis results |
| **Exit Codes** | Enables scripting and CI/CD integration |
| **No External Dependencies** | Uses only Java SE as per requirements |

### Data Validation

The `OrganizationDataValidator` performs these checks:

| Check | Type | Description |
|-------|------|-------------|
| Empty data | Error | No employees in file |
| No CEO | Error | No employee without manager |
| Multiple CEOs | Error | More than one employee without manager |
| Invalid manager ref | Error | Manager ID doesn't exist |
| Circular reference | Error | Employee chain forms a cycle |
| Zero salary | Warning | Employee has zero salary |
| High salary | Warning | Salary exceeds 10,000,000 |

## Docker

### Building the Image

```bash
docker build -t organization-analyzer:1.0.0 .
```

### Running with Docker

```bash
# Run with included sample data
docker run organization-analyzer:1.0.0

# Run with custom CSV file
docker run -v /path/to/data:/app/data organization-analyzer:1.0.0 /app/data/your-file.csv

# Run with custom configuration
docker run -e JAVA_OPTS="-Danalyzer.salary.min-percentage=0.25" \
    organization-analyzer:1.0.0
```

### Docker Image Details

| Property | Value |
|----------|-------|
| Base Image | `eclipse-temurin:17-jre` |
| Image Size | ~270MB |
| Build | Multi-stage (Maven build → JRE runtime) |
| Default Data | `/app/data/employees.csv` (80 employees) |

## Testing

### Running Tests

```bash
# Run all tests (75 tests)
mvn test

# Run specific test class
mvn test -Dtest=OrganizationAnalyzerTest

# Run data-driven tests only
mvn test -Dtest=DataDrivenOrganizationAnalyzerTest

# Run with coverage (if configured)
mvn test jacoco:report
```

### Test Architecture

The project uses a **data-driven testing approach**:

```
src/test/
├── java/
│   └── com/bigcompany/analyzer/
│       ├── ApplicationTest.java                    # CLI integration tests
│       ├── DataDrivenOrganizationAnalyzerTest.java # Parameterized tests
│       ├── config/
│       │   └── AnalyzerConfigTest.java             # Config tests
│       ├── model/
│       │   └── EmployeeTest.java                   # Employee tests
│       ├── parser/
│       │   └── EmployeeCsvParserTest.java          # Parser tests
│       ├── validation/
│       │   └── OrganizationDataValidatorTest.java  # Validator tests
│       └── service/
│           ├── OrganizationAnalyzerTest.java       # Analyzer tests
│           └── TestResultFormatter.java            # Test helper
└── resources/
    └── testcases/                                  # 24 data-driven test cases
        ├── 01_underpaid_manager/
        │   ├── input.csv
        │   └── expected.txt
        └── ...
```

### Test Categories

| Category | Count | Description |
|----------|-------|-------------|
| Unit Tests | 51 | Individual class testing |
| Data-Driven | 24 | Input/output file comparison |
| Integration | 10 | CLI and end-to-end tests |
| **Total** | **75** | All tests |

### Adding New Test Cases

1. Create a new directory under `src/test/resources/testcases/`
2. Add `input.csv` with employee data
3. Add `expected.txt` with expected output
4. Run tests - new case is automatically discovered

## CSV File Format

```csv
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
125,Bob,Ronstad,47000,123
```

| Column | Description | Required |
|--------|-------------|----------|
| Id | Unique employee identifier | Yes |
| firstName | Employee's first name | Yes |
| lastName | Employee's last name | Yes |
| salary | Employee's salary (numeric) | Yes |
| managerId | ID of employee's manager | No (empty for CEO) |

## Project Structure

```
organization-analyzer/
├── pom.xml                         # Maven build configuration
├── Dockerfile                      # Docker build configuration
├── .dockerignore                   # Docker build exclusions
├── README.md                       # This file
├── docs/
│   └── class-diagram.puml          # UML class diagram
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/bigcompany/analyzer/
    │   │       ├── Application.java
    │   │       ├── config/
    │   │       │   └── AnalyzerConfig.java
    │   │       ├── model/
    │   │       │   └── Employee.java
    │   │       ├── parser/
    │   │       │   ├── CsvParseException.java
    │   │       │   └── EmployeeCsvParser.java
    │   │       ├── validation/
    │   │       │   ├── OrganizationDataValidator.java
    │   │       │   └── ValidationResult.java
    │   │       └── service/
    │   │           ├── AnalysisReportPrinter.java
    │   │           ├── AnalysisStatistics.java
    │   │           ├── OrganizationAnalyzer.java
    │   │           ├── ReportingLineResult.java
    │   │           └── SalaryAnalysisResult.java
    │   └── resources/
    │       ├── analyzer.properties     # Default configuration
    │       ├── logging.properties      # Logging configuration
    │       └── employees.csv           # Sample data (80 employees)
    └── test/
        ├── java/                       # 75 test classes
        └── resources/
            └── testcases/              # 24 data-driven test cases
```

## Key Features

| Feature | Description |
|---------|-------------|
| **Production-Grade CLI** | Proper argument parsing, help, version, exit codes |
| **Externalized Configuration** | Properties file and system property support |
| **Data Validation** | Comprehensive integrity checks before analysis |
| **Execution Statistics** | Timing, compliance rates, issue counts |
| **75 Automated Tests** | Unit, integration, and data-driven tests |
| **Uber-JAR Packaging** | Single executable JAR via Maven Shade |
| **Docker Support** | Multi-stage build for containerization |
| **Structured Logging** | Java Util Logging with configurable levels |
| **No External Dependencies** | Pure Java SE (except JUnit for testing) |

## Logging

Logging is configured via `src/main/resources/logging.properties`:

```properties
# Set log level (SEVERE, WARNING, INFO, FINE, FINER, FINEST)
com.bigcompany.analyzer.level=INFO

# Console output format
java.util.logging.SimpleFormatter.format=[%1$tF %1$tT] [%4$s] %3$s - %5$s%6$s%n
```

**Override at runtime:**
```bash
java -Djava.util.logging.config.file=custom-logging.properties \
     -jar organization-analyzer-1.0.0.jar data.csv
```

## Build Profiles

```bash
# Standard build
mvn clean package

# Production build (stricter checks, -Werror)
mvn clean package -Pproduction

# Skip tests
mvn clean package -DskipTests
```

## License

This project is provided as part of a coding exercise.
