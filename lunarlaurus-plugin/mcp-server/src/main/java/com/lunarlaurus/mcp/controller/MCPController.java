package com.lunarlaurus.mcp.controller;

import com.lunarlaurus.mcp.model.*;
import com.lunarlaurus.mcp.service.MCPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * MCP Protocol Controller
 * 
 * Implements the Model Context Protocol endpoints:
 * - POST /mcp/list-tools: Returns available tools
 * - POST /mcp/call-tool: Executes a tool with given arguments
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class MCPController {

    private final MCPService mcpService;

    /**
     * List all available tools
     */
    @PostMapping("/list-tools")
    public ResponseEntity<ListToolsResponse> listTools(@RequestBody(required = false) ListToolsRequest request) {
        log.info("Listing available tools");
        ListToolsResponse response = mcpService.listTools();
        return ResponseEntity.ok(response);
    }

    /**
     * Execute a tool with given arguments
     */
    @PostMapping("/call-tool")
    public ResponseEntity<CallToolResponse> callTool(@RequestBody CallToolRequest request) {
        log.info("Calling tool: {} with arguments: {}", request.getName(), request.getArguments());
        
        try {
            CallToolResponse response = mcpService.callTool(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calling tool: {}", request.getName(), e);
            return ResponseEntity.ok(CallToolResponse.error(e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MCP Server is running");
    }
}
