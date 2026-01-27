FROM ubuntu:22.04 AS builder

# Install build dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    git \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Clone and build llama.cpp
WORKDIR /build
RUN git clone https://github.com/ggerganov/llama.cpp.git
WORKDIR /build/llama.cpp
RUN cmake -B build -DLLAMA_NATIVE=OFF -DLLAMA_AVX2=ON
RUN cmake --build build --config Release -j$(nproc)

# Runtime stage
FROM ubuntu:22.04

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    libgomp1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy built binaries
COPY --from=builder /build/llama.cpp/build/bin/llama-server /usr/local/bin/llama-server
RUN chmod +x /usr/local/bin/llama-server

# Create models directory
RUN mkdir -p /models

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Start server
ENTRYPOINT ["/usr/local/bin/llama-server", \
    "--host", "0.0.0.0", \
    "--port", "8080", \
    "--model", "/models/mistral-7b-instruct-q4_K_M.gguf", \
    "--ctx-size", "8192", \
    "--threads", "20", \
    "--parallel", "2"]
