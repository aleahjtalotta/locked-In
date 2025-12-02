package com.lockedin.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.scene.image.Image;

/**
 * Shared, in-memory inventory that lives across puzzle screens.
 */
public final class InventoryManager {

    // Map puzzle legacy id -> item id (one reward per puzzle)
    private static final Map<Long, Integer> PUZZLE_TO_ITEM = Map.of(
            301L, 101, // Shadow Whisper -> Shadow Candle
            302L, 102, // Whisperlight Count -> Lantern Dial
            303L, 103, // Haunted Arrangement -> Frame Hook
            304L, 104, // Painting Search -> Mirror Brush
            305L, 105, // The Spilled Riddle -> Ash Brush
            306L, 106  // Mirror Riddle -> Chant Scroll
    );

    private static final Map<Integer, String> ITEM_NAMES = Map.of(
            101, "Shadow Candle",
            102, "Lantern Dial",
            103, "Frame Hook",
            104, "Mirror Brush",
            105, "Ash Brush",
            106, "Chant Scroll"
    );

    private static final Map<Integer, String> ITEM_HINTS = Map.of(
            101, "A small flame to reveal what darkness hides.",
            102, "Adjust the dial to redirect light where it matters.",
            103, "A hook to hang what’s out of place.",
            104, "Sweep glass clean; a clear mirror may show the truth.",
            105, "Brush away residue to uncover markings beneath.",
            106, "Words to repeat; perhaps the echo unlocks the way."
    );

    private static final Map<Integer, String> ITEM_IMAGES = Map.of(
            101, "/com/ourgroup1/images/shadowCandle.png",
            102, "/com/ourgroup1/images/lanternDial.png",
            103, "/com/ourgroup1/images/frameHook.png",
            104, "/com/ourgroup1/images/mirrorBrush.png",
            105, "/com/ourgroup1/images/polishedCrest.png", // fallback icon for Ash Brush
            106, "/com/ourgroup1/images/galleryKey.png"     // fallback icon for Chant Scroll
    );

    private static final List<InventoryItem> items = new ArrayList<>();

    private InventoryManager() {
    }

    public static List<InventoryItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds the item associated with the given puzzle, if not already present.
     */
    public static void addItemForPuzzle(long puzzleLegacyId) {
        Integer itemId = PUZZLE_TO_ITEM.get(puzzleLegacyId);
        if (itemId == null) {
            return;
        }
        addItemByLegacyId(itemId);
    }

    /**
     * Adds an item by its legacy ID (used when restoring from saved player inventory).
     */
    public static void addItemByLegacyId(Number itemId) {
        if (itemId == null) {
            return;
        }
        int id = itemId.intValue();
        boolean alreadyPresent = items.stream().anyMatch(i -> i.id() == id);
        if (alreadyPresent) {
            return;
        }
        InventoryItem item = buildItem(id);
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Clears and rebuilds inventory from solved puzzle legacy IDs.
     */
    public static void rebuildFromSolvedLegacyIds(java.util.Collection<Long> solvedLegacyIds) {
        items.clear();
        if (solvedLegacyIds == null) {
            return;
        }
        solvedLegacyIds.forEach(InventoryManager::addItemForPuzzle);
    }

    /**
     * Adds items from a player's saved inventory (legacy item IDs).
     */
    public static void addFromPlayerInventory(java.util.Collection<com.classes.Item> savedItems) {
        if (savedItems == null) {
            return;
        }
        for (com.classes.Item item : savedItems) {
            addItemByLegacyId(item.getLegacyId());
        }
    }

    private static InventoryItem buildItem(int id) {
        String name = ITEM_NAMES.getOrDefault(id, "Item " + id);
        String hint = ITEM_HINTS.getOrDefault(id, "No hint available for this item.");
        Image image = loadImage(id);
        return new InventoryItem(id, name, hint, image);
    }

    private static Image loadImage(int id) {
        String path = ITEM_IMAGES.get(id);
        if (path == null) {
            return null;
        }
        var url = InventoryManager.class.getResource(path);
        if (url == null) {
            return null;
        }
        return new Image(url.toExternalForm());
    }

    public static Optional<InventoryItem> findById(int id) {
        return items.stream().filter(i -> i.id() == id).findFirst();
    }

    public static void clear() {
        items.clear();
    }
}
