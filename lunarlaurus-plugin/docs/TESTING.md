# LunarLaurus MCP Server - Testing Guide

## Overview

This document provides comprehensive testing procedures for the LunarLaurus Local Compute Plugin.

## Pre-Testing Checklist

Before running tests, ensure:

- [ ] All services are running (`docker ps` shows 3-4 containers)
- [ ] GPU models are pulled (Mistral 7B, Nomic Embed)
- [ ] CPU model is present in models directory
- [ ] Health endpoints respond successfully

## Quick Health Check

```bash
# Check all services
curl http://localhost:8000/mcp/health
curl http://localhost:11434/api/version
curl http://localhost:8080/health
```

Expected responses:
- MCP Server: "MCP Server is running"
- Ollama: JSON with version info
- Llama.cpp: JSON with status

## Automated Test Suite

Run the complete test suite:

```bash
./scripts/test-mcp.sh
```

This tests all five tools with sample data.

## Manual Testing

### Test 1: Text Generation (GPU)

**Purpose:** Verify GPU inference works correctly

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "local_llm",
    "arguments": {
      "prompt": "Write a haiku about technology",
      "max_tokens": 50,
      "temperature": 0.8,
      "model": "gpu"
    }
  }' | jq '.'
```

**Expected:**
- Response time: 2-5 seconds
- isError: false
- content contains a haiku

**GPU Verification:**
```bash
nvidia-smi  # Should show ~4GB VRAM usage
```

### Test 2: Text Generation (CPU)

**Purpose:** Verify CPU inference works correctly

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "local_llm",
    "arguments": {
      "prompt": "Explain machine learning in one sentence",
      "max_tokens": 100,
      "temperature": 0.3,
      "model": "cpu"
    }
  }' | jq '.'
```

**Expected:**
- Response time: 8-15 seconds
- isError: false
- content contains explanation

**CPU Verification:**
```bash
docker exec lunarlaurus-llama-cpp top -bn1 | head -20
```

### Test 3: Automatic Routing

**Purpose:** Verify intelligent routing based on token count

```bash
# Short prompt (should use GPU)
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "local_llm",
    "arguments": {
      "prompt": "Hello, how are you?",
      "max_tokens": 50,
      "model": "auto"
    }
  }' | jq '.'

# Long prompt (should use CPU)
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "local_llm",
    "arguments": {
      "prompt": "'$(head -c 10000 /dev/urandom | base64)'",
      "max_tokens": 200,
      "model": "auto"
    }
  }' | jq '.'
```

**Verification:**
Check MCP server logs to see routing decisions:
```bash
docker-compose logs mcp-server | grep "Routing to"
```

### Test 4: Summarization

**Purpose:** Verify text summarization works

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "summarizer",
    "arguments": {
      "text": "Artificial intelligence (AI) is intelligence demonstrated by machines, in contrast to the natural intelligence displayed by humans and animals. Leading AI textbooks define the field as the study of intelligent agents: any device that perceives its environment and takes actions that maximize its chance of successfully achieving its goals. Colloquially, the term artificial intelligence is often used to describe machines (or computers) that mimic cognitive functions that humans associate with the human mind, such as learning and problem solving. AI research has been defined as the field of study of intelligent agents, which refers to any system that perceives its environment and takes actions that maximize its chance of achieving its goals.",
      "max_length": 50
    }
  }' | jq '.'
```

**Expected:**
- Response time: 5-10 seconds
- Summary is approximately 50 words or less
- Key concepts preserved

### Test 5: Single Embedding

**Purpose:** Verify embedding generation

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "embedding",
    "arguments": {
      "text": "The quick brown fox jumps over the lazy dog"
    }
  }' | jq '.'
```

**Expected:**
- Response time: 1-3 seconds
- Success message about embedding dimensions (768)
- isError: false

### Test 6: Batch Embeddings

**Purpose:** Verify batch embedding processing

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "embedding",
    "arguments": {
      "text": [
        "Machine learning is a subset of AI",
        "Deep learning uses neural networks",
        "Natural language processing handles text"
      ]
    }
  }' | jq '.'
```

**Expected:**
- Response time: 2-5 seconds
- Success message about 3 embeddings generated
- isError: false

### Test 7: Code Analysis

**Purpose:** Verify code analysis functionality

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "code_analysis",
    "arguments": {
      "code": "def factorial(n):\n    if n == 0:\n        return 1\n    return n * factorial(n-1)",
      "language": "python"
    }
  }' | jq '.'
```

**Expected:**
- Response time: 3-8 seconds
- Analysis mentions: recursion, base case, potential stack overflow
- isError: false

### Test 8: Translation

**Purpose:** Verify translation functionality

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name": "translation",
    "arguments": {
      "text": "Hello, how are you today?",
      "source_language": "english",
      "target_language": "spanish"
    }
  }' | jq '.'
```

**Expected:**
- Response time: 2-5 seconds
- Translated text in Spanish
- isError: false

## Performance Testing

### Latency Test

Measure response times for different backends:

```bash
# GPU latency
time curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{"name":"local_llm","arguments":{"prompt":"Hello","max_tokens":10,"model":"gpu"}}' \
  -o /dev/null -s

