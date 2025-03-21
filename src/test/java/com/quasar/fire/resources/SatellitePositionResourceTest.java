package com.quasar.fire.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.infinispan.client.Remote;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class SatellitePositionResourceTest {
    private static final Logger LOGGER = Logger.getLogger(PositionResourceTest.class);
    @Inject
    @Remote("SATELLITE_POSITIONS")
    RemoteCache<String, String> cacheSatellites;
    @Inject
    ObjectMapper mapper;
    private static JsonNode request;

    @Test
    void givenTopSecretSplitSatellitKenobiThenSuccesTest() {
        given().contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/topsecret")
                .then()
                .statusCode(200)
                .body("position.x", equalTo(-81.25042f))
                .body("position.y", equalTo(-112.498375f))
                .body("message", equalTo("este es un mensaje secreto"));
    }

    @BeforeEach
    void init() {
        try {
            request = mapper.readValue(new String(Files.readAllBytes(Path.of("src/test/resources/request/RequestTopSecret.json"))), JsonNode.class);
            String sato = new String(Files.readAllBytes(Path.of("src/test/resources/cache/SatoCache.json")));
            String kenobi = new String(Files.readAllBytes(Path.of("src/test/resources/cache/KenobiCache.json")));
            String skywalker = new String(Files.readAllBytes(Path.of("src/test/resources/cache/SkywalkerCache.json")));
            cacheSatellites.clear();
            cacheSatellites.put("sato", sato);
            cacheSatellites.put("kenobi", kenobi);
            cacheSatellites.put("skywalker", skywalker);
        } catch (Exception exception) {
            LOGGER.errorf("Error al cargar los datos de prueba en el cache %s", exception);
        }
    }


}
