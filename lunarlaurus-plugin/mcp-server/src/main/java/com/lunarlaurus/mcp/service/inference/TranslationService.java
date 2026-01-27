package com.lunarlaurus.mcp.service.inference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Translation Service
 * 
 * Translates text between languages using local multilingual models
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final LocalLLMService localLLMService;

    public String translate(String text, String sourceLang, String targetLang) {
        log.info("Translating from {} to {}", sourceLang, targetLang);

        String prompt;
        if ("auto".equals(sourceLang)) {
            prompt = String.format(
                "[INST] Translate the following text to %s:\n\n%s\n\nTranslation: [/INST]",
                targetLang, text
            );
        } else {
            prompt = String.format(
                "[INST] Translate the following text from %s to %s:\n\n%s\n\nTranslation: [/INST]",
                sourceLang, targetLang, text
            );
        }

        // Use GPU for faster translation
        return localLLMService.generate(prompt, text.length() / 2, 0.3, "gpu");
    }
}
