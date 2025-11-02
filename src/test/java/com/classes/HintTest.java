package com.classes;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class HintTest {

    @Test
    public void constructorDefaultsNullTextToEmptyString() {
        Hint hint = new Hint(UUID.randomUUID(), null, null);

        assertEquals("", hint.getText());
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsWhenIdIsNull() {
        new Hint(null, null, "Missing id");
    }
}
