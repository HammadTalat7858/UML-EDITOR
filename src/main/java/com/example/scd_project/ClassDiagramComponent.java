package com.example.scd_project;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;



public class ClassDiagramComponent extends StackPane {
    private Label classNameLabel;
    private VBox attributeSection;
    private VBox operationSection;
    private Rectangle container;
    private double mouseX;
    private double mouseY;

    public ClassDiagramComponent(String className) {
        // Create rectangle to represent the class box
        container = new Rectangle(200, 150);
        container.setFill(Color.LIGHTGRAY);
        container.setStroke(Color.BLACK);

        // Create label for the class name
        classNameLabel = new Label(className);
        classNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Create sections for attributes and operations
        attributeSection = new VBox();
        operationSection = new VBox();
        attributeSection.setStyle("-fx-border-color: black; -fx-padding: 5;");
        operationSection.setStyle("-fx-border-color: black; -fx-padding: 5;");

        Label attributeHeader = new Label("Attributes:");
        Label operationHeader = new Label("Operations:");
        VBox content = new VBox(5, classNameLabel, attributeHeader, attributeSection, operationHeader, operationSection);
        content.setStyle("-fx-padding: 5;");

        this.getChildren().addAll(container, content);
        enableSmoothDragging();
    }

    private void enableSmoothDragging() {
        this.setOnMousePressed(event -> {
            mouseX = event.getSceneX() - this.getLayoutX();
            mouseY = event.getSceneY() - this.getLayoutY();
            event.consume();
        });

        this.setOnMouseDragged(event -> {
            double newX = event.getSceneX() - mouseX;
            double newY = event.getSceneY() - mouseY;
            this.setLayoutX(newX);
            this.setLayoutY(newY);
            event.consume();
        });
    }

    public void addAttribute(String attribute) {
        Label attrLabel = new Label(attribute);
        attributeSection.getChildren().add(attrLabel);
    }

    public void addOperation(String operation) {
        Label opLabel = new Label(operation);
        operationSection.getChildren().add(opLabel);
    }

    public String getClassName() {
        return classNameLabel.getText();
    }

    public void setClassName(String newName) {
        classNameLabel.setText(newName);
    }
}
