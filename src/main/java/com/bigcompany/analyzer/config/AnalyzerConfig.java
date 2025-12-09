package com.bigcompany.analyzer.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration holder for the Organization Analyzer.
 * 
 * <p>Loads configuration from properties file with sensible defaults.
 * Configuration can be overridden via system properties.
 * 
 * <p>This class is immutable and thread-safe.
 */
public final class AnalyzerConfig {

    private static final Logger LOGGER = Logger.getLogger(AnalyzerConfig.class.getName());
    private static final String CONFIG_FILE = "analyzer.properties";

    private final double minSalaryPercentageAboveSubordinates;
    private final double maxSalaryPercentageAboveSubordinates;
    private final int maxReportingLineLength;

    private static final double DEFAULT_MIN_SALARY_PERCENTAGE = 0.20;
    private static final double DEFAULT_MAX_SALARY_PERCENTAGE = 0.50;
    private static final int DEFAULT_MAX_REPORTING_LINE_LENGTH = 4;

    private AnalyzerConfig(double minSalaryPercentage, double maxSalaryPercentage, int maxReportingLineLength) {
        this.minSalaryPercentageAboveSubordinates = minSalaryPercentage;
        this.maxSalaryPercentageAboveSubordinates = maxSalaryPercentage;
        this.maxReportingLineLength = maxReportingLineLength;
    }

    /**
     * Creates configuration with default values.
     * 
     * @return configuration with default values
     */
    public static AnalyzerConfig defaults() {
        return new AnalyzerConfig(
                DEFAULT_MIN_SALARY_PERCENTAGE,
                DEFAULT_MAX_SALARY_PERCENTAGE,
                DEFAULT_MAX_REPORTING_LINE_LENGTH
        );
    }

    /**
     * Loads configuration from properties file and system properties.
     * System properties take precedence over file properties.
     * 
     * @return loaded configuration
     */
    public static AnalyzerConfig load() {
        Properties props = new Properties();

        try (InputStream is = AnalyzerConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
                LOGGER.fine("Loaded configuration from " + CONFIG_FILE);
            } else {
                LOGGER.fine("No configuration file found, using defaults");
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to load configuration file: " + e.getMessage());
        }

        double minSalary = getDoubleProperty(props, "analyzer.salary.min-percentage", DEFAULT_MIN_SALARY_PERCENTAGE);
        double maxSalary = getDoubleProperty(props, "analyzer.salary.max-percentage", DEFAULT_MAX_SALARY_PERCENTAGE);
        int maxDepth = getIntProperty(props, "analyzer.reporting-line.max-length", DEFAULT_MAX_REPORTING_LINE_LENGTH);

        validateConfig(minSalary, maxSalary, maxDepth);

        return new AnalyzerConfig(minSalary, maxSalary, maxDepth);
    }

    private static double getDoubleProperty(Properties props, String key, double defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            try {
                return Double.parseDouble(systemValue);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid system property " + key + ": " + systemValue);
            }
        }

        String propValue = props.getProperty(key);
        if (propValue != null) {
            try {
                return Double.parseDouble(propValue);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid property " + key + ": " + propValue);
            }
        }

        return defaultValue;
    }

    private static int getIntProperty(Properties props, String key, int defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            try {
                return Integer.parseInt(systemValue);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid system property " + key + ": " + systemValue);
            }
        }

        String propValue = props.getProperty(key);
        if (propValue != null) {
            try {
                return Integer.parseInt(propValue);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid property " + key + ": " + propValue);
            }
        }

        return defaultValue;
    }

    private static void validateConfig(double minSalary, double maxSalary, int maxDepth) {
        if (minSalary < 0 || minSalary > 1) {
            throw new IllegalArgumentException("Min salary percentage must be between 0 and 1: " + minSalary);
        }
        if (maxSalary < 0 || maxSalary > 2) {
            throw new IllegalArgumentException("Max salary percentage must be between 0 and 2: " + maxSalary);
        }
        if (minSalary > maxSalary) {
            throw new IllegalArgumentException("Min salary percentage cannot exceed max: " + minSalary + " > " + maxSalary);
        }
        if (maxDepth < 1) {
            throw new IllegalArgumentException("Max reporting line length must be at least 1: " + maxDepth);
        }
    }

    public double getMinSalaryPercentageAboveSubordinates() {
        return minSalaryPercentageAboveSubordinates;
    }

    public double getMaxSalaryPercentageAboveSubordinates() {
        return maxSalaryPercentageAboveSubordinates;
    }

    public int getMaxReportingLineLength() {
        return maxReportingLineLength;
    }

    /**
     * Calculates the minimum expected salary for a manager.
     * 
     * @param averageSubordinateSalary average salary of direct subordinates
     * @return minimum expected manager salary
     */
    public double calculateMinExpectedSalary(double averageSubordinateSalary) {
        return averageSubordinateSalary * (1 + minSalaryPercentageAboveSubordinates);
    }

    /**
     * Calculates the maximum expected salary for a manager.
     * 
     * @param averageSubordinateSalary average salary of direct subordinates
     * @return maximum expected manager salary
     */
    public double calculateMaxExpectedSalary(double averageSubordinateSalary) {
        return averageSubordinateSalary * (1 + maxSalaryPercentageAboveSubordinates);
    }

    @Override
    public String toString() {
        return String.format("AnalyzerConfig{minSalary=%.0f%%, maxSalary=%.0f%%, maxReportingLine=%d}",
                minSalaryPercentageAboveSubordinates * 100,
                maxSalaryPercentageAboveSubordinates * 100,
                maxReportingLineLength);
    }
}
