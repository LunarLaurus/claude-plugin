package com.lunarlaurus.mcp.service.inference;

import com.lunarlaurus.mcp.model.HealthStatus;
import com.lunarlaurus.mcp.model.ModelBackendEntry;
import com.lunarlaurus.mcp.service.registry.ModelRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Local LLM Service
 *
 * Handles text generation requests to local LLM backends:
 * - Dynamic backends: Models created via /api/models (registry-based routing)
 * - GPU backend: Ollama/vLLM on RTX4000 for fast inference (static fallback)
 * - CPU backend: llama.cpp on Intel 8260 for medium tasks (static fallback)
 *
 * Backend resolution order:
 * 1. Query registry for dynamic models (by model name or backend type)
 * 2. Fall back to static configuration if no healthy dynamic backend found
 */
@Slf4j
@Service
public class LocalLLMService {

    @Value("${llm.gpu.endpoint:http://localhost:11434/api/generate}")
    private String gpuEndpoint;

    @Value("${llm.cpu.endpoint:http://localhost:8080/completion}")
    private String cpuEndpoint;

    @Value("${llm.gpu.model:mistral:7b}")
    private String gpuModel;

    @Value("${llm.cpu.model:mistral-7b-instruct}")
    private String cpuModel;

    @Autowired(required = false)
    private ModelRegistryService modelRegistry;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(String prompt, Integer maxTokens, Double temperature, String backend) {
        String endpoint = resolveEndpoint(backend);
        return callLLMEndpoint(endpoint, prompt, maxTokens, temperature);
    }

    /**
     * Resolve endpoint for the given backend
     * Queries registry first, falls back to static config
     *
     * @param backend Backend identifier (can be "gpu", "cpu", "auto", or model name)
     * @return Resolved endpoint URL
     */
    private String resolveEndpoint(String backend) {
        // Try registry first if available
        if (modelRegistry != null) {
            Optional<ModelBackendEntry> registryEntry = modelRegistry.findByModelName(backend);

            // If found by model name and healthy, use it
            if (registryEntry.isPresent() &&
                    registryEntry.get().getHealth() == HealthStatus.HEALTHY) {
                String endpoint = registryEntry.get().getEndpoint();
                log.debug("Using dynamic backend from registry: model={}, endpoint={}",
                        backend, endpoint);
                return endpoint;
            }

            // If backend is "auto" or "gpu" or "cpu", try to find any healthy backend
            if ("auto".equalsIgnoreCase(backend) || "gpu".equalsIgnoreCase(backend)) {
                Optional<ModelBackendEntry> anyHealthy = modelRegistry.listAll().stream()
                        .filter(entry -> entry.getHealth() == HealthStatus.HEALTHY)
                        .findFirst();

                if (anyHealthy.isPresent()) {
                    String endpoint = anyHealthy.get().getEndpoint();
                    log.debug("Using dynamic backend from registry (auto-selected): model={}, endpoint={}",
                            anyHealthy.get().getModelName(), endpoint);
                    return endpoint;
                }
            }
        }

        // Fall back to static config
        String endpoint = determineEndpointFromConfig(backend);
        log.debug("Using static config endpoint: backend={}, endpoint={}", backend, endpoint);
        return endpoint;
    }

    /**
     * Determine endpoint from static configuration
     * Legacy fallback for static backends
     */
    private String determineEndpointFromConfig(String backend) {
        if ("gpu".equalsIgnoreCase(backend)) {
            return gpuEndpoint;
        } else if ("cpu".equalsIgnoreCase(backend)) {
            return cpuEndpoint;
        }
        // Default to GPU for "auto" or unknown
        return gpuEndpoint;
    }

    /**
     * Call LLM endpoint with unified logic
     * Handles both Ollama and llama.cpp formats
     */
    private String callLLMEndpoint(String endpoint, String prompt, Integer maxTokens, Double temperature) {
        // Determine if this is an Ollama endpoint or llama.cpp endpoint
        if (endpoint.contains("/api/generate")) {
            return generateGPU(prompt, maxTokens, temperature, endpoint);
        } else {
            return generateCPU(prompt, maxTokens, temperature, endpoint);
        }
    }

    /**
     * Generate using Ollama-compatible backend
     */
    private String generateGPU(String prompt, Integer maxTokens, Double temperature, String endpoint) {
        try {
            log.info("Generating text on Ollama backend: endpoint={}", endpoint);

            Map<String, Object> request = new HashMap<>();
            request.put("model", gpuModel);
            request.put("prompt", prompt);
            request.put("stream", false);

            Map<String, Object> options = new HashMap<>();
            options.put("num_predict", maxTokens);
            options.put("temperature", temperature);
            request.put("options", options);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("response");
            }

            throw new RuntimeException("Ollama inference failed");
        } catch (Exception e) {
            log.error("Ollama inference error: {}", e.getMessage());
            throw new RuntimeException("Ollama inference failed: " + e.getMessage());
        }
    }

    /**
     * Generate using llama.cpp-compatible backend
     */
    private String generateCPU(String prompt, Integer maxTokens, Double temperature, String endpoint) {
        try {
            log.info("Generating text on llama.cpp backend: endpoint={}", endpoint);

            Map<String, Object> request = new HashMap<>();
            request.put("prompt", prompt);
            request.put("n_predict", maxTokens);
            request.put("temperature", temperature);
            request.put("stop", List.of("</s>", "[/INST]", "###"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    endpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("content");
            }

            throw new RuntimeException("llama.cpp inference failed");
        } catch (Exception e) {
            log.error("llama.cpp inference error: {}", e.getMessage());
            throw new RuntimeException("llama.cpp inference failed: " + e.getMessage());
        }
    }
}
