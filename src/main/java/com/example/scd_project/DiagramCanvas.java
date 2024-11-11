package com.example.scd_project;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class DiagramCanvas extends Pane {
    private PropertiesPanel propertiesPanel;
    private ClassDiagramComponent selectedClass;
    private boolean creatingRelationship = false;  // Relationship creation mode flag

    public DiagramCanvas(PropertiesPanel propertiesPanel) {
        this.propertiesPanel = propertiesPanel;
        this.setStyle("-fx-background-color: white;");

        this.setOnMouseClicked(this::handleCanvasClick);
    }

    public void addClassComponent(ClassDiagramComponent classComponent) {
        this.getChildren().add(classComponent);

        // Click event to display properties in the PropertiesPanel
        classComponent.setOnMouseClicked(event -> {
            if (creatingRelationship) {
                // Create a relationship with the previously selected class
                if (selectedClass != null && selectedClass != classComponent) {
                    double startX = selectedClass.getLayoutX() + selectedClass.getWidth() / 2;
                    double startY = selectedClass.getLayoutY() + selectedClass.getHeight() / 2;
                    double endX = classComponent.getLayoutX() + classComponent.getWidth() / 2;
                    double endY = classComponent.getLayoutY() + classComponent.getHeight() / 2;

                    // Create an arrow (association as an example)
                    Arrow newArrow = new Arrow(startX, startY, endX, endY, ArrowType.ASSOCIATION);
                    this.getChildren().addAll(newArrow.getLine(), newArrow.getArrowhead());

                    // Reset the selection after creating the relationship
                    selectedClass = null;
                    creatingRelationship = false;
                } else {
                    // Select the class as the starting point for the relationship
                    selectedClass = classComponent;
                }
            } else {
                propertiesPanel.setClassProperties(classComponent);
                selectedClass = classComponent;
            }
        });
    }

    private void handleCanvasClick(MouseEvent event) {
        if (selectedClass != null && !creatingRelationship) {
            // Clear selection if not creating a relationship
            selectedClass = null;
            propertiesPanel.setClassProperties(null);
        }
    }

    // Method to enable relationship creation mode
    public void setCreatingRelationship(boolean creatingRelationship) {
        this.creatingRelationship = creatingRelationship;
        selectedClass = null; // Clear any current selection to start fresh
    }
}
