package com.quasar.fire.domain.service;

import com.quasar.fire.domain.model.MessageFragment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageReconstructionServiceTest {

    private final MessageReconstructionService service = new MessageReconstructionService();

    @Test
    void shouldReconstructMessageFromFragments() {
        List<MessageFragment> fragments = List.of(
                new MessageFragment(new String[]{"este", "", "", "mensaje", ""}),
                new MessageFragment(new String[]{"", "es", "", "", "secreto"}),
                new MessageFragment(new String[]{"este", "", "un", "", ""})
        );

        String result = service.reconstruct(fragments);

        assertEquals("este es un mensaje secreto", result);
    }

    @Test
    void shouldReconstructFromDifferentLengthFragments() {
        List<MessageFragment> fragments = List.of(
                new MessageFragment(new String[]{"", "este", "es", "un", "mensaje"}),
                new MessageFragment(new String[]{"este", "", "un", "mensaje", "", "secreto"}),
                new MessageFragment(new String[]{"", "", "es", "", "mensaje"})
        );

        String result = service.reconstruct(fragments);

        assertEquals("este es un mensaje secreto", result);
    }

    @Test
    void shouldReturnEmptyWhenNoFragments() {
        String result = service.reconstruct(List.of());
        assertEquals("", result);
    }

    @Test
    void shouldReturnEmptyWhenNull() {
        String result = service.reconstruct(null);
        assertEquals("", result);
    }
}
