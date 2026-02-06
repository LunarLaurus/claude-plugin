package com.lunarlaurus.mcp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Model Create Request
 *
 * Request DTO for creating a new dynamic LLM backend.
 * Follows lombok-dto-annotations standard with full stack + validation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCreateRequest {

    /**
     * Model name (e.g., "mistral:7b-instruct")
     */
    @NotBlank(message = "Model name is required")
    private String modelName;

    /**
     * Backend type (OLLAMA, LLAMA_CPP, or CUSTOM)
     */
    @NotNull(message = "Backend type is required")
    private BackendType type;

    /**
     * Custom Docker image name (required if type=CUSTOM)
     */
    private String customImage;

    /**
     * Optional environment variables for the container
     */
    private Map<String, String> env;
}
