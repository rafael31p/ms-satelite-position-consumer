package com.quasar.fire.domain.service;

import com.quasar.fire.domain.model.MessageFragment;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MessageReconstructionService {

    public String reconstruct(List<MessageFragment> fragments) {
        if (fragments == null || fragments.isEmpty()) {
            return "";
        }

        int maxLength = fragments.stream()
                .mapToInt(MessageFragment::length)
                .max()
                .orElse(0);

        return IntStream.range(0, maxLength)
                .mapToObj(i -> fragments.stream()
                        .filter(f -> f.hasWordAt(i))
                        .map(f -> f.wordAt(i))
                        .findFirst()
                        .orElse(""))
                .filter(word -> !word.isBlank())
                .distinct()
                .collect(Collectors.joining(" "));
    }
}
