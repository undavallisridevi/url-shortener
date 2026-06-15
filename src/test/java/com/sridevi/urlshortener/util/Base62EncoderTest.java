package com.sridevi.urlshortener.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {
    private final Base62Encoder encoder = new Base62Encoder();
    @Test void encodesKnownValues() {
        assertEquals("0", encoder.encode(0));
        assertEquals("z", encoder.encode(61));
        assertEquals("10", encoder.encode(62));
        assertEquals("Q0u", encoder.encode(100000));
    }
    @Test void rejectsNegativeValues() { assertThrows(IllegalArgumentException.class, () -> encoder.encode(-1)); }
}
