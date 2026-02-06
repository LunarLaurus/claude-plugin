# @Value with Defaults

Always provide sensible defaults in @Value annotations. Don't force configuration.

```java
// ✓ Good - works locally, overridable in production
@Value("${llm.gpu.endpoint:http://localhost:11434/api/generate}")
private String gpuEndpoint;

@Value("${llm.gpu.model:mistral:7b}")
private String gpuModel;

// ✗ Avoid - breaks without config
@Value("${llm.gpu.endpoint}")
private String gpuEndpoint;  // Throws exception if property missing
```

**Syntax:**
```java
@Value("${property.name:default-value}")
```

**Why:**
1. **Local dev** - Developers run app without environment setup
2. **Portability** - Same code runs dev/test/prod with different configs
3. **Graceful degradation** - App starts with localhost endpoints, no cryptic startup errors

**When defaults make sense:**
- Localhost URLs (databases, APIs, model servers)
- Common model names (mistral:7b, nomic-embed-text)
- Port numbers (8080, 11434)
- Feature flags (enabled: true/false)

**When to require config:**
- Production credentials (never default passwords!)
- Deployment-specific URLs (production API endpoints)
- Critical security settings
