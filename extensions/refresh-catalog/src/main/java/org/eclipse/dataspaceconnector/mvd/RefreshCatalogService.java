package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 *
 */
class RefreshCatalogService {
    private final FederatedCacheNodeDirectory nodeDirectory;
    private final Path nodeJsonDir;
    private final String nodeJsonPrefix;
    private final Monitor monitor;
    private final TypeManager typeManager;

    RefreshCatalogService(FederatedCacheNodeDirectory nodeDirectory, Path nodeJsonDir, String nodeJsonPrefix, Monitor monitor, TypeManager typeManager) {
        if (!nodeJsonDir.toFile().isDirectory()) {
            throw new EdcException(nodeJsonDir + " should be a directory");
        }
        this.nodeDirectory = nodeDirectory;
        this.nodeJsonDir = nodeJsonDir;
        this.nodeJsonPrefix = nodeJsonPrefix;
        this.monitor = monitor;
        this.typeManager = typeManager;
    }

    void saveNodeEntries() {
        try {
            monitor.info("Refreshing catalog");
            var files = Files.find(nodeJsonDir, 1,
                    (path, attrs) -> path.toFile().getName().startsWith(nodeJsonPrefix));
            List<FederatedCacheNode> existingNodes = nodeDirectory.getAll();
            files
                    .map(this::parseFederatedCacheNode)
                    .filter(n -> existingNodes.stream().noneMatch(m -> Objects.equals(m.getName(), n.getName())))
                    .forEach(this::insertNode);
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private FederatedCacheNode parseFederatedCacheNode(Path path) {
        try {
            return typeManager.readValue(Files.readString(path), FederatedCacheNode.class);
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }

    private void insertNode(FederatedCacheNode node) {
        monitor.info("Adding catalog node " + node.getName());
        nodeDirectory.insert(node);
    }
}


