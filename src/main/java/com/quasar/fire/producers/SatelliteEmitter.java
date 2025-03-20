/*package com.quasar.fire.producers;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
public class SatelliteEmitter {

    @Inject
    @Channel("satellite-position")
    Emitter<String> emitter;

    public Uni<Void> sendMessage(String value, String key) {
        return Uni.createFrom().voidItem().onItem().invoke(() ->
                emitter.send(Message.of(value).addMetadata(
                        OutgoingKafkaRecordMetadata.builder().withKey(key).build()
                )));
    }

}*/
