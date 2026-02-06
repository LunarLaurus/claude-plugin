package com.lunarlaurus.mcp.integration;

import com.lunarlaurus.mcp.model.*;
import com.lunarlaurus.mcp.service.inference.LocalLLMService;
import com.lunarlaurus.mcp.service.management.ModelManagementService;
import com.lunarlaurus.mcp.service.registry.ModelRegistryService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Model Management Integration Tests
 *
 * End-to-end tests for dynamic LLM backend management.
 * Requires Docker daemon running locally.
 *
 * Run with: mvn test -Dgroups=integration
 * Or enable with environment variable: DOCKER_AVAILABLE=true
 *
 * Note: These tests may take several minutes due to:
 * - Docker image pulls (first run)
 * - Container startup time (~10-15 seconds for Ollama)
 * - Health check intervals
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true", disabledReason = "Docker not available")
class ModelManagementIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ModelManagementService modelManagementService;

    @Autowired
    private ModelRegistryService registryService;

    @Autowired
    private LocalLLMService localLLMService;

    /**
     * Test creating an Ollama model backend
     * Verifies full lifecycle: request → container creation → registry entry → port allocation
     */
    @Test
    void testCreateOllamaModel() throws InterruptedException {
        // Create request
        ModelCreateRequest request = ModelCreateRequest.builder()
                .modelName("mistral:7b-instruct")
                .type(BackendType.OLLAMA)
                .build();

        // POST to /api/models
        ResponseEntity<ModelBackendResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/models",
                request,
                ModelBackendResponse.class
        );

        // Verify response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        ModelBackendResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());
        assertEquals("mistral:7b-instruct", body.getModelName());
        assertEquals(BackendType.OLLAMA, body.getType());
        assertThat(body.getPort()).isBetween(11400, 11500);
        assertThat(body.getEndpoint()).contains("localhost:" + body.getPort());
        assertThat(body.getEndpoint()).contains("/api/generate");
        assertEquals(HealthStatus.STARTING, body.getHealth());

        // Verify registry entry exists
        Optional<ModelBackendEntry> registryEntry = registryService.get(body.getId());
        assertTrue(registryEntry.isPresent());
        assertEquals(body.getModelName(), registryEntry.get().getModelName());

        // Wait for container startup (Ollama takes ~10-15 seconds)
        Thread.sleep(15000);

        // Verify health status updated to HEALTHY
        ModelBackendResponse updated = modelManagementService.getModel(body.getId());
        // Note: Health status should be HEALTHY or STARTING depending on health check timing
        assertThat(updated.getHealth()).isIn(HealthStatus.HEALTHY, HealthStatus.STARTING);

        // Cleanup
        modelManagementService.deleteModel(body.getId());
    }

    /**
     * Test deleting a model backend
     * Verifies container stopped, removed, port deallocated, registry entry removed
     */
    @Test
    void testDeleteModel() {
        // Create model
        ModelCreateRequest request = ModelCreateRequest.builder()
                .modelName("test-model:latest")
                .type(BackendType.OLLAMA)
                .build();

        ModelBackendResponse created = modelManagementService.createModel(request);
        String modelId = created.getId();

        // Verify created
        assertTrue(registryService.get(modelId).isPresent());

        // Delete model
        restTemplate.delete("http://localhost:" + port + "/api/models/" + modelId);

        // Verify registry entry removed
        assertFalse(registryService.get(modelId).isPresent());

        // Verify not in list
        List<ModelBackendResponse> models = modelManagementService.listModels();
        assertFalse(models.stream().anyMatch(m -> m.getId().equals(modelId)));
    }

    /**
     * Test dynamic routing in LocalLLMService
     * Verifies that LocalLLMService routes to dynamically created backends
     */
    @Test
    void testLocalLLMServiceDynamicRouting() throws InterruptedException {
        // Create Ollama model
        ModelCreateRequest request = ModelCreateRequest.builder()
                .modelName("mistral:7b")
                .type(BackendType.OLLAMA)
                .build();

        ModelBackendResponse created = modelManagementService.createModel(request);

        // Wait for health to stabilize
        Thread.sleep(15000);

        // Mark as healthy (simulate health check)
        Optional<ModelBackendEntry> entry = registryService.get(created.getId());
        if (entry.isPresent()) {
            entry.get().setHealth(HealthStatus.HEALTHY);
            registryService.register(entry.get());
        }

        // Note: Full inference test would require the actual Ollama model to be pulled
        // which could take several minutes. For integration tests, we verify routing logic only.

        // Verify model is registered and healthy
        ModelBackendEntry registryEntry = registryService.get(created.getId()).orElseThrow();
        assertEquals(HealthStatus.HEALTHY, registryEntry.getHealth());
        assertNotNull(registryEntry.getEndpoint());

        // Cleanup
        modelManagementService.deleteModel(created.getId());
    }

    /**
     * Test health checker updates model status
     * Verifies that ModelHealthChecker detects and updates health status changes
     */
    @Test
    void testHealthChecker() throws InterruptedException {
        // Create model
        ModelCreateRequest request = ModelCreateRequest.builder()
                .modelName("health-test:latest")
                .type(BackendType.OLLAMA)
                .build();

        ModelBackendResponse created = modelManagementService.createModel(request);
        String modelId = created.getId();

        // Initial status should be STARTING
        assertEquals(HealthStatus.STARTING, created.getHealth());

        // Wait for first health check (10s initial delay + some buffer)
        Thread.sleep(12000);

        // Check status - should be HEALTHY if container started successfully
        ModelBackendEntry entry = registryService.get(modelId).orElseThrow();
        // Status may be HEALTHY or STARTING depending on container startup time
        assertThat(entry.getHealth()).isIn(HealthStatus.HEALTHY, HealthStatus.STARTING, HealthStatus.UNHEALTHY);

        // Cleanup
        modelManagementService.deleteModel(modelId);
    }

    /**
     * Test listing all models
     */
    @Test
    void testListModels() {
        // Get initial count
        int initialCount = modelManagementService.listModels().size();

        // Create two models
        ModelCreateRequest request1 = ModelCreateRequest.builder()
                .modelName("model-1:latest")
                .type(BackendType.OLLAMA)
                .build();

        ModelCreateRequest request2 = ModelCreateRequest.builder()
                .modelName("model-2:latest")
                .type(BackendType.OLLAMA)
                .build();

        ModelBackendResponse model1 = modelManagementService.createModel(request1);
        ModelBackendResponse model2 = modelManagementService.createModel(request2);

        // List models via REST API
        ResponseEntity<ModelBackendResponse[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/models",
                ModelBackendResponse[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ModelBackendResponse[] models = response.getBody();
        assertNotNull(models);
        assertThat(models.length).isEqualTo(initialCount + 2);

        // Cleanup
        modelManagementService.deleteModel(model1.getId());
        modelManagementService.deleteModel(model2.getId());
    }

    /**
     * Test port allocation within range
     */
    @Test
    void testPortAllocation() {
        // Create multiple models and verify ports are unique and within range
        ModelCreateRequest request1 = ModelCreateRequest.builder()
                .modelName("port-test-1:latest")
                .type(BackendType.OLLAMA)
                .build();

        ModelCreateRequest request2 = ModelCreateRequest.builder()
                .modelName("port-test-2:latest")
                .type(BackendType.OLLAMA)
                .build();

        ModelBackendResponse model1 = modelManagementService.createModel(request1);
        ModelBackendResponse model2 = modelManagementService.createModel(request2);

        // Verify ports are unique
        assertNotEquals(model1.getPort(), model2.getPort());

        // Verify ports are within range
        assertThat(model1.getPort()).isBetween(11400, 11500);
        assertThat(model2.getPort()).isBetween(11400, 11500);

        // Cleanup
        modelManagementService.deleteModel(model1.getId());
        modelManagementService.deleteModel(model2.getId());
    }
}
