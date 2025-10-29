package com.classes;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ItemTest {

    @Test
    public void constructorPreservesProvidedValues() {
        UUID id = UUID.randomUUID();
        Long legacyId = 42L;
        Item item = new Item(id, legacyId, "Skeleton Key", true);

        assertEquals(id, item.getId());
        assertEquals(legacyId, item.getLegacyId());
        assertEquals("Skeleton Key", item.getName());
        assertTrue(item.isReusable());
    }

    @Test
    public void constructorDefaultsNameWhenNull() {
        UUID id = UUID.randomUUID();
        Item item = new Item(id, null, null, false);

        assertEquals("Unknown Item", item.getName());
        assertNull(item.getLegacyId());
        assertFalse(item.isReusable());
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullId() {
        new Item(null, 1L, "Faulty Item", true);
    }
}
