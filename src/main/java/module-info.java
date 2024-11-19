module com.example.pscd {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.pscd to javafx.fxml;
    opens com.example.usecase to javafx.fxml;
    opens com.example to javafx.fxml;
    exports com.example.pscd;
    exports com.example.usecase;
    exports com.example;

}
