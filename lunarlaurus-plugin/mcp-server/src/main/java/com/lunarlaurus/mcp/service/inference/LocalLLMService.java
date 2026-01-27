package com.lunarlaurus.mcp.service.inference;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Local LLM Service
 * 
 * Handles text generation requests to local LLM backends:
 * - GPU backend: Ollama/vLLM on RTX4000 for fast inference
 * - CPU backend: llama.cpp on Intel 8260 for medium tasks
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

    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(String prompt, Integer maxTokens, Double temperature, String backend) {
        if ("gpu".equals(backend)) {
            return generateGPU(prompt, maxTokens, temperature);
        } else {
            return generateCPU(prompt, maxTokens, temperature);
        }
    }

    /**
     * Generate using GPU backend (Ollama)
     */
    private String generateGPU(String prompt, Integer maxTokens, Double temperature) {
        try {
            log.info("Generating text on GPU with model: {}", gpuModel);

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
                gpuEndpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("response");
            }

            throw new RuntimeException("GPU inference failed");
        } catch (Exception e) {
            log.error("GPU inference error: {}", e.getMessage());
            throw new RuntimeException("GPU inference failed: " + e.getMessage());
        }
    }

    /**
     * Generate using CPU backend (llama.cpp server)
     */
    private String generateCPU(String prompt, Integer maxTokens, Double temperature) {
        try {
            log.info("Generating text on CPU with model: {}", cpuModel);

            Map<String, Object> request = new HashMap<>();
            request.put("prompt", prompt);
            request.put("n_predict", maxTokens);
            request.put("temperature", temperature);
            request.put("stop", List.of("</s>", "[/INST]", "###"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                cpuEndpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("content");
            }

            throw new RuntimeException("CPU inference failed");
        } catch (Exception e) {
            log.error("CPU inference error: {}", e.getMessage());
            throw new RuntimeException("CPU inference failed: " + e.getMessage());
        }
    }
}
