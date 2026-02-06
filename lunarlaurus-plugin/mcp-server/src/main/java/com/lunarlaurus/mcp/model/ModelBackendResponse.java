package com.lunarlaurus.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model Backend Response
 *
 * Response DTO for model backend operations.
 * Follows lombok-dto-annotations standard with full stack.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelBackendResponse {

    /**
     * Unique model identifier (UUID)
     */
    private String id;

    /**
     * Model name
     */
    private String modelName;

    /**
     * Backend type
     */
    private BackendType type;

    /**
     * Full HTTP endpoint URL
     */
    private String endpoint;

    /**
     * Allocated host port
     */
    private int port;

    /**
     * Creation timestamp (ISO-8601 formatted)
     */
    private String createdAt;

    /**
     * Current health status
     */
    private HealthStatus health;
}
