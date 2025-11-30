package com.lockedin.ui;

import java.io.InputStream;
import java.util.Objects;
import javafx.scene.image.Image;

/**
 * Represents a selectable avatar entry combining a display name, identifier, and preview image.
 */
public final class AvatarOption {
    private final String id;
    private final String displayName;
    private final String resourcePath;
    private final Image image;

    public AvatarOption(String id, String displayName, String resourcePath) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNullElse(displayName, id);
        this.resourcePath = resourcePath;
        this.image = loadImage(resourcePath);
    }

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        InputStream stream = AvatarOption.class.getResourceAsStream(path);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String resourcePath() {
        return resourcePath;
    }

    public Image image() {
        return image;
    }
}
