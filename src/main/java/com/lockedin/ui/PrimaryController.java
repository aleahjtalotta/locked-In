package com.lockedin.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PrimaryController {

    @FXML
    private Label someLabel;   // matches fx:id="someLabel"

    @FXML
    private void switchToSecondary() {
        someLabel.setText("You escaped!");
    }
}
