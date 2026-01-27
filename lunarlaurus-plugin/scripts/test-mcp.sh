#!/bin/bash

# LunarLaurus MCP Server Test Script
# Tests all MCP endpoints

set -e

MCP_URL="http://localhost:8000/mcp"

echo "=========================================="
echo "LunarLaurus MCP Server Tests"
echo "=========================================="
echo ""

# Test 1: Health check
echo "Test 1: Health Check"
echo "--------------------"
curl -s "$MCP_URL/health"
echo ""
echo ""

# Test 2: List tools
echo "Test 2: List Tools"
echo "--------------------"
curl -s -X POST "$MCP_URL/list-tools" \
    -H "Content-Type: application/json" \
    -d '{}' | jq '.'
echo ""

# Test 3: Local LLM (GPU)
echo "Test 3: Local LLM Generation"
echo "-----------------------------"
curl -s -X POST "$MCP_URL/call-tool" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "local_llm",
        "arguments": {
            "prompt": "Explain quantum computing in one sentence.",
            "max_tokens": 100,
            "temperature": 0.7,
            "model": "gpu"
        }
    }' | jq '.'
echo ""

# Test 4: Summarizer
echo "Test 4: Text Summarization"
echo "--------------------------"
curl -s -X POST "$MCP_URL/call-tool" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "summarizer",
        "arguments": {
            "text": "Artificial intelligence (AI) is intelligence demonstrated by machines, in contrast to the natural intelligence displayed by humans and animals. Leading AI textbooks define the field as the study of intelligent agents: any device that perceives its environment and takes actions that maximize its chance of successfully achieving its goals. Colloquially, the term artificial intelligence is often used to describe machines (or computers) that mimic cognitive functions that humans associate with the human mind, such as learning and problem solving.",
            "max_length": 50
        }
    }' | jq '.'
echo ""

# Test 5: Embedding
echo "Test 5: Text Embedding"
echo "----------------------"
curl -s -X POST "$MCP_URL/call-tool" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "embedding",
        "arguments": {
            "text": "Hello, this is a test sentence for embedding generation."
        }
    }' | jq '.'
echo ""

# Test 6: Code Analysis
echo "Test 6: Code Analysis"
echo "---------------------"
curl -s -X POST "$MCP_URL/call-tool" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "code_analysis",
        "arguments": {
            "code": "def factorial(n):\n    if n == 0:\n        return 1\n    return n * factorial(n-1)",
            "language": "python"
        }
    }' | jq '.'
echo ""

# Test 7: Translation
echo "Test 7: Translation"
echo "-------------------"
curl -s -X POST "$MCP_URL/call-tool" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "translation",
        "arguments": {
            "text": "Hello, how are you?",
            "source_language": "english",
            "target_language": "spanish"
        }
    }' | jq '.'
echo ""

echo "=========================================="
echo "All Tests Complete!"
echo "=========================================="
