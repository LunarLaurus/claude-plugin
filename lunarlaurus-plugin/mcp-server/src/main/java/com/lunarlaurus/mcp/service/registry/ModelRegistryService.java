package com.lunarlaurus.mcp.service.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lunarlaurus.mcp.model.ModelBackendEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Model Registry Service
 *
 * Thread-safe model registry with hybrid persistence (in-memory + YAML file).
 * Manages model backend lifecycle, port allocation, and persistent storage.
 *
 * Key features:
 * - Thread-safe concurrent operations (ConcurrentHashMap)
 * - Automatic YAML file sync on every register/unregister
 * - Port allocation from configurable range (default 11400-11500)
 * - Atomic file writes with backup
 */
@Slf4j
@Service
public class ModelRegistryService {

    @Value("${registry.file:./data/registry.yml}")
    private String registryFilePath;

    @Value("${registry.port-range.start:11400}")
    private int portRangeStart;

    @Value("${registry.port-range.end:11500}")
    private int portRangeEnd;

    private final ConcurrentHashMap<String, ModelBackendEntry> registry = new ConcurrentHashMap<>();
    private final AtomicInteger nextPort;
    private final ObjectMapper yamlMapper;

    public ModelRegistryService() {
        this.nextPort = new AtomicInteger(11400); // Will be updated in @PostConstruct
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new JavaTimeModule());
        this.yamlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void init() {
        // Initialize port counter
        nextPort.set(portRangeStart);

        // Create data directory if needed
        try {
            Path registryPath = Paths.get(registryFilePath);
            Files.createDirectories(registryPath.getParent());
            log.info("Registry data directory ensured: {}", registryPath.getParent());
        } catch (IOException e) {
            log.warn("Failed to create registry data directory: {}", e.getMessage());
        }

        // Load existing registry from file
        loadFromFile();
        log.info("Model registry initialized: {} entries loaded, port range {}-{}",
                registry.size(), portRangeStart, portRangeEnd);
    }

    /**
     * Register a model backend entry
     * Thread-safe operation with automatic file persistence
     *
     * @param entry Model backend entry to register
     */
    public synchronized void register(ModelBackendEntry entry) {
        registry.put(entry.getId(), entry);
        log.info("Registered model backend: id={}, model={}, port={}",
                entry.getId(), entry.getModelName(), entry.getPort());
        saveToFile();
    }

    /**
     * Unregister a model backend entry
     *
     * @param id Model ID to unregister
     */
    public synchronized void unregister(String id) {
        ModelBackendEntry removed = registry.remove(id);
        if (removed != null) {
            log.info("Unregistered model backend: id={}, model={}", id, removed.getModelName());
            saveToFile();
        } else {
            log.warn("Attempted to unregister non-existent model: id={}", id);
        }
    }

    /**
     * Get a model backend entry by ID
     *
     * @param id Model ID
     * @return Optional containing the entry if found
     */
    public Optional<ModelBackendEntry> get(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    /**
     * List all registered model backends
     *
     * @return List of all entries (unmodifiable)
     */
    public List<ModelBackendEntry> listAll() {
        return List.copyOf(registry.values());
    }

    /**
     * Find a model backend entry by model name
     *
     * @param modelName Model name to search for
     * @return Optional containing the first matching entry
     */
    public Optional<ModelBackendEntry> findByModelName(String modelName) {
        return registry.values().stream()
                .filter(entry -> entry.getModelName().equals(modelName))
                .findFirst();
    }

    /**
     * Allocate the next available port from the configured range
     * Thread-safe atomic operation
     *
     * @return Allocated port number
     * @throws PortExhaustionException if no ports available
     */
    public int allocatePort() {
        int port = nextPort.getAndIncrement();

        if (port > portRangeEnd) {
            throw new PortExhaustionException(
                    String.format("Port pool exhausted! Range %d-%d is fully allocated. " +
                            "Consider expanding registry.port-range in configuration.",
                            portRangeStart, portRangeEnd));
        }

        log.debug("Allocated port: {}", port);
        return port;
    }

    /**
     * Deallocate a port (currently no-op, ports are not recycled)
     * Future enhancement: track freed ports for reuse
     *
     * @param port Port to deallocate
     */
    public void deallocatePort(int port) {
        // Current implementation: ports are not recycled
        // Future: maintain a TreeSet of available ports for reuse
        log.debug("Port {} deallocated (not recycled in current implementation)", port);
    }

    /**
     * Save registry to YAML file
     * Atomic write with backup creation
     */
    synchronized void saveToFile() {
        try {
            Path registryPath = Paths.get(registryFilePath);
            Path backupPath = Paths.get(registryFilePath + ".bak");

            // Create backup of existing file
            if (Files.exists(registryPath)) {
                Files.copy(registryPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Write registry to temp file, then atomic move
            Path tempPath = Files.createTempFile("registry", ".yml");
            RegistrySnapshot snapshot = new RegistrySnapshot(new ArrayList<>(registry.values()));
            yamlMapper.writeValue(tempPath.toFile(), snapshot);
            Files.move(tempPath, registryPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            log.debug("Registry saved to file: {} ({} entries)", registryFilePath, registry.size());
        } catch (IOException e) {
            log.error("Failed to save registry to file: {}", e.getMessage());
            // Don't throw - in-memory registry is still valid
        }
    }

    /**
     * Load registry from YAML file
     * Called on startup to restore state
     */
    void loadFromFile() {
        try {
            File registryFile = new File(registryFilePath);
            if (!registryFile.exists()) {
                log.info("Registry file not found, starting with empty registry: {}", registryFilePath);
                return;
            }

            RegistrySnapshot snapshot = yamlMapper.readValue(registryFile, RegistrySnapshot.class);
            if (snapshot != null && snapshot.getEntries() != null) {
                snapshot.getEntries().forEach(entry -> registry.put(entry.getId(), entry));

                // Update port counter to highest allocated port + 1
                int maxPort = snapshot.getEntries().stream()
                        .mapToInt(ModelBackendEntry::getPort)
                        .max()
                        .orElse(portRangeStart - 1);
                nextPort.set(Math.max(maxPort + 1, portRangeStart));

                log.info("Registry loaded from file: {} entries, next port: {}",
                        registry.size(), nextPort.get());
            }
        } catch (IOException e) {
            log.error("Failed to load registry from file: {}", e.getMessage());
            // Start with empty registry
        }
    }

    /**
     * YAML serialization wrapper
     */
    private static class RegistrySnapshot {
        private List<ModelBackendEntry> entries;

        public RegistrySnapshot() {
        }

        public RegistrySnapshot(List<ModelBackendEntry> entries) {
            this.entries = entries;
        }

        public List<ModelBackendEntry> getEntries() {
            return entries;
        }

        public void setEntries(List<ModelBackendEntry> entries) {
            this.entries = entries;
        }
    }
}
