module com.ourgroup1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;

    opens com.ourgroup1 to javafx.fxml;
    exports com.ourgroup1;

    exports com.classes;
}
