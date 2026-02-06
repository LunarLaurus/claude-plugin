package com.lunarlaurus.mcp.service.health;

import com.lunarlaurus.mcp.model.HealthStatus;
import com.lunarlaurus.mcp.model.ModelBackendEntry;
import com.lunarlaurus.mcp.service.docker.DockerOrchestrationService;
import com.lunarlaurus.mcp.service.registry.ModelRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Model Health Checker
 *
 * Background scheduled task that monitors the health of all registered model backends.
 * Periodically checks container health via Docker and updates registry accordingly.
 *
 * Configuration:
 * - registry.health-check.enabled: Enable/disable health checking (default: true)
 * - registry.health-check.interval: Check interval in milliseconds (default: 30000)
 * - registry.health-check.initial-delay: Initial delay before first check (default: 10000)
 *
 * Health checking:
 * - Queries Docker to verify container is running
 * - Updates registry when health status changes
 * - Logs health transitions (STARTING → HEALTHY, HEALTHY → UNHEALTHY, etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "registry.health-check.enabled", havingValue = "true", matchIfMissing = true)
public class ModelHealthChecker {

    private final ModelRegistryService registryService;
    private final DockerOrchestrationService dockerService;

    /**
     * Scheduled health check for all registered models
     * Runs with fixed delay (waits for previous execution to complete before starting next)
     */
    @Scheduled(
            fixedDelayString = "${registry.health-check.interval:30000}",
            initialDelayString = "${registry.health-check.initial-delay:10000}"
    )
    public void checkAllModels() {
        List<ModelBackendEntry> models = registryService.listAll();

        if (models.isEmpty()) {
            log.debug("No models registered, skipping health check");
            return;
        }

        log.debug("Running health check for {} registered models", models.size());
        int healthyCount = 0;
        int unhealthyCount = 0;

        for (ModelBackendEntry model : models) {
            try {
                boolean healthy = dockerService.isContainerHealthy(model.getContainerId());
                HealthStatus newStatus = healthy ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY;

                // Update registry if status changed
                if (model.getHealth() != newStatus) {
                    log.info("Model {} health changed: {} → {} (container: {})",
                            model.getModelName(),
                            model.getHealth(),
                            newStatus,
                            model.getContainerId().substring(0, 12));

                    model.setHealth(newStatus);
                    registryService.register(model);
                }

                if (healthy) {
                    healthyCount++;
                } else {
                    unhealthyCount++;
                }

            } catch (Exception e) {
                log.error("Health check failed for model {} (container: {}): {}",
                        model.getModelName(),
                        model.getContainerId().substring(0, 12),
                        e.getMessage());

                // Mark as unhealthy on error
                if (model.getHealth() != HealthStatus.UNHEALTHY) {
                    model.setHealth(HealthStatus.UNHEALTHY);
                    registryService.register(model);
                }
                unhealthyCount++;
            }
        }

        log.debug("Health check complete: {} healthy, {} unhealthy", healthyCount, unhealthyCount);
    }
}
