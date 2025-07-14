package org.acme.flipt;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
@QuarkusTestResource(FliptTestResource.class)
public class FliptClientTest {

    @Test
    void testEvaluateFlagEndpoint() {
        given()
                .when().get("/flipt/{id}/evaluate/feature-flag", "e311a066-5982-4b5f-8fe0-d109951941d1")
                .then()
                .statusCode(200)
                .body("enabled", is(true));
        given()
                .when().get("/flipt/{id}/evaluate/feature-flag", UUID.randomUUID().toString())
                .then()
                .statusCode(200)
                .body("enabled", is(false));
    }

}
