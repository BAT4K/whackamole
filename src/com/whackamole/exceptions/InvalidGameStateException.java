package com.whackamole.exceptions;

/**
 * Module 5: Custom UNCHECKED exception.
 * This is thrown for "impossible" situations, indicating a programmer error.
 */
public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException(String message) {
        super(message);
    }
}