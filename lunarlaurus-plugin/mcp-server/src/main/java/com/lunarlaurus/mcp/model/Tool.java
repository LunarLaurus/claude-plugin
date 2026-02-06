package com.lunarlaurus.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    private String name;
    private String description;
    private Map<String, Object> inputSchema;
}
