package org.example.exception;

public class StateMachineIllegalStateException extends IllegalStateException {
    // Constructor that accepts a message
    public StateMachineIllegalStateException(String message) {
        super(message); // Call the superclass constructor
    }

    // Constructor that accepts a message and a cause
    public StateMachineIllegalStateException(String message, Throwable cause) {
        super(message, cause); // Call the superclass constructor
    }
}
