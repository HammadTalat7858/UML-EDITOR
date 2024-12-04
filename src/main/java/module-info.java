module com.example.pscd {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires org.junit.jupiter.api;
    requires org.testfx.junit5;
    requires org.junit.platform.commons;
    requires org.testfx;
    requires javafx.graphics;


    opens com.example.ClassDiagram to javafx.fxml,org.testfx,org.junit.platform.commons;
    opens com.example.UseCaseDiagram to javafx.fxml,org.testfx,org.junit.platform.commons;

    opens com.example to javafx.fxml;
    exports com.example.ClassDiagram;
    exports com.example.UseCaseDiagram;
    exports com.example;


}