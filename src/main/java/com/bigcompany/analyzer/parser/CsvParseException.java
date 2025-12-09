package com.bigcompany.analyzer.parser;

public class CsvParseException extends RuntimeException {
    
    public CsvParseException(String message) {
        super(message);
    }

    public CsvParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvParseException(int lineNumber, String message) {
        super(String.format("Error at line %d: %s", lineNumber, message));
    }

    public CsvParseException(int lineNumber, String message, Throwable cause) {
        super(String.format("Error at line %d: %s", lineNumber, message), cause);
    }
}
