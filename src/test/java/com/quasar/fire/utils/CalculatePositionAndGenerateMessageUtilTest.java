package com.quasar.fire.utils;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class CalculatePositionAndGenerateMessageUtilTest {
    @Test
    void getMessageTest() {
        List<String[]> messages = List.of(
                new String[]{"este", "", "", "mensaje", ""},
                new String[]{"", "es", "", "", "secreto"},
                new String[]{"este", "", "un", "", ""}
        );
        String message = CalculatePositionAndGenerateMessageUtil.getMessage(messages);

        assertEquals("este es un mensaje secreto", message);
    }
}
