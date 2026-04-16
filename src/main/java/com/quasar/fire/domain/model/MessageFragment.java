package com.quasar.fire.domain.model;

import java.util.Objects;

public record MessageFragment(String[] words) {

    public MessageFragment {
        Objects.requireNonNull(words, "Message fragment words must not be null");
    }

    public int length() {
        return words.length;
    }

    public String wordAt(int index) {
        if (index < 0 || index >= words.length) {
            return "";
        }
        return words[index];
    }

    public boolean hasWordAt(int index) {
        return index >= 0 && index < words.length && !words[index].isBlank();
    }
}
