package com.lockedin.ui;

import java.util.Optional;
import java.util.function.Supplier;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

/**
 * Handles common wiring for puzzle screens so subclasses only supply the
 * completion callback and next destination.
 */
public abstract class BasePuzzleController implements SceneBindable {

    private final Runnable completionAction;
    private final Supplier<String> nextScreenSupplier;

    protected BasePuzzleController(Runnable completionAction, Supplier<String> nextScreenSupplier) {
        this.completionAction = completionAction;
        this.nextScreenSupplier = nextScreenSupplier;
    }

    @Override
    public void onSceneLoaded(Parent root) {
        findButtonWithText(root, "Enter").ifPresent(button -> button.setOnAction(this::onPuzzleSolved));
    }

    private void onPuzzleSolved(ActionEvent event) {
        completionAction.run();
        SceneNavigator.switchTo(event, nextScreenSupplier.get());
    }

    private Optional<Button> findButtonWithText(Parent root, String text) {
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> text.equals(btn.getText()))
                .findFirst();
    }
}
