package com.lunarlaurus.mcp.service;

import com.lunarlaurus.mcp.model.*;
import com.lunarlaurus.mcp.service.inference.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MCP Service - Core logic for tool execution and routing
 * 
 * Implements intelligent routing:
 * - Short prompts (<2k tokens) → GPU (RTX4000)
 * - Medium prompts (2k-8k tokens) → CPU
 * - Long/complex prompts → Cloud fallback (if configured)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MCPService {

    private final LocalLLMService localLLMService;
    private final SummarizerService summarizerService;
    private final EmbeddingService embeddingService;
    private final CodeAnalysisService codeAnalysisService;
    private final TranslationService translationService;

    // Token thresholds for routing
    private static final int GPU_MAX_TOKENS = 2000;
    private static final int CPU_MAX_TOKENS = 8000;

    public ListToolsResponse listTools() {
        List<Tool> tools = new ArrayList<>();

        // Local LLM tool
        tools.add(Tool.builder()
            .name("local_llm")
            .description("Generate text using local LLaMA/Mistral model. Supports various tasks like Q&A, content generation, and analysis.")
            .inputSchema(createLocalLLMSchema())
            .build());

        // Summarizer tool
        tools.add(Tool.builder()
            .name("summarizer")
            .description("Summarize long text locally. Efficient for documents, articles, and large content.")
            .inputSchema(createSummarizerSchema())
            .build());

        // Embedding tool
        tools.add(Tool.builder()
            .name("embedding")
            .description("Generate embeddings for text using local embedding model. Useful for semantic search and similarity.")
            .inputSchema(createEmbeddingSchema())
            .build());

        // Code analysis tool
        tools.add(Tool.builder()
            .name("code_analysis")
            .description("Analyze code for patterns, potential issues, and suggestions. Supports multiple programming languages.")
            .inputSchema(createCodeAnalysisSchema())
            .build());

        // Translation tool
        tools.add(Tool.builder()
            .name("translation")
            .description("Translate text between languages using local model. Supports major languages.")
            .inputSchema(createTranslationSchema())
            .build());

        return new ListToolsResponse(tools);
    }

    public CallToolResponse callTool(CallToolRequest request) {
        String toolName = request.getName();
        Map<String, Object> arguments = request.getArguments();

        try {
            switch (toolName) {
                case "local_llm":
                    return handleLocalLLM(arguments);
                case "summarizer":
                    return handleSummarizer(arguments);
                case "embedding":
                    return handleEmbedding(arguments);
                case "code_analysis":
                    return handleCodeAnalysis(arguments);
                case "translation":
                    return handleTranslation(arguments);
                default:
                    throw new IllegalArgumentException("Unknown tool: " + toolName);
            }
        } catch (Exception e) {
            log.error("Error executing tool: {}", toolName, e);
            return CallToolResponse.error("Error executing " + toolName + ": " + e.getMessage());
        }
    }

    private CallToolResponse handleLocalLLM(Map<String, Object> arguments) {
        String prompt = (String) arguments.get("prompt");
        Integer maxTokens = (Integer) arguments.getOrDefault("max_tokens", 512);
        Double temperature = ((Number) arguments.getOrDefault("temperature", 0.2)).doubleValue();
        String model = (String) arguments.getOrDefault("model", "auto");

        // Estimate token count (rough estimate: 1 token ≈ 4 characters)
        int estimatedTokens = prompt.length() / 4;

        log.info("Local LLM request - Estimated tokens: {}, Max tokens: {}", estimatedTokens, maxTokens);

        String result = localLLMService.generate(prompt, maxTokens, temperature, 
            determineBackend(estimatedTokens, model));

        return CallToolResponse.success(result);
    }

    private CallToolResponse handleSummarizer(Map<String, Object> arguments) {
        String text = (String) arguments.get("text");
        Integer maxLength = (Integer) arguments.getOrDefault("max_length", 200);

        log.info("Summarizer request - Text length: {}", text.length());

        String summary = summarizerService.summarize(text, maxLength);
        return CallToolResponse.success(summary);
    }

    private CallToolResponse handleEmbedding(Map<String, Object> arguments) {
        Object textObj = arguments.get("text");
        
        if (textObj instanceof String) {
            String text = (String) textObj;
            float[] embedding = embeddingService.generateEmbedding(text);
            return CallToolResponse.success("Generated embedding with " + embedding.length + " dimensions");
        } else if (textObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> texts = (List<String>) textObj;
            List<float[]> embeddings = embeddingService.generateBatchEmbeddings(texts);
            return CallToolResponse.success("Generated " + embeddings.size() + " embeddings");
        }
        
        throw new IllegalArgumentException("Invalid text argument for embedding");
    }

    private CallToolResponse handleCodeAnalysis(Map<String, Object> arguments) {
        String code = (String) arguments.get("code");
        String language = (String) arguments.getOrDefault("language", "auto");

        log.info("Code analysis request - Language: {}", language);

        String analysis = codeAnalysisService.analyze(code, language);
        return CallToolResponse.success(analysis);
    }

    private CallToolResponse handleTranslation(Map<String, Object> arguments) {
        String text = (String) arguments.get("text");
        String sourceLang = (String) arguments.getOrDefault("source_language", "auto");
        String targetLang = (String) arguments.get("target_language");

        log.info("Translation request - From {} to {}", sourceLang, targetLang);

        String translation = translationService.translate(text, sourceLang, targetLang);
        return CallToolResponse.success(translation);
    }

    private String determineBackend(int estimatedTokens, String requestedModel) {
        if (!"auto".equals(requestedModel)) {
            return requestedModel;
        }

        if (estimatedTokens < GPU_MAX_TOKENS) {
            log.info("Routing to GPU backend (tokens: {})", estimatedTokens);
            return "gpu";
        } else if (estimatedTokens < CPU_MAX_TOKENS) {
            log.info("Routing to CPU backend (tokens: {})", estimatedTokens);
            return "cpu";
        } else {
            log.info("Request exceeds local capacity, consider cloud fallback (tokens: {})", estimatedTokens);
            return "cpu"; // Still use CPU but with warning
        }
    }

    // Schema creation methods
    private Map<String, Object> createLocalLLMSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("prompt", Map.of("type", "string", "description", "The prompt for text generation"));
        properties.put("max_tokens", Map.of("type", "number", "default", 512, "description", "Maximum tokens to generate"));
        properties.put("temperature", Map.of("type", "number", "default", 0.2, "description", "Sampling temperature (0-1)"));
        properties.put("model", Map.of("type", "string", "default", "auto", "enum", List.of("auto", "gpu", "cpu"), 
            "description", "Backend selection: auto (smart routing), gpu (RTX4000), cpu (Intel 8260)"));
        
        schema.put("properties", properties);
        schema.put("required", List.of("prompt"));
        return schema;
    }

    private Map<String, Object> createSummarizerSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("text", Map.of("type", "string", "description", "Text to summarize"));
        properties.put("max_length", Map.of("type", "number", "default", 200, "description", "Maximum summary length in words"));
        
        schema.put("properties", properties);
        schema.put("required", List.of("text"));
        return schema;
    }

    private Map<String, Object> createEmbeddingSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> textProperty = new HashMap<>();
        textProperty.put("description", "Text or array of texts to embed");
        textProperty.put("oneOf", List.of(
            Map.of("type", "string"),
            Map.of("type", "array", "items", Map.of("type", "string"))
        ));
        properties.put("text", textProperty);
        
        schema.put("properties", properties);
        schema.put("required", List.of("text"));
        return schema;
    }

    private Map<String, Object> createCodeAnalysisSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("code", Map.of("type", "string", "description", "Code to analyze"));
        properties.put("language", Map.of("type", "string", "default", "auto", 
            "description", "Programming language (auto-detect if not specified)"));
        
        schema.put("properties", properties);
        schema.put("required", List.of("code"));
        return schema;
    }

    private Map<String, Object> createTranslationSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("text", Map.of("type", "string", "description", "Text to translate"));
        properties.put("source_language", Map.of("type", "string", "default", "auto", 
            "description", "Source language code (auto-detect if not specified)"));
        properties.put("target_language", Map.of("type", "string", "description", "Target language code"));
        
        schema.put("properties", properties);
        schema.put("required", List.of("text", "target_language"));
        return schema;
    }
}
