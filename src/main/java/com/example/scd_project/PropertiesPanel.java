package com.example.scd_project;


import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class PropertiesPanel {
    private VBox propertiesPanel;
    private TextField classNameField;
    private TextArea attributesArea;
    private TextArea operationsArea;
    private Button addAttributeButton;
    private Button addOperationButton;
    private ClassDiagramComponent currentClass;

    public PropertiesPanel() {
        propertiesPanel = new VBox(10);
        propertiesPanel.setStyle("-fx-padding: 10; -fx-background-color: #F0F0F0;");

        // Class name field
        Label classNameLabel = new Label("Class Name:");
        classNameField = new TextField();
        propertiesPanel.getChildren().addAll(classNameLabel, classNameField);

        // Attributes section
        Label attributesLabel = new Label("Attributes:");
        attributesArea = new TextArea();
        attributesArea.setPromptText("Enter attributes (e.g., int id, String name)");
        propertiesPanel.getChildren().addAll(attributesLabel, attributesArea);

        // Operations section
        Label operationsLabel = new Label("Operations:");
        operationsArea = new TextArea();
        operationsArea.setPromptText("Enter operations (e.g., void setId(int id))");
        propertiesPanel.getChildren().addAll(operationsLabel, operationsArea);

        // Buttons to add attributes and operations
        addAttributeButton = new Button("Add Attribute");
        addOperationButton = new Button("Add Operation");
        propertiesPanel.getChildren().addAll(addAttributeButton, addOperationButton);

        // Button actions
        addAttributeButton.setOnAction(event -> {
            if (currentClass != null && !attributesArea.getText().isEmpty()) {
                currentClass.addAttribute(attributesArea.getText());
                attributesArea.clear();
            }
        });

        addOperationButton.setOnAction(event -> {
            if (currentClass != null && !operationsArea.getText().isEmpty()) {
                currentClass.addOperation(operationsArea.getText());
                operationsArea.clear();
            }
        });

        // Update class name in the component
        classNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentClass != null) {
                currentClass.setClassName(newVal);
            }
        });
    }

    public void setClassProperties(ClassDiagramComponent classComponent) {
        currentClass = classComponent;
        classNameField.setText(classComponent.getClassName());
    }

    public VBox getPropertiesPanel() {
        return propertiesPanel;
    }
}
