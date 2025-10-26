package com.classes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Stores the hints that are still available to the player.
 */
public class Hints {
    private final Queue<Hint> remainingHints = new LinkedList<>();

    /**
     * Enqueues an already constructed hint for later use.
     *
     * @param hint hint instance to add; ignored when {@code null}
     */
    public void addHint(Hint hint) {
        if (hint != null) {
            remainingHints.offer(hint);
        }
    }

    /**
     * Convenience overload that creates a hint without a legacy identifier.
     *
     * @param id   unique identifier for the hint
     * @param text hint message presented to the player
     */
    public void addHint(UUID id, String text) {
        addHint(id, null, text);
    }

    /**
     * Enqueues a hint using the supplied identifiers and message.
     *
     * @param id       unique identifier for the hint
     * @param legacyId optional legacy numeric identifier; may be {@code null}
     * @param text     hint message presented to the player
     */
    public void addHint(UUID id, Long legacyId, String text) {
        addHint(new Hint(id, legacyId, text));
    }

    /**
     * @return the next hint without removing it, or {@code null} when the queue is empty
     */
    public Hint peekNextHint() {
        return remainingHints.peek();
    }

    /**
     * @return the next hint and removes it from the queue; {@code null} when no hints remain
     */
    public Hint consumeNextHint() {
        return remainingHints.poll();
    }

    /**
     * @return unmodifiable view of the hints still available to the player
     */
    public List<Hint> getRemainingHints() {
        return Collections.unmodifiableList(remainingHints.stream().toList());
    }
}
