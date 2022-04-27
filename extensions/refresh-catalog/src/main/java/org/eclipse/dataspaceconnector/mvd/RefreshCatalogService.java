package org.eclipse.dataspaceconnector.mvd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
class RefreshCatalogService {
    private final FederatedCacheNodeDirectory nodeDirectory;
    private final Path nodeJsonDir;
    private final String nodeJsonPrefix;
    private final Monitor monitor;
    private final ScheduledExecutorService executor;
    private final ObjectMapper mapper;

    RefreshCatalogService(FederatedCacheNodeDirectory nodeDirectory, Path nodeJsonDir, String nodeJsonPrefix, Monitor monitor) {
        if (!nodeJsonDir.toFile().isDirectory()) {
            throw new EdcException(nodeJsonDir + " should be a directory");
        }
        this.nodeDirectory = nodeDirectory;
        this.nodeJsonDir = nodeJsonDir;
        this.nodeJsonPrefix = nodeJsonPrefix;
        this.monitor = monitor;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    void start() {
        saveNodeEntries();
        executor.scheduleWithFixedDelay(this::saveNodeEntries, 5, 5, TimeUnit.SECONDS);
    }

    void shutdown() {
        executor.shutdown();
    }

    private void saveNodeEntries() {
        try {
            monitor.info("Refreshing catalog");
            var files = Files.find(nodeJsonDir, 1,
                    (path, attrs) -> path.toFile().getName().startsWith(nodeJsonPrefix));
            List<FederatedCacheNode> existingNodes = nodeDirectory.getAll();
            files
                    .map(this::getFileFederatedCacheNodeFunction)
                    .filter(n -> existingNodes.stream().noneMatch(m -> Objects.equals(m.getName(), n.getName())))
                    .forEach(this::insertNode);
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private FederatedCacheNode getFileFederatedCacheNodeFunction(Path file) {
        try {
            return mapper.readValue(file.toFile(), FederatedCacheNode.class);
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private void insertNode(FederatedCacheNode node) {
        monitor.info("Adding catalog node " + node.getName());
        nodeDirectory.insert(node);
    }
}


