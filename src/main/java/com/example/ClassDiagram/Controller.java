package com.example.ClassDiagram;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class Controller {

    public Pane canvasContainer;
    private Scale scaleTransform;

    @FXML
    private VBox toolboxVBox;

    @FXML   private Button deleteButton;
    @FXML
    private Button classButton;

    @FXML
    private Button associationButton;
    @FXML
    private Button aggregationButton;
    @FXML
    private Button compositionButton;
    @FXML
    private Button InheritanceButton;
    @FXML
    private ComboBox<String> attributeAccessModifier;

    @FXML
    private ComboBox<String> operationAccessModifier;
    @FXML
    private MenuItem jpegMenuItem;

    @FXML
    private MenuItem pngMenuItem;
    @FXML
    private MenuItem GenerateCode;
    @FXML
    private TreeView<String> classHierarchyView; // Hierarchy view for classes
    private TreeItem<String> rootItem;

    @FXML
    private MenuItem SaveAs;


    @FXML
    private MenuItem Load;
    @FXML
    private MenuItem Close;
    @FXML
    private MenuItem loadusecase;
    @FXML
    private MenuItem loadClass;

    @FXML
    private VBox propertiesPanel;



    @FXML
    private TextField attributesField;
    private Object selectedComponent = null;

    @FXML
    private TextField operationsField;
    @FXML
    private Button addAttributeButton;
    @FXML
    private Button interfaceButton;
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
        rootItem = new TreeItem<>("Model");
        rootItem.setExpanded(true);
        classHierarchyView.setRoot(rootItem);

        // Set up zoom functionality
        scaleTransform = new Scale(1, 1, 0, 0);  // Initialize with no scaling
        canvasContainer.getTransforms().add(scaleTransform);
        // Make canvasContainer focusable and request focus
        canvasContainer.setFocusTraversable(true);
        canvasContainer.requestFocus();

// Request focus when the user clicks on the canvasContainer
        canvasContainer.setOnMouseClicked(event -> canvasContainer.requestFocus());

        // Add event listeners for zooming
        canvasContainer.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);

        // Wait until the Scene is set before adding the KeyEvent filter
        canvasContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleZoomKeys);
            }
        });


        canvasContainer.widthProperty().addListener((observable, oldValue, newValue) -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.setWidth(newValue.doubleValue());
            drawGrid(gc);  // Redraw the grid with the new width
            redrawCanvas(gc);  // Redraw class diagrams and connections
        });



        canvasContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.setHeight(newValue.doubleValue());
            drawGrid(gc);  // Redraw the grid with the new height
            redrawCanvas(gc);  // Redraw class diagrams and connections
        });

        canvasContainer.focusedProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("canvasContainer focus: " + newVal);
        });

        canvas.focusedProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Canvas focus: " + newVal);
        });


        canvasContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.DELETE) {
                        deleteSelectedComponent();
                    }
                });
            }
        });





        // Draw the grid once
        drawGrid(gc);

        List<Button> buttons = new ArrayList<>();
        buttons.add(classButton);
        buttons.add(associationButton);
        buttons.add(aggregationButton);
        buttons.add(compositionButton);
        buttons.add(InheritanceButton);
        buttons.add(interfaceButton);

        for (Button button : buttons) {
            button.setOnAction(event -> handleButtonClick(button, buttons));
        }

        // Set an event on the canvas container to create the class diagram when clicked
        canvasContainer.setOnMouseClicked(event -> {
            if (activeButton == classButton) {
                // Create a new class diagram if the Class button is active
                createClassDiagram(gc, event.getX(), event.getY());
            } else if (activeButton == interfaceButton) {
                // Create a new interface diagram if the Interface button is active
                createInterfaceDiagram(gc, event.getX(), event.getY());
            } else {
                // Handle selection and inline editing of a class or interface diagram
                handleClassEditing(event, gc);
            }
        });



        // Set mouse events for dragging
        canvasContainer.setOnMousePressed(this::onMousePressed);
        canvasContainer.setOnMouseDragged(this::onMouseDragged);
        canvasContainer.setOnMouseReleased(this::onMouseReleased);


        // Attach handlers for adding attributes and operations
        addAttributeButton.setOnAction(event -> onAddAttribute(gc));
        addOperationButton.setOnAction(event -> onAddOperation(gc));
        deleteButton.setOnAction(actionEvent -> deleteSelectedComponent());
        toolboxVBox.setOnMouseClicked(event -> {
            clearSelection();
            deselectAllButtons(buttons);
        });

            propertiesPanel.setOnMouseClicked(event -> {
                clearSelection();
                deselectAllButtons(buttons);

            });


    }
    @FXML
    private void loadUseCaseDiagram() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Switch to Use Case Diagram? Unsaved changes will be lost.", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Switch");
        confirmation.setHeaderText(null);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/UseCaseDiagram/Use-Case.fxml"));
                Parent useCaseRoot = loader.load();
                Stage currentStage = (Stage) canvasContainer.getScene().getWindow();
                currentStage.setScene(new Scene(useCaseRoot));
                currentStage.setTitle("Use Case Diagram");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to load Use Case Diagram: " + e.getMessage());
            }
        }
    }
    @FXML
    private void loadClassDiagram() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Switch to Class Diagram? Unsaved changes will be lost.", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Switch");
        confirmation.setHeaderText(null);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ClassDiagram/class_diagram.fxml"));
                Parent classDiagramRoot = loader.load();
                Stage currentStage = (Stage) canvasContainer.getScene().getWindow();
                currentStage.setScene(new Scene(classDiagramRoot));
                currentStage.setTitle("Class Diagram");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to load Class Diagram: " + e.getMessage());
            }
        }
    }
    private void updateClassHierarchy() {
        Platform.runLater(() -> {
            rootItem.getChildren().clear(); // Clear the hierarchy
            for (ClassDiagram diagram : diagrams.values()) {
                TreeItem<String> classNode = new TreeItem<>(diagram.className);

                // Add attributes as children
                for (String attribute : diagram.attributes) {
                    classNode.getChildren().add(new TreeItem<>("Attribute: " + attribute));
                }

                // Add operations as children
                for (String operation : diagram.operations) {
                    classNode.getChildren().add(new TreeItem<>("Operation: " + operation));
                }

                rootItem.getChildren().add(classNode);
            }
        });
    }
    private void createInterfaceDiagram(GraphicsContext gc, double x, double y) {
        String key = "Interface" + x + "," + y;
        if (!diagrams.containsKey(key)) {
            diagrams.put(key, new InterfaceDiagram(x, y)); // Create and store a new interface diagram
            drawInterfaceDiagram(gc, (InterfaceDiagram) diagrams.get(key));
            updateClassHierarchy();
        }
    }
    private void drawInterfaceDiagram(GraphicsContext gc, InterfaceDiagram interfaceDiagram) {
        double x = interfaceDiagram.x;
        double y = interfaceDiagram.y;

        // Calculate the required width based on the widest text
        double maxTextWidth = getMaxTextWidth(gc, interfaceDiagram);
        double width = Math.max(classDiagramWidth, maxTextWidth + 40); // Add more padding

        double rowHeight = 30; // Row height for each section
        double baseHeight = rowHeight * 2; // Height for <<interface>> and name rows
        double operationHeight = Math.max(rowHeight, rowHeight * interfaceDiagram.operations.size()); // At least one row for operations
        double height = baseHeight + operationHeight; // Total height of the interface diagram

        // Update the interface diagram dimensions
        interfaceDiagram.width = width;
        interfaceDiagram.height = height;

        // Check if this diagram is selected
        boolean isSelected = selectedComponent == interfaceDiagram;

        // Draw the interface rectangle with a light green fill and green border if selected
        gc.setFill(isSelected ? Color.LIGHTGREEN : Color.WHITE); // Light green for selected
        gc.fillRect(x, y, width, height);
        gc.setStroke(isSelected ? Color.GREEN : Color.BLACK); // Green border for selected
        gc.setLineWidth(isSelected ? 3 : 2); // Thicker border for selected
        gc.strokeRect(x, y, width, height);

        // Draw <<interface>> label
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("<<interface>>", x + (width - 80) / 2, y + 15);

        // Draw interface name
        gc.setFont(Font.font("Arial", 14));
        gc.fillText(interfaceDiagram.interfaceName, x + 10, y + 40);

        // Draw operations (leave space if empty)
        gc.setFont(Font.font("Arial", 12));
        if (interfaceDiagram.operations.isEmpty()) {
            gc.fillText(" ", x + 10, y + baseHeight + rowHeight / 2 + 5); // Placeholder for empty operations
        } else {
            for (int i = 0; i < interfaceDiagram.operations.size(); i++) {
                gc.fillText(
                        interfaceDiagram.operations.get(i),
                        x + 10,
                        y + baseHeight + (i + 1) * rowHeight - 10 // Adjusted for spacing
                );
            }
        }

        // Draw connection points
        gc.setFill(Color.RED); // Red for connection points
        double[][] connectionPoints = interfaceDiagram.getConnectionPoints();
        double radius = 3.0; // Smaller radius for connection points

        for (double[] point : connectionPoints) {
            gc.fillOval(point[0] - radius, point[1] - radius, 2 * radius, 2 * radius);
        }
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
    @FXML
    private void handleCloseAction() {
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("You are about to close the application.");
        alert.setContentText("Are you sure you want to exit?");

        // Wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Exit the application
            Platform.exit();
        } else {
            // If the user cancels, do nothing
            alert.close();
        }
    }



    private void clearSelection() {
        selectedComponent = null; // Clear the selected component (class or line)
        selectedDiagramKey = null; // Clear the selected diagram key
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();
        redrawCanvas(gc); // Redraw the canvas to remove highlighting
    }


    private void handleZoomKeys(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.ADD ) {
                // Zoom in
                scaleTransform.setX(scaleTransform.getX() * 1.1);
                scaleTransform.setY(scaleTransform.getY() * 1.1);
            } else if (event.getCode() == KeyCode.SUBTRACT) {
                // Zoom out
                scaleTransform.setX(scaleTransform.getX() * 0.9);
                scaleTransform.setY(scaleTransform.getY() * 0.9);
            }
        }
    }

    private void handleButtonClick(Button clickedButton, List<Button> buttons) {
        if (activeButton == clickedButton) {
            // If the clicked button is already active, deselect it
            clickedButton.getStyleClass().remove("tool-button-selected");
            if (!clickedButton.getStyleClass().contains("tool-button")) {
                clickedButton.getStyleClass().add("tool-button");
            }
            activeButton = null; // Clear the active button
        } else {
            // Otherwise, deselect all buttons and select the clicked one
            deselectAllButtons(buttons);
            clickedButton.getStyleClass().add("tool-button-selected");
            activeButton = clickedButton; // Update the active button
        }
    }


    private void deselectAllButtons(List<Button> buttons) {
        for (Button button : buttons) {
            // Remove the "tool-button-selected" style class
            button.getStyleClass().remove("tool-button-selected");

            // Ensure the default "tool-button" style class is present
            if (!button.getStyleClass().contains("tool-button")) {
                button.getStyleClass().add("tool-button");
            }
        }
        activeButton = null; // Clear the active button
    }


    private void drawGrid(GraphicsContext gc) {
        double canvasWidth = canvasContainer.getWidth();
        double canvasHeight = canvasContainer.getHeight();

        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        double gridSpacing = 10;

        gc.setLineWidth(0.5);
        gc.setStroke(Color.rgb(180, 180, 180));

        for (double x = 0; x <= canvasWidth; x += gridSpacing) {
            gc.strokeLine(x, 0, x, canvasHeight);
        }
        for (double y = 0; y <= canvasHeight; y += gridSpacing) {
            gc.strokeLine(0, y, canvasWidth, y);
        }
    }


    private void createClassDiagram(GraphicsContext gc, double x, double y) {
        String key = "Class" + x + "," + y;
        if (!diagrams.containsKey(key)) {
            diagrams.put(key, new ClassDiagram(x, y)); // Create and store a new class diagram
            drawClassDiagram(gc, diagrams.get(key));
            updateClassHierarchy();
        }
    }
    private void drawClassDiagram(GraphicsContext gc, ClassDiagram classDiagram) {
        double x = classDiagram.x;
        double y = classDiagram.y;

        // Calculate the required width based on the widest text
        double maxTextWidth = getMaxTextWidth(gc, classDiagram);
        double width = Math.max(classDiagramWidth, maxTextWidth + 40); // Add more padding

        double rowHeight = 30; // Row height for each section
        double baseHeight = rowHeight; // Height for the class name row
        double attributeHeight = Math.max(rowHeight, rowHeight * classDiagram.attributes.size()); // At least one row for attributes
        double operationHeight = Math.max(rowHeight, rowHeight * classDiagram.operations.size()); // At least one row for operations
        double height = baseHeight + attributeHeight + operationHeight; // Total height of the class diagram

        // Update the class diagram dimensions
        classDiagram.width = width;
        classDiagram.height = height;

        // Check if this diagram is selected
        boolean isSelected = selectedComponent == classDiagram;

        // Draw the class rectangle with a light blue fill and blue border if selected
        gc.setFill(isSelected ? Color.LIGHTBLUE : Color.WHITE); // Light blue for selected
        gc.fillRect(x, y, width, height);
        gc.setStroke(isSelected ? Color.BLUE : Color.BLACK); // Blue border for selected
        gc.setLineWidth(isSelected ? 3 : 2); // Thicker border for selected
        gc.strokeRect(x, y, width, height);

        // Draw separators
        gc.strokeLine(x, y + rowHeight, x + width, y + rowHeight); // Separator below class name
        gc.strokeLine(x, y + rowHeight + attributeHeight, x + width, y + rowHeight + attributeHeight); // Separator below attributes

        // Draw class name
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));

