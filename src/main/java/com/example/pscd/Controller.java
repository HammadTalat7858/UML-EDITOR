package com.example.pscd;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Controller {

    public Pane canvasContainer;

    @FXML
    private VBox toolboxVBox; // Reference to the VBox containing the buttons

    @FXML
    private Button classButton;
    @FXML
    private Button interfaceButton;
    @FXML
    private Button associationButton;
    @FXML
    private Button aggregationButton;
    @FXML
    private Button compositionButton;

    private Button activeButton;  // To keep track of the active (clicked) button
    private double offsetX, offsetY;  // For storing the mouse offset while dragging
    private String selectedDiagramKey = null;  // To store the key for the selected diagram

    private final double classDiagramWidth = 120;
    private final double classDiagramHeight = 90;

    // Map to store multiple diagrams' positions with unique keys
    private Map<String, double[]> diagrams = new HashMap<>();

    @FXML
    public void initialize() {
        Canvas canvas = new Canvas(910, 780);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);

        // Draw the grid once
        drawGrid(gc);

        List<Button> buttons = new ArrayList<>();
        buttons.add(classButton);
        buttons.add(interfaceButton);
        buttons.add(associationButton);
        buttons.add(aggregationButton);
        buttons.add(compositionButton);

        for (Button button : buttons) {
            button.setOnAction(event -> handleButtonClick(button, buttons));
        }

        // Set an event on the canvas container to create the class diagram when clicked
        canvasContainer.setOnMouseClicked(event -> {
            if (activeButton == classButton) {
                createClassDiagram(gc, event.getX(), event.getY());
            }
            // Check if a diagram is clicked
            selectDiagram(event.getX(), event.getY());
        });

        // Set mouse events for dragging
        canvasContainer.setOnMousePressed(this::onMousePressed);
        canvasContainer.setOnMouseDragged(this::onMouseDragged);

        // Set an event on the toolbox VBox to deselect all buttons when clicking on an empty space
        toolboxVBox.setOnMouseClicked(event -> deselectAllButtons(buttons));
    }

    private void handleButtonClick(Button clickedButton, List<Button> buttons) {
        // Reset styles of all buttons
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: gray; -fx-font-weight: normal;");
        }

        // Apply selected style to the clicked button
        clickedButton.setStyle("-fx-background-color: white; -fx-text-fill: blue; -fx-border-color: gray; -fx-font-weight: bold;");
        activeButton = clickedButton;
    }

    private void deselectAllButtons(List<Button> buttons) {
        // Reset the style for all buttons
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: gray; -fx-font-weight: normal;");
        }
        activeButton = null; // No active button selected
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setLineWidth(0.5);
        gc.setStroke(Color.rgb(180, 180, 180));
        for (int x = 0; x <= 910; x += 10) {
            gc.strokeLine(x, 0, x, 780);
        }
        for (int y = 0; y <= 780; y += 10) {
            gc.strokeLine(0, y, 910, y);
        }
    }

    private void createClassDiagram(GraphicsContext gc, double x, double y) {
        // Create a unique key for the diagram
        String key = "Class" + x + "," + y;

        // Store the diagram's position in the map
        diagrams.put(key, new double[]{x, y});

        // Draw the class diagram on the canvas
        drawClassDiagram(gc, x, y);
    }

    private void drawClassDiagram(GraphicsContext gc, double x, double y) {
        // Draw rectangle for the class diagram
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, classDiagramWidth, classDiagramHeight);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, classDiagramWidth, classDiagramHeight);

        // Draw lines for class name, attributes, and operations sections
        gc.strokeLine(x, y + 30, x + classDiagramWidth, y + 30);  // Line between class name and attributes
        gc.strokeLine(x, y + 60, x + classDiagramWidth, y + 60);  // Line between attributes and operations

        // Add text placeholders for class name, attributes, and operations
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 12));
        gc.fillText("ClassName", x + 10, y + 20);
        gc.fillText("Attributes", x + 10, y + 50);
        gc.fillText("Operations", x + 10, y + 80);
    }

    private void selectDiagram(double mouseX, double mouseY) {
        // Iterate through all diagrams to find the one clicked
        for (Map.Entry<String, double[]> entry : diagrams.entrySet()) {
            double[] position = entry.getValue();
            double diagramX = position[0];
            double diagramY = position[1];

            // Check if the mouse click is within the bounds of the diagram
            if (mouseX >= diagramX && mouseX <= diagramX + classDiagramWidth &&
                    mouseY >= diagramY && mouseY <= diagramY + classDiagramHeight) {
                selectedDiagramKey = entry.getKey();  // Select the diagram
                return;
            }
        }
        selectedDiagramKey = null;  // No diagram clicked
    }

    private void onMousePressed(MouseEvent event) {
        if (selectedDiagramKey != null) {
            double[] position = diagrams.get(selectedDiagramKey);
            // Store the initial mouse position relative to the class diagram
            offsetX = event.getX() - position[0];
            offsetY = event.getY() - position[1];
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (selectedDiagramKey != null) {
            double[] position = diagrams.get(selectedDiagramKey);
            double newX = event.getX() - offsetX;
            double newY = event.getY() - offsetY;

            // Ensure the diagram stays within the boundaries of the pane
            newX = Math.max(0, Math.min(newX, canvasContainer.getWidth() - classDiagramWidth));
            newY = Math.max(0, Math.min(newY, canvasContainer.getHeight() - classDiagramHeight));

            // Update the position of the selected diagram
            position[0] = newX;
            position[1] = newY;

            // Redraw the canvas with the updated position
            Canvas canvas = (Canvas) canvasContainer.getChildren().get(0);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());  // Clear the canvas
            drawGrid(gc);  // Redraw the grid

            // Redraw all diagrams, including the moved one
            for (Map.Entry<String, double[]> entry : diagrams.entrySet()) {
                double[] pos = entry.getValue();
                drawClassDiagram(gc, pos[0], pos[1]);
            }
        }
    }
}
