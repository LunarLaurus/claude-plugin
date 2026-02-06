package com.lunarlaurus.mcp.service.registry;

/**
 * Port Exhaustion Exception
 *
 * Thrown when the configured port range is fully allocated and no ports are available.
 */
public class PortExhaustionException extends RuntimeException {

    public PortExhaustionException(String message) {
        super(message);
    }
}
