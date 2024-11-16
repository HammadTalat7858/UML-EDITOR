package com.example.pscd;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {

    public Pane canvasContainer;
    private Scale scaleTransform;  // To keep track of the scale transformation

    @FXML
    private VBox toolboxVBox;

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
    @FXML
    private TextField classNameField;
    @FXML
    private TextField attributesField;
    @FXML
    private TextField operationsField;
    @FXML
    private Button addAttributeButton;
    @FXML
    private Button addOperationButton;
    private Button activeButton;  // To keep track of the active (clicked) button
    private double offsetX, offsetY;  // For storing the mouse offset while dragging
    private String selectedDiagramKey = null;  // To store the key for the selected diagram

    private final double classDiagramWidth = 120;

    // Map to store class diagrams
    private Map<String, ClassDiagram> diagrams = new HashMap<>();

    // Line drawing state
    private boolean isDrawingLine = false;
    private double startX, startY, endX, endY;
    private ClassDiagram startDiagram, endDiagram;
    private List<LineConnection> lineConnections = new ArrayList<>();

    @FXML
    public void initialize() {
        Canvas canvas = new Canvas(910, 780);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);

        // Set up zoom functionality
        scaleTransform = new Scale(1, 1, 0, 0);  // Initialize with no scaling
        canvasContainer.getTransforms().add(scaleTransform);

        // Add event listeners for zooming
        canvasContainer.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);

        // Wait until the Scene is set before adding the KeyEvent filter
        canvasContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleZoomKeys);
            }
        });

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
                // Create a new class diagram if the Class button is active
                createClassDiagram(gc, event.getX(), event.getY());
            } else {
                // Handle selection and inline editing of a class diagram
                handleClassNameEditing(event, gc);
            }
        });


        // Set mouse events for dragging
        canvasContainer.setOnMousePressed(this::onMousePressed);
        canvasContainer.setOnMouseDragged(this::onMouseDragged);
        canvasContainer.setOnMouseReleased(this::onMouseReleased);

        // Set an event on the toolbox VBox to deselect all buttons when clicking on an empty space
        toolboxVBox.setOnMouseClicked(event -> deselectAllButtons(buttons));

        // Attach handlers for adding attributes and operations
        addAttributeButton.setOnAction(event -> onAddAttribute(gc));
        addOperationButton.setOnAction(event -> onAddOperation(gc));
    }

    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) { // Check if Ctrl is held down
            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;

            // Update scale factors
            double newScaleX = scaleTransform.getX() * zoomFactor;
            double newScaleY = scaleTransform.getY() * zoomFactor;

            // Constrain scaling to reasonable limits
            newScaleX = Math.max(1, Math.min(newScaleX, 5)); // Prevent zooming out too far or in too much
            newScaleY = Math.max(1, Math.min(newScaleY, 5));

            scaleTransform.setX(newScaleX);
            scaleTransform.setY(newScaleY);

            // Redraw canvas
            Canvas canvas = (Canvas) canvasContainer.getChildren().get(0);
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            drawGrid(gc);
            redrawDiagrams(gc); // Ensure all diagrams are redrawn
            event.consume();
        }
    }

    private void handleZoomKeys(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
                // Zoom in
                scaleTransform.setX(scaleTransform.getX() * 1.1);
                scaleTransform.setY(scaleTransform.getY() * 1.1);
            } else if (event.getCode() == KeyCode.MINUS) {
                // Zoom out
                scaleTransform.setX(scaleTransform.getX() * 0.9);
                scaleTransform.setY(scaleTransform.getY() * 0.9);
            }
        }
    }

    private void handleButtonClick(Button clickedButton, List<Button> buttons) {
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: gray; -fx-font-weight: normal;");
        }

        // Apply selected style to the clicked button
        clickedButton.setStyle("-fx-background-color: white; -fx-text-fill: blue; -fx-border-color: gray; -fx-font-weight: bold;");
        activeButton = clickedButton;
        System.out.println("Active button set to: " + clickedButton.getText());
    }

    private void deselectAllButtons(List<Button> buttons) {
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: gray; -fx-font-weight: normal;");
        }
        activeButton = null; // No active button selected
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setLineWidth(0.5);
        gc.setStroke(Color.rgb(180, 180, 180));
        for (int x = 0; x <= 880; x += 10) {
            gc.strokeLine(x, 0, x, 800);
        }
        for (int y = 0; y <= 800; y += 10) {
            gc.strokeLine(0, y, 880, y);
        }
    }

    private void createClassDiagram(GraphicsContext gc, double x, double y) {
        String key = "Class" + x + "," + y;
        if (!diagrams.containsKey(key)) {
            diagrams.put(key, new ClassDiagram(x, y)); // Create and store a new class diagram
            drawClassDiagram(gc, diagrams.get(key));
        }
    }
    private void drawClassDiagram(GraphicsContext gc, ClassDiagram classDiagram) {
        double x = classDiagram.x;
        double y = classDiagram.y;
        double width = classDiagramWidth;
        double baseHeight = 50;
        double attributeHeight = 20 * classDiagram.attributes.size();
        double operationHeight = 20 * classDiagram.operations.size();
        double height = baseHeight + attributeHeight + operationHeight;

        // Draw the class rectangle
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, width, height);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, width, height);

        // Draw separators
        gc.strokeLine(x, y + 30, x + width, y + 30);
        gc.strokeLine(x, y + 30 + attributeHeight, x + width, y + 30 + attributeHeight);

        // Draw class name
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));
        gc.fillText("Class1", x + 10, y + 20);
        // Draw attributes
        gc.setFont(Font.font("Arial", 12));
        for (int i = 0; i < classDiagram.attributes.size(); i++) {
            gc.fillText("+ " + classDiagram.attributes.get(i), x + 10, y + 40 + i * 20);
        }

        // Draw operations
        for (int i = 0; i < classDiagram.operations.size(); i++) {
            gc.fillText("+ " + classDiagram.operations.get(i), x + 10, y + 50 + attributeHeight + i * 20);
        }

        // Draw connection points
        classDiagram.height = height;
        gc.setFill(Color.RED);
        double[][] connectionPoints = classDiagram.getConnectionPoints();
        double radius = 5.0; // Radius of connection points

        for (double[] point : connectionPoints) {
            gc.fillOval(point[0] - radius, point[1] - radius, 2 * radius, 2 * radius);
        }


    }


    private void selectDiagram(double mouseX, double mouseY) {
        for (Map.Entry<String, ClassDiagram> entry : diagrams.entrySet()) {
            ClassDiagram diagram = entry.getValue();
            if (mouseX >= diagram.x && mouseX <= diagram.x + diagram.width &&
                    mouseY >= diagram.y && mouseY <= diagram.y + diagram.getHeight()) {
                selectedDiagramKey = entry.getKey();
                return;
            }
        }
        selectedDiagramKey = null; // No diagram selected
    }

    private void onMousePressed(MouseEvent event) {
        if (activeButton == null) {
            // Select a diagram for moving
            selectDiagram(event.getX(), event.getY());
            if (selectedDiagramKey != null) {
                ClassDiagram diagram = diagrams.get(selectedDiagramKey);
                offsetX = event.getX() - diagram.x;
                offsetY = event.getY() - diagram.y;
                System.out.println("Selected diagram: " + selectedDiagramKey);
            }
        } else if (activeButton == associationButton || activeButton == aggregationButton || activeButton == compositionButton) {
            // Start line drawing
            for (ClassDiagram diagram : diagrams.values()) {
                for (double[] point : diagram.getConnectionPoints()) {
                    if (isNear(event.getX(), event.getY(), point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startDiagram = diagram;
                        isDrawingLine = true;
                        return;
                    }
                }
            }
        }
    }


    private void onMouseDragged(MouseEvent event) {
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();

        if (activeButton == null) {
            // Handle dragging a class diagram
            if (selectedDiagramKey != null) {
                ClassDiagram diagram = diagrams.get(selectedDiagramKey);

                // Update the diagram's position while respecting canvas boundaries
                double newX = Math.max(0, Math.min(event.getX() - offsetX, canvasContainer.getWidth() - diagram.width));
                double newY = Math.max(0, Math.min(event.getY() - offsetY, canvasContainer.getHeight() - diagram.getHeight()));

                diagram.x = newX;
                diagram.y = newY;

                // Clear and redraw the canvas to reflect changes
                gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
                redrawCanvas(gc); // Automatically adjusts lines connected to the diagram
            }
        } else if (isDrawingLine) {
            // Handle line preview
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw existing elements, including connection points
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, event.getX(), event.getY());
        }
    }
    private void handleClassNameEditing(MouseEvent event, GraphicsContext gc) {
        // Check if any existing class diagram is clicked
        for (Map.Entry<String, ClassDiagram> entry : diagrams.entrySet()) {
            ClassDiagram classDiagram = entry.getValue();
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Check if the click occurred in the name area of the class diagram
            if (mouseX >= classDiagram.x && mouseX <= classDiagram.x + classDiagramWidth &&
                    mouseY >= classDiagram.y && mouseY <= classDiagram.y + 30) {

                // Ensure it's a double-click
                if (event.getClickCount() == 2) {
                    // Create a TextField at the class name's position
                    TextField nameField = new TextField(classDiagram.className);
                    nameField.setLayoutX(classDiagram.x + 10);
                    nameField.setLayoutY(classDiagram.y + 5);
                    nameField.setPrefWidth(classDiagramWidth - 20);
                    nameField.setStyle("-fx-border-color: blue; -fx-background-color: lightblue;");

                    // Add the TextField to the canvas
                    canvasContainer.getChildren().add(nameField);
                    nameField.requestFocus();

                    // Update the class name when "Enter" is pressed
                    nameField.setOnKeyPressed(keyEvent -> {
                        if (keyEvent.getCode() == KeyCode.ENTER) {
                            classDiagram.className = nameField.getText();
                            canvasContainer.getChildren().remove(nameField);
                            redrawCanvas(gc);
                        }
                    });

                    // Update the class name when focus is lost
                    nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) {
                            classDiagram.className = nameField.getText();
                            canvasContainer.getChildren().remove(nameField);
                            redrawCanvas(gc);
                        }
                    });

                    return; // Stop checking other diagrams after finding the clicked one
                }
            }
        }
    }


    private void onMouseReleased(MouseEvent event) {
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();

        if (isDrawingLine) {
            for (ClassDiagram diagram : diagrams.values()) {
                for (int i = 0; i < diagram.getConnectionPoints().length; i++) {
                    double[] point = diagram.getConnectionPoints()[i];
                    if (isNear(event.getX(), event.getY(), point[0], point[1]) && diagram != startDiagram) {
                        int startConnectionIndex = getNearestConnectionIndex(startDiagram, startX, startY);
                        int endConnectionIndex = i;

                        // Create a dynamic LineConnection
                        lineConnections.add(new LineConnection(
                                startDiagram, startConnectionIndex,
                                diagram, endConnectionIndex, activeButton
                        ));

                        // Redraw everything
                        redrawCanvas(gc);

                        // Reset state
                        isDrawingLine = false;
                        startDiagram = null;
                        endDiagram = null;
                        return;
                    }
                }
            }

            // If no valid endpoint is found
            isDrawingLine = false;
            redrawCanvas(gc);
            showError("Cannot draw line. No valid connection point reached.");
        }
    }

    private int getNearestConnectionIndex(ClassDiagram diagram, double x, double y) {
        double[][] connectionPoints = diagram.getConnectionPoints();
        for (int i = 0; i < connectionPoints.length; i++) {
            if (isNear(x, y, connectionPoints[i][0], connectionPoints[i][1])) {
                return i;
            }
        }
        return -1; // Should not happen for a valid start point
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Invalid Connection");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redrawDiagrams(GraphicsContext gc) {
        for (ClassDiagram diagram : diagrams.values()) {
            drawClassDiagram(gc, diagram);
        }
    }


    @FXML
    private void onAddAttribute(GraphicsContext gc) {
        if (selectedDiagramKey != null) {
            ClassDiagram diagram = diagrams.get(selectedDiagramKey);
            String attribute = attributesField.getText().trim();
            if (!attribute.isEmpty()) {
                diagram.attributes.add(attribute);
                attributesField.clear();
                redrawCanvas(gc);
            }
        }
    }

    @FXML
    private void onAddOperation(GraphicsContext gc) {
        if (selectedDiagramKey != null) {
            ClassDiagram diagram = diagrams.get(selectedDiagramKey);
            String operation = operationsField.getText().trim();
            if (!operation.isEmpty()) {
                diagram.operations.add(operation);
                operationsField.clear();
                redrawCanvas(gc);
            }
        }
    }

    private void redrawCanvas(GraphicsContext gc) {
        Canvas canvas = (Canvas) canvasContainer.getChildren().get(0);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid(gc);

        // Redraw all class diagrams
        for (ClassDiagram diagram : diagrams.values()) {
            drawClassDiagram(gc, diagram);
        }

        // Redraw all dynamic line connections
        for (LineConnection line : lineConnections) {
            double[] start = line.getStartPoint();
            double[] end = line.getEndPoint();
            drawLine(gc, start[0], start[1], end[0], end[1], line.lineType);
        }
    }


    private void drawLine(GraphicsContext gc, double startX, double startY, double endX, double endY, Button lineType) {
        gc.setLineWidth(2);
        if (lineType == associationButton) {
            gc.setStroke(Color.BLACK);
        } else if (lineType == aggregationButton) {
            gc.setStroke(Color.DARKBLUE);
        } else if (lineType == compositionButton) {
            gc.setStroke(Color.DARKGREEN);
        }
        gc.strokeLine(startX, startY, endX, endY);
    }


    private boolean isNear(double x1, double y1, double x2, double y2) {
        double tolerance = 10.0; // Snap radius
        return Math.abs(x1 - x2) < tolerance && Math.abs(y1 - y2) < tolerance;
    }



    private static class ClassDiagram {
        double x, y;
        double width = 120;  // Width of the rectangle
        double height = 50;  // Height of the rectangle
        List<String> attributes = new ArrayList<>();
        List<String> operations = new ArrayList<>();
        String className = "Class"; // Default class name

        ClassDiagram(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double[][] getConnectionPoints() {
            return new double[][]{
                    {x + width / 2, y},             // Top-center
                    {x + width, y + height / 2},    // Right-center
                    {x + width / 2, y + height},    // Bottom-center
                    {x, y + height / 2}             // Left-center
            };
        }

        public void drawConnectionPoints(GraphicsContext gc) {
            gc.setFill(Color.RED);
            double radius = 5.0;  // Radius of connection points
            for (double[] point : getConnectionPoints()) {
                gc.fillOval(point[0] - radius, point[1] - radius, 2 * radius, 2 * radius);
            }
        }
        double getHeight() {
            return 50 + 20 * attributes.size() + 20 * operations.size();
        }

    }

    private static class LineConnection {
        ClassDiagram startDiagram;
        ClassDiagram endDiagram;
        int startConnectionIndex; // Index of the connection point in the start diagram
        int endConnectionIndex;   // Index of the connection point in the end diagram
        Button lineType;

        LineConnection(ClassDiagram startDiagram, int startConnectionIndex,
                       ClassDiagram endDiagram, int endConnectionIndex, Button lineType) {
            this.startDiagram = startDiagram;
            this.startConnectionIndex = startConnectionIndex;
            this.endDiagram = endDiagram;
            this.endConnectionIndex = endConnectionIndex;
            this.lineType = lineType;
        }

        public double[] getStartPoint() {
            return startDiagram.getConnectionPoints()[startConnectionIndex];
        }

        public double[] getEndPoint() {
            return endDiagram.getConnectionPoints()[endConnectionIndex];
        }
    }



}
