package com.lunarlaurus.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallToolResponse {
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
