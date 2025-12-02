package com.lockedin.ui;

import com.classes.DataLoader;
import com.classes.DataWriter;
import com.classes.GameSystem;
import com.classes.PlayerList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * Controller for the sign-up screen that registers a new user in users.json.
 */
public class SignUpController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private ComboBox<AvatarOption> imageComboBox;

    private ObservableList<AvatarOption> avatarChoices;

    @FXML
    private void initialize() {
        configureAvatarChoices();
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        errorLabel.setText("");

        if (isBlank(name) || isBlank(email)) {
            errorLabel.setText("Please enter both name and email.");
            return;
        }

        Path dataDir = Paths.get("JSON");
        DataLoader loader = new DataLoader(dataDir);
        Optional<GameSystem> systemOpt = loader.loadGame();
        if (systemOpt.isEmpty()) {
            errorLabel.setText("Unable to load user data.");
            return;
        }

        GameSystem system = systemOpt.get();
        PlayerList players = system.getPlayers();
        if (players.emailExists(email)) {
            errorLabel.setText("That email already exists, try again.");
            return;
        }

        AvatarOption selectedAvatar = imageComboBox == null ? null : imageComboBox.getValue();
        String avatarId = selectedAvatar == null ? null : selectedAvatar.id();

        com.classes.Player newPlayer;
        try {
            newPlayer = players.createPlayer(name, email, avatarId);
        } catch (IllegalArgumentException e) {
            errorLabel.setText("That email already exists, try again.");
            return;
        }

        DataWriter writer = new DataWriter(dataDir);
        if (!writer.saveGame(system)) {
            errorLabel.setText("Unable to save new user.");
            return;
        }

        SessionContext.setActivePlayer(newPlayer);
        GameState.syncFrom(system, newPlayer);
        switchToWelcomeNewUser(event);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneNavigator.back(event);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void switchToWelcomeNewUser(ActionEvent event) {
        SceneNavigator.switchTo(event, "/com/ourgroup1/WelcomeNewUser.fxml");
    }

    private void configureAvatarChoices() {
        if (imageComboBox == null) {
            return;
        }
        avatarChoices = FXCollections.observableArrayList(defaultAvatars());
        imageComboBox.setItems(avatarChoices);
        imageComboBox.setCellFactory(list -> new AvatarListCell());
        imageComboBox.setButtonCell(new AvatarListCell());
        if (!avatarChoices.isEmpty()) {
            imageComboBox.getSelectionModel().selectFirst();
        }
    }

    private List<AvatarOption> defaultAvatars() {
        return List.of(
                new AvatarOption("bat_avatar", "Bat", "/com/ourgroup1/images/BatAvatar.png"),
                new AvatarOption("pumpkin_avatar", "Pumpkin", "/com/ourgroup1/images/PumpkinAvatar.png"),
                new AvatarOption("ghost_avatar", "Ghost", "/com/ourgroup1/images/GhostAvatar.png")
        );
    }

    private static final class AvatarListCell extends ListCell<AvatarOption> {
        private final ImageView preview = new ImageView();

        private AvatarListCell() {
            preview.setFitHeight(32);
            preview.setFitWidth(48);
            preview.setPreserveRatio(true);
        }

        @Override
        protected void updateItem(AvatarOption item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            setText(item.displayName());
            if (item.image() != null) {
                preview.setImage(item.image());
                setGraphic(preview);
            } else {
                setGraphic(null);
            }
        }
    }
}
