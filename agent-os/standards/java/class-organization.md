# Class Organization

## Public Classes in Separate Files

Each public class MUST be in its own .java file matching the class name.

```java
// ✗ COMPILATION ERROR
// File: MCPModels.java
public class Tool { }          // Error: public class Tool should be in Tool.java
public class Content { }       // Error: public class Content should be in Content.java

// ✓ Correct - separate files
// File: Tool.java
public class Tool { }

// File: Content.java
public class Content { }
```

**Why:** Java language requirement. Compiler error:
```
class Tool is public, should be declared in a file named Tool.java
```

**Package-private exception:**
Multiple package-private classes CAN share a file:
```java
// File: InternalModels.java
class Helper { }      // ✓ OK - package-private
class Util { }        // ✓ OK - package-private
```

## Static Factory Methods

Provide static factory methods for common construction patterns.

```java
@Data
@Builder
public class CallToolResponse {
    private List<Content> content;
    private Boolean isError;

    // Static factories for common cases
    public static CallToolResponse success(String text) {
        return CallToolResponse.builder()
            .content(List.of(new Content("text", text)))
            .isError(false)
            .build();
    }

    public static CallToolResponse error(String message) {
        return CallToolResponse.builder()
            .content(List.of(new Content("text", "Error: " + message)))
            .isError(true)
            .build();
    }
}
```

**Usage:**
```java
return CallToolResponse.success("Done!");
return CallToolResponse.error("Something broke");

// vs verbose builder
return CallToolResponse.builder()
    .content(List.of(new Content("text", "Done!")))
    .isError(false)
    .build();
```

**Why:** Readable, self-documenting code. success() and error() are clearer than builder boilerplate.
