package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Federated cache directory using Registration Service as backend.
 */
public class RegistrationServiceNodeDirectory implements FederatedCacheNodeDirectory {

    private final RegistryApi api;

    /**
     * Constructs {@link RegistrationServiceNodeDirectory}
     *
     * @param api RegistrationService API client.
     */
    public RegistrationServiceNodeDirectory(RegistryApi api) {
        this.api = api;
    }

    @Override
    public List<FederatedCacheNode> getAll() {
        return api.listParticipants().stream().map(this::map).collect(Collectors.toList());
    }

    private FederatedCacheNode map(Participant participant) {
        return new FederatedCacheNode(participant.getName(), participant.getUrl(), participant.getSupportedProtocols());
    }

    @Override
    public void insert(FederatedCacheNode federatedCacheNode) {
        throw new UnsupportedOperationException();
    }
}
