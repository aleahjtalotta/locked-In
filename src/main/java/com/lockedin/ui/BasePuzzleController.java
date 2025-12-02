package com.lockedin.ui;

import com.classes.Puzzle;
import com.lockedin.ui.InventoryManager;
import com.lockedin.ui.InventoryItem;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

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
    private Label timerLabel;

    private Label itemHintOverlay;
    private java.util.List<ImageView> inventorySlots = java.util.Collections.emptyList();
    private Integer activeHintItemId = null;

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
        this.timerLabel = findTimerLabel(root).orElse(null);

        if (enterButton != null) {
            enterButton.setOnAction(this::handleEnterClick);
        } else {
            findButtonWithText(root, "Enter").ifPresent(button -> button.setOnAction(this::handleEnterClick));
        }
        bindInventorySlots(root);
        refreshInventoryUI();
        CountdownTimerManager.bindLabel(timerLabel);
        CountdownTimerManager.startIfNeeded();
        configurePauseButton(root);
        configureLeaveButton(root);
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
            CompletionRecorder.recordTimeIfComplete();
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
            InventoryManager.addItemForPuzzle(puzzleLegacyId);
            refreshInventoryUI();
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

    private Optional<Label> findTimerLabel(Parent root) {
        Node node = root.lookup("#timerLabel");
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
        findPauseButton(root).ifPresent(button -> button.setOnAction(event -> {
            CountdownTimerManager.pauseAndPersist();
            SceneNavigator.switchTo(event, "PauseScreen.fxml");
        }));
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

    private void configureLeaveButton(Parent root) {
        findLeaveButton(root).ifPresent(button -> button.setOnAction(event -> {
            String target = GameState.getNextHubScreen();
            SceneNavigator.switchTo(event, target);
        }));
    }

    private Optional<ButtonBase> findLeaveButton(Parent root) {
        Node byId = root.lookup("#leaveButton");
        if (byId instanceof ButtonBase button) {
            return Optional.of(button);
        }
        return root.lookupAll(".button")
                .stream()
                .filter(node -> node instanceof ButtonBase)
                .map(node -> (ButtonBase) node)
                .filter(btn -> "Leave".equals(btn.getText()))
                .findFirst();
    }

    private void bindInventorySlots(Parent root) {
        var slots = new java.util.ArrayList<ImageView>(6);
        for (int i = 0; i < 6; i++) {
            Node node = root.lookup("#image" + i);
            if (node instanceof ImageView iv) {
                slots.add(iv);
            }
        }
        this.inventorySlots = slots;
        ensureItemHintOverlay(root);
    }

    private void refreshInventoryUI() {
        if (inventorySlots.isEmpty()) {
            return;
        }
        var items = InventoryManager.getItems();
        for (int i = 0; i < inventorySlots.size(); i++) {
            ImageView slot = inventorySlots.get(i);
            if (i < items.size()) {
                InventoryItem item = items.get(i);
                slot.setImage(item.image());
                slot.setOpacity(1.0);
                slot.setOnMouseClicked(e -> showItemHint(item));
            } else {
                slot.setImage(null);
                slot.setOpacity(0.35);
                slot.setOnMouseClicked(null);
            }
        }
    }

    private void ensureItemHintOverlay(Parent root) {
        if (!(root instanceof Pane pane)) {
            return;
        }
        Label label = new Label();
        label.setVisible(false);
        label.setManaged(false);
        label.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-text-fill: #e4e1b4; -fx-padding: 12; -fx-font-size: 13; -fx-border-color: #e4e1b4; -fx-border-width: 1;");
        pane.getChildren().add(label);
        // Center the overlay within the pane
        label.layoutXProperty().bind(pane.widthProperty().subtract(label.widthProperty()).divide(2));
        label.layoutYProperty().bind(pane.heightProperty().subtract(label.heightProperty()).divide(2));
        this.itemHintOverlay = label;
        label.setOnMouseClicked(e -> hideItemHint());
    }

    private void showItemHint(InventoryItem item) {
        if (itemHintOverlay == null) {
            return;
        }
        // Toggle: hide if same item is already displayed
        if (itemHintOverlay.isVisible() && activeHintItemId != null && activeHintItemId == item.id()) {
            hideItemHint();
            return;
        }
        activeHintItemId = item.id();
        itemHintOverlay.setText(item.name() + ":\n" + item.hint());
        itemHintOverlay.setVisible(true);
        itemHintOverlay.setManaged(true);
        itemHintOverlay.toFront();
    }

    private void hideItemHint() {
        activeHintItemId = null;
        if (itemHintOverlay != null) {
            itemHintOverlay.setVisible(false);
            itemHintOverlay.setManaged(false);
        }
    }
}
