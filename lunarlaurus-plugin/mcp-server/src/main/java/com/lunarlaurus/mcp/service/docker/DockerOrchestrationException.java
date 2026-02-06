package com.lunarlaurus.mcp.service.docker;

/**
 * Docker Orchestration Exception
 *
 * Custom exception for Docker operations failures.
 * Wraps docker-java SDK exceptions with descriptive error messages.
 */
public class DockerOrchestrationException extends RuntimeException {

    public DockerOrchestrationException(String message) {
        super(message);
    }

    public DockerOrchestrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
