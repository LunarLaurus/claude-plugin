package com.lunarlaurus.mcp.service.management;

/**
 * Model Creation Exception
 *
 * Thrown when model backend creation fails during any step of the orchestration process.
 */
public class ModelCreationException extends RuntimeException {

    public ModelCreationException(String message) {
        super(message);
    }

    public ModelCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
