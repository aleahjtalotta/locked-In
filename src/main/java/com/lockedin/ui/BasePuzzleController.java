package com.lockedin.ui;

import com.classes.Puzzle;
import java.util.Optional;
import java.util.function.Supplier;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

/**
 * Handles common wiring for puzzle screens so subclasses only supply the
 * completion callback and next destination.
 */
public abstract class BasePuzzleController implements SceneBindable {

    private final Runnable completionAction;
    private final Supplier<String> nextScreenSupplier;
    private final Long puzzleLegacyId;
    private boolean puzzleSolved;
    private boolean hintUsed;

    private Parent root;
    private Button enterButton;
    private TextField answerField;
    private Label feedbackLabel;

    protected BasePuzzleController(Runnable completionAction,
                                   Supplier<String> nextScreenSupplier,
                                   Long puzzleLegacyId) {
        this.completionAction = completionAction;
        this.nextScreenSupplier = nextScreenSupplier;
        this.puzzleLegacyId = puzzleLegacyId;
    }

    @Override
    public void onSceneLoaded(Parent root) {
        this.root = root;
        this.answerField = findAnswerField(root).orElse(null);
        this.enterButton = findEnterButton(root).orElse(null);
        this.feedbackLabel = findFeedbackLabel(root).orElse(null);

        if (enterButton != null) {
            enterButton.setOnAction(this::handleEnterClick);
        } else {
            findButtonWithText(root, "Enter").ifPresent(button -> button.setOnAction(this::handleEnterClick));
        }
        configurePauseButton(root);
        configureHintButton(root);
    }

    private void handleEnterClick(ActionEvent event) {
        if (!puzzleSolved) {
            boolean correct = validateAnswer();
            if (!correct) {
                return;
            }
            puzzleSolved = true;
            disableAnswerInput();
            showFeedback("Correct!");
        }
        SceneNavigator.switchTo(event, nextScreenSupplier.get());
    }

    private boolean validateAnswer() {
        Optional<Puzzle> puzzleOpt = PuzzleProvider.findPuzzleByLegacyId(puzzleLegacyId);
        if (puzzleOpt.isEmpty()) {
            showFeedback("Puzzle data unavailable.");
            return false;
        }
        if (answerField == null) {
            showFeedback("Answer input unavailable.");
            return false;
        }
        Puzzle puzzle = puzzleOpt.get();
        String answer = answerField.getText();
        boolean correct = puzzle.isCorrectAnswer(answer);
        if (correct) {
            puzzle.markSolved();
            ProgressSaver.recordSolved(puzzleLegacyId);
            completionAction.run();
        } else {
            showFeedback("Incorrect answer, try again!");
        }
        return correct;
    }

    private void disableAnswerInput() {
        if (answerField != null) {
            answerField.setDisable(true);
        }
    }

    private void showFeedback(String message) {
        if (feedbackLabel != null) {
            feedbackLabel.setText(message);
            feedbackLabel.setVisible(true);
            feedbackLabel.setManaged(true);
        }
    }

    private Optional<Button> findButtonWithText(Parent root, String text) {
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> text.equals(btn.getText()))
                .findFirst();
    }

    private Optional<Button> findEnterButton(Parent root) {
        Node node = root.lookup("#enterButton");
        if (node instanceof Button button) {
            return Optional.of(button);
        }
        return Optional.empty();
    }

    private Optional<TextField> findAnswerField(Parent root) {
        Node node = root.lookup("#answerField");
        if (node instanceof TextField field) {
            return Optional.of(field);
        }
        return Optional.empty();
    }

    private Optional<Label> findFeedbackLabel(Parent root) {
        Node node = root.lookup("#feedbackLabel");
        if (node instanceof Label label) {
            return Optional.of(label);
        }
        return Optional.empty();
    }

    private void configureHintButton(Parent root) {
        findHintButton(root).ifPresent(button -> button.setOnAction(event -> {
            displayHint(root);
            if (!hintUsed) {
                ProgressSaver.recordHintUsed(puzzleLegacyId);
                hintUsed = true;
            }
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

    private void configurePauseButton(Parent root) {
        findPauseButton(root).ifPresent(button -> button.setOnAction(event -> SceneNavigator.switchTo(event, "PauseScreen.fxml")));
    }

    private Optional<ButtonBase> findPauseButton(Parent root) {
        Node byId = root.lookup("#pauseButton");
        if (byId instanceof ButtonBase button) {
            return Optional.of(button);
        }
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof ButtonBase)
                .map(node -> (ButtonBase) node)
                .filter(btn -> "Pause".equals(btn.getText()))
                .findFirst();
    }
}
