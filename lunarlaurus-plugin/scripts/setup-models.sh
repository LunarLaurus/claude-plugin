#!/bin/bash

# LunarLaurus Model Setup Script
# Downloads and configures recommended models for your hardware

set -e

echo "=========================================="
echo "LunarLaurus Model Setup"
echo "=========================================="
echo ""

# Create models directory
MODELS_DIR="./models"
mkdir -p "$MODELS_DIR"

echo "Models will be downloaded to: $MODELS_DIR"
echo ""

# Function to download with progress
download_model() {
    local url=$1
    local output=$2
    local name=$3
    
    echo "Downloading $name..."
    if command -v wget &> /dev/null; then
        wget --progress=bar:force -O "$output" "$url"
    elif command -v curl &> /dev/null; then
        curl -L -o "$output" --progress-bar "$url"
    else
        echo "Error: Neither wget nor curl found. Please install one."
        exit 1
    fi
    echo "✓ Downloaded $name"
    echo ""
}

echo "=========================================="
echo "1. GPU Models (for Ollama on RTX4000)"
echo "=========================================="
echo ""
echo "Ollama will download models automatically on first use."
echo "Recommended models for RTX4000 (8GB VRAM):"
echo "  - mistral:7b-instruct (General purpose, fast)"
echo "  - llama3.2:7b (Latest LLaMA, good quality)"
echo "  - nomic-embed-text (Embeddings)"
echo ""
echo "To pull models manually after starting Ollama:"
echo "  docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct"
echo "  docker exec -it lunarlaurus-ollama-gpu ollama pull nomic-embed-text"
echo ""

echo "=========================================="
echo "2. CPU Models (for llama.cpp on Intel 8260)"
echo "=========================================="
echo ""

# Mistral 7B Instruct Q4_K_M (recommended for CPU)
if [ ! -f "$MODELS_DIR/mistral-7b-instruct-q4_K_M.gguf" ]; then
    echo "Downloading Mistral 7B Instruct Q4_K_M (~4.4GB)..."
    echo "This is the recommended CPU model - good balance of speed and quality"
    download_model \
        "https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.2-GGUF/resolve/main/mistral-7b-instruct-v0.2.Q4_K_M.gguf" \
        "$MODELS_DIR/mistral-7b-instruct-q4_K_M.gguf" \
        "Mistral 7B Instruct Q4_K_M"
else
    echo "✓ Mistral 7B Instruct Q4_K_M already downloaded"
fi

# Optional: Smaller model for faster CPU inference
echo ""
echo "Optional: Download smaller/faster models? (y/n)"
read -r download_optional

if [ "$download_optional" = "y" ] || [ "$download_optional" = "Y" ]; then
    # TinyLLaMA for very fast CPU inference
    if [ ! -f "$MODELS_DIR/tinyllama-1.1b-chat-q4_K_M.gguf" ]; then
        echo "Downloading TinyLLaMA 1.1B Q4_K_M (~700MB)..."
        echo "This is a very fast model for simple tasks"
        download_model \
            "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf" \
            "$MODELS_DIR/tinyllama-1.1b-chat-q4_K_M.gguf" \
            "TinyLLaMA 1.1B Q4_K_M"
    else
        echo "✓ TinyLLaMA 1.1B Q4_K_M already downloaded"
    fi
fi

echo ""
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Start the services: docker-compose up -d"
echo "2. Pull GPU models: docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct"
echo "3. Pull embedding model: docker exec -it lunarlaurus-ollama-gpu ollama pull nomic-embed-text"
echo "4. Test the MCP server: curl http://localhost:8000/mcp/health"
echo ""
echo "For more information, see the documentation in docs/"
