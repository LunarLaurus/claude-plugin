package com.lunarlaurus.mcp.model;

/**
 * Health Status
 *
 * Container health states for dynamic model backends.
 */
public enum HealthStatus {
    /**
     * Container is starting up, not yet ready for requests
     */
    STARTING,

    /**
     * Container is running and healthy
     */
    HEALTHY,

    /**
     * Container is running but unhealthy or unresponsive
     */
    UNHEALTHY,

    /**
     * Container has been stopped
     */
    STOPPED
}