# CPU latency
time curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{"name":"local_llm","arguments":{"prompt":"Hello","max_tokens":10,"model":"cpu"}}' \
  -o /dev/null -s
```

**Expected:**
- GPU: 2-4 seconds
- CPU: 5-10 seconds

### Throughput Test

Test concurrent requests:

```bash
# Install Apache Bench if needed
sudo apt-get install apache2-utils

# Test with 10 concurrent requests
ab -n 10 -c 2 -T 'application/json' \
  -p test-payload.json \
  http://localhost:8000/mcp/call-tool
```

Create test-payload.json:
```json
{"name":"local_llm","arguments":{"prompt":"Test","max_tokens":20}}
```

**Expected:**
- All requests succeed
- No timeouts
- Reasonable total time (20-40 seconds for GPU, 50-100 seconds for CPU)

### Load Test

Stress test with many requests:

```bash
# Install vegeta if needed
go install github.com/tsenart/vegeta@latest

# Create targets file
echo 'POST http://localhost:8000/mcp/call-tool
Content-Type: application/json
@test-payload.json' > targets.txt

# Run load test (10 req/sec for 30 seconds)
vegeta attack -targets=targets.txt -rate=10 -duration=30s | vegeta report
```

**Expected:**
- Success rate > 95%
- P99 latency < 20 seconds
- No OOM errors

## Error Handling Tests

### Test Invalid Tool Name

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{"name":"invalid_tool","arguments":{}}' | jq '.'
```

**Expected:**
- isError: true
- Error message: "Unknown tool: invalid_tool"

### Test Missing Required Arguments

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{"name":"local_llm","arguments":{}}' | jq '.'
```

**Expected:**
- isError: true
- Error mentioning missing "prompt"

### Test Invalid Model Backend

```bash
curl -X POST http://localhost:8000/mcp/call-tool \
  -H "Content-Type: application/json" \
  -d '{
    "name":"local_llm",
    "arguments":{"prompt":"test","model":"invalid"}
  }' | jq '.'
```

**Expected:**
- Request handled gracefully
- Falls back to auto routing or returns error

## Integration Tests

### Test with Claude SDK (Python)

```python
import requests

mcp_url = "http://localhost:8000/mcp"

# List tools
response = requests.post(f"{mcp_url}/list-tools")
print("Available tools:", response.json())

# Call tool
response = requests.post(f"{mcp_url}/call-tool", json={
    "name": "local_llm",
    "arguments": {
        "prompt": "Explain Docker in one sentence",
        "max_tokens": 50
    }
})
print("Result:", response.json())
```

### Test with Java Client

See mcp-server/src/test/java for JUnit tests.

```bash
cd mcp-server
mvn test
```

## Monitoring Validation

### Check Prometheus Metrics

```bash
# Enable monitoring profile
docker-compose --profile monitoring up -d

# Check metrics endpoint
curl http://localhost:8000/actuator/metrics
curl http://localhost:8000/actuator/health
```

### View Grafana Dashboard

1. Open http://localhost:3000
2. Login with admin/admin
3. Check pre-configured dashboards
4. Verify metrics are being collected

## Troubleshooting Failed Tests

### If GPU tests fail:

```bash
# Check GPU availability
nvidia-smi

# Check Ollama logs
docker-compose logs ollama-gpu

# Verify model is loaded
docker exec -it lunarlaurus-ollama-gpu ollama list
```

### If CPU tests fail:

```bash
# Check llama.cpp logs
docker-compose logs llama-cpp-cpu

# Verify model file exists
docker exec -it lunarlaurus-llama-cpp ls -lh /models/

# Check CPU usage
docker stats lunarlaurus-llama-cpp
```

### If MCP server tests fail:

```bash
# Check application logs
docker-compose logs mcp-server

# Check Java process
docker exec -it lunarlaurus-mcp-server ps aux

# Verify network connectivity
docker exec -it lunarlaurus-mcp-server curl http://ollama-gpu:11434/api/version
```

## Test Results Documentation

After testing, document results:

```markdown
## Test Results - [Date]

### Environment
- Hardware: [Your specs]
- Docker version: [Version]
- Model versions: [Models used]

### Test Summary
- Total tests: [Number]
- Passed: [Number]
- Failed: [Number]
- Avg GPU latency: [Time]
- Avg CPU latency: [Time]

### Issues Found
1. [Issue description]
2. [Issue description]

### Performance Notes
- [Observations]
```

## Continuous Testing

Set up cron job for regular health checks:

```bash
# Add to crontab
*/15 * * * * /opt/lunarlaurus-plugin/scripts/health-check.sh >> /var/log/mcp-health.log 2>&1
```

Create health-check.sh:
```bash
#!/bin/bash
curl -s http://localhost:8000/mcp/health || echo "MCP Server down at $(date)"
```

## Conclusion

Regular testing ensures the LunarLaurus MCP Server maintains high reliability and performance. Run the automated test suite after any changes and document results for future reference.
