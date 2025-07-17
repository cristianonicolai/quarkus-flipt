package org.acme.flipt;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.Map;

public class FliptTestResource implements QuarkusTestResourceLifecycleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FliptTestResource.class);

    private GenericContainer<?> fliptContainer;

    @Override
    public Map<String, String> start() {
        String configPath = Paths.get("src/test/resources/features.yml").toAbsolutePath().toString();
        String certKey = Paths.get("src/test/resources/server.key").toAbsolutePath().toString();
        String certCa = Paths.get("src/test/resources/server.crt").toAbsolutePath().toString();

        fliptContainer = new GenericContainer<>("flipt/flipt:latest")
                .withEnv("FLIPT_LOG_LEVEL", "DEBUG")
                .withEnv("DO_NOT_TRACK", "TRUE")
                .withEnv("FLIPT_META_CHECK_FOR_UPDATES", "FALSE")
                .withEnv("FLIPT_STORAGE_TYPE", "local")
                .withEnv("FLIPT_STORAGE_LOCAL_PATH", "/data")
//                .withExposedPorts(8080)
                .withEnv("FLIPT_SERVER_PROTOCOL", "https")
                .withEnv("FLIPT_SERVER_HTTPS_PORT", "8443")
                .withEnv("FLIPT_SERVER_CERT_FILE", "/certs/server.crt")
                .withEnv("FLIPT_SERVER_CERT_KEY", "/certs/server.key")
                .withExposedPorts(8443)
                .withCopyFileToContainer(MountableFile.forHostPath(configPath), "/data/features.yml")
                .withCopyFileToContainer(MountableFile.forHostPath(certCa), "/certs/server.crt")
                .withCopyFileToContainer(MountableFile.forHostPath(certKey), "/certs/server.key")
                .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .waitingFor(Wait.forHttps("/health").allowInsecure());

        fliptContainer.start();

        String fliptUrl = String.format("https://%s:%d", fliptContainer.getHost(), fliptContainer.getMappedPort(8443));

        LOGGER.info("Flipt container started at: {}", fliptUrl);

        return Map.of("flipt.url", fliptUrl, "flipt.ssl.ca-path", certCa);
    }

    @Override
    public void stop() {
        if (fliptContainer != null) {
            fliptContainer.stop();
        }
    }

}
