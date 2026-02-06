# Tool Descriptions with Hardware Context

@Tool descriptions include specific hardware details and performance characteristics.

```java
@Tool(name = "local_llm",
      description = "Generate text using a local LLaMA/Mistral model running on rack servers. "
            + "Supports Q&A, content generation, and analysis. Auto-routes between GPU (RTX4000, fast, <2k tokens) "
            + "and CPU (Intel 8260, slower, handles longer context) backends.")
```

- Mention exact hardware (RTX4000, Intel 8260)
- Include performance hints (fast/slower, token limits)
- Describe routing logic (auto-routes between backends)
- List use cases (Q&A, content generation, analysis)

**Why:** Claude needs performance context to choose the right tool. "Generate text" is vague - "Fast GPU for <2k tokens, slower CPU for longer" guides intelligent tool selection.
