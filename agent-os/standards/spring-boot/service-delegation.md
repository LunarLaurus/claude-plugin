# Service Layer Delegation

MCP tool providers contain ZERO business logic. They immediately delegate to @Service beans.

```java
@Tool(name = "local_llm")
public String localLlm(@ToolParam String prompt) {
    return localLLMService.generate(prompt, 512, 0.2, "auto");
}
```

- Tool provider = thin adapter between MCP protocol and service layer
- All logic lives in services (@Service beans in service.inference package)
- Services can be reused by HTTP controllers, CLI commands, other tool providers
- Tool provider only handles: parameter defaulting, logging entry, calling service

**Why:** Services are called from multiple entry points (MCP tools, HTTP API, future CLI). Logic centralization prevents drift.
