package com.example.scd_project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Create main layout
        BorderPane root = new BorderPane();

        // Create components
        PropertiesPanel propertiesPanel = new PropertiesPanel();
        DiagramCanvas diagramCanvas = new DiagramCanvas(propertiesPanel);
        ToolPanel toolPanel = new ToolPanel(diagramCanvas);

        // Set components in the layout
        root.setLeft(toolPanel.getPanel());
        root.setCenter(diagramCanvas);
        root.setRight(propertiesPanel.getPropertiesPanel());

        // Create scene and set stage
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("UML Class Diagram Creator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
