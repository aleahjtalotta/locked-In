package com.lockedin.ui;

import javafx.scene.image.Image;

/**
 * Simple value object for inventory items.
 */
public record InventoryItem(int id, String name, String hint, Image image) {
}
