module com.lockedin {
    requires javafx.controls;
    //requires javafx.fxml;
    requires json.simple;
    requires junit;



    exports com.classes;
    exports com.lockedin.ui;


    //opens com.classes to javafx.fxml;
   // exports com.classes;
}
