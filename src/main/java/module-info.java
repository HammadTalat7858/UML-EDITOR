module com.example.pscd {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.ClassDiagram to javafx.fxml;
    opens com.example.UseCaseDiagram to javafx.fxml;
    opens com.example to javafx.fxml;
    exports com.example.ClassDiagram;
    exports com.example.UseCaseDiagram;
    exports com.example;

}
