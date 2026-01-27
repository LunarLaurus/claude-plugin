# LunarLaurus MCP Server - Quick Reference

## Essential Commands

### Starting/Stopping

```bash
# Start all services
./scripts/start.sh

# Stop all services
docker-compose down

# Restart a service
docker-compose restart mcp-server

# Stop and remove volumes (clean start)
docker-compose down -v
```

### Model Management

```bash
# Download CPU models
./scripts/setup-models.sh

# Pull GPU models
docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct
docker exec -it lunarlaurus-ollama-gpu ollama pull nomic-embed-text

# List loaded models
docker exec -it lunarlaurus-ollama-gpu ollama list

# Remove a model
docker exec -it lunarlaurus-ollama-gpu ollama rm <model-name>
```

### Testing

```bash
# Run full test suite
./scripts/test-mcp.sh

# Quick health check
curl http://localhost:8000/mcp/health

# List available tools
curl -X POST http://localhost:8000/mcp/list-tools | jq '.'
```

### Monitoring

```bash
# View logs (all services)
docker-compose logs -f

# View logs (specific service)
docker-compose logs -f mcp-server
docker-compose logs -f ollama-gpu
docker-compose logs -f llama-cpp-cpu

# GPU monitoring
nvidia-smi -l 1

# Container stats
docker stats

# Start monitoring stack
docker-compose --profile monitoring up -d
```

## API Quick Reference

### Base URL
```
http://localhost:8000/mcp
```

### Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/health` | GET | Health check |
| `/list-tools` | POST | List available tools |
| `/call-tool` | POST | Execute a tool |

### Tool Names

- `local_llm` - Text generation
- `summarizer` - Text summarization
- `embedding` - Generate embeddings
- `code_analysis` - Code review
- `translation` - Language translation

### Example Requests

#### Text Generation
```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "local_llm",
    "arguments": {
      "prompt": "Your prompt here",
      "max_tokens": 512,
      "temperature": 0.7,
      "model": "auto"
    }
  }'
```

#### Summarization
```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "summarizer",
    "arguments": {
      "text": "Long text to summarize...",
      "max_length": 200
    }
  }'
```

#### Embedding
```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "embedding",
    "arguments": {
      "text": "Text to embed"
    }
  }'
```

## Model Recommendations

### GPU (RTX4000 - 8GB)
- **Primary:** mistral:7b-instruct (~4GB, fast, high quality)
- **Embedding:** nomic-embed-text (~274MB)
- **Alternative:** phi3:mini (~2GB, very fast)

### CPU (Intel 8260)
- **Primary:** Mistral 7B Q4_K_M (4.4GB, balanced)
- **Fast:** TinyLLaMA 1.1B Q4_K_M (700MB)
- **Quality:** Mistral 7B Q5_K_M (5.3GB)

## Routing Rules

| Tokens | Backend | Typical Speed |
|--------|---------|---------------|
| < 2000 | GPU | 40-60 tok/sec |
| 2000-8000 | CPU | 8-15 tok/sec |
| > 8000 | CPU (consider cloud) | 8-15 tok/sec |

## Troubleshooting

### GPU Not Working
```bash
nvidia-smi  # Check GPU
docker run --rm --gpus all nvidia/cuda:12.0-base nvidia-smi  # Test Docker GPU
sudo systemctl restart docker  # Restart Docker
```

### Model Not Loading
```bash
docker-compose logs ollama-gpu  # Check logs
docker exec -it lunarlaurus-ollama-gpu ollama list  # List models
df -h  # Check disk space
```

### Slow Performance
```bash
nvidia-smi  # Check GPU utilization
docker stats  # Check resource usage
# Consider using smaller model or lower threads
```

### Service Won't Start
```bash
docker-compose logs <service-name>  # Check logs
docker ps -a  # Check container status
docker-compose down && docker-compose up -d  # Clean restart
```

## Configuration Files

| File | Purpose |
|------|---------|
| `docker-compose.yml` | Service orchestration |
| `mcp-server/src/main/resources/application.yml` | MCP server config |
| `.env` | Environment variables |
| `docker/prometheus/prometheus.yml` | Monitoring config |

## Performance Tuning

### GPU Performance
- Use smaller quantizations (Q4 vs Q5)
- Reduce context size
- Use phi3:mini for simple tasks

### CPU Performance
- Adjust thread count in docker-compose.yml
- Use Q4_K_M quantization
- Enable parallel processing

### Memory Management
- Monitor with `docker stats`
- Adjust batch sizes
- Clean up old models regularly

## Port Reference

| Port | Service | Purpose |
|------|---------|---------|
| 8000 | MCP Server | Main API |
| 8080 | llama.cpp | CPU inference |
| 11434 | Ollama | GPU inference |
| 9090 | Prometheus | Metrics |
| 3000 | Grafana | Dashboards |

## Security Notes

For production:
- Use reverse proxy (nginx/traefik)
- Implement API authentication
- Enable TLS/SSL
- Restrict network access
- Regular security updates

## Support

- Documentation: `docs/` directory
- Testing Guide: `docs/TESTING.md`
- Full Guide: `docs/LunarLaurus_Implementation_Guide.docx`
- Scripts: `scripts/` directory

## Version Info

- Project: LunarLaurus Local Compute Plugin
- Version: 1.0.0
- Author: LunarLaurus, Laurus Industries
- Java: 17
- Spring Boot: 3.2.1
