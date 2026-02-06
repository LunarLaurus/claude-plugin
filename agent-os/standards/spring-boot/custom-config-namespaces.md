# Custom Config Namespaces

Use top-level custom namespaces for domain config. Don't nest everything under spring.*

```yaml
# ✓ Good - domain-specific namespaces
llm:
  gpu:
    endpoint: http://localhost:11434/api/generate
    model: mistral:7b-instruct
  cpu:
    endpoint: http://localhost:8080/completion
    model: mistral-7b-instruct-q4

embedding:
  endpoint: http://localhost:11434/api/embeddings
  model: nomic-embed-text

# ✗ Avoid - cramming everything under spring
spring:
  llm:
    gpu:
      endpoint: http://localhost:11434/api/generate
```

**Inject with @Value:**
```java
@Value("${llm.gpu.endpoint}")
private String gpuEndpoint;
```

**Why:**
1. **Domain clarity** - llm: and embedding: are domain concepts, not Spring framework concerns
2. **Avoid collisions** - Spring Boot may add spring.llm.* in future releases
3. **Readability** - Shorter property paths, cleaner config files

**When to use spring.* vs custom:**
- Spring framework behavior → spring.*
- Spring Boot autoconfiguration → spring.*
- Your domain models/services → Custom top-level namespace
