package com.bigcompany.analyzer.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of data validation containing errors and warnings.
 * 
 * <p>This class is immutable once created via the Builder.
 */
public final class ValidationResult {

    private final List<String> errors;
    private final List<String> warnings;

    private ValidationResult(List<String> errors, List<String> warnings) {
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
    }

    /**
     * Returns true if validation passed (no errors).
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Returns true if there are any warnings.
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public int getWarningCount() {
        return warnings.size();
    }

    /**
     * Creates a successful validation result with no errors or warnings.
     */
    public static ValidationResult success() {
        return new ValidationResult(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Creates a new builder for constructing validation results.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return String.format("ValidationResult{valid=%s, errors=%d, warnings=%d}",
                isValid(), errors.size(), warnings.size());
    }

    /**
     * Builder for constructing ValidationResult instances.
     */
    public static final class Builder {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        private Builder() {}

        public Builder addError(String error) {
            errors.add(error);
            return this;
        }

        public Builder addError(String format, Object... args) {
            errors.add(String.format(format, args));
            return this;
        }

        public Builder addWarning(String warning) {
            warnings.add(warning);
            return this;
        }

        public Builder addWarning(String format, Object... args) {
            warnings.add(String.format(format, args));
            return this;
        }

        public Builder merge(ValidationResult other) {
            errors.addAll(other.getErrors());
            warnings.addAll(other.getWarnings());
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(errors, warnings);
        }
    }
}
