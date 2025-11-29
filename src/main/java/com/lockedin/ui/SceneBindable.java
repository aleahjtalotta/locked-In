package com.lockedin.ui;

import javafx.scene.Parent;

/**
 * Allows controllers without FXML-injected fields to wire themselves to the
 * loaded scene graph after FXMLLoader finishes.
 */
public interface SceneBindable {
    void onSceneLoaded(Parent root);
}
