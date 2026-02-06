package com.lunarlaurus.mcp.service.management;

import com.lunarlaurus.mcp.model.BackendType;
import com.lunarlaurus.mcp.model.HealthStatus;
import com.lunarlaurus.mcp.model.ModelBackendEntry;
import com.lunarlaurus.mcp.model.ModelBackendResponse;
import com.lunarlaurus.mcp.model.ModelCreateRequest;
import com.lunarlaurus.mcp.service.docker.DockerOrchestrationException;
import com.lunarlaurus.mcp.service.docker.DockerOrchestrationService;
import com.lunarlaurus.mcp.service.registry.ModelRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Model Management Service
 *
 * Orchestration layer that combines Docker operations, registry management, and health monitoring.
 * Handles the full lifecycle of dynamically created LLM backend containers.
 *
 * Key responsibilities:
 * - Validate model creation requests
 * - Allocate ports and determine Docker images
 * - Orchestrate container creation and startup
 * - Register/unregister models in registry
 * - Monitor model health status
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelManagementService {

    private final DockerOrchestrationService dockerService;
    private final ModelRegistryService registryService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Create a new model backend
     *
     * @param request Model creation request
     * @return Model backend response with endpoint and status
     * @throws ModelCreationException if creation fails at any step
     */
    public ModelBackendResponse createModel(ModelCreateRequest request) {
        log.info("Creating model backend: model={}, type={}", request.getModelName(), request.getType());

        String modelId = UUID.randomUUID().toString();
        int allocatedPort = 0;
        String containerId = null;

        try {
            // Step 1: Allocate port
            allocatedPort = registryService.allocatePort();
            log.debug("Allocated port {} for model {}", allocatedPort, request.getModelName());

            // Step 2: Determine Docker image and internal port based on backend type
            String dockerImage = determineDockerImage(request);
            int containerPort = determineContainerPort(request.getType());
            log.info("Using Docker image: {}, container port: {}", dockerImage, containerPort);

            // Step 3: Pull image if missing
            dockerService.pullImageIfMissing(dockerImage);

            // Step 4: Create container with port mapping
            Map<Integer, Integer> portMappings = new HashMap<>();
            portMappings.put(allocatedPort, containerPort);

            String containerName = generateContainerName(request.getModelName(), modelId);
            containerId = dockerService.createContainer(
                    dockerImage,
                    containerName,
                    portMappings,
                    request.getEnv()
            );

            // Step 5: Start container
            dockerService.startContainer(containerId);

            // Step 6: Build endpoint URL
            String endpoint = buildEndpointUrl(allocatedPort, request.getType());

            // Step 7: Create registry entry
            ModelBackendEntry entry = ModelBackendEntry.builder()
                    .id(modelId)
                    .modelName(request.getModelName())
                    .type(request.getType())
                    .containerId(containerId)
                    .endpoint(endpoint)
                    .port(allocatedPort)
                    .createdAt(LocalDateTime.now())
                    .health(HealthStatus.STARTING)
                    .build();

            // Step 8: Register in registry
            registryService.register(entry);

            log.info("Model backend created successfully: id={}, endpoint={}", modelId, endpoint);

            // Return response
            return ModelBackendResponse.builder()
                    .id(modelId)
                    .modelName(request.getModelName())
                    .type(request.getType())
                    .endpoint(endpoint)
                    .port(allocatedPort)
                    .createdAt(entry.getCreatedAt().format(ISO_FORMATTER))
                    .health(HealthStatus.STARTING)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create model backend: {}", e.getMessage());

            // Cleanup on failure
            if (containerId != null) {
                try {
                    dockerService.stopContainer(containerId);
                    dockerService.removeContainer(containerId);
                } catch (Exception cleanupError) {
                    log.warn("Failed to cleanup container after error: {}", cleanupError.getMessage());
                }
            }

            if (allocatedPort > 0) {
                registryService.deallocatePort(allocatedPort);
            }

            throw new ModelCreationException(
                    "Failed to create model " + request.getModelName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Delete a model backend
     *
     * @param id Model ID to delete
     * @throws ModelNotFoundException if model not found
     */
    public void deleteModel(String id) {
        log.info("Deleting model backend: id={}", id);

        ModelBackendEntry entry = registryService.get(id)
                .orElseThrow(() -> new ModelNotFoundException("Model not found: " + id));

        try {
            // Stop and remove container
            dockerService.stopContainer(entry.getContainerId());
            dockerService.removeContainer(entry.getContainerId());

            // Deallocate port
            registryService.deallocatePort(entry.getPort());

            // Unregister from registry
            registryService.unregister(id);

            log.info("Model backend deleted successfully: id={}, model={}", id, entry.getModelName());

        } catch (DockerOrchestrationException e) {
            log.error("Failed to delete model backend {}: {}", id, e.getMessage());
            throw new ModelDeletionException("Failed to delete model " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * List all registered models
     *
     * @return List of model backend responses
     */
    public List<ModelBackendResponse> listModels() {
        return registryService.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get model by ID
     *
     * @param id Model ID
     * @return Model backend response
     * @throws ModelNotFoundException if not found
     */
    public ModelBackendResponse getModel(String id) {
        ModelBackendEntry entry = registryService.get(id)
                .orElseThrow(() -> new ModelNotFoundException("Model not found: " + id));
        return toResponse(entry);
    }

    /**
     * Get and update model health status
     *
     * @param id Model ID
     * @return Current health status
     */
    public HealthStatus getModelHealth(String id) {
        ModelBackendEntry entry = registryService.get(id)
                .orElseThrow(() -> new ModelNotFoundException("Model not found: " + id));

        // Check container health
        boolean healthy = dockerService.isContainerHealthy(entry.getContainerId());
        HealthStatus newStatus = healthy ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY;

        // Update registry if status changed
        if (entry.getHealth() != newStatus) {
            entry.setHealth(newStatus);
            registryService.register(entry);
            log.info("Model {} health updated: {}", id, newStatus);
        }

        return newStatus;
    }

    /**
     * Determine Docker image based on backend type and request
     */
    private String determineDockerImage(ModelCreateRequest request) {
        switch (request.getType()) {
            case OLLAMA:
                return "ollama/ollama:latest";
            case LLAMA_CPP:
                return "ghcr.io/ggerganov/llama.cpp:server";
            case CUSTOM:
                if (request.getCustomImage() == null || request.getCustomImage().isBlank()) {
                    throw new ModelCreationException("Custom image name is required for CUSTOM backend type");
                }
                return request.getCustomImage();
            default:
                throw new ModelCreationException("Unknown backend type: " + request.getType());
        }
    }

    /**
     * Determine container internal port based on backend type
     */
    private int determineContainerPort(BackendType type) {
        switch (type) {
            case OLLAMA:
                return 11434;
            case LLAMA_CPP:
                return 8080;
            case CUSTOM:
                return 8080; // Default for custom backends
            default:
                throw new ModelCreationException("Unknown backend type: " + type);
        }
    }

    /**
     * Build endpoint URL based on port and backend type
     */
    private String buildEndpointUrl(int port, BackendType type) {
        String baseUrl = "http://localhost:" + port;

        switch (type) {
            case OLLAMA:
                return baseUrl + "/api/generate";
            case LLAMA_CPP:
                return baseUrl + "/completion";
            case CUSTOM:
                return baseUrl; // Custom backends define their own paths
            default:
                return baseUrl;
        }
    }

    /**
     * Generate unique container name
     */
    private String generateContainerName(String modelName, String modelId) {
        String sanitized = modelName.replaceAll("[^a-zA-Z0-9_.-]", "-");
        String shortId = modelId.substring(0, 8);
        return "llm-" + sanitized + "-" + shortId;
    }

    /**
     * Convert registry entry to response DTO
     */
    private ModelBackendResponse toResponse(ModelBackendEntry entry) {
        return ModelBackendResponse.builder()
                .id(entry.getId())
                .modelName(entry.getModelName())
                .type(entry.getType())
                .endpoint(entry.getEndpoint())
                .port(entry.getPort())
                .createdAt(entry.getCreatedAt().format(ISO_FORMATTER))
                .health(entry.getHealth())
                .build();
    }
}
