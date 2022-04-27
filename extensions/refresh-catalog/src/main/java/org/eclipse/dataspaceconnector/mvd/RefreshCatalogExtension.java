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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RefreshCatalogExtension implements ServiceExtension {
    @Inject
    private FederatedCacheNodeDirectory nodeDirectory;

    private File nodesDirectory;
    private Monitor monitor;
    private ObjectMapper mapper;
    private ScheduledExecutorService executor;

    @Override
    public void initialize(ServiceExtensionContext context) {
        monitor = context.getMonitor();
        nodesDirectory = new File(Objects.requireNonNull(System.getenv("NODES_JSON_DIR"), "Env var NODES_JSON_DIR is null"));
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (!nodesDirectory.isDirectory()) {
            throw new EdcException(nodesDirectory + " should be a directory");
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
        File[] array = nodesDirectory.listFiles();
        monitor.info("Refreshing catalog from " + array.length + " entries");
        List<FederatedCacheNode> existingNodes = nodeDirectory.getAll();
        Arrays.stream(array)
                .map(this::getFileFederatedCacheNodeFunction)
                .filter(n -> !existingNodes.stream().anyMatch(m -> Objects.equals(m.getName(), n.getName())))
                .forEach(this::insertNode);
    }

    private FederatedCacheNode getFileFederatedCacheNodeFunction(File file) {
        try {
            return mapper.readValue(file, FederatedCacheNode.class);
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private void insertNode(FederatedCacheNode node) {
        monitor.info("Adding catalog node " + node.getName());
        nodeDirectory.insert(node);
    }
}


