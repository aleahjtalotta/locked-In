module com.lockedin {
    requires javafx.controls;
    requires javafx.fxml;     // optional if you end up using FXML
    requires json.simple;
    // requires junit;        // comment this out unless you actually run with the junit module

    opens com.lockedin.ui to javafx.fxml;  // only needed if you use FXML controllers here

    exports com.lockedin.ui;
    exports com.classes;
}
