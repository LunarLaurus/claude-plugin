package com.lunarlaurus.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model Backend Entry
 *
 * Registry entry for a dynamically created LLM backend container.
 * Tracks container metadata, health status, and endpoint information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelBackendEntry {
    /**
     * Unique identifier (UUID)
     */
    private String id;

    /**
     * Model name (e.g., "mistral:7b-instruct")
     */
    private String modelName;

    /**
     * Backend container type
     */
    private BackendType type;

    /**
     * Docker container ID (64-char hex string)
     */
    private String containerId;

    /**
     * Full HTTP endpoint URL (e.g., "http://localhost:11401/api/generate")
     */
    private String endpoint;

    /**
     * Allocated host port
     */
    private int port;

    /**
     * Timestamp when model was created
     */
    private LocalDateTime createdAt;

    /**
     * Current health status
     */
    private HealthStatus health;
}