// Use a Text object to calculate the text width
        Text textHelper = new Text(classDiagram.className);
        textHelper.setFont(gc.getFont());
        double textWidth = textHelper.getBoundsInLocal().getWidth(); // Calculate text width
        double textHeight = textHelper.getBoundsInLocal().getHeight(); // Calculate text height

        double textX = x + (width - textWidth) / 2; // Center the text horizontally
        double textY = y + rowHeight / 2 + textHeight / 4; // Center the text vertically

        gc.fillText(classDiagram.className, textX, textY);

        // Draw attributes (leave space if empty)
        gc.setFont(Font.font("Arial", 12));
        if (classDiagram.attributes.isEmpty()) {
            gc.fillText(" ", x + 10, y + rowHeight + rowHeight / 2 + 5); // Placeholder for empty attributes
        } else {
            for (int i = 0; i < classDiagram.attributes.size(); i++) {
                gc.fillText(
                        classDiagram.attributes.get(i),
                        x + 10,
                        y + rowHeight + (i + 1) * rowHeight - 10 // Adjusted for spacing
                );
            }
        }

        // Draw operations (leave space if empty)
        if (classDiagram.operations.isEmpty()) {
            gc.fillText(" ", x + 10, y + rowHeight + attributeHeight + rowHeight / 2 + 5); // Placeholder for empty operations
        } else {
            for (int i = 0; i < classDiagram.operations.size(); i++) {
                gc.fillText(
                        classDiagram.operations.get(i),
                        x + 10,
                        y + rowHeight + attributeHeight + (i + 1) * rowHeight - 10 // Adjusted for spacing
                );
            }
        }

        // Draw connection points
        gc.setFill(Color.RED); // Red for connection points
        double[][] connectionPoints = classDiagram.getConnectionPoints();
        double radius = 3.0; // Smaller radius for connection points

        for (double[] point : connectionPoints) {
            gc.fillOval(point[0] - radius, point[1] - radius, 2 * radius, 2 * radius);
        }
    }

    private double getMaxTextWidth(GraphicsContext gc, ClassDiagram classDiagram) {
        Text textHelper = new Text();
        textHelper.setFont(Font.font("Arial", 12));

        // Calculate the width of the class name
        textHelper.setText(classDiagram.className);
        double maxWidth = textHelper.getLayoutBounds().getWidth();

        // Calculate the width of attributes
        for (String attribute : classDiagram.attributes) {
            textHelper.setText(attribute);
            double attributeWidth = textHelper.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, attributeWidth);
        }

        // Calculate the width of operations
        for (String operation : classDiagram.operations) {
            textHelper.setText(operation);
            double operationWidth = textHelper.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, operationWidth);
        }

        return maxWidth;
    }

    private void selectDiagram(double mouseX, double mouseY) {
        // Iterate over all class diagrams to find the one containing the mouse coordinates
        for (Map.Entry<String, ClassDiagram> entry : diagrams.entrySet()) {
            ClassDiagram diagram = entry.getValue();

            // Check if the mouse click falls within the bounding box of the class diagram
            if (mouseX >= diagram.x && mouseX <= diagram.x + diagram.width &&
                    mouseY >= diagram.y && mouseY <= diagram.y + diagram.height) {
                selectedDiagramKey = entry.getKey(); // Store the selected diagram key
                selectedComponent = diagram;        // Mark the class diagram as selected
                return;
            }
        }

        // If no diagram is selected, clear the selection
        selectedDiagramKey = null;
        selectedComponent = null;
    }

    private boolean isEmptyArea(double mouseX, double mouseY) {
        // Check if mouse is near any class diagram
        for (ClassDiagram diagram : diagrams.values()) {
            if (mouseX >= diagram.x && mouseX <= diagram.x + diagram.width &&
                    mouseY >= diagram.y && mouseY <= diagram.y + diagram.height) {
                return false; // Mouse is over a class diagram
            }
        }

        // Check if mouse is near any line
        for (LineConnection line : lineConnections) {
            if (isNearLine(mouseX, mouseY, line)) {
                return false; // Mouse is near a line
            }

            // Check control points of the line
            for (double[] controlPoint : line.controlPoints) {
                if (isNear(mouseX, mouseY, controlPoint[0], controlPoint[1])) {
                    return false; // Mouse is near a control point
                }
            }
        }

        // If none of the above conditions matched, it's an empty area
        return true;
    }

    private void onMousePressed(MouseEvent event) {
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();

        // Handle double-click for editing line text
        if (event.getClickCount() == 2) {
            for (LineConnection line : lineConnections) {
                if (isNearLine(event.getX(), event.getY(), line)) {
                    showLineTextField(event.getX(), event.getY(), line, gc);
                    return;
                }
            }
        }
        else if (activeButton == null && selectedComponent instanceof LineConnection &&(!(isEmptyArea(event.getX(), event.getY())))) {
            LineConnection selectedLine = (LineConnection) selectedComponent;

            // Check if clicking near an existing control point
            for (double[] controlPoint : selectedLine.controlPoints) {
                if (isNear(event.getX(), event.getY(), controlPoint[0], controlPoint[1])) {
                    // Select the control point
                    selectedComponent = controlPoint;
                    return;
                }
            }

            // Check if clicking near the line to add a new control point
            if (isNearLine(event.getX(), event.getY(), selectedLine)) {
                selectedLine.controlPoints.add(new double[]{event.getX(), event.getY()});
                redrawCanvas(gc);
                return;
            }
            boolean componentSelected = false;

            // Check if a line is clicked
            for (LineConnection line : lineConnections) {
                if (isNearLine(event.getX(), event.getY(), line)) {
                    selectedComponent = line;
                    componentSelected = true;
                    break;
                }
            }

            // Check if a class diagram is clicked
            if (!componentSelected) {
                selectDiagram(event.getX(), event.getY());
                if (selectedDiagramKey != null) {
                    selectedComponent = diagrams.get(selectedDiagramKey);
                    ClassDiagram diagram = diagrams.get(selectedDiagramKey);
                    offsetX = event.getX() - diagram.x;
                    offsetY = event.getY() - diagram.y;
                    componentSelected = true;
                }
            }

            // If no component is selected, deselect everything
            if (!componentSelected) {
                selectedComponent = null;
            }

            // Redraw to reflect deselection or selection
            redrawCanvas(gc);
        }
        else if (activeButton == null) {

            boolean componentSelected = false;

            // Check if a line is clicked
            for (LineConnection line : lineConnections) {
                if (isNearLine(event.getX(), event.getY(), line)) {
                    selectedComponent = line;
                    componentSelected = true;
                    break;
                }
            }

            // Check if a class diagram is clicked
            if (!componentSelected) {
                selectDiagram(event.getX(), event.getY());
                if (selectedDiagramKey != null) {
                    selectedComponent = diagrams.get(selectedDiagramKey);
                    ClassDiagram diagram = diagrams.get(selectedDiagramKey);
                    offsetX = event.getX() - diagram.x;
                    offsetY = event.getY() - diagram.y;
                    componentSelected = true;
                }
            }

            // If no component is selected, deselect everything
            if (!componentSelected) {
                selectedComponent = null;
            }

            // Redraw to reflect deselection or selection
            redrawCanvas(gc);
        }

        else if (activeButton == associationButton || activeButton == aggregationButton ||
                activeButton == compositionButton || activeButton == InheritanceButton) {
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

    @FXML
    private void deleteSelectedComponent() {
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();

        if (selectedComponent instanceof ClassDiagram) {
            // Delete the selected class diagram and its connections
            ClassDiagram diagram = (ClassDiagram) selectedComponent;

            // Remove the diagram from the map
            diagrams.values().remove(diagram);

            // Remove all lines connected to this diagram
            lineConnections.removeIf(line -> line.startDiagram == diagram || line.endDiagram == diagram);

            selectedComponent = null;
            updateClassHierarchy();
        } else if (selectedComponent instanceof LineConnection) {
            // Delete the selected line connection
            LineConnection line = (LineConnection) selectedComponent;

            lineConnections.remove(line); // Remove the line
            selectedComponent = null; // Clear selection
        }

        // Redraw canvas to reflect changes
        gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
        redrawCanvas(gc);
    }



    private void onMouseDragged(MouseEvent event) {
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();

        if (activeButton == null) {
            if (selectedComponent instanceof ClassDiagram) {
                // Handle dragging a class diagram
                ClassDiagram diagram = (ClassDiagram) selectedComponent;

                // Update the diagram's position while respecting canvas boundaries
                double newX = Math.max(0, Math.min(event.getX() - offsetX, canvasContainer.getWidth() - diagram.width));
                double newY = Math.max(0, Math.min(event.getY() - offsetY, canvasContainer.getHeight() - diagram.getHeight()));

                diagram.x = newX;
                diagram.y = newY;

                // Clear and redraw the canvas to reflect changes
                gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
                redrawCanvas(gc);
            } else if (selectedComponent instanceof double[]) {
                // Handle dragging an existing control point
                double[] controlPoint = (double[]) selectedComponent;
                controlPoint[0] = event.getX();
                controlPoint[1] = event.getY();

                // Clear and redraw the canvas to reflect changes
                gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
                redrawCanvas(gc);
            } else if (selectedComponent instanceof LineConnection) {
                // Handle dragging the line by moving all control points
                LineConnection selectedLine = (LineConnection) selectedComponent;

                for (double[] controlPoint : selectedLine.controlPoints) {
                    controlPoint[0] += event.getX() - startX;
                    controlPoint[1] += event.getY() - startY;
                }

                // Update startX and startY to track the dragging motion
                startX = event.getX();
                startY = event.getY();

                // Clear and redraw the canvas to reflect changes
                gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
                redrawCanvas(gc);
            }
        } else if (isDrawingLine) {
            // Handle line preview
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc);
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, event.getX(), event.getY());
        }
    }

    private void handleClassEditing(MouseEvent event, GraphicsContext gc) {
        for (Map.Entry<String, ClassDiagram> entry : diagrams.entrySet()) {
            ClassDiagram classDiagram = entry.getValue();
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Check if the click occurred in the name area of the class diagram
            if (mouseX >= classDiagram.x && mouseX <= classDiagram.x + classDiagramWidth) {
                if (mouseY >= classDiagram.y && mouseY <= classDiagram.y + 30) {
                    // Double-click to edit class name
                    if (event.getClickCount() == 2) {
                        editClassName(classDiagram, gc);
                    }
                    return;
                }

                // Check if the click occurred in the attributes area
                double attributeStartY = classDiagram.y + 30;
                double attributeEndY = attributeStartY + 20 * classDiagram.attributes.size();
                if (mouseY >= attributeStartY && mouseY <= attributeEndY) {
                    if (event.getClickCount() == 2) {
                        int index = (int) ((mouseY - attributeStartY) / 20);
                        editAttribute(classDiagram, index, gc);
                    }
                    return;
                }

                // Check if the click occurred in the operations area
                double operationStartY = attributeEndY + 10;
                double operationEndY = operationStartY + 20 * classDiagram.operations.size();
                if (mouseY >= operationStartY && mouseY <= operationEndY) {
                    if (event.getClickCount() == 2) {
                        int index = (int) ((mouseY - operationStartY) / 20);
                        editOperation(classDiagram, index, gc);
                    }
                    return;
                }
            }
        }
    }

    private void editAttribute(ClassDiagram classDiagram, int index, GraphicsContext gc) {
        double startY = classDiagram.y + 30 + index * 20; // Position of the attribute row
        String attribute = classDiagram.attributes.get(index);

        // Create the TextField
        TextField attributeField = new TextField(attribute);
        attributeField.setLayoutX(classDiagram.x + 12); // Align with attribute text
        attributeField.setLayoutY(startY - 8); // Adjust Y to match text alignment
        attributeField.setPrefWidth(classDiagramWidth - 24); // Fit inside the class box
        attributeField.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-font-size: 12px; -fx-text-fill: black;");

        // Add the TextField to the canvas
        canvasContainer.getChildren().add(attributeField);
        attributeField.requestFocus();

        // Commit changes on Enter
        attributeField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                classDiagram.attributes.set(index, attributeField.getText().trim());
                canvasContainer.getChildren().remove(attributeField);
                redrawCanvas(gc);
            }
        });

        // Commit changes on focus loss
        attributeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                classDiagram.attributes.set(index, attributeField.getText().trim());
                canvasContainer.getChildren().remove(attributeField);
                redrawCanvas(gc);
            }
        });
    }
    private void editOperation(ClassDiagram classDiagram, int index, GraphicsContext gc) {
        double attributeHeight = 20 * classDiagram.attributes.size();
        double startY = classDiagram.y + 30 + attributeHeight + 20 + index * 20; // Position of the operation row
        String operation = classDiagram.operations.get(index);

        // Create the TextField
        TextField operationField = new TextField(operation);
        operationField.setLayoutX(classDiagram.x + 12); // Align with operation text
        operationField.setLayoutY(startY - 8); // Adjust Y to match text alignment
        operationField.setPrefWidth(classDiagramWidth - 24); // Fit inside the class box
        operationField.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-font-size: 12px; -fx-text-fill: black;");

        // Add the TextField to the canvas
        canvasContainer.getChildren().add(operationField);
        operationField.requestFocus();

        // Commit changes on Enter
        operationField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                classDiagram.operations.set(index, operationField.getText().trim());
                canvasContainer.getChildren().remove(operationField);
                redrawCanvas(gc);
            }
        });

        // Commit changes on focus loss
        operationField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                classDiagram.operations.set(index, operationField.getText().trim());
                canvasContainer.getChildren().remove(operationField);
                redrawCanvas(gc);
            }
        });
    }
    private void editClassName(ClassDiagram classDiagram, GraphicsContext gc) {
        TextField nameField = new TextField(classDiagram.className);
        nameField.setLayoutX(classDiagram.x + 10);
        nameField.setLayoutY(classDiagram.y + 5);
        nameField.setPrefWidth(classDiagramWidth - 20);
        nameField.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-font-size: 12px; -fx-text-fill: black;");

        canvasContainer.getChildren().add(nameField);
        nameField.requestFocus();

        nameField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                classDiagram.className = nameField.getText().trim();
                canvasContainer.getChildren().remove(nameField);
                redrawCanvas(gc);
                updateClassHierarchy();
            }
        });

        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                classDiagram.className = nameField.getText().trim();
                canvasContainer.getChildren().remove(nameField);
                redrawCanvas(gc);
                updateClassHierarchy();
            }
        });
    }
    private String getAccessModifierSymbol(String accessModifier) {
        switch (accessModifier.toLowerCase()) {
            case "public":
                return "+";
            case "private":
                return "-";
            case "protected":
                return "#";
            case "package-private":
                return "~";
            default:
                return ""; // Default case (no symbol)
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
    @FXML
    private void loadDiagramFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Diagram");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Diagram Files", "*.diagram"));
        File file = fileChooser.showOpenDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                // Read diagrams and connections from file
                diagrams = (HashMap<String, ClassDiagram>) ois.readObject();
                lineConnections = (ArrayList<LineConnection>) ois.readObject();

                // Reinitialize  fields or objects as needed
                initializeConnections();

                // Redraw the canvas with the loaded data
                GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();
                gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
                redrawCanvas(gc);

                showInfo("Diagram loaded successfully from " + file.getName());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showError("Error loading diagram: " + e.getMessage());
            }
        }
    }


    @FXML
    private void saveDiagramToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Diagram Files", "*.diagram"));
        File file = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Write diagrams and connections to file
                oos.writeObject(new HashMap<>(diagrams));
                oos.writeObject(new ArrayList<>(lineConnections));
                showInfo("Diagram saved successfully to " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error saving diagram: " + e.getMessage());
            }
        }
    }
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    private void exportAsJPEG() {
        saveCanvasToFile("jpeg");
    }

    @FXML
    private void exportAsPNG() {
        saveCanvasToFile("png");
    }

    private void saveCanvasToFile(String format) {
        Canvas canvas = (Canvas) canvasContainer.getChildren().get(0);

        // Take a snapshot of the canvas
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);

        // Open a file chooser to save the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram as " + format.toUpperCase());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(format.toUpperCase() + " Files", "*." + format));
        File file = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            System.out.println("Saving to file: " + file.getAbsolutePath());
            try {
                // Convert WritableImage to BufferedImage
                BufferedImage bufferedImage = convertToBufferedImage(writableImage);

                // Handle JPEG alpha channel issue
                if ("jpeg".equalsIgnoreCase(format)) {
                    bufferedImage = removeAlphaChannel(bufferedImage);
                }

                // Save the image to the file
                boolean result = ImageIO.write(bufferedImage, format, file);
                if (!result) {
                    System.err.println("ImageIO.write() failed for format: " + format);
                } else {
                    System.out.println("Image successfully saved to: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error saving file: " + e.getMessage());
            }
        } else {
            System.err.println("File selection cancelled.");
        }
    }


    private BufferedImage convertToBufferedImage(WritableImage writableImage) {
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        PixelReader pixelReader = writableImage.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }
    private BufferedImage removeAlphaChannel(BufferedImage originalImage) {
        BufferedImage rgbImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = rgbImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, java.awt.Color.WHITE, null); // Use white as the background color
        graphics.dispose();
        return rgbImage;
    }


    @FXML
    private void onAddAttribute(GraphicsContext gc) {
        if (selectedDiagramKey != null) {
            ClassDiagram diagram = diagrams.get(selectedDiagramKey);
            String attributeName = attributesField.getText().trim();
            String accessModifier = attributeAccessModifier.getValue(); // Get the selected access modifier

            if (!attributeName.isEmpty() && accessModifier != null) {
                // Determine the prefix based on the access modifier
                String prefix = getAccessModifierSymbol(accessModifier);
                diagram.attributes.add(prefix + " " + attributeName); // Add the attribute with the prefix
                attributesField.clear();
                redrawCanvas(gc);
                updateClassHierarchy();
            } else {
                showError("Please enter an attribute name and select an access modifier.");
            }
        }
    }



    @FXML
    private void onAddOperation(GraphicsContext gc) {
        if (selectedDiagramKey != null) {
            ClassDiagram diagram = diagrams.get(selectedDiagramKey);
            String operationName = operationsField.getText().trim();
            String accessModifier = operationAccessModifier.getValue(); // Get the selected access modifier

            if (!operationName.isEmpty() && accessModifier != null) {
                // Determine the prefix based on the access modifier
                String prefix = getAccessModifierSymbol(accessModifier);
                diagram.operations.add(prefix + " " + operationName); // Add the operation with the prefix
                operationsField.clear();

                redrawCanvas(gc);
                updateClassHierarchy();
            } else {
                showError("Please enter an operation name and select an access modifier.");
            }
        }
    }


    private void redrawCanvas(GraphicsContext gc) {
        Canvas canvas = (Canvas) canvasContainer.getChildren().get(0);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid(gc);

        // Redraw all class diagrams
        for (ClassDiagram diagram : diagrams.values()) {
            if (diagram instanceof InterfaceDiagram) {
                drawInterfaceDiagram(gc, (InterfaceDiagram) diagram);
            } else {
                drawClassDiagram(gc, diagram);
            }
        }

        // Redraw all dynamic line connections
        for (LineConnection line : lineConnections) {
            List<double[]> points = line.getAllPoints(); // Get all points (start, control points, and end)
            boolean isSelected = line.equals(selectedComponent);

            // Draw each segment of the line
            gc.setStroke(isSelected ? Color.web("#5DADE2") : Color.BLACK);
            gc.setLineWidth(isSelected ? 4 : 2);

            for (int i = 0; i < points.size() - 1; i++) {
                double[] start = points.get(i);
                double[] end = points.get(i + 1);

                // Stop the last segment before a shape if necessary
                if (i == points.size() - 2) { // Last segment
                    double[] adjustedEnd = end.clone(); // Clone end to modify
                    if (line.lineType == InheritanceButton) {
                        adjustedEnd = calculateTriangleBaseIntersection(end, start, 15); // Adjust for hollow triangle
                    } else if (line.lineType == aggregationButton || line.lineType == compositionButton) {
                        adjustedEnd = calculateDiamondBaseIntersection(end, start, 15); // Adjust for diamond
                    }
                    gc.strokeLine(start[0], start[1], adjustedEnd[0], adjustedEnd[1]);
                } else {
                    gc.strokeLine(start[0], start[1], end[0], end[1]);
                }
            }

            // Draw control points for intermediate points only
            gc.setFill(Color.BLACK);
            for (int i = 1; i < points.size() - 1; i++) {
                double[] controlPoint = points.get(i);
                gc.fillOval(controlPoint[0] - 4, controlPoint[1] - 4, 8, 8); // Draw control point circles
            }

            // Draw shapes at the end of the line
            double[] start = points.get(points.size() - 2); // Second-to-last point
            double[] end = points.get(points.size() - 1);   // Last point
            if (line.lineType == aggregationButton) {
                drawDiamond(gc, end[0], end[1], start[0], start[1], isSelected, false); // Hollow diamond
            } else if (line.lineType == compositionButton) {
                drawDiamond(gc, end[0], end[1], start[0], start[1], isSelected, true); // Filled diamond
            } else if (line.lineType == InheritanceButton) {
                drawHollowTriangle(gc, end[0], end[1], start[0], start[1], isSelected); // Hollow triangle
            }

            // Draw line text if it exists
            if (line.text != null && !line.text.isEmpty()) {
                double midX = (start[0] + end[0]) / 2;
                double midY = (start[1] + end[1]) / 2;

                // Keep text black for simplicity
                gc.setFill(Color.BLACK);
                gc.setFont(Font.font("Arial", 12));
                gc.fillText(line.text, midX - line.text.length() * 3, midY - 5); // Adjust position based on text length
            }
        }
    }
    private double[] calculateTriangleBaseIntersection(double[] end, double[] start, double triangleSize) {
        double angle = Math.atan2(end[1] - start[1], end[0] - start[0]);
        double intersectionX = end[0] - triangleSize * Math.cos(angle);
        double intersectionY = end[1] - triangleSize * Math.sin(angle);
        return new double[]{intersectionX, intersectionY};
    }

    private double[] calculateDiamondBaseIntersection(double[] end, double[] start, double diamondSize) {
        double angle = Math.atan2(end[1] - start[1], end[0] - start[0]);
        double intersectionX = end[0] - 2 * diamondSize * Math.cos(angle);
        double intersectionY = end[1] - 2 * diamondSize * Math.sin(angle);
        return new double[]{intersectionX, intersectionY};
    }




    private void drawHollowTriangle(GraphicsContext gc, double endX, double endY, double startX, double startY, boolean isSelected) {
        double triangleSize = 15; // Size of the triangle

        // Calculate the angle of the line
        double angle = Math.atan2(endY - startY, endX - startX);

        // Calculate the three points of the triangle
        double[] xPoints = new double[3];
        double[] yPoints = new double[3];
        xPoints[0] = endX; // Tip of the triangle
        yPoints[0] = endY;
        xPoints[1] = endX - triangleSize * Math.cos(angle - Math.PI / 6); // Left base
        yPoints[1] = endY - triangleSize * Math.sin(angle - Math.PI / 6);
        xPoints[2] = endX - triangleSize * Math.cos(angle + Math.PI / 6); // Right base
        yPoints[2] = endY - triangleSize * Math.sin(angle + Math.PI / 6);

        // Draw the triangle with highlight if selected
        gc.setStroke(isSelected ? Color.web("#5DADE2") : Color.BLACK);
        gc.setLineWidth(isSelected ? 3 : 2); // Thicker border if selected
        gc.strokePolygon(xPoints, yPoints, 3); // Hollow triangle
    }



    private void showLineTextField(double mouseX, double mouseY, LineConnection line, GraphicsContext gc) {
        // Calculate the midpoint of the line
        double[] start = line.getStartPoint();
        double[] end = line.getEndPoint();
        double midX = (start[0] + end[0]) / 2;
        double midY = (start[1] + end[1]) / 2;

        // Create the TextField
        TextField lineTextField = new TextField(line.text == null ? "" : line.text); // Use existing text if present
        lineTextField.setLayoutX(midX - 50); // Adjust position
        lineTextField.setLayoutY(midY - 10);
        lineTextField.setPrefWidth(100);
        lineTextField.setStyle("-fx-border-color: blue; -fx-background-color: lightyellow;");

        // Add the TextField to the canvas
        canvasContainer.getChildren().add(lineTextField);
        lineTextField.requestFocus();

        // Commit changes on Enter
        lineTextField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                line.text = lineTextField.getText().trim();
                canvasContainer.getChildren().remove(lineTextField);
                redrawCanvas(gc); // Redraw the canvas with the updated text
            }
        });

        // Remove TextField on focus loss
        lineTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                line.text = lineTextField.getText().trim();
                canvasContainer.getChildren().remove(lineTextField);
                redrawCanvas(gc);
            }
        });
    }



    private boolean isNearLine(double mouseX, double mouseY, LineConnection line) {
        List<double[]> points = new ArrayList<>();
        points.add(line.getStartPoint()); // Add starting point
        points.addAll(line.controlPoints); // Add all control points
        points.add(line.getEndPoint()); // Add ending point

        // Iterate through the segments between points
        for (int i = 0; i < points.size() - 1; i++) {
            double[] start = points.get(i);
            double[] end = points.get(i + 1);

            // Check if the mouse is near the segment
            if (pointToSegmentDistance(mouseX, mouseY, start[0], start[1], end[0], end[1]) < 10.0) {
                return true; // Close enough to the line segment
            }
        }

        return false; // Not near any segment of the line
    }

    private double pointToSegmentDistance(double px, double py, double x1, double y1, double x2, double y2) {
        double lineLengthSquared = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);

        if (lineLengthSquared == 0) {
            // The line segment is a single point
            return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));
        }

        // Calculate the projection of the point onto the line segment
        double t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / lineLengthSquared;
        t = Math.max(0, Math.min(1, t)); // Clamp t to the segment

        // Find the projection point on the line segment
        double projX = x1 + t * (x2 - x1);
        double projY = y1 + t * (y2 - y1);

        // Return the distance from the point to the projection
        return Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
    }


    // Helper method to draw the diamond
    private void drawDiamond(GraphicsContext gc, double endX, double endY, double startX, double startY, boolean isSelected, boolean filled) {
        double diamondSize = 15; // Size of the diamond
        Color fillColor = isSelected ? Color.web("#5DADE2") : Color.BLACK; // Use the attribute button color for selected lines
        Color borderColor = isSelected ? Color.web("#5DADE2"): Color.BLACK; // Use blue for the border if selected

        // Calculate the angle of the line
        double angle = Math.atan2(endY - startY, endX - startX);

        // Calculate the four points of the diamond
        double[] xPoints = new double[4];
        double[] yPoints = new double[4];
        xPoints[0] = endX; // Tip of the diamond
        yPoints[0] = endY;
        xPoints[1] = endX - diamondSize * Math.cos(angle - Math.PI / 4); // Top-left corner
        yPoints[1] = endY - diamondSize * Math.sin(angle - Math.PI / 4);
        xPoints[2] = endX - 2 * diamondSize * Math.cos(angle); // Bottom corner
        yPoints[2] = endY - 2 * diamondSize * Math.sin(angle);
        xPoints[3] = endX - diamondSize * Math.cos(angle + Math.PI / 4); // Top-right corner
        yPoints[3] = endY - diamondSize * Math.sin(angle + Math.PI / 4);

        // Draw the diamond
        if (filled) {
            gc.setFill(fillColor); // Fill with the selected color
            gc.fillPolygon(xPoints, yPoints, 4);
        }

        // Draw the border
        gc.setStroke(borderColor);
        gc.setLineWidth(isSelected ? 3 : 2); // Thicker border if selected
        gc.strokePolygon(xPoints, yPoints, 4);
    }


    private boolean isNear(double x1, double y1, double x2, double y2) {
        double tolerance = 10.0; // Snap radius
        return Math.abs(x1 - x2) < tolerance && Math.abs(y1 - y2) < tolerance;
    }
    private void initializeConnections() {
        for (LineConnection line : lineConnections) {
            line.initializeButton(aggregationButton, compositionButton, associationButton, InheritanceButton);
        }
    }
    @FXML private void exportToJavaCode() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Folder");
        File folder = directoryChooser.showDialog(canvasContainer.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            for (ClassDiagram diagram : diagrams.values()) {
                String classCode = generateClassCode(diagram);
                File javaFile = new File(folder, diagram.className + ".java");

                try (PrintWriter writer = new PrintWriter(javaFile)) {
                    writer.write(classCode);
                    showInfo("Class " + diagram.className + " exported to " + javaFile.getAbsolutePath());
                } catch (IOException e) {
                    showError("Error exporting class " + diagram.className + ": " + e.getMessage());
                }
            }
        }
    }
    private String parseAccessModifier(String declaration) {
        String trimmed = declaration.trim();
        String accessModifier = "";

        // Check for symbols at the start of the declaration
        if (trimmed.startsWith("+")) {
            accessModifier = "public";
            trimmed = trimmed.substring(1).trim();
        } else if (trimmed.startsWith("-")) {
            accessModifier = "private";
            trimmed = trimmed.substring(1).trim();
        } else if (trimmed.startsWith("#")) {
            accessModifier = "protected";
            trimmed = trimmed.substring(1).trim();
        } else if (trimmed.startsWith("~")) {
            accessModifier = ""; // Default package-private in Java
            trimmed = trimmed.substring(1).trim();
        } else {
            accessModifier = "public"; // Default to public if no symbol
        }

        return accessModifier + " " + trimmed;
    }
    private String parseAttribute(String attribute) {
        // Extract the parts of the attribute in the format: accessModifier name:type
        String[] parts = attribute.split(":");
        if (parts.length == 2) {
            String accessModifierAndName = parseAccessModifier(parts[0]);
            String[] accessParts = accessModifierAndName.split(" ", 2);
            if (accessParts.length == 2) {
                String accessModifier = accessParts[0];
                String name = accessParts[1];
                String type = parts[1].trim();
                return accessModifier + " " + type + " " + name; // Format as: accessModifier type name
            }
        }
        // If the format is invalid, return a comment to indicate the issue
        return "// Invalid attribute format: " + attribute;
    }

    private String parseOperation(String operation) {
        // Extract the parts of the operation in the format: accessModifier name():returnType
        String[] parts = operation.split(":");
        if (parts.length == 2) {
            String accessModifierAndName = parseAccessModifier(parts[0]);
            String[] accessModifierAndNameParts = accessModifierAndName.split(" ");
            if (accessModifierAndNameParts.length == 2) {
                String accessModifier = accessModifierAndNameParts[0];
                String name = accessModifierAndNameParts[1];
                String returnType = parts[1].trim();
                return accessModifier + " " + returnType + " " + name + " {\n        // TODO: Implement this method\n    }";
            }
        }
        // If the format is invalid, return a comment to indicate the issue
        return "// Invalid operation format: " + operation;
    }


    private String generateClassCode(ClassDiagram classDiagram) {
        StringBuilder code = new StringBuilder();

        // Find relationships
        List<String> inheritance = new ArrayList<>();
        List<String> associations = new ArrayList<>();

        for (LineConnection connection : lineConnections) {
            if (connection.startDiagram == classDiagram) {
                if (connection.lineType == InheritanceButton) {
                    inheritance.add(connection.endDiagram.className);
                } else if (connection.lineType == associationButton || connection.lineType == aggregationButton ||
                        connection.lineType == compositionButton) {
                    associations.add(connection.endDiagram.className);
                }
            }
        }

        // Class Declaration with Inheritance
        code.append("public class ").append(classDiagram.className);
        if (!inheritance.isEmpty()) {
            code.append(" extends ").append(inheritance.get(0)); // Handle only one parent class
        }
        code.append(" {\n\n");

        // Association Fields
        for (String associatedClass : associations) {
            code.append("    private ").append(associatedClass).append(" ").append(Character.toLowerCase(associatedClass.charAt(0)))
                    .append(associatedClass.substring(1)).append(";\n");
        }

        // Attributes
        for (String attribute : classDiagram.attributes) {
            code.append("    ").append(parseAttribute(attribute)).append(";\n");
        }
        code.append("\n");

        // Operations
        for (String operation : classDiagram.operations) {
            code.append("    ").append(parseOperation(operation)).append("\n\n");
        }

        code.append("}\n");
        return code.toString();
    }




    private static class ClassDiagram implements Serializable {
        double x, y;
        double width = 120;  // Width of the rectangle
        double height = 50;  // Height of the rectangle
        List<String> attributes = new ArrayList<>();
        List<String> operations = new ArrayList<>();
        String className = "Class"; // Default class name
        private static final long serialVersionUID = 1L;

        ClassDiagram(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double[][] getConnectionPoints() {
            double halfWidth = width / 2;
            double halfHeight = height / 2;

            return new double[][]{
                    // Top side (2 points)
                    {x + width / 3, y},              // Top-left
                    {x + 2 * width / 3, y},          // Top-right

                    // Bottom side (2 points)
                    {x + width / 3, y + height},     // Bottom-left
                    {x + 2 * width / 3, y + height}, // Bottom-right

                    // Left side (1 point)
                    {x, y + halfHeight},             // Left-center

                    // Right side (1 point)
                    {x + width, y + halfHeight},     // Right-center
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

    private static class LineConnection implements Serializable {
        ClassDiagram startDiagram;
        ClassDiagram endDiagram;
        int startConnectionIndex;
        int endConnectionIndex;
        transient Button lineType; // Mark Button as transient
        String lineTypeText;       // Store the button's text or purpose for reinitialization
        String text;
        List<double[]> controlPoints = new ArrayList<>(); // Intermediate control points
        private static final long serialVersionUID = 1L;

        LineConnection(ClassDiagram startDiagram, int startConnectionIndex,
                       ClassDiagram endDiagram, int endConnectionIndex, Button lineType) {
            this.startDiagram = startDiagram;
            this.startConnectionIndex = startConnectionIndex;
            this.endDiagram = endDiagram;
            this.endConnectionIndex = endConnectionIndex;
            this.lineType = lineType;
            this.lineTypeText = lineType.getText(); // Save button text or identifier
            this.text = "";
        }

        public double[] getStartPoint() {
            return startDiagram.getConnectionPoints()[startConnectionIndex];
        }

        public double[] getEndPoint() {
            return endDiagram.getConnectionPoints()[endConnectionIndex];
        }

        public List<double[]> getAllPoints() {
            // Combine start, control points, and end points for rendering
            List<double[]> points = new ArrayList<>();
            points.add(getStartPoint());
            points.addAll(controlPoints);
            points.add(getEndPoint());
            return points;
        }

        private void initializeButton(Button aggregationButton, Button compositionButton,
                                      Button associationButton, Button inheritanceButton) {
            if (lineType == null && lineTypeText != null) {
                if ("Aggregation".equals(lineTypeText)) {
                    this.lineType = aggregationButton;
                } else if ("Composition".equals(lineTypeText)) {
                    this.lineType = compositionButton;
                } else if ("Association".equals(lineTypeText)) {
                    this.lineType = associationButton;
                } else if ("Inheritance".equals(lineTypeText)) {
                    this.lineType = inheritanceButton;
                }
            }
        }
    }

    private static class InterfaceDiagram extends ClassDiagram {
        String interfaceName = "Interface"; // Default interface name

        InterfaceDiagram(double x, double y) {
            super(x, y);
        }

        @Override
        public double getHeight() {
            return 50 + 20 * operations.size(); // Interfaces don't have attributes
        }
    }


}
