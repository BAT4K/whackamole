package com.whackamole.exceptions;

/**
 * Module 5: Custom CHECKED exception.
 * This is thrown when there's a recoverable error during file I/O for scores.
 */
public class HighScoreException extends Exception {

    /**
     * Constructor to wrap a lower-level exception (e.g., IOException).
     * @param message A user-friendly error message.
     * @param cause The original exception.
     */
    public HighScoreException(String message, Throwable cause) {
        super(message, cause);
    }
}