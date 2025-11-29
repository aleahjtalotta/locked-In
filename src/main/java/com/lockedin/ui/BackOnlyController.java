package com.lockedin.ui;

import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Wires a simple back button to the navigation history.
 */
public class BackOnlyController implements SceneBindable {

    @Override
    public void onSceneLoaded(Parent root) {
        findBackButton(root).ifPresent(button -> button.setOnAction(this::handleBack));
    }

    private void handleBack(ActionEvent event) {
        SceneNavigator.back(event);
    }

    private Optional<Button> findBackButton(Parent root) {
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> "Back".equals(btn.getText()))
                .findFirst();
    }
}
