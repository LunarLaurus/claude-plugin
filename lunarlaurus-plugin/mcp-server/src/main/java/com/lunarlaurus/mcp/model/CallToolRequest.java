package com.lunarlaurus.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallToolRequest {
    private String name;
    private Map<String, Object> arguments;
}
