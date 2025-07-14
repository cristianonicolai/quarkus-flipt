package org.acme.flipt;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.Map;

public class FliptTestResource implements QuarkusTestResourceLifecycleManager {

    private GenericContainer<?> fliptContainer;

    @Override
    public Map<String, String> start() {
        String configPath = Paths.get("src/test/resources/features.yml").toAbsolutePath().toString();

        fliptContainer = new GenericContainer<>("flipt/flipt:latest")
                .withEnv("FLIPT_LOG_LEVEL", "DEBUG")
                .withEnv("DO_NOT_TRACK", "TRUE")
                .withEnv("FLIPT_META_CHECK_FOR_UPDATES", "FALSE")
                .withEnv("FLIPT_STORAGE_TYPE", "local")
                .withEnv("FLIPT_STORAGE_LOCAL_PATH", "/data")
                .withExposedPorts(8080)
//                .withEnv("FLIPT_SERVER_PROTOCOL", "https")
//                .withEnv("FLIPT_SERVER_HTTPS_PORT", "8443")
//                .withEnv("FLIPT_SERVER_CERT_FILE", "https")
//                .withEnv("FLIPT_SERVER_CERT_KEY", "https")
//                .withExposedPorts(8443)
                .withCopyFileToContainer(MountableFile.forHostPath(configPath), "/data/features.yml")
//                .withCommand("flipt", "-c", "/etc/flipt/config/flipt.yml")
                .waitingFor(Wait.forHttp("/health"));

        fliptContainer.start();

        String fliptUrl = String.format("http://%s:%d", fliptContainer.getHost(), fliptContainer.getMappedPort(8080));

        System.out.println("Flipt container started at: " + fliptUrl);

        return Map.of("flipt.url", fliptUrl);
    }

    @Override
    public void stop() {
        if (fliptContainer != null) {
            fliptContainer.stop();
        }
    }

}
