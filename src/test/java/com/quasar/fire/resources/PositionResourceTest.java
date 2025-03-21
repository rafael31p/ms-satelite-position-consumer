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
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class PositionResourceTest {
    private static final Logger LOGGER = Logger.getLogger(PositionResourceTest.class);
    @Inject
    ObjectMapper mapper;
    @Inject
    @Remote("SATELLITE_POSITIONS")
    RemoteCache<String, String> cacheSatellites;


    private static JsonNode request;
    private static String path = "src/test/resources/request/";

    @Test
    @Order(0)
    void givenTopSecretSplitKenobiThenSuccesTest() {
        loadRequest("RequestTopSecretSplitKenobi.json");
        String nameSatellite = "kenobi";
        given().contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/topsecret_split/"+nameSatellite)
                .then()
                .statusCode(200)
                .body("name", equalTo(nameSatellite))
                .body("distance", equalTo(447.213f));
    }
    @Test
    @Order(1)
    void givenTopSecretSplitSkywalkerThenSuccesTest() {
        loadRequest("RequestTopSecretSplitSkywalker.json");
        String nameSatellite = "skywalker";
        given().contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/topsecret_split/"+nameSatellite)
                .then()
                .statusCode(200)
                .body("name", equalTo(nameSatellite))
                .body("distance", equalTo(223.606f));
    }
    @Test
    @Order(2)
    void givenTopSecretSplitSatoThenSuccesTest() {
        loadRequest("RequestTopSecretSplitSato.json");
        String nameSatellite = "sato";
        given().contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/topsecret_split/"+nameSatellite)
                .then()
                .statusCode(200)
                .body("name", equalTo(nameSatellite))
                .body("distance", equalTo(632.455f));
    }
    @Test
    void givenTopSecrectSplitThenSucessTest() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/topsecret_split")
                .then()
                .statusCode(200)
                .body("position.x", equalTo(-81.25042f))
                .body("position.y", equalTo(-112.498375f))
                .body("message", equalTo("este es un mensaje secreto"));
    }
    private void loadRequest(String fileName) {
        try {
            request = mapper.readValue(new String(Files.readAllBytes(Path.of(path + fileName))), JsonNode.class);
        } catch (Exception exception) {
            LOGGER.errorf("Error al cargar los datos de prueba en el cache %s", exception);
        }
    }

    @BeforeEach
    void init() {
        try {
            String sato = new String(Files.readAllBytes(Path.of("src/test/resources/cache/SatoCache.json")));
            String kenobi = new String(Files.readAllBytes(Path.of("src/test/resources/cache/KenobiCache.json")));
            String skywalker = new String(Files.readAllBytes(Path.of("src/test/resources/cache/SkywalkerCache.json")));
            cacheSatellites.clear();
            cacheSatellites.put("sato", sato);
            cacheSatellites.put("kenobi", kenobi);
            cacheSatellites.put("skywalker", skywalker);
        }catch (Exception exception) {
            LOGGER.errorf("Error al cargar los datos de prueba en el cache %s", exception);
        }
    }



}
