package com.lunarlaurus.mcp.tools;

import com.lunarlaurus.mcp.service.inference.CodeAnalysisService;
import com.lunarlaurus.mcp.service.inference.EmbeddingService;
import com.lunarlaurus.mcp.service.inference.LocalLLMService;
import com.lunarlaurus.mcp.service.inference.SummarizerService;
import com.lunarlaurus.mcp.service.inference.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * MCP Tool Provider
 *
 * Exposes local compute tools via MCP protocol using Spring AI annotations.
 * Each method delegates to the existing service layer — no logic duplication.
 *
 * Tools are auto-discovered by Spring AI's annotation scanner and registered
 * with the MCP stdio server for Claude Code to invoke.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolProvider {

    private final LocalLLMService localLLMService;
    private final SummarizerService summarizerService;
    private final EmbeddingService embeddingService;
    private final CodeAnalysisService codeAnalysisService;
    private final TranslationService translationService;

    private static final int GPU_MAX_TOKENS = 2000;

    @Tool(name = "local_llm", description = "Generate text using a local LLaMA/Mistral model. Supports Q&A, content generation, and analysis. "
            + "Auto-routes between dynamically created backends (via /api/models REST API) or static GPU (RTX4000, fast, <2k tokens) "
            + "and CPU (Intel 8260, slower, handles longer context) backends. Dynamic backends enable on-demand model deployment from HuggingFace. "
            + "Specify 'backend' param to override auto-routing.")
    public String localLlm(
            @ToolParam(description = "The prompt for text generation") String prompt,
            @ToolParam(description = "Maximum tokens to generate (default 512)", required = false) Integer maxTokens,
            @ToolParam(description = "Sampling temperature 0-1 (default 0.2)", required = false) Double temperature,
            @ToolParam(description = "Backend: 'auto' (smart routing to dynamic or static), model name (e.g., 'mistral:7b'), 'gpu' (RTX4000), or 'cpu' (Intel 8260). Default: auto", required = false) String backend) {

        int tokens = maxTokens != null ? maxTokens : 512;
        double temp = temperature != null ? temperature : 0.2;
        String model = backend != null ? backend : "auto";

        String resolvedBackend = determineBackend(prompt, model);
        log.info("local_llm: prompt length={}, maxTokens={}, backend={}", prompt.length(), tokens, resolvedBackend);

        return localLLMService.generate(prompt, tokens, temp, resolvedBackend);
    }

    @Tool(name = "summarize_text", description = "Summarize large text or file contents using a local LLM. "
            + "Good for condensing documents, articles, code comments, or any lengthy content.")
    public String summarizeText(
            @ToolParam(description = "The text to summarize") String text,
            @ToolParam(description = "Maximum summary length in words (default 200)", required = false) Integer maxLength) {

        int length = maxLength != null ? maxLength : 200;
        log.info("summarize_text: input length={}, maxLength={}", text.length(), length);

        return summarizerService.summarize(text, length);
    }

    @Tool(name = "generate_embedding", description = "Generate a semantic embedding vector for text using a local embedding model (nomic-embed-text). "
            + "Useful for semantic search, similarity comparison, and clustering.")
    public String generateEmbedding(
            @ToolParam(description = "The text to generate an embedding for") String text) {

        log.info("generate_embedding: text length={}", text.length());

        float[] embedding = embeddingService.generateEmbedding(text);
        return "Generated embedding with " + embedding.length + " dimensions: " + Arrays.toString(embedding);
    }

    @Tool(name = "analyze_code", description = "Perform a first-pass code review using a local LLM. "
            + "Identifies potential bugs, performance issues, security concerns, and best practice violations.")
    public String analyzeCode(
            @ToolParam(description = "The source code to analyze") String code,
            @ToolParam(description = "Programming language (e.g. 'java', 'python'). Default: auto-detect", required = false) String language) {

        String lang = language != null ? language : "auto";
        log.info("analyze_code: code length={}, language={}", code.length(), lang);

        return codeAnalysisService.analyze(code, lang);
    }

    @Tool(name = "translate_text", description = "Translate text between languages using a local multilingual LLM. "
            + "Supports major languages including English, Spanish, French, German, Chinese, Japanese, Korean, etc.")
    public String translateText(
            @ToolParam(description = "The text to translate") String text,
            @ToolParam(description = "Source language code (e.g. 'en', 'es'). Default: auto-detect", required = false) String sourceLanguage,
            @ToolParam(description = "Target language code (e.g. 'en', 'es', 'fr', 'de', 'zh', 'ja', 'ko')") String targetLanguage) {

        String sourceLang = sourceLanguage != null ? sourceLanguage : "auto";
        log.info("translate_text: text length={}, from={}, to={}", text.length(), sourceLang, targetLanguage);

        return translationService.translate(text, sourceLang, targetLanguage);
    }

    private String determineBackend(String prompt, String requestedModel) {
        if (!"auto".equals(requestedModel)) {
            return requestedModel;
        }
        int estimatedTokens = prompt.length() / 4;
        return estimatedTokens < GPU_MAX_TOKENS ? "gpu" : "cpu";
    }
}
