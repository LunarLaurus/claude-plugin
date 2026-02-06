# Lombok DTO Annotation Stack

DTOs with fields use the full Lombok stack: @Data + @Builder + @NoArgsConstructor + @AllArgsConstructor

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallToolRequest {
    @NotBlank
    private String name;

    @NotNull
    private Map<String, Object> arguments;
}
```

**Why each annotation:**
- **@Data** → getters, setters, toString, equals, hashCode
- **@Builder** → Fluent construction: CallToolRequest.builder().name("foo").build()
- **@NoArgsConstructor** → Required by Jackson (JSON deserialization), Spring Data JPA
- **@AllArgsConstructor** → Convenience for tests, manual construction

**Validation:**
Add javax.validation annotations on fields:
```java
@NotBlank String name;           // Required, non-empty string
@NotNull Map<String, Object> x;  // Required, can be empty map
@Min(1) @Max(1000) Integer max;  // Range validation
```

Requires: spring-boot-starter-validation in pom.xml

**Exception: Empty classes**
Classes with NO fields use @Data ONLY:
```java
@Data
public class ListToolsRequest {
    // Empty - reserved for future fields
}
```
Reason: @NoArgsConstructor + @Data both generate no-arg constructor → compilation error
