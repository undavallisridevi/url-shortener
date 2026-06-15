package com.sridevi.urlshortener.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    public String encode(long value) {
        if (value < 0) throw new IllegalArgumentException("Value must be non-negative");
        if (value == 0) return "0";
        StringBuilder result = new StringBuilder();
        while (value > 0) {
            result.append(ALPHABET[(int) (value % 62)]);
            value /= 62;
        }
        return result.reverse().toString();
    }
}
