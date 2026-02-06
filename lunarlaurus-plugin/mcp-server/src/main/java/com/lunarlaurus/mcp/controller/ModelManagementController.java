package com.lunarlaurus.mcp.controller;

import com.lunarlaurus.mcp.model.ModelBackendResponse;
import com.lunarlaurus.mcp.model.ModelCreateRequest;
import com.lunarlaurus.mcp.service.management.ModelManagementService;
import com.lunarlaurus.mcp.service.management.ModelNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Model Management Controller
 *
 * REST API for dynamic LLM backend management.
 * Follows service-delegation standard - zero business logic, all operations delegated to ModelManagementService.
 *
 * Endpoints:
 * - POST   /api/models       - Create new model backend
 * - DELETE /api/models/{id}  - Delete model backend
 * - GET    /api/models       - List all model backends
 * - GET    /api/models/{id}  - Get specific model backend
 */
@Slf4j
@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class ModelManagementController {

    private final ModelManagementService modelManagementService;

    /**
     * Create a new model backend
     *
     * @param request Model creation request (validated)
     * @return 201 Created with Location header and model response
     */
    @PostMapping
    public ResponseEntity<ModelBackendResponse> createModel(@Valid @RequestBody ModelCreateRequest request) {
        log.info("POST /api/models - Creating model: {}", request.getModelName());

        ModelBackendResponse response = modelManagementService.createModel(request);

        // Build Location header with model ID
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Delete a model backend
     *
     * @param id Model ID to delete
     * @return 204 No Content on success, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable String id) {
        log.info("DELETE /api/models/{} - Deleting model", id);

        modelManagementService.deleteModel(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * List all registered model backends
     *
     * @return 200 OK with list of models
     */
    @GetMapping
    public ResponseEntity<List<ModelBackendResponse>> listModels() {
        log.debug("GET /api/models - Listing all models");

        List<ModelBackendResponse> models = modelManagementService.listModels();

        return ResponseEntity.ok(models);
    }

    /**
     * Get a specific model backend by ID
     *
     * @param id Model ID
     * @return 200 OK with model details, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ModelBackendResponse> getModel(@PathVariable String id) {
        log.debug("GET /api/models/{} - Getting model", id);

        ModelBackendResponse response = modelManagementService.getModel(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Exception handler for ModelNotFoundException
     * Returns 404 Not Found with error message
     */
    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleModelNotFound(ModelNotFoundException e) {
        log.warn("Model not found: {}", e.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Exception handler for validation errors
     * Returns 400 Bad Request
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        log.warn("Validation error: {}", message);
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Exception handler for generic errors
     * Returns 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception e) {
        log.error("Unexpected error in model management: {}", e.getMessage(), e);
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error: " + e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Error response DTO
     */
    public record ErrorResponse(int status, String message) {
    }
}
