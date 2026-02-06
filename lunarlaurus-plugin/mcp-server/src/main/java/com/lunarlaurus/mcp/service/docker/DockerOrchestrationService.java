package com.lunarlaurus.mcp.service.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Docker Orchestration Service
 *
 * Service layer abstraction for Docker container lifecycle management using docker-java SDK.
 * Handles container creation, lifecycle, image pulling, and health checking.
 *
 * This service wraps the docker-java SDK to provide:
 * - Simplified container lifecycle operations
 * - Proper error handling with descriptive messages
 * - Environment-aware configuration (Unix socket vs Windows tcp)
 */
@Slf4j
@Service
public class DockerOrchestrationService {

    @Value("${docker.host:unix:///var/run/docker.sock}")
    private String dockerHost;

    @Value("${docker.image-pull-timeout:300000}")
    private long imagePullTimeoutMs;

    private DockerClient dockerClient;

    @PostConstruct
    public void init() {
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost(dockerHost)
                    .build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .build();

            this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
            log.info("Docker client initialized with host: {}", dockerHost);

            // Verify Docker daemon is accessible
            dockerClient.pingCmd().exec();
            log.info("Docker daemon connectivity verified");
        } catch (Exception e) {
            log.error("Failed to initialize Docker client: {}", e.getMessage());
            throw new DockerOrchestrationException(
                    "Failed to connect to Docker daemon at " + dockerHost + ". Is Docker running?", e);
        }
    }

    /**
     * Pull Docker image if not present locally
     *
     * @param imageName Full image name with tag (e.g., "ollama/ollama:latest")
     * @throws DockerOrchestrationException if pull fails or times out
     */
    public void pullImageIfMissing(String imageName) {
        try {
            // Check if image exists locally
            try {
                dockerClient.inspectImageCmd(imageName).exec();
                log.debug("Image {} already exists locally, skipping pull", imageName);
                return;
            } catch (NotFoundException e) {
                log.info("Image {} not found locally, pulling from registry", imageName);
            }

            // Pull image with timeout
            boolean completed = dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback())
                    .awaitCompletion(imagePullTimeoutMs, TimeUnit.MILLISECONDS);

            if (!completed) {
                throw new DockerOrchestrationException(
                        "Image pull timed out after " + imagePullTimeoutMs + "ms for image: " + imageName);
            }

            log.info("Successfully pulled image: {}", imageName);
        } catch (DockerException e) {
            throw new DockerOrchestrationException("Failed to pull image " + imageName + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DockerOrchestrationException("Image pull interrupted for " + imageName, e);
        }
    }

    /**
     * Create a Docker container with specified configuration
     *
     * @param imageName     Docker image name
     * @param containerName Unique container name
     * @param portMappings  Map of host port to container port (e.g., {11401: 11434})
     * @param env           Environment variables for the container
     * @return Container ID
     * @throws DockerOrchestrationException if container creation fails
     */
    public String createContainer(String imageName, String containerName,
                                   Map<Integer, Integer> portMappings, Map<String, String> env) {
        try {
            log.info("Creating container: name={}, image={}, ports={}", containerName, imageName, portMappings);

            // Build port bindings
            ExposedPort[] exposedPorts = portMappings.values().stream()
                    .map(ExposedPort::tcp)
                    .toArray(ExposedPort[]::new);

            Ports portBindings = new Ports();
            portMappings.forEach((hostPort, containerPort) ->
                    portBindings.bind(ExposedPort.tcp(containerPort), Ports.Binding.bindPort(hostPort))
            );

            // Build environment variables
            String[] envArray = null;
            if (env != null && !env.isEmpty()) {
                envArray = env.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .toArray(String[]::new);
            }

            CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                    .withName(containerName)
                    .withExposedPorts(exposedPorts)
                    .withHostConfig(HostConfig.newHostConfig()
                            .withPortBindings(portBindings))
                    .withEnv(envArray)
                    .exec();

            log.info("Container created successfully: id={}, name={}", container.getId(), containerName);
            return container.getId();
        } catch (DockerException e) {
            throw new DockerOrchestrationException(
                    "Failed to create container " + containerName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Start a Docker container
     *
     * @param containerId Container ID to start
     * @throws DockerOrchestrationException if start fails
     */
    public void startContainer(String containerId) {
        try {
            log.info("Starting container: {}", containerId);
            dockerClient.startContainerCmd(containerId).exec();
            log.info("Container started successfully: {}", containerId);
        } catch (DockerException e) {
            throw new DockerOrchestrationException(
                    "Failed to start container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Stop a Docker container
     *
     * @param containerId Container ID to stop
     * @throws DockerOrchestrationException if stop fails
     */
    public void stopContainer(String containerId) {
        try {
            log.info("Stopping container: {}", containerId);
            dockerClient.stopContainerCmd(containerId)
                    .withTimeout(10) // 10 second graceful shutdown
                    .exec();
            log.info("Container stopped successfully: {}", containerId);
        } catch (DockerException e) {
            throw new DockerOrchestrationException(
                    "Failed to stop container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Remove a Docker container
     *
     * @param containerId Container ID to remove
     * @throws DockerOrchestrationException if removal fails
     */
    public void removeContainer(String containerId) {
        try {
            log.info("Removing container: {}", containerId);
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true) // Force removal even if running
                    .exec();
            log.info("Container removed successfully: {}", containerId);
        } catch (DockerException e) {
            throw new DockerOrchestrationException(
                    "Failed to remove container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Check if a container is healthy (running and port bound)
     *
     * @param containerId Container ID to check
     * @return true if container is running and healthy
     */
    public boolean isContainerHealthy(String containerId) {
        try {
            InspectContainerResponse container = dockerClient.inspectContainerCmd(containerId).exec();

            Boolean running = container.getState().getRunning();
            if (running == null || !running) {
                log.debug("Container {} is not running", containerId);
                return false;
            }

            // Container is running
            log.debug("Container {} is healthy (running)", containerId);
            return true;
        } catch (NotFoundException e) {
            log.warn("Container {} not found during health check", containerId);
            return false;
        } catch (DockerException e) {
            log.error("Health check failed for container {}: {}", containerId, e.getMessage());
            return false;
        }
    }

    /**
     * Get container logs
     *
     * @param containerId Container ID
     * @param tailLines   Number of lines to retrieve from end of logs
     * @return Container logs as string
     */
    public String getContainerLogs(String containerId, int tailLines) {
        try {
            log.debug("Retrieving last {} lines of logs for container {}", tailLines, containerId);

            // Note: docker-java's log streaming is complex, simplified version here
            // For production, consider using LogContainerResultCallback for streaming
            String logs = dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withTail(tailLines)
                    .exec(new LogToStringCallback())
                    .toString();

            return logs;
        } catch (DockerException e) {
            throw new DockerOrchestrationException(
                    "Failed to retrieve logs for container " + containerId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Simple callback to convert log frames to string
     */
    private static class LogToStringCallback extends com.github.dockerjava.api.async.ResultCallbackTemplate<
            com.github.dockerjava.api.async.ResultCallbackTemplate<LogToStringCallback, com.github.dockerjava.api.model.Frame>,
            com.github.dockerjava.api.model.Frame> {

        private final StringBuilder log = new StringBuilder();

        @Override
        public void onNext(com.github.dockerjava.api.model.Frame frame) {
            log.append(new String(frame.getPayload()));
        }

        @Override
        public String toString() {
            return log.toString();
        }
    }
}
