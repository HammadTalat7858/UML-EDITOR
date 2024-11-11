package com.example.scd_project;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ToolPanel {
    private VBox toolPanel;
    private DiagramCanvas diagramCanvas;

    public ToolPanel(DiagramCanvas diagramCanvas) {
        this.diagramCanvas = diagramCanvas;
        toolPanel = new VBox(10);
        toolPanel.setStyle("-fx-padding: 10; -fx-background-color: #E0E0E0;");

        // Create the "Class" button
        Button classButton = new Button("Class");
        classButton.setOnAction(event -> {
            ClassDiagramComponent newClassComponent = new ClassDiagramComponent("New Class");
            diagramCanvas.addClassComponent(newClassComponent);
        });

        // Create the "Relationship" button
        Button relationshipButton = new Button("Create Relationship");
        relationshipButton.setOnAction(event -> {
            // Toggle the mode to create relationships
            diagramCanvas.setCreatingRelationship(true);
        });

        toolPanel.getChildren().addAll(classButton, relationshipButton);
    }

    public VBox getPanel() {
        return toolPanel;
    }
}
