package org.eclipse.dataspaceconnector.mvd;

import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNode;
import org.eclipse.dataspaceconnector.catalog.spi.FederatedCacheNodeDirectory;
import org.eclipse.dataspaceconnector.spi.monitor.ConsoleMonitor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshCatalogServiceTest {

    @Test
    void start() throws InterruptedException {
        var directory = mock(FederatedCacheNodeDirectory.class);
        var nodeJsonDir = Path.of(getClass().getClassLoader().getResource("24-node1.json").getPath()).getParent();
        var nodeJsonPrefix = "24-";
        var monitor = new ConsoleMonitor();
        var extension = new RefreshCatalogService(directory, nodeJsonDir, nodeJsonPrefix, monitor);

        when(directory.getAll()).thenReturn(List.of(new FederatedCacheNode("node24-2", "", List.of(""))));

        var latch = new CountDownLatch(2);

        doAnswer(i -> {
            latch.countDown();
            return null;
        }).when(directory).insert(any());

        extension.start();

        assertThat(latch.await(1, TimeUnit.MINUTES)).isTrue();

        verify(directory, times(1)).insert(argThat(n -> "node24-1".equals(n.getName())));
        verify(directory, times(0)).insert(argThat(n -> "node24-2".equals(n.getName())));
        verify(directory, times(1)).insert(argThat(n -> "node24-3".equals(n.getName())));
    }
}