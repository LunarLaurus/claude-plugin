package com.lunarlaurus.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * MCP Protocol Model Classes
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Tool {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ListToolsRequest {
    // Currently empty, reserved for future pagination/filtering
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class ListToolsResponse {
    private List<Tool> tools;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class CallToolRequest {
    private String name;
    private Map<String, Object> arguments;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CallToolResponse {
    private List<Content> content;
    private Boolean isError;

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

@Data
@NoArgsConstructor
@AllArgsConstructor
class Content {
    private String type;
    private String text;
}
