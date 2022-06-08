package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.registration.client.ApiClient;
import org.eclipse.dataspaceconnector.registration.client.ApiClientFactory;
import org.eclipse.dataspaceconnector.registration.client.api.RegistryApi;
import org.eclipse.dataspaceconnector.registration.client.models.Participant;

import java.util.List;
import java.util.stream.Collectors;

public class RegistrationServiceNodeDirectory implements FederatedCacheNodeDirectory {

    static final String API_URL = "http://localhost:8181/api";

    ApiClient apiClient = ApiClientFactory.createApiClient(API_URL);
    RegistryApi api = new RegistryApi(apiClient);

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
