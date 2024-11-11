package com.example.scd_project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

public class MainController {

    @FXML
    private VBox toolPanel;
    @FXML
    private Pane diagramCanvas;
    @FXML
    private VBox propertiesPanel;
    @FXML
    private TextField classNameField;
    @FXML
    private TextArea attributesArea;
    @FXML
    private TextArea operationsArea;
    @FXML
    private Button addAttributeButton;
    @FXML
    private Button addOperationButton;
    @FXML
    private Button classButton;
    @FXML
    private Button relationshipButton;

    private DiagramCanvas canvas;
    private ClassDiagramComponent selectedClass;
    private boolean isCreatingRelationship = false;

    public void initialize() {
        // Set up the DiagramCanvas and ToolPanel
        canvas = new DiagramCanvas(new PropertiesPanel());
        diagramCanvas.getChildren().add(canvas);

        // Initially, no class is selected
        selectedClass = null;

        // Handle mouse clicks on the diagram canvas
        canvas.setOnMouseClicked(this::handleCanvasClick);
    }

    @FXML
    private void handleClassButtonAction() {
        // Create a new ClassDiagramComponent (representing a class in UML)
        ClassDiagramComponent newClass = new ClassDiagramComponent("New Class");
        canvas.addClassComponent(newClass);
    }

    @FXML
    private void handleRelationshipButtonAction() {
        // Toggle relationship creation mode
        isCreatingRelationship = !isCreatingRelationship;
        relationshipButton.setText(isCreatingRelationship ? "Cancel Relationship" : "Create Relationship");
    }

    @FXML
    private void handleAddAttribute() {
        if (selectedClass != null && !attributesArea.getText().isEmpty()) {
            selectedClass.addAttribute(attributesArea.getText());
            attributesArea.clear();
        }
    }

    @FXML
    private void handleAddOperation() {
        if (selectedClass != null && !operationsArea.getText().isEmpty()) {
            selectedClass.addOperation(operationsArea.getText());
            operationsArea.clear();
        }
    }

    private void handleCanvasClick(MouseEvent event) {
        if (isCreatingRelationship && selectedClass != null) {
            // Create a relationship when a class is selected and the canvas is clicked
            double startX = selectedClass.getLayoutX() + selectedClass.getWidth() / 2;
            double startY = selectedClass.getLayoutY() + selectedClass.getHeight() / 2;

            // Create an arrow from the selected class to the clicked point (for example, an association)
            Arrow relationshipArrow = new Arrow(startX, startY, event.getX(), event.getY(), ArrowType.ASSOCIATION);
            canvas.getChildren().addAll(relationshipArrow.getLine(), relationshipArrow.getArrowhead());

            // Reset the state after creating the relationship
            isCreatingRelationship = false;
            relationshipButton.setText("Create Relationship");
        } else {
            // If no relationship is being created, select a class on click
            selectedClass = canvas.getClassAtPosition(event.getX(), event.getY());
            if (selectedClass != null) {
                // Update the properties panel with the selected class information
                propertiesPanel.getChildren().clear();
                propertiesPanel.getChildren().addAll(
                        new Label("Class Name:"),
                        classNameField,
                        new Label("Attributes:"),
                        attributesArea,
                        addAttributeButton,
                        new Label("Operations:"),
                        operationsArea,
                        addOperationButton
                );
                classNameField.setText(selectedClass.getClassName());
            }
        }
    }
}
