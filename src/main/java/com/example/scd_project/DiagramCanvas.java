package com.example.scd_project;

import javafx.scene.layout.Pane;

public class DiagramCanvas extends Pane {
    private PropertiesPanel propertiesPanel;

    public DiagramCanvas(PropertiesPanel propertiesPanel) {
        this.propertiesPanel = propertiesPanel;
        this.setStyle("-fx-background-color: white;");
    }

    public void addClassComponent(ClassDiagramComponent classComponent) {
        this.getChildren().add(classComponent);

        // Click event to display properties in the PropertiesPanel
        classComponent.setOnMouseClicked(event -> {
            propertiesPanel.setClassProperties(classComponent);
        });
    }
}

