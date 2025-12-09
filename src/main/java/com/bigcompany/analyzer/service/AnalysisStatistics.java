package com.bigcompany.analyzer.service;

import java.time.Duration;
import java.time.Instant;

/**
 * Statistics about the analysis execution.
 * 
 * <p>Captures metrics about the analysis run for monitoring and reporting.
 */
public final class AnalysisStatistics {

    private final int totalEmployees;
    private final int totalManagers;
    private final int underpaidManagerCount;
    private final int overpaidManagerCount;
    private final int longReportingLineCount;
    private final int maxReportingLineDepth;
    private final Duration executionTime;
    private final Instant analysisTimestamp;

    private AnalysisStatistics(Builder builder) {
        this.totalEmployees = builder.totalEmployees;
        this.totalManagers = builder.totalManagers;
        this.underpaidManagerCount = builder.underpaidManagerCount;
        this.overpaidManagerCount = builder.overpaidManagerCount;
        this.longReportingLineCount = builder.longReportingLineCount;
        this.maxReportingLineDepth = builder.maxReportingLineDepth;
        this.executionTime = builder.executionTime;
        this.analysisTimestamp = builder.analysisTimestamp;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public int getTotalManagers() {
        return totalManagers;
    }

    public int getUnderpaidManagerCount() {
        return underpaidManagerCount;
    }

    public int getOverpaidManagerCount() {
        return overpaidManagerCount;
    }

    public int getLongReportingLineCount() {
        return longReportingLineCount;
    }

    public int getMaxReportingLineDepth() {
        return maxReportingLineDepth;
    }

    public Duration getExecutionTime() {
        return executionTime;
    }

    public Instant getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public int getTotalIssues() {
        return underpaidManagerCount + overpaidManagerCount + longReportingLineCount;
    }

    public boolean hasIssues() {
        return getTotalIssues() > 0;
    }

    public double getComplianceRate() {
        if (totalManagers == 0) {
            return 100.0;
        }
        int compliantManagers = totalManagers - underpaidManagerCount - overpaidManagerCount;
        return (compliantManagers * 100.0) / totalManagers;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format(
                "AnalysisStatistics{employees=%d, managers=%d, underpaid=%d, overpaid=%d, longLines=%d, execTime=%dms}",
                totalEmployees, totalManagers, underpaidManagerCount, overpaidManagerCount,
                longReportingLineCount, executionTime.toMillis());
    }

    public static final class Builder {
        private int totalEmployees;
        private int totalManagers;
        private int underpaidManagerCount;
        private int overpaidManagerCount;
        private int longReportingLineCount;
        private int maxReportingLineDepth;
        private Duration executionTime = Duration.ZERO;
        private Instant analysisTimestamp = Instant.now();

        private Builder() {}

        public Builder totalEmployees(int count) {
            this.totalEmployees = count;
            return this;
        }

        public Builder totalManagers(int count) {
            this.totalManagers = count;
            return this;
        }

        public Builder underpaidManagerCount(int count) {
            this.underpaidManagerCount = count;
            return this;
        }

        public Builder overpaidManagerCount(int count) {
            this.overpaidManagerCount = count;
            return this;
        }

        public Builder longReportingLineCount(int count) {
            this.longReportingLineCount = count;
            return this;
        }

        public Builder maxReportingLineDepth(int depth) {
            this.maxReportingLineDepth = depth;
            return this;
        }

        public Builder executionTime(Duration duration) {
            this.executionTime = duration;
            return this;
        }

        public Builder analysisTimestamp(Instant timestamp) {
            this.analysisTimestamp = timestamp;
            return this;
        }

        public AnalysisStatistics build() {
            return new AnalysisStatistics(this);
        }
    }
}
