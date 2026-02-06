# Tool Naming: Snake_Case

@Tool names use snake_case, NOT camelCase.

```java
@Tool(name = "local_llm")          // ✓ Correct
public String localLlm(...) { }

@Tool(name = "localLlm")           // ✗ Wrong
public String localLlm(...) { }
```

- Tool name ("local_llm") matches MCP protocol convention
- Java method name (localLlm) follows Java convention
- They don't have to match - tool name is the external API contract

**Why:** MCP specification uses snake_case for tool names, aligning with JSON/Python ecosystem conventions. Java methods stay camelCase per language idioms.
