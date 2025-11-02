package com.classes;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class HintsTest {

    @Test
    public void addHintEnqueuesNonNullInstances() {
        Hints hints = new Hints();
        Hint hint = new Hint(UUID.randomUUID(), null, "Look behind the painting.");

        hints.addHint(hint);

        assertSame(hint, hints.peekNextHint());
    }

    @Test
    public void addHintIgnoresNullInstance() {
        Hints hints = new Hints();

        hints.addHint((Hint) null);

        assertNull(hints.peekNextHint());
    }

    @Test
    public void addHintWithIdentifiersCreatesHintAndQueuesIt() {
        Hints hints = new Hints();
        UUID id = UUID.randomUUID();

        hints.addHint(id, 12L, "Remember the code order.");

        Hint created = hints.peekNextHint();
        assertSame(id, created.getId());
        assertEquals(Long.valueOf(12L), created.getLegacyId());
        assertEquals("Remember the code order.", created.getText());
    }

    @Test
    public void peekNextHintDoesNotRemoveHintFromQueue() {
        Hints hints = new Hints();
        Hint hint = new Hint(UUID.randomUUID(), null, "Count the switches.");

        hints.addHint(hint);

        Hint firstPeek = hints.peekNextHint();
        Hint secondPeek = hints.peekNextHint();

        assertSame(firstPeek, secondPeek);
    }

    @Test
    public void consumeNextHintRemovesHintFromQueue() {
        Hints hints = new Hints();
        Hint hint = new Hint(UUID.randomUUID(), null, "Check the floor tiles.");

        hints.addHint(hint);
        Hint consumed = hints.consumeNextHint();

        assertSame(hint, consumed);
        assertNull(hints.peekNextHint());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRemainingHintsReturnsUnmodifiableView() {
        Hints hints = new Hints();
        hints.addHint(UUID.randomUUID(), "Look up.");

        hints.getRemainingHints().add(new Hint(UUID.randomUUID(), null, "Another hint"));
    }
}
