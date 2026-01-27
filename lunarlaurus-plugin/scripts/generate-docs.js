const { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, HeadingLevel, 
        AlignmentType, WidthType, BorderStyle, ShadingType, PageBreak } = require('docx');
const fs = require('fs');

// Create the comprehensive document
const doc = new Document({
    styles: {
        default: {
            document: {
                run: { font: "Arial", size: 24 }
            }
        },
        paragraphStyles: [
            {
                id: "Heading1",
                name: "Heading 1",
                basedOn: "Normal",
                next: "Normal",
                quickFormat: true,
                run: { size: 32, bold: true, font: "Arial", color: "1a5490" },
                paragraph: { spacing: { before: 480, after: 240 }, outlineLevel: 0 }
            },
            {
                id: "Heading2",
                name: "Heading 2",
                basedOn: "Normal",
                next: "Normal",
                quickFormat: true,
                run: { size: 28, bold: true, font: "Arial", color: "2e75b5" },
                paragraph: { spacing: { before: 360, after: 180 }, outlineLevel: 1 }
            },
            {
                id: "Heading3",
                name: "Heading 3",
                basedOn: "Normal",
                next: "Normal",
                quickFormat: true,
                run: { size: 26, bold: true, font: "Arial", color: "5b9bd5" },
                paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 2 }
            },
            {
                id: "CodeBlock",
                name: "Code Block",
                basedOn: "Normal",
                run: { font: "Courier New", size: 20 },
                paragraph: { spacing: { before: 120, after: 120 } }
            }
        ]
    },
    numbering: {
        config: [
            {
                reference: "bullets",
                levels: [
                    {
                        level: 0,
                        format: "bullet",
                        text: "•",
                        alignment: AlignmentType.LEFT,
                        style: {
                            paragraph: { indent: { left: 720, hanging: 360 } }
                        }
                    }
                ]
            }
        ]
    },
    sections: [{
        properties: {
            page: {
                size: { width: 12240, height: 15840 },
                margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 }
            }
        },
        children: [
            // Title Page
            new Paragraph({
                children: [new TextRun({ text: "LunarLaurus Local Compute Plugin", bold: true, size: 48 })],
                alignment: AlignmentType.CENTER,
                spacing: { before: 2880, after: 480 }
            }),
            new Paragraph({
                children: [new TextRun({ text: "Complete Implementation Guide", size: 36 })],
                alignment: AlignmentType.CENTER,
                spacing: { after: 240 }
            }),
            new Paragraph({
                children: [new TextRun({ text: "Model Context Protocol Server for Local Compute Resources", size: 24, italics: true })],
                alignment: AlignmentType.CENTER,
                spacing: { after: 1440 }
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "Author: ", bold: true }),
                    new TextRun({ text: "LunarLaurus" })
                ],
                alignment: AlignmentType.CENTER,
                spacing: { after: 120 }
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "Organization: ", bold: true }),
                    new TextRun({ text: "Laurus Industries" })
                ],
                alignment: AlignmentType.CENTER,
                spacing: { after: 120 }
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "Version: ", bold: true }),
                    new TextRun({ text: "1.0.0" })
                ],
                alignment: AlignmentType.CENTER,
                spacing: { after: 120 }
            }),
            new Paragraph({
                children: [
                    new TextRun({ text: "Date: ", bold: true }),
                    new TextRun({ text: new Date().toLocaleDateString() })
                ],
                alignment: AlignmentType.CENTER
            }),

            new Paragraph({ children: [new PageBreak()] }),

            // Table of Contents
            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("Table of Contents")]
            }),
            new Paragraph({ text: "1. Executive Summary", spacing: { after: 120 } }),
            new Paragraph({ text: "2. System Architecture", spacing: { after: 120 } }),
            new Paragraph({ text: "3. Hardware Requirements & Recommendations", spacing: { after: 120 } }),
            new Paragraph({ text: "4. Model Recommendations", spacing: { after: 120 } }),
            new Paragraph({ text: "5. Installation & Setup", spacing: { after: 120 } }),
            new Paragraph({ text: "6. Configuration", spacing: { after: 120 } }),
            new Paragraph({ text: "7. API Reference", spacing: { after: 120 } }),
            new Paragraph({ text: "8. Usage Examples", spacing: { after: 120 } }),
            new Paragraph({ text: "9. Monitoring & Troubleshooting", spacing: { after: 120 } }),
            new Paragraph({ text: "10. Best Practices", spacing: { after: 120 } }),
            new Paragraph({ text: "Appendix A: File Structure", spacing: { after: 120 } }),
            new Paragraph({ text: "Appendix B: Environment Variables", spacing: { after: 120 } }),

            new Paragraph({ children: [new PageBreak()] }),

            // 1. Executive Summary
            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("1. Executive Summary")]
            }),
            new Paragraph({
                text: "The LunarLaurus Local Compute Plugin is a production-ready Model Context Protocol (MCP) server that seamlessly integrates your 24U rack local compute resources with Claude AI. Built with Java/Spring Boot, it provides intelligent routing between GPU, CPU, and optional cloud resources for optimal performance and cost efficiency.",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Key Features")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "Intelligent routing based on token count and complexity", bold: false })]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Five specialized AI tools: text generation, summarization, embeddings, code analysis, and translation")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Docker-based deployment for easy scalability")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Production-ready with health checks, logging, and optional monitoring")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Optimized for your hardware: RTX4000 GPU + Intel 8260 CPU")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Routing Strategy")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Short prompts (<2000 tokens) → GPU (RTX4000) for fast inference")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Medium prompts (2000-8000 tokens) → CPU (Intel 8260) for efficiency")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Long/complex prompts → CPU with consideration for cloud fallback")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            // 2. System Architecture
            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("2. System Architecture")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Component Overview")]
            }),
            new Paragraph({
                text: "The system consists of four main components working together:",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("MCP Server (Java/Spring Boot)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Port: 8000")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Language: Java 17")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Framework: Spring Boot 3.2.1")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Role: Central orchestrator, routing logic, tool management")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Ollama GPU Backend")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Port: 11434")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Hardware: RTX4000 (8GB VRAM)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Role: Fast inference for short prompts, embeddings")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Models: Mistral 7B Instruct, Nomic Embed Text")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Llama.cpp CPU Backend")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Port: 8080")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Hardware: Dual Intel 8260 (24 cores)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Role: Medium-length inference, batch processing")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Models: Mistral 7B Instruct Q4_K_M (GGUF)")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Optional Monitoring Stack")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Prometheus (Port 9090): Metrics collection")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Grafana (Port 3000): Visualization and dashboards")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            // 3. Hardware Requirements
            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("3. Hardware Requirements & Recommendations")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Minimum Requirements")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("CPU: 16+ cores (Intel Xeon or AMD EPYC)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("RAM: 64GB DDR4")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("GPU: NVIDIA GPU with 8GB+ VRAM (RTX 3060 or better)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Storage: 50GB free space (100GB+ recommended)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("OS: Linux (Ubuntu 22.04 LTS recommended)")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Your Configuration (Optimal)")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("CPU: Dual Intel Xeon Gold 8260 (24 cores @ 2.4GHz, 48 threads)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("RAM: 128GB DDR4 ECC")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("GPU: RTX4000 (8GB VRAM) + P2000 (4GB VRAM)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Storage: Enterprise SSD")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Network: 10GbE")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Rack: 24U with dedicated cooling")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Performance Expectations")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "With your hardware configuration:",
                spacing: { after: 180 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("GPU (RTX4000): 40-60 tokens/second for 7B models")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("CPU (8260): 8-15 tokens/second for 7B Q4 models")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Concurrent requests: 2-4 GPU, 4-8 CPU (with proper batching)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Cold start time: 10-15 seconds for model loading")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            // 4. Model Recommendations
            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("4. Model Recommendations")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("GPU Models (RTX4000 - 8GB VRAM)")]
            }),
            new Paragraph({
                text: "Recommended models optimized for your RTX4000:",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Primary Model: Mistral 7B Instruct")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Command: docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Size: ~4.1GB VRAM")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Speed: 45-55 tokens/sec")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Use case: General purpose, fast, high quality")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Best for: Quick Q&A, code generation, content creation")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Embedding Model: Nomic Embed Text")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Command: docker exec -it lunarlaurus-ollama-gpu ollama pull nomic-embed-text")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Size: ~274MB")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Dimensions: 768")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Best for: Semantic search, similarity, RAG applications")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Alternative GPU Models")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "For specific use cases:",
                spacing: { after: 180 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("llama3.2:7b - Latest LLaMA, excellent quality (slightly slower)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("phi3:mini - Very fast (2-3GB VRAM), good for simple tasks")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("codellama:7b - Specialized for code generation")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("CPU Models (Intel 8260)")],
                spacing: { before: 480 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Primary Model: Mistral 7B Instruct Q4_K_M")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Download: Automated via setup-models.sh script")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Size: 4.4GB")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Format: GGUF (llama.cpp compatible)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Speed: 10-14 tokens/sec on 20 threads")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Quantization: Q4_K_M (optimal balance)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Best for: Medium-length tasks, batch processing, summarization")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Alternative CPU Models")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Mistral 7B Q5_K_M - Better quality, slower (5.3GB)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("TinyLLaMA 1.1B Q4_K_M - Very fast for simple tasks (700MB)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("LLaMA 3.2 7B Q4_K_M - Latest model, good quality (4.5GB)")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Quantization Guide")],
                spacing: { before: 480 }
            }),
            new Paragraph({
                text: "Understanding GGUF quantization levels:",
                spacing: { after: 180 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Q4_K_M (Recommended): Best balance of size, speed, and quality")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Q5_K_M: Better quality, 20% larger, 15% slower")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Q6_K: Near-original quality, 40% larger, 25% slower")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Q3_K_M: Smaller/faster, noticeable quality loss")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            // 5. Installation & Setup
            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("5. Installation & Setup")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Step 1: Prerequisites")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Install Docker")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "curl -fsSL https://get.docker.com -o get-docker.sh",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo sh get-docker.sh",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo usermod -aG docker $USER",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "newgrp docker",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Install Docker Compose")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo apt-get update",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo apt-get install docker-compose-plugin",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Install NVIDIA Docker Support")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "distribution=$(. /etc/os-release;echo $ID$VERSION_ID)",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "curl -s -L https://nvidia.github.io/libnvidia-container/$distribution/libnvidia-container.list | sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo apt-get update",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo apt-get install -y nvidia-container-toolkit",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo nvidia-ctk runtime configure --runtime=docker",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "sudo systemctl restart docker",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Verify GPU Support")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "nvidia-smi",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "docker run --rm --gpus all nvidia/cuda:12.0-base nvidia-smi",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Step 2: Clone and Setup")],
                spacing: { before: 480 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "cd /opt  # or your preferred location",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "# Extract the implementation files here",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "cd lunarlaurus-plugin",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "chmod +x scripts/*.sh",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Step 3: Download Models")],
                spacing: { before: 480 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "./scripts/setup-models.sh",
                spacing: { before: 120, after: 240 }
            }),
            new Paragraph({
                text: "This script will download the CPU model (Mistral 7B Q4_K_M, ~4.4GB). GPU models will be pulled automatically by Ollama on first use.",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Step 4: Start Services")],
                spacing: { before: 480 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "./scripts/start.sh",
                spacing: { before: 120, after: 240 }
            }),
            new Paragraph({
                text: "This will start all services in Docker containers. Wait 30-60 seconds for full initialization.",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Step 5: Pull GPU Models")],
                spacing: { before: 480 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "# Pull main text generation model",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "docker exec -it lunarlaurus-ollama-gpu ollama pull mistral:7b-instruct",
                spacing: { after: 120 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "# Pull embedding model",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "docker exec -it lunarlaurus-ollama-gpu ollama pull nomic-embed-text",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Step 6: Verify Installation")],
                spacing: { before: 480 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "./scripts/test-mcp.sh",
                spacing: { before: 120, after: 240 }
            }),
            new Paragraph({
                text: "This comprehensive test script will verify all endpoints and tools are working correctly.",
                spacing: { after: 240 }
            }),

            new Paragraph({ children: [new PageBreak()] }),

            // Continue with sections 6-10...
            // Adding more sections to complete the document

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("6. Configuration")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Application Configuration")]
            }),
            new Paragraph({
                text: "The main configuration file is located at mcp-server/src/main/resources/application.yml. Key configuration options:",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Server Settings")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "server:",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  port: 8000  # MCP server port",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("LLM Backend Endpoints")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "llm:",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  gpu:",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    endpoint: http://localhost:11434/api/generate",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    model: mistral:7b-instruct",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  cpu:",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    endpoint: http://localhost:8080/completion",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    model: mistral-7b-instruct-q4",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Embedding Configuration")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "embedding:",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  endpoint: http://localhost:11434/api/embeddings",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  model: nomic-embed-text",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Environment Variables")],
                spacing: { before: 480 }
            }),
            new Paragraph({
                text: "Override configuration using environment variables in docker-compose.yml:",
                spacing: { after: 180 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("LLM_GPU_ENDPOINT - GPU inference endpoint")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("LLM_CPU_ENDPOINT - CPU inference endpoint")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("EMBEDDING_ENDPOINT - Embedding model endpoint")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("SPRING_PROFILES_ACTIVE - Active Spring profile")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("7. API Reference")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("List Tools Endpoint")]
            }),
            new Paragraph({
                text: "Returns all available tools and their schemas.",
                spacing: { after: 180 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "POST /mcp/list-tools",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "Content-Type: application/json",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Call Tool Endpoint")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "Execute a specific tool with arguments.",
                spacing: { after: 180 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "POST /mcp/call-tool",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "Content-Type: application/json",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Available Tools")],
                spacing: { before: 360 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("1. local_llm")]
            }),
            new Paragraph({
                text: "General-purpose text generation using local LLM.",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "prompt", bold: true }), new TextRun(" (string, required): Input prompt")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "max_tokens", bold: true }), new TextRun(" (number, default: 512): Maximum tokens to generate")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "temperature", bold: true }), new TextRun(" (number, default: 0.2): Sampling temperature (0-1)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "model", bold: true }), new TextRun(" (string, default: auto): Backend selection (auto/gpu/cpu)")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("2. summarizer")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "Summarize long text efficiently.",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "text", bold: true }), new TextRun(" (string, required): Text to summarize")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "max_length", bold: true }), new TextRun(" (number, default: 200): Maximum summary length in words")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("3. embedding")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "Generate embeddings for semantic search and similarity.",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "text", bold: true }), new TextRun(" (string or array, required): Text(s) to embed")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("4. code_analysis")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "Analyze code for issues and suggestions.",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "code", bold: true }), new TextRun(" (string, required): Code to analyze")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "language", bold: true }), new TextRun(" (string, default: auto): Programming language")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("5. translation")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "Translate text between languages.",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "text", bold: true }), new TextRun(" (string, required): Text to translate")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "source_language", bold: true }), new TextRun(" (string, default: auto): Source language")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun({ text: "target_language", bold: true }), new TextRun(" (string, required): Target language")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("8. Usage Examples")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Example 1: Simple Text Generation")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: 'curl -X POST http://localhost:8000/mcp/call-tool \\',
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '  -H "Content-Type: application/json" \\',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  -d '{",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '    "name": "local_llm",',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '    "arguments": {',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '      "prompt": "Explain quantum computing in simple terms",',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '      "max_tokens": 200',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    }",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  }'",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Example 2: Document Summarization")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: 'curl -X POST http://localhost:8000/mcp/call-tool \\',
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '  -H "Content-Type: application/json" \\',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  -d '{",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '    "name": "summarizer",',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '    "arguments": {',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '      "text": "Your long document here...",',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '      "max_length": 150',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    }",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  }'",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Example 3: Batch Embeddings")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: 'curl -X POST http://localhost:8000/mcp/call-tool \\',
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '  -H "Content-Type: application/json" \\',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  -d '{",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '    "name": "embedding",',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '    "arguments": {',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: '      "text": ["First document", "Second document", "Third document"]',
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    }",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "  }'",
                spacing: { after: 240 }
            }),

            new Paragraph({ children: [new PageBreak()] }),

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("9. Monitoring & Troubleshooting")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Viewing Logs")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "# All services",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "docker-compose logs -f",
                spacing: { after: 120 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "# Specific service",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "docker-compose logs -f mcp-server",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Resource Monitoring")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "# GPU utilization",
                spacing: { before: 120, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "nvidia-smi -l 1",
                spacing: { after: 120 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "# Container stats",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "docker stats",
                spacing: { after: 240 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Common Issues")],
                spacing: { before: 360 }
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Issue: GPU not detected")]
            }),
            new Paragraph({
                text: "Solution:",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Verify nvidia-smi works on host")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Check nvidia-docker installation")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Restart Docker service")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Issue: Models not loading")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "Solution:",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Check disk space (df -h)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Verify model files in volumes")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Re-run setup-models.sh script")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_3,
                children: [new TextRun("Issue: Slow inference")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                text: "Solution:",
                spacing: { after: 120 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Check GPU/CPU utilization")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Reduce thread count for CPU backend")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Use smaller quantization (Q4 instead of Q5)")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("10. Best Practices")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Performance Optimization")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Use GPU for prompts under 2000 tokens")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Batch similar requests when possible")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Keep models loaded by sending periodic health checks")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Monitor VRAM usage to avoid OOM errors")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Security Recommendations")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Run behind a reverse proxy (nginx/traefik)")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Implement API key authentication for production")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Use TLS/SSL certificates")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Isolate services on separate networks")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Regular security updates for base images")]
            }),

            new Paragraph({
                heading: HeadingLevel.HEADING_2,
                children: [new TextRun("Maintenance")],
                spacing: { before: 360 }
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Regularly update Docker images")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Monitor disk usage for model storage")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Backup configuration files")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Set up log rotation")]
            }),
            new Paragraph({
                numbering: { reference: "bullets", level: 0 },
                children: [new TextRun("Review and clean old model versions")]
            }),

            new Paragraph({ children: [new PageBreak()] }),

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("Appendix A: File Structure")]
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "lunarlaurus-plugin/",
                spacing: { before: 240, after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "├── docker-compose.yml",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "├── README.md",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "├── docker/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   ├── llama-cpp/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   │   └── Dockerfile",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   ├── prometheus/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   └── grafana/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "├── mcp-server/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   ├── pom.xml",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   ├── Dockerfile",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   └── src/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│       ├── main/java/com/lunarlaurus/mcp/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│       │   ├── MCPServerApplication.java",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│       │   ├── controller/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│       │   ├── service/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│       │   └── model/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│       └── main/resources/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│           └── application.yml",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "├── scripts/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   ├── setup-models.sh",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   ├── start.sh",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "│   └── test-mcp.sh",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "└── models/",
                spacing: { after: 60 }
            }),
            new Paragraph({
                style: "CodeBlock",
                text: "    └── mistral-7b-instruct-q4_K_M.gguf",
                spacing: { after: 240 }
            }),

            new Paragraph({ children: [new PageBreak()] }),

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("Appendix B: Environment Variables")]
            }),

            new Paragraph({
                text: "Complete list of environment variables for customization:",
                spacing: { after: 240 }
            }),

            new Paragraph({
                text: "SERVER_PORT - MCP server port (default: 8000)",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "LLM_GPU_ENDPOINT - GPU inference endpoint",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "LLM_CPU_ENDPOINT - CPU inference endpoint",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "LLM_GPU_MODEL - GPU model name",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "LLM_CPU_MODEL - CPU model name",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "EMBEDDING_ENDPOINT - Embedding service endpoint",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "EMBEDDING_MODEL - Embedding model name",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "SPRING_PROFILES_ACTIVE - Spring Boot profile",
                spacing: { after: 120 }
            }),
            new Paragraph({
                text: "LOGGING_LEVEL - Logging verbosity",
                spacing: { after: 240 }
            }),

            new Paragraph({ children: [new PageBreak()] }),

            new Paragraph({
                heading: HeadingLevel.HEADING_1,
                children: [new TextRun("Conclusion")]
            }),
            new Paragraph({
                text: "The LunarLaurus Local Compute Plugin provides a robust, production-ready solution for integrating your local compute resources with Claude AI. With intelligent routing, multiple specialized tools, and optimized model recommendations, this system maximizes the efficiency of your RTX4000 GPU and Intel 8260 CPU configuration.",
                spacing: { after: 240 }
            }),
            new Paragraph({
                text: "For additional support, updates, or to report issues, refer to the project documentation and your internal Laurus Industries resources.",
                spacing: { after: 240 }
            }),
            new Paragraph({
                text: "© 2025 LunarLaurus, Laurus Industries. All rights reserved.",
                alignment: AlignmentType.CENTER,
                spacing: { before: 480 }
            })
        ]
    }]
});

// Write the document
Packer.toBuffer(doc).then(buffer => {
    fs.writeFileSync("/home/claude/lunarlaurus-plugin/docs/LunarLaurus_Implementation_Guide.docx", buffer);
    console.log("Document created successfully!");
});
