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
class SatellitePositionResourceFailTest {
    private static final Logger LOGGER = Logger.getLogger(SatellitePositionResourceFailTest.class);
    @Inject
    @Remote("NAVE_POSITIONS")
    RemoteCache<String, String> cacheNave;
    @Inject
    ObjectMapper mapper;
    private static JsonNode request;

    @Test
    void givenTopSecrectSplitSatelliteKenobiThenSuccesTest() {
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


    @BeforeEach
    void init(){
        cacheNave.clear();
        try{
            request = mapper.readValue(new String(Files.readAllBytes(Path.of("src/test/resources/request/RequestTopSecretSplitKenobi.json"))), JsonNode.class);
            cacheNave.put("kenobi",new String(Files.readAllBytes(Path.of("src/test/resources/cache/KenobiDistanceCache.json"))));
        }catch (Exception exception) {
            LOGGER.errorf("Error al cargar los datos de prueba en el cache %s", exception);
        }
    }
}
