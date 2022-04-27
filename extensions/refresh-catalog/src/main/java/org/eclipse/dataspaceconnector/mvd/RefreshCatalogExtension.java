package org.eclipse.dataspaceconnector.mvd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
public class RefreshCatalogExtension implements ServiceExtension {
    @Inject
    FederatedCacheNodeDirectory nodeDirectory;

    private Path nodeJsonPath;
    private Monitor monitor;
    private ObjectMapper mapper;
    private ScheduledExecutorService executor;
    private String prefix;

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();
        nodeJsonPath = Path.of(Objects.requireNonNull(System.getenv("NODES_JSON_DIR"), "Env var NODES_JSON_DIR is null"));
        prefix = Objects.requireNonNull(System.getenv("NODES_JSON_FILES_PREFIX"), "Env var NODES_JSON_FILES_PREFIX is null");
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (!nodeJsonPath.toFile().isDirectory()) {
            throw new EdcException(nodeJsonPath + " should be a directory");
        }
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void start() {
        saveNodeEntries();
        executor.scheduleWithFixedDelay(this::saveNodeEntries, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    private void saveNodeEntries() {
        try {
            var files = Files.find(nodeJsonPath, 1,
                    (path, attrs) -> path.toFile().getName().startsWith(prefix));
            monitor.info("Refreshing catalog from " +

            Files.find(nodeJsonPath, 1,
                    (path, attrs) -> path.toFile().getName().startsWith(prefix)).collect(Collectors.toList()).size());
            List<FederatedCacheNode> existingNodes = nodeDirectory.getAll();
            files
                    .map(this::getFileFederatedCacheNodeFunction)
                    .filter(n -> !existingNodes.stream().anyMatch(m -> Objects.equals(m.getName(), n.getName())))
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


