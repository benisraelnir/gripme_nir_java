package com.grip.exceptions;

/**
 * Exception thrown when attempting to start a service that is already running.
 * Java equivalent of Python's AlreadyRunningError.
 */
public class AlreadyRunningException extends RuntimeException {
    
    /**
     * Constructs a new AlreadyRunningException with no message.
     */
    public AlreadyRunningException() {
        super();
    }

    /**
     * Constructs a new AlreadyRunningException with the specified message.
     *
     * @param message the detail message
     */
    public AlreadyRunningException(String message) {
        super(message);
    }

    /**
     * Constructs a new AlreadyRunningException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public AlreadyRunningException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AlreadyRunningException with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public AlreadyRunningException(Throwable cause) {
        super(cause);
    }
}