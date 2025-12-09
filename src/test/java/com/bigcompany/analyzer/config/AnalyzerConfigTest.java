package com.bigcompany.analyzer.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnalyzerConfig Tests")
class AnalyzerConfigTest {

    @Test
    @DisplayName("Should create config with default values")
    void shouldCreateDefaultConfig() {
        AnalyzerConfig config = AnalyzerConfig.defaults();

        assertEquals(0.20, config.getMinSalaryPercentageAboveSubordinates());
        assertEquals(0.50, config.getMaxSalaryPercentageAboveSubordinates());
        assertEquals(4, config.getMaxReportingLineLength());
    }

    @Test
    @DisplayName("Should calculate min expected salary correctly")
    void shouldCalculateMinExpectedSalary() {
        AnalyzerConfig config = AnalyzerConfig.defaults();

        double minExpected = config.calculateMinExpectedSalary(50000);

        assertEquals(60000, minExpected, 0.01);
    }

    @Test
    @DisplayName("Should calculate max expected salary correctly")
    void shouldCalculateMaxExpectedSalary() {
        AnalyzerConfig config = AnalyzerConfig.defaults();

        double maxExpected = config.calculateMaxExpectedSalary(50000);

        assertEquals(75000, maxExpected, 0.01);
    }

    @Test
    @DisplayName("Should load config from properties")
    void shouldLoadConfigFromProperties() {
        AnalyzerConfig config = AnalyzerConfig.load();

        assertNotNull(config);
        assertTrue(config.getMinSalaryPercentageAboveSubordinates() >= 0);
        assertTrue(config.getMaxSalaryPercentageAboveSubordinates() >= 0);
        assertTrue(config.getMaxReportingLineLength() >= 1);
    }

    @Test
    @DisplayName("Should have readable toString")
    void shouldHaveReadableToString() {
        AnalyzerConfig config = AnalyzerConfig.defaults();

        String str = config.toString();

        assertTrue(str.contains("20%"));
        assertTrue(str.contains("50%"));
        assertTrue(str.contains("4"));
    }
}
