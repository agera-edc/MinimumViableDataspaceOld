package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.system.Inject;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
public class RefreshCatalogExtension implements ServiceExtension {
    @Inject
    FederatedCacheNodeDirectory nodeDirectory;

    private RefreshCatalogService service;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        var nodeJsonPath = Path.of(Objects.requireNonNull(System.getenv("NODES_JSON_DIR"), "Env var NODES_JSON_DIR is null"));
        var nodeJsonPrefix = Objects.requireNonNull(System.getenv("NODES_JSON_FILES_PREFIX"), "Env var NODES_JSON_FILES_PREFIX is null");
        service = new RefreshCatalogService(nodeDirectory, nodeJsonPath, nodeJsonPrefix, monitor);
    }

    @Override
    public void start() {
        service.start();
    }

    @Override
    public void shutdown() {
        service.shutdown();
    }
}


