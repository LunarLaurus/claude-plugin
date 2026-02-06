package com.lunarlaurus.mcp.model;

/**
 * Backend Type
 *
 * Supported LLM backend container types for dynamic model management.
 */
public enum BackendType {
    /**
     * Ollama backend - Standard Ollama server
     * Image: ollama/ollama:latest
     * Internal port: 11434
     */
    OLLAMA,

    /**
     * llama.cpp backend - GGUF-based inference server
     * Image: ghcr.io/ggerganov/llama.cpp:server
     * Internal port: 8080
     */
    LLAMA_CPP,

    /**
     * Custom backend - User-provided Docker image
     * Image: specified in request
     * Internal port: varies
     */
    CUSTOM
}
