package com.lunarlaurus.mcp.service.inference;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Embedding Service
 * 
 * Generates embeddings using local embedding models (e.g., all-MiniLM-L6-v2)
 * Supports both single and batch embedding generation
 */
@Slf4j
@Service
public class EmbeddingService {

    @Value("${embedding.endpoint:http://localhost:11434/api/embeddings}")
    private String embeddingEndpoint;

    @Value("${embedding.model:nomic-embed-text}")
    private String embeddingModel;

    private final RestTemplate restTemplate = new RestTemplate();

    public float[] generateEmbedding(String text) {
        try {
            log.info("Generating embedding for text (length: {})", text.length());

            Map<String, Object> request = new HashMap<>();
            request.put("model", embeddingModel);
            request.put("prompt", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                embeddingEndpoint, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Double> embedding = (List<Double>) response.getBody().get("embedding");
                return toFloatArray(embedding);
            }

            throw new RuntimeException("Embedding generation failed");
        } catch (Exception e) {
            log.error("Embedding generation error: {}", e.getMessage());
            throw new RuntimeException("Embedding generation failed: " + e.getMessage());
        }
    }

    public List<float[]> generateBatchEmbeddings(List<String> texts) {
        log.info("Generating batch embeddings for {} texts", texts.size());
        
        return texts.stream()
            .map(this::generateEmbedding)
            .collect(Collectors.toList());
    }

    private float[] toFloatArray(List<Double> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).floatValue();
        }
        return array;
    }
}
