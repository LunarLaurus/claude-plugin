# LunarLaurus Local Compute Plugin

**Author:** LunarLaurus  
**Organization:** Laurus Industries  
**Version:** 1.0.0

## Overview

The LunarLaurus Local Compute Plugin is a Model Context Protocol (MCP) server that integrates your 24U rack local compute resources with Claude. It provides intelligent routing between GPU, CPU, and cloud resources for optimal performance and token efficiency.

## Features

- **Intelligent Routing**: Automatically routes requests based on complexity
  - Short prompts (<2k tokens) → GPU (RTX4000)
  - Medium prompts (2k-8k tokens) → CPU (Intel 8260)
  - Long/complex prompts → Cloud fallback option
- **Multiple AI Tools**:
  - `local_llm`: General-purpose text generation
  - `summarizer`: Text summarization
  - `embedding`: Semantic embeddings for search/similarity
  - `code_analysis`: Code review and analysis
  - `translation`: Multi-language translation
- **Docker-Based**: Easy deployment with Docker Compose
- **Production-Ready**: Health checks, logging, and monitoring

## Hardware Requirements

### Minimum
- CPU: Dual Intel Xeon (24+ cores recommended)
- RAM: 64GB
- GPU: NVIDIA GPU with 8GB+ VRAM (RTX 4000 or better)
- Storage: 50GB for models
- OS: Linux with Docker support

### Tested Configuration
- CPU: Dual Intel 8260 (24 cores free)
- RAM: 128GB
- GPU: RTX4000 (8GB) + P2000 (4GB)
- Rack: 24U cluster

## Quick Start

### 1. Prerequisites

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo apt-get install docker-compose-plugin

# Install NVIDIA Docker (for GPU support)
distribution=$(. /etc/os-release;echo $ID$VERSION_ID)
curl -s -L https://nvidia.github.io/nvidia-docker/gpgkey | sudo apt-key add -
curl -s -L https://nvidia.github.io/nvidia-docker/$distribution/nvidia-docker.list | sudo tee /etc/apt/sources.list.d/nvidia-docker.list
sudo apt-get update && sudo apt-get install -y nvidia-docker2
sudo systemctl restart docker
```

### 2. Download Models

```bash
./scripts/setup-models.sh
```

### 3. Start Services

```bash
./scripts/start.sh
```

### 4. Pull GPU Models

```bash
docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct
docker exec -it lunarlaurus-ollama-gpu ollama pull nomic-embed-text
```

### 5. Test the Server

```bash
./scripts/test-mcp.sh
```

## Architecture

```
┌─────────────────────────────────────────────────┐
│                  Claude AI                       │
│            (via MCP Protocol)                    │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│          MCP Server (Spring Boot)                │
│         Port 8000 - Java 17                      │
│                                                   │
│  ┌───────────────────────────────────────────┐  │
│  │        Intelligent Router                  │  │
│  │  - Token counting                          │  │
│  │  - Backend selection                       │  │
│  │  - Load balancing                          │  │
│  └───────────────────────────────────────────┘  │
└────┬─────────────────────┬──────────────────┬───┘
     │                     │                  │
     ▼                     ▼                  ▼
┌─────────┐        ┌──────────────┐    ┌──────────┐
│ Ollama  │        │ llama.cpp    │    │ Ollama   │
│  (GPU)  │        │   (CPU)      │    │  (GPU)   │
│ RTX4000 │        │ Intel 8260   │    │ Embeddings│
│ Port    │        │ Port 8080    │    │ Port     │
│ 11434   │        │              │    │ 11434    │
└─────────┘        └──────────────┘    └──────────┘
```

## API Endpoints

### List Available Tools
```bash
POST /mcp/list-tools
```

### Call a Tool
```bash
POST /mcp/call-tool
Content-Type: application/json

{
  "name": "local_llm",
  "arguments": {
    "prompt": "Your prompt here",
    "max_tokens": 512,
    "temperature": 0.7
  }
}
```

### Health Check
```bash
GET /mcp/health
```

## Configuration

Edit `mcp-server/src/main/resources/application.yml` to customize:

- Model endpoints
- Model names
- Routing thresholds
- Logging levels

## Model Recommendations

### GPU Models (RTX4000 - 8GB VRAM)

| Model | Size | Use Case | Speed | Quality |
|-------|------|----------|-------|---------|
| **mistral:7b-instruct** | ~4GB | General purpose, fast responses | ⚡⚡⚡ | ⭐⭐⭐⭐ |
| llama3.2:7b | ~4GB | Latest LLaMA, good quality | ⚡⚡ | ⭐⭐⭐⭐ |
| phi3:mini | ~2GB | Very fast, good for simple tasks | ⚡⚡⚡⚡ | ⭐⭐⭐ |
| **nomic-embed-text** | ~274MB | Embeddings | ⚡⚡⚡⚡ | ⭐⭐⭐⭐ |

**Recommended for RTX4000: mistral:7b-instruct + nomic-embed-text**

### CPU Models (Intel 8260)

| Model | Size | Use Case | Speed | Quality |
|-------|------|----------|-------|---------|
| **Mistral 7B Q4_K_M** | ~4.4GB | Balanced speed/quality | ⚡⚡ | ⭐⭐⭐⭐ |
| Mistral 7B Q5_K_M | ~5.3GB | Better quality, slower | ⚡ | ⭐⭐⭐⭐⭐ |
| TinyLLaMA 1.1B Q4_K_M | ~700MB | Very fast, simple tasks | ⚡⚡⚡⚡ | ⭐⭐⭐ |

**Recommended for CPU: Mistral 7B Q4_K_M**

## Usage Examples

### Example 1: Text Generation
```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "local_llm",
    "arguments": {
      "prompt": "Write a haiku about computers",
      "max_tokens": 50,
      "temperature": 0.8
    }
  }'
```

### Example 2: Code Analysis
```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "code_analysis",
    "arguments": {
      "code": "function add(a, b) { return a + b; }",
      "language": "javascript"
    }
  }'
```

### Example 3: Embeddings
```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "embedding",
    "arguments": {
      "text": ["Document 1", "Document 2", "Document 3"]
    }
  }'
```

## Monitoring

### View Logs
```bash
docker-compose logs -f mcp-server
docker-compose logs -f ollama-gpu
docker-compose logs -f llama-cpp-cpu
```

### Check Resource Usage
```bash
# GPU usage
nvidia-smi

# Container stats
docker stats
```

### Optional: Prometheus + Grafana
```bash
# Start monitoring stack
docker-compose --profile monitoring up -d

# Access Grafana at http://localhost:3000
# Default credentials: admin/admin
```

## Troubleshooting

### GPU not detected
```bash
# Check NVIDIA drivers
nvidia-smi

# Check Docker GPU support
docker run --rm --gpus all nvidia/cuda:11.0-base nvidia-smi
```

### Model download fails
```bash
# Manual download
docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct

# Check disk space
df -h
```

### MCP server not responding
```bash
# Check logs
docker-compose logs mcp-server

# Restart service
docker-compose restart mcp-server
```

## Development

### Build from source
```bash
cd mcp-server
mvn clean package
java -jar target/mcp-server-1.0.0.jar
```

### Run tests
```bash
mvn test
```

## License

Proprietary - LunarLaurus, Laurus Industries

## Support

For issues or questions, check the documentation in `docs/` or create an issue in your project repository.
