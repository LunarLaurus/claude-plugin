package com.lunarlaurus.mcp.service.management;

/**
 * Model Deletion Exception
 *
 * Thrown when model backend deletion fails.
 */
public class ModelDeletionException extends RuntimeException {

    public ModelDeletionException(String message) {
        super(message);
    }

    public ModelDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
