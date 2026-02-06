# Tool DTO Patterns

MCP tools can return Lombok DTOs - Spring AI serializes them to JSON automatically.

```java
@Tool(name = "analyze_code")
public CodeAnalysisResult analyzeCode(
        @Valid @ToolParam(description = "Code to analyze") CodeInput input) {

    // Spring validates @Valid DTOs automatically
    return codeAnalysisService.analyze(input);
}

@Data
public class CodeInput {
    @NotBlank String code;
    String language = "auto";
}

@Data
public class CodeAnalysisResult {
    List<Issue> issues;
    int linesAnalyzed;
    String summary;
}
```

**Validation layers:**
1. **@Valid on parameters** - Spring validates DTO fields (requires spring-boot-starter-validation)
2. **Field-level validation** - Explicit checks before service calls
3. **Service-level validation** - Business rule validation in service layer

**When to use DTOs vs String:**
- Simple text I/O → String is fine
- Structured input/output → Use DTOs for type safety, validation, IDE support
- Microservice integration → DTOs make REST/RPC interop easier

**Why:** DTOs provide type safety, automatic validation, better tooling support, and easier evolution of tool interfaces.
