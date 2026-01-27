package com.lunarlaurus.mcp.service.inference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Summarizer Service
 * 
 * Summarizes long text using local LLM with specialized prompts
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SummarizerService {

    private final LocalLLMService localLLMService;

    public String summarize(String text, Integer maxLength) {
        log.info("Summarizing text of length: {}", text.length());

        String prompt = String.format(
            "[INST] Summarize the following text in approximately %d words. " +
            "Focus on key points and main ideas. Be concise and clear.\n\nText:\n%s\n\nSummary: [/INST]",
            maxLength, text
        );

        // Use CPU backend for summarization (typically longer context)
        return localLLMService.generate(prompt, maxLength * 2, 0.3, "cpu");
    }
}
