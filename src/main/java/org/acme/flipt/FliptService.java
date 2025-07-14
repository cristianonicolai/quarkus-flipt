package org.acme.flipt;

import io.flipt.client.FliptClient;
import io.flipt.client.FliptException;
import io.flipt.client.models.BooleanEvaluationResponse;
import io.flipt.client.models.TlsConfig;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class FliptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FliptService.class);

    @ConfigProperty(name = "flipt.url")
    String fliptUrl;

    @ConfigProperty(name = "flipt.ssl.ca-path")
    Optional<String> certFile;

    private FliptClient fliptClient;

    public void start(@Observes StartupEvent event) {
        try {
            LOGGER.info("Connecting to Flipt service using url: {}", fliptUrl);
            TlsConfig tlsConfig = null;
            if (fliptUrl.startsWith("https://")) {
                if (certFile.isPresent()) {
                    tlsConfig = TlsConfig.builder().caCertFile(certFile.get()).insecureSkipHostnameVerify(true).build();
                } else {
                    LOGGER.warn("Using HTTPS connection to Flipt but no certificate file provided");
                    tlsConfig = TlsConfig.builder().insecureSkipVerify(true).build();
                }
            }
            this.fliptClient = FliptClient.builder()
                    .url(fliptUrl)
                    .tlsConfig(tlsConfig)
                    .build();
        } catch (FliptException e) {
            LOGGER.error("Failed to initialize Flipt client", e);
            throw new RuntimeException(e);
        }
    }

    public void stop(@Observes ShutdownEvent event) {
        if (fliptClient != null) {
            fliptClient.close();
        }
    }

    public boolean evaluateBooleanFlag(String flagKey, String entityId) {
        try {
            BooleanEvaluationResponse response = fliptClient.evaluateBoolean(flagKey, entityId, Map.of());
            return response.isEnabled();
        } catch (FliptException.EvaluationException e) {
            e.printStackTrace();
            LOGGER.error("Failed to evaluate feature flag", e);
            throw new RuntimeException(e);
        }
    }

}
