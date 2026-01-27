package com.lunarlaurus.mcp.service.inference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Code Analysis Service
 * 
 * Analyzes code for patterns, potential issues, and suggestions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeAnalysisService {

    private final LocalLLMService localLLMService;

    public String analyze(String code, String language) {
        log.info("Analyzing code (language: {})", language);

        String prompt = String.format(
            "[INST] Analyze the following %s code. Identify:\n" +
            "1. Potential bugs or issues\n" +
            "2. Performance considerations\n" +
            "3. Code quality and best practices\n" +
            "4. Security concerns (if any)\n\n" +
            "Code:\n```%s\n%s\n```\n\n" +
            "Provide a concise analysis: [/INST]",
            language.equals("auto") ? "" : language,
            language.equals("auto") ? "" : language,
            code
        );

        // Use GPU for faster analysis of shorter code snippets
        int estimatedTokens = code.length() / 4;
        String backend = estimatedTokens < 2000 ? "gpu" : "cpu";

        return localLLMService.generate(prompt, 1000, 0.2, backend);
    }
}
