package org.example.exception;

public class FibaProcessorIllegalStateException extends IllegalStateException {
    // Constructor that accepts a message
    public FibaProcessorIllegalStateException(String message) {
        super(message); // Call the superclass constructor
    }

    // Constructor that accepts a message and a cause
    public FibaProcessorIllegalStateException(String message, Throwable cause) {
        super(message, cause); // Call the superclass constructor
    }
}
