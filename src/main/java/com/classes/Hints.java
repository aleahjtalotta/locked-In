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

    public void addHint(Hint hint) {
        if (hint != null) {
            remainingHints.offer(hint);
        }
    }

    public void addHint(UUID id, String text) {
        addHint(id, null, text);
    }

    public void addHint(UUID id, Long legacyId, String text) {
        addHint(new Hint(id, legacyId, text));
    }

    public Hint peekNextHint() {
        return remainingHints.peek();
    }

    public Hint consumeNextHint() {
        return remainingHints.poll();
    }

    public List<Hint> getRemainingHints() {
        return Collections.unmodifiableList(remainingHints.stream().toList());
    }
}
