package com.lockedin.ui;

import java.util.Optional;
import java.util.function.Supplier;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;

/**
 * Handles common wiring for puzzle screens so subclasses only supply the
 * completion callback and next destination.
 */
public abstract class BasePuzzleController implements SceneBindable {

    private final Runnable completionAction;
    private final Supplier<String> nextScreenSupplier;
    private final Long puzzleLegacyId;

    protected BasePuzzleController(Runnable completionAction,
                                   Supplier<String> nextScreenSupplier,
                                   Long puzzleLegacyId) {
        this.completionAction = completionAction;
        this.nextScreenSupplier = nextScreenSupplier;
        this.puzzleLegacyId = puzzleLegacyId;
    }

    @Override
    public void onSceneLoaded(Parent root) {
        findButtonWithText(root, "Enter").ifPresent(button -> button.setOnAction(this::onPuzzleSolved));
        configureHintButton(root);
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

    private void configureHintButton(Parent root) {
        findHintButton(root).ifPresent(button -> button.setOnAction(event -> {
            displayHint(root);
            if (button instanceof ToggleButton toggle) {
                toggle.setSelected(false);
            }
        }));
    }

    private void displayHint(Parent root) {
        Label hintLabel = findHintLabel(root).orElse(null);
        if (hintLabel == null) {
            return;
        }
        String message = HintProvider.getHintForPuzzle(puzzleLegacyId)
                .orElse("No hint is available for this puzzle.");
        hintLabel.setText(message);
        hintLabel.setVisible(true);
        hintLabel.setManaged(true);
    }

    private Optional<ButtonBase> findHintButton(Parent root) {
        Node node = root.lookup("#hintButton");
        if (node instanceof ButtonBase button) {
            return Optional.of(button);
        }
        return Optional.empty();
    }

    private Optional<Label> findHintLabel(Parent root) {
        Node node = root.lookup("#hintLabel");
        if (node instanceof Label label) {
            return Optional.of(label);
        }
        return Optional.empty();
    }
}
