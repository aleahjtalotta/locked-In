module com.ourgroup1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;

    opens com.ourgroup1 to javafx.fxml;
    exports com.ourgroup1;

    // for the sake of testing this is commented out temperarily
    //opens com.model to javafx.fxml;
    //exports com.model;


    // changed so it runs
    opens com.classes to javafx.fxml;
    exports com.classes;

}
