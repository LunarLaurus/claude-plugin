#!/bin/bash

# LunarLaurus Quick Start Script
# Starts the entire MCP server stack

set -e

echo "=========================================="
echo "LunarLaurus MCP Server - Quick Start"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "Error: Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check for NVIDIA GPU support
if command -v nvidia-smi &> /dev/null; then
    echo "✓ NVIDIA GPU detected"
    nvidia-smi --query-gpu=name,memory.total --format=csv,noheader
else
    echo "⚠ Warning: nvidia-smi not found. GPU acceleration may not work."
    echo "  Make sure NVIDIA drivers and nvidia-docker are installed."
fi

echo ""
echo "Starting services..."
echo ""

# Start the services
if docker compose version &> /dev/null 2>&1; then
    docker compose up -d
else
    docker-compose up -d
fi

echo ""
echo "Waiting for services to be ready..."
sleep 10

# Check service health
echo ""
echo "Checking service health..."
echo ""

# Check MCP Server
if curl -s http://localhost:8000/mcp/health > /dev/null; then
    echo "✓ MCP Server is running on http://localhost:8000"
else
    echo "⚠ MCP Server is not responding yet, it may still be starting..."
fi

# Check Ollama GPU
if curl -s http://localhost:11434/api/version > /dev/null; then
    echo "✓ Ollama GPU is running on http://localhost:11434"
else
    echo "⚠ Ollama GPU is not responding yet, it may still be starting..."
fi

# Check llama.cpp CPU
if curl -s http://localhost:8080/health > /dev/null; then
    echo "✓ Llama.cpp CPU is running on http://localhost:8080"
else
    echo "⚠ Llama.cpp CPU is not responding yet, it may still be starting..."
fi

echo ""
echo "=========================================="
echo "Services Started!"
echo "=========================================="
echo ""
echo "MCP Server endpoint: http://localhost:8000"
echo ""
echo "Next steps:"
echo "1. Pull GPU models:"
echo "   docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct"
echo "   docker exec -it lunarlaurus-ollama-gpu ollama pull nomic-embed-text"
echo ""
echo "2. Test the MCP server:"
echo "   curl -X POST http://localhost:8000/mcp/list-tools"
echo ""
echo "3. View logs:"
echo "   docker-compose logs -f mcp-server"
echo ""
echo "4. To stop services:"
echo "   docker-compose down"
echo ""
