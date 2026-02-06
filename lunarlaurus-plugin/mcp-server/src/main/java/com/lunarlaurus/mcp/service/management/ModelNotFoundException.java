package com.lunarlaurus.mcp.service.management;

/**
 * Model Not Found Exception
 *
 * Thrown when attempting to access a model that doesn't exist in the registry.
 */
public class ModelNotFoundException extends RuntimeException {

    public ModelNotFoundException(String message) {
        super(message);
    }
}
