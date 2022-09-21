module se233.hibiscus.hibiscus {
    requires javafx.controls;
    requires javafx.fxml;
    requires zip4j;

    opens se233.hibiscus to javafx.fxml;
    opens se233.hibiscus.controller to javafx.fxml;

    exports se233.hibiscus;
    exports se233.hibiscus.controller ;
}