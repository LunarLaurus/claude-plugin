# Stdio Profile Configuration

When running as MCP stdio server, stdout MUST be reserved for JSON-RPC protocol. All logging goes to file.

## Required Configuration

**application-stdio.yml:**
```yaml
spring:
  main:
    web-application-type: none    # Disable web server
    banner-mode: off              # CRITICAL: No banner to stdout
  ai:
    mcp:
      server:
        stdio: true               # Enable stdio transport

logging:
  file:
    name: ./logs/mcp-stdio.log    # All logs to file
  pattern:
    console: ""                   # CRITICAL: Empty console pattern
  level:
    root: INFO
    com.yourapp: DEBUG
```

**Activate profile:**
```bash
java -jar app.jar --spring.profiles.active=stdio
```

## Why This Matters

MCP stdio protocol uses stdin/stdout for communication:
```
Claude Code ←→ stdout/stdin ←→ Java MCP Server
```

Any output to stdout breaks JSON-RPC parsing. Common failures:
- Spring Boot banner → Claude sees malformed JSON
- Log statements → Intermittent parsing errors
- Exception stack traces → Connection drops

## Common Mistakes

❌ **Forgot banner-mode: off**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
{"jsonrpc":"2.0","method":"tools/list"...}
```
Result: Claude Code can't parse the response

❌ **Logging to console**
```java
System.out.println("Debug: " + value);  // Breaks protocol!
```
Use log.debug() instead - goes to file in stdio profile

❌ **Running without stdio profile**
```bash
java -jar app.jar  # Missing --spring.profiles.active=stdio
```
Result: Logs pollute stdout, MCP connection fails

## References

- [MCP Specification - Stdio Transport](https://spec.modelcontextprotocol.io/specification/basic/transports/#stdio)
- [Spring AI MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/reference/features/logging.html)
