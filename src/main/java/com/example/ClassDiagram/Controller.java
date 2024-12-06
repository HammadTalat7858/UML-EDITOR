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
import javafx.scene.control.Label;
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

/**
 * Controller class for managing the logic and operations related to the Class Diagram
 * in the UML Editor.
 *
 * <p>This class handles all the logical functionality associated with the Class Diagram,
 * including the management of UML model components such as classes, interfaces,
 * and their relationships (e.g., associations, inheritances). It serves
 * as a bridge between the UI and the business logic layers.</p>
 *
 * <p>Responsibilities of this class include:</p>
 * <ul>
 *   <li>Creating and updating UML components in the class diagram.</li>
 *   <li>Validating relationships and constraints, such as inheritance.</li>
 *   <li>Saving and loading class diagram data as part of a project.</li>
 *   <li>Supporting code generation based on the class diagram model.</li>
 * </ul>
 *
 * <p>Constraints:</p>
 * <ul>
 *   <li>Implements relevant design patterns for maintainability and scalability.</li>
 *   <li>Handles exceptions related to model integrity and persistence.</li>
 * </ul>
 *
 * @author Hammad Tallat
 * @author Ahmed Moeez
 * @author Muhammad Hassnain
 *
 */

public class Controller {

    /** Container pane for the canvas where diagrams are drawn. */
    public Pane canvasContainer;

    /** Scale transformation object for zoom functionality. */
    private Scale scaleTransform;

    /** VBox container for the toolbox buttons. */
    @FXML
    VBox toolboxVBox;

    /** Button for deleting selected components. */
    @FXML
    Button deleteButton;

    /** Button to create a new class diagram. */
    @FXML
    Button classButton;

    /** Button to create an association relationship. */
    @FXML
    Button associationButton;

    /** Button to create an aggregation relationship. */
    @FXML
    Button aggregationButton;

    /** Button to create a composition relationship. */
    @FXML
    Button compositionButton;

    /** Button to create an inheritance relationship. */
    @FXML
    Button InheritanceButton;

    /** ComboBox for selecting access modifiers of attributes. */
    @FXML
    ComboBox<String> attributeAccessModifier;

    /** ComboBox for selecting access modifiers of operations. */
    @FXML
    ComboBox<String> operationAccessModifier;

    /** MenuItem for exporting the diagram as a JPEG image. */
    @FXML
    private MenuItem jpegMenuItem;

    /** MenuItem for exporting the diagram as a PNG image. */
    @FXML
    private MenuItem pngMenuItem;

    /** MenuItem for generating code from the UML diagram. */
    @FXML
    MenuItem GenerateCode;

    /** TreeView to display the class hierarchy. */
    @FXML
    private TreeView<String> classHierarchyView;

    /** Root item of the class hierarchy view. */
    private TreeItem<String> rootItem;

    /** MenuItem for the "Save As" functionality to save the current project. */
    @FXML
    private MenuItem SaveAs;

    /** MenuItem for loading a project. */
    @FXML
    private MenuItem Load;

    /** MenuItem for closing the current project or the application. */
    @FXML
    private MenuItem Close;

    /** MenuItem for loading a use-case diagram into the editor. */
    @FXML
    private MenuItem loadusecase;

    /** MenuItem for loading a class diagram into the editor. */
    @FXML
    private MenuItem loadClass;

    /** VBox serving as the properties panel for displaying and modifying attributes of selected components. */
    @FXML
    private VBox propertiesPanel;

    /** TextField for entering or displaying attributes of the selected UML component. */
    @FXML
    TextField attributesField;

    /** Currently selected component in the editor, used for context-specific actions. */
    private Object selectedComponent = null;

    /** TextField for entering or displaying operations of the selected UML component. */
    @FXML
    TextField operationsField;

    /** Button for adding a new attribute to the selected UML component. */
    @FXML
    Button addAttributeButton;

    /** Button for creating an interface component in the diagram. */
    @FXML
    Button interfaceButton;

    /** Button for adding a new operation to the selected UML component. */
    @FXML
    Button addOperationButton;

    /** Keeps track of the currently active button (clicked) in the editor. */
    private Button activeButton;

    /** For storing the mouse offset while dragging. */
    private double offsetX, offsetY;

    /** Key for storing the currently selected UML diagram in the project. */
    private String selectedDiagramKey = null;

    /** Default width for class diagrams in the editor. */
    private final double classDiagramWidth = 120;

    /** Map for storing UML class diagrams, identified by their unique keys. */
    Map<String, ClassDiagram> diagrams = new HashMap<>();

    /** Flag indicating whether a line is currently being drawn between components. */
    private boolean isDrawingLine = false;

    /** Starting and Ending coordinates for a line being drawn. */
    private double startX, startY, endX, endY;

    /** Reference to the starting and ending diagram of a line connection. */
    private ClassDiagram startDiagram, endDiagram;

    /** List of all line connections created in the diagram editor. */
    List<LineConnection> lineConnections = new ArrayList<>();


    /**
     * Initializes the controller by setting up the canvas, event listeners, and UI components.
     * <p>
     * Tasks include:
     * <ul>
     *   <li>Setting up the canvas for UML diagram creation and editing.</li>
     *   <li>Configuring zoom, resizing, and keyboard shortcuts (e.g., DELETE key).</li>
     *   <li>Attaching actions for buttons to create and manage UML components.</li>
     *   <li>Enabling mouse interactions for dragging, editing, and adding components.</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        Canvas canvas = new Canvas(910, 780);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);
        rootItem = new TreeItem<>("Model");
        rootItem.setExpanded(true);
        classHierarchyView.setRoot(rootItem);

        scaleTransform = new Scale(1, 1, 0, 0);  // Initialize with no scaling
        canvasContainer.getTransforms().add(scaleTransform);
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
            resetSelection();
        });

            propertiesPanel.setOnMouseClicked(event -> {
                clearSelection();
                deselectAllButtons(buttons);
                resetSelection();

            });


    }

    /**
     * Handles loading of the use-case diagram by switching scenes.
     * Prompts the user to confirm if unsaved changes will be lost.
     */
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

    /**
     * Handles loading of the class diagram by switching scenes.
     * Prompts the user to confirm if unsaved changes will be lost.
     */
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

    /**
     * Updates the class hierarchy view in the TreeView component.
     * <p>
     * This method clears the existing hierarchy and repopulates it with the latest UML diagram information.
     * It includes attributes and operations of each class or interface.
     * </p>
     */
    private void updateClassHierarchy() {
        Platform.runLater(() -> {
            rootItem.getChildren().clear(); // Clear the hierarchy
            for (ClassDiagram diagram : diagrams.values()) {
                TreeItem<String> node;

                if (diagram instanceof InterfaceDiagram) {
                    InterfaceDiagram interfaceDiagram = (InterfaceDiagram) diagram;
                    node = new TreeItem<>("");
                    node.setGraphic(createItalicLabel(interfaceDiagram.interfaceName)); // Add italic label
                } else {
                    // Display as "Class: <className>"
                    node = new TreeItem<>(""+diagram.className);
                }

                // Add attributes as children (only for ClassDiagram)
                if (!(diagram instanceof InterfaceDiagram)) {
                    for (String attribute : diagram.attributes) {
                        node.getChildren().add(new TreeItem<>("Attribute: " + attribute));
                    }
                }

                // Add operations as children (for both ClassDiagram and InterfaceDiagram)
                for (String operation : diagram.operations) {
                    node.getChildren().add(new TreeItem<>("Operation: " + operation));
                }

                // Add the node to the root item
                rootItem.getChildren().add(node);
            }
        });
    }

    /**
     * Creates a styled Label with italic text.
     *
     * @param text the text to display in the label
     * @return a Label with italicized text style
     */
    private Label createItalicLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-style: italic; -fx-font-size: 12px;"); // Apply italic style
        return label;
    }

    /**
     * Creates a new Interface Diagram at the specified coordinates.
     * If a diagram at the location doesn't already exist, it is created, drawn, and added to the hierarchy.
     *
     * @param gc the GraphicsContext for drawing the diagram
     * @param x the x-coordinate for the diagram
     * @param y the y-coordinate for the diagram
     */
    private void createInterfaceDiagram(GraphicsContext gc, double x, double y) {
        String key = "Interface" + x + "," + y;
        if (!diagrams.containsKey(key)) {
            diagrams.put(key, new InterfaceDiagram(x, y)); // Create and store a new interface diagram
            drawInterfaceDiagram(gc, (InterfaceDiagram) diagrams.get(key));
            updateClassHierarchy();
        }
    }

    /**
     * Updates the UI to reflect the current selection.
     * Disables or enables the "Add Attribute" button based on the selected component type.
     * Updates the button's style and tooltip accordingly.
     */
    @FXML
    private void updateUIForSelection() {
        if (selectedComponent instanceof InterfaceDiagram) {
            // Disable the "Add Attribute" button
            addAttributeButton.setDisable(true);

            // Add the disabled-button CSS class for the blur effect
            if (!addAttributeButton.getStyleClass().contains("disabled-button")) {
                addAttributeButton.getStyleClass().add("disabled-button");
            }

            // Optional: Set a tooltip explaining why it's disabled
            Tooltip tooltip = new Tooltip("Attributes cannot be added to an Interface.");
            Tooltip.install(addAttributeButton, tooltip);
        } else {
            // Enable the "Add Attribute" button
            addAttributeButton.setDisable(false);

            // Remove the disabled-button CSS class
            addAttributeButton.getStyleClass().remove("disabled-button");

            // Remove any tooltip
            Tooltip.uninstall(addAttributeButton, null);
        }
    }

    /**
     * Handles changes to the selected component, updating the UI and redrawing the canvas.
     */
    private void handleSelectionChange() {
        updateUIForSelection();
        redrawCanvas(((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D());
    }

    /**
     * Draws an Interface Diagram at its specified location with a rectangle, labels, and operations.
     * Highlights the diagram if selected and draws connection points.
     *
     * @param gc the GraphicsContext used for drawing
     * @param interfaceDiagram the InterfaceDiagram to draw
     */
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
        gc.setFill(isSelected ? Color.LIGHTBLUE : Color.WHITE); // Light blue for selected
        gc.fillRect(x, y, width, height);
        gc.setStroke(isSelected ? Color.BLUE : Color.BLACK); // Blue border for selected
        gc.setLineWidth(isSelected ? 3 : 2); // Thicker border for selected
        gc.strokeRect(x, y, width, height);

        // Draw <<interface>> label
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        String interfaceLabel = "<<interface>>";
        Text labelHelper = new Text(interfaceLabel);
        labelHelper.setFont(gc.getFont());
        double labelWidth = labelHelper.getBoundsInLocal().getWidth();
        double labelX = x + (width - labelWidth) / 2; // Center horizontally
        gc.fillText(interfaceLabel, labelX, y + 15);

        // Draw interface name
        gc.setFont(Font.font("Arial", 14));
        String interfaceName = interfaceDiagram.interfaceName;
        Text nameHelper = new Text(interfaceName);
        nameHelper.setFont(gc.getFont());
        double nameWidth = nameHelper.getBoundsInLocal().getWidth();
        double nameX = x + (width - nameWidth) / 2; // Center horizontally
        gc.fillText(interfaceName, nameX, y + 40);

        // Draw separator line below <<interface>> and name
        gc.strokeLine(x, y + baseHeight, x + width, y + baseHeight);

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

    /**
     * Calculates the maximum text width for a diagram's labels and operations.
     *
     * @param gc the GraphicsContext for measuring text
     * @param interfaceDiagram the InterfaceDiagram whose text widths are calculated
     * @return the maximum text width
     */
    private double getMaxTextWidth(GraphicsContext gc, InterfaceDiagram interfaceDiagram) {
        Text textHelper = new Text();
        textHelper.setFont(Font.font("Arial", 12));

        // Calculate the width of the <<interface>> label
        textHelper.setText("<<interface>>");
        double maxWidth = textHelper.getLayoutBounds().getWidth();

        // Calculate the width of the interface name
        textHelper.setFont(Font.font("Arial", 14)); // Use a larger font for the name
        textHelper.setText(interfaceDiagram.interfaceName);
        double interfaceNameWidth = textHelper.getLayoutBounds().getWidth();
        maxWidth = Math.max(maxWidth, interfaceNameWidth);

        // Calculate the width of operations
        textHelper.setFont(Font.font("Arial", 12)); // Reset to the smaller font for operations
        for (String operation : interfaceDiagram.operations) {
            textHelper.setText(operation);
            double operationWidth = textHelper.getLayoutBounds().getWidth();
            maxWidth = Math.max(maxWidth, operationWidth);
        }

        return maxWidth;
    }

    /**
     * Resets the current selection, clearing any highlights or tool states.
     * Restores the UI to its default state and redraws the canvas.
     */
    @FXML
    private void resetSelection() {
        // Clear the selected component and diagram key
        selectedComponent = null;
        selectedDiagramKey = null;

        // Enable the "Add Attribute" button and remove any blur effect
        addAttributeButton.setDisable(false);
        addAttributeButton.getStyleClass().remove("disabled-button");

        // Remove any tooltip on the "Add Attribute" button
        Tooltip.uninstall(addAttributeButton, null);

        // Deselect any active toolbox button
        List<Button> buttons = Arrays.asList(classButton, associationButton, aggregationButton, compositionButton, InheritanceButton, interfaceButton);
        deselectAllButtons(buttons);

        // Redraw the canvas to reflect deselection
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();
        gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
        redrawCanvas(gc);

        // Clear other UI elements or selection-specific visuals if required
        updateUIForSelection(); // Ensure the UI is fully reset
    }

    /**
     * Handles zooming on the canvas using the ScrollEvent with the Control key held down.
     * Adjusts the scaling factors to zoom in or out, constraining the zoom to reasonable limits.
     * Redraws the canvas with the updated scale and redraws all diagrams.
     *
     * @param event the ScrollEvent triggered by the user's scrolling action
     */

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

    /**
     * Handles the close action for the application.
     * Displays a confirmation dialog to the user asking if they want to exit the application.
     * If the user clicks "OK", the application is terminated. If the user cancels,
     * the dialog is simply closed without taking further action.
     */
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

    /**
     * Clears the current selection of components and diagrams from the canvas.
     * Resets the selected component and diagram key, then redraws the canvas
     * to remove any highlighting or visual indicators of selection.
     */
    private void clearSelection() {
        selectedComponent = null; // Clear the selected component (class or line)
        selectedDiagramKey = null; // Clear the selected diagram key
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();
        redrawCanvas(gc); // Redraw the canvas to remove highlighting
    }

    /**
     * Handles zooming actions using the Control key along with the ADD and SUBTRACT keys.
     * Zooms in or out on the canvas by adjusting the scaling factors when the user presses
     * the Control key along with either the ADD (+) or SUBTRACT (-) key.
     *
     * @param event the KeyEvent triggered by the user's keyboard input
     */
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

    /**
     * Handles the logic for clicking on a toolbar button.
     * If the clicked button is already active, it deselects it.
     * Otherwise, it deselects all buttons in the provided list and
     * selects the clicked button, updating its visual state with CSS classes.
     *
     * @param clickedButton the button that was clicked
     * @param buttons a list of all toolbar buttons for deselection
     */
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

    /**
     * Deselects all buttons in the toolbox.
     * This method removes the "tool-button-selected" style class from all buttons
     * in the provided list and ensures the default "tool-button" style class is
     * applied. It also clears the current active button.
     *
     * @param buttons the list of toolbox buttons to deselect
     */
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

    /**
     * Draws a grid on the canvas by using the provided GraphicsContext.
     * The grid lines are spaced every 10 pixels and have a light gray color.
     *
     * @param gc the GraphicsContext of the canvas where the grid is drawn
     */
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

    /**
     * Creates and stores a new class diagram at the specified position (x, y).
     * If a class diagram does not already exist at this position, it creates one,
     * stores it in the `diagrams` map, draws it on the canvas, and updates the class hierarchy.
     *
     * @param gc the GraphicsContext of the canvas for rendering the diagram
     * @param x the x-coordinate where the class diagram should be placed
     * @param y the y-coordinate where the class diagram should be placed
     */
    private void createClassDiagram(GraphicsContext gc, double x, double y) {
        String key = "Class" + x + "," + y;
        if (!diagrams.containsKey(key)) {
            diagrams.put(key, new ClassDiagram(x, y)); // Create and store a new class diagram
            drawClassDiagram(gc, diagrams.get(key));
            updateClassHierarchy();
        }
    }

    /**
     * Draws a class diagram on the provided {@link GraphicsContext}.
     *
     * <p>This method renders the class diagram with a rectangle containing the class name,
     * attributes, and operations. It also displays connection points for diagram connections.
     * If the diagram is selected, it is highlighted with a specific border and background.</p>
     *
     * @param gc           the {@link GraphicsContext} used for drawing
     * @param classDiagram the {@link ClassDiagram} to be drawn, containing coordinates, dimensions, and content
     */
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

    /**
     * Calculates the maximum text width required for the given class diagram's contents.
     *
     * <p>This method iterates through the class name, attributes, and operations to determine
     * the widest text, ensuring sufficient space for rendering the class diagram.</p>
     *
     * @param gc           the {@link GraphicsContext} used for text measurement
     * @param classDiagram the {@link ClassDiagram} whose text content is analyzed
     * @return the maximum width of the text in the class diagram
     */
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

    /**
     * Selects a class diagram based on mouse coordinates.
     *
     * <p>This method determines whether the mouse click falls within any class diagram's
     * bounding box. If a diagram is selected, its key and reference are stored, and the UI
     * is updated accordingly.</p>
     *
     * @param mouseX the X-coordinate of the mouse click
     * @param mouseY the Y-coordinate of the mouse click
     */
    private void selectDiagram(double mouseX, double mouseY) {
        // Reset selection
        selectedDiagramKey = null;
        selectedComponent = null;

        // Iterate over all class diagrams to find the one containing the mouse coordinates
        for (Map.Entry<String, ClassDiagram> entry : diagrams.entrySet()) {
            ClassDiagram diagram = entry.getValue();

            // Check if the mouse click falls within the bounding box of the class diagram
            if (mouseX >= diagram.x && mouseX <= diagram.x + diagram.width &&
                    mouseY >= diagram.y && mouseY <= diagram.y + diagram.height) {
                selectedDiagramKey = entry.getKey(); // Store the selected diagram key
                selectedComponent = diagram;        // Mark the class diagram as selected

                // Update the UI based on the selected component
                updateUIForSelection();
                return;
            }
        }

        // If no diagram is selected, clear the selection and update the UI
        updateUIForSelection();
    }

    /**
     * Checks if a given mouse position corresponds to an empty area on the canvas.
     *
     * <p>This method verifies if the mouse coordinates are outside the bounds of all class diagrams
     * and are not near any line or its control points. It is useful for determining whether
     * user interactions, such as adding new diagrams, can occur at the specified location.</p>
     *
     * @param mouseX the X-coordinate of the mouse position
     * @param mouseY the Y-coordinate of the mouse position
     * @return {@code true} if the mouse position is in an empty area, {@code false} otherwise
     */
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

    /**
     * Handles mouse press events on the canvas.
     *
     * <p>This method manages interactions based on the mouse click location and type (e.g., single click, double-click).
     * It handles component selection, control point addition, line drawing, and interaction with specific UI elements.</p>
     *
     * @param event the {@link MouseEvent} representing the mouse press action
     */
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

    /**
     * Deletes the currently selected component from the canvas.
     *
     * <p>This method supports removing class diagrams and line connections. It updates the
     * data structures to reflect the changes and redraws the canvas to ensure the UI is consistent.</p>
     */
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

    /**
     * Handles mouse drag events on the canvas.
     *
     * <p>This method enables dragging of class diagrams, line control points, or lines themselves.
     * If a new line is being drawn, it provides a visual preview. It ensures boundaries are respected
     * and updates the canvas in real-time.</p>
     *
     * @param event the {@link MouseEvent} representing the mouse drag action
     */
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

    /**
     * Handles mouse click events for editing class diagram components.
     *
     * <p>This method identifies which part of a class diagram is clicked (e.g., class name, attributes, or operations)
     * and opens a text field for editing if a double-click is detected. It supports both Class and Interface diagrams.</p>
     *
     * @param event the {@link MouseEvent} representing the mouse click
     * @param gc the {@link GraphicsContext} for redrawing the canvas
     */
    private void handleClassEditing(MouseEvent event, GraphicsContext gc) {
        for (Map.Entry<String, ClassDiagram> entry : diagrams.entrySet()) {
            ClassDiagram diagram = entry.getValue();
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Check if the click occurred within the diagram bounds
            if (mouseX >= diagram.x && mouseX <= diagram.x + diagram.width) {
                // Handle name editing for both Class and Interface diagrams
                if (diagram instanceof InterfaceDiagram) {
                    // Check if the click is near the interface name
                    double nameStartY = diagram.y + 30; // Start position below <<Interface>>
                    double nameEndY = nameStartY + 20;  // Adjust for text height
                    if (mouseY >= nameStartY && mouseY <= nameEndY) {
                        if (event.getClickCount() == 2) {
                            editClassName(diagram, gc);
                        }
                        return;
                    }
                } else {
                    // Handle name editing for Class diagrams
                    if (mouseY >= diagram.y && mouseY <= diagram.y + 30) {
                        if (event.getClickCount() == 2) {
                            editClassName(diagram, gc);
                        }
                        return;
                    }
                }

                // Handle attributes and operations for Class diagrams
                if (!(diagram instanceof InterfaceDiagram)) {
                    double attributeStartY = diagram.y + 30;
                    double attributeEndY = attributeStartY + Math.max(20, 20 * diagram.attributes.size()); // Ensure at least one row
                    if (mouseY >= attributeStartY && mouseY <= attributeEndY) {
                        if (event.getClickCount() == 2) {
                            int index = (int) ((mouseY - attributeStartY) / 20);
                            editAttribute(diagram, index, gc);
                        }
                        return;
                    }
                }

                // Handle operations for both Class and Interface diagrams
                double operationStartY = diagram instanceof InterfaceDiagram
                        ? diagram.y + 50 // Start after <<Interface>> and name
                        : diagram.y + 30 + Math.max(20, 20 * diagram.attributes.size()) + 10;
                double operationEndY = operationStartY + Math.max(20, 20 * diagram.operations.size()); // Ensure at least one row
                if (mouseY >= operationStartY && mouseY <= operationEndY) {
                    if (event.getClickCount() == 2) {
                        int index = (int) ((mouseY - operationStartY) / 20);
                        editOperation(diagram, index, gc);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Allows editing of a specific attribute in a class diagram.
     *
     * <p>A text field is displayed over the selected attribute for editing. Changes are committed when the
     * user presses Enter or the text field loses focus. The canvas is redrawn to reflect the updated attribute.</p>
     *
     * @param classDiagram the {@link ClassDiagram} containing the attribute to edit
     * @param index the index of the attribute in the list of attributes
     * @param gc the {@link GraphicsContext} for redrawing the canvas
     */
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

    /**
     * Allows editing of a specific operation in a class diagram.
     *
     * <p>A text field is displayed over the selected operation for editing. Changes are committed when the
     * user presses Enter or the text field loses focus. The canvas is redrawn to reflect the updated operation.</p>
     *
     * @param classDiagram the {@link ClassDiagram} containing the operation to edit
     * @param index the index of the operation in the list of operations
     * @param gc the {@link GraphicsContext} for redrawing the canvas
     */
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

    /**
     * Allows editing of a class or interface name in a diagram.
     *
     * <p>A text field is displayed over the class or interface name for editing. Changes are committed when the
     * user presses Enter or the text field loses focus. The canvas is redrawn to reflect the updated name,
     * and the class hierarchy is updated if applicable.</p>
     *
     * @param diagram the {@link ClassDiagram} or {@link InterfaceDiagram} whose name is being edited
     * @param gc the {@link GraphicsContext} for redrawing the canvas
     */
    private void editClassName(ClassDiagram diagram, GraphicsContext gc) {
        String currentName;
        double x, y, width;

        // Determine the diagram type and fetch the relevant properties
        if (diagram instanceof InterfaceDiagram) {
            InterfaceDiagram interfaceDiagram = (InterfaceDiagram) diagram;
            currentName = interfaceDiagram.interfaceName;
            x = interfaceDiagram.x;
            y = interfaceDiagram.y + 30; // Position below <<Interface>>
            width = interfaceDiagram.width;
        } else {
            currentName = diagram.className;
            x = diagram.x;
            y = diagram.y + 15; // Adjusted to be near the class name's vertical position
            width = diagram.width;
        }

        // Create the TextField with the current name
        TextField nameField = new TextField(currentName);

        // Calculate the width dynamically based on the text
        Text textHelper = new Text(currentName);
        textHelper.setFont(Font.font("Arial", 14)); // Match the font used in diagram rendering
        double textWidth = textHelper.getBoundsInLocal().getWidth();

        // Position the TextField centered horizontally and correctly vertically
        nameField.setPrefWidth(textWidth + 20); // Add padding for a better appearance
        nameField.setLayoutX(x + (width - nameField.getPrefWidth()) / 2); // Center horizontally
        nameField.setLayoutY(y - 10); // Slightly adjust Y to align properly

        // Style the TextField
        nameField.setStyle("-fx-background-color: white; -fx-border-color: transparent; -fx-font-size: 12px; -fx-text-fill: black;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(nameField);
        nameField.requestFocus();

        // Commit changes on Enter
        nameField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String updatedName = nameField.getText().trim();

                if (diagram instanceof InterfaceDiagram) {
                    ((InterfaceDiagram) diagram).interfaceName = updatedName;
                } else {
                    diagram.className = updatedName;
                }

                canvasContainer.getChildren().remove(nameField);
                redrawCanvas(gc); // Redraw the canvas with the updated name
                updateClassHierarchy(); // Update any hierarchy display
            }
        });

        // Commit changes on focus loss
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String updatedName = nameField.getText().trim();

                if (diagram instanceof InterfaceDiagram) {
                    ((InterfaceDiagram) diagram).interfaceName = updatedName;
                } else {
                    diagram.className = updatedName;
                }

                canvasContainer.getChildren().remove(nameField);
                redrawCanvas(gc); // Redraw the canvas with the updated name
                updateClassHierarchy(); // Update any hierarchy display
            }
        });
    }

    /**
     * Converts an access modifier into its corresponding UML symbol.
     *
     * @param accessModifier the access modifier as a string (e.g., "public", "private")
     * @return the UML symbol for the access modifier ("+", "-", "#", or "~"),
     *         or an empty string if the access modifier is unrecognized
     */
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

    /**
     * Handles the mouse release event when drawing a line between diagrams.
     *
     * <p>This method checks if the mouse release occurred near a valid connection point on
     * another diagram. If a valid connection is established, a line connection is created
     * and the canvas is redrawn. Otherwise, an error message is shown.</p>
     *
     * @param event the {@link MouseEvent} representing the mouse release action
     */
    private void onMouseReleased(MouseEvent event) {
        GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();

        if (isDrawingLine) {
            for (ClassDiagram diagram : diagrams.values()) {
                for (int i = 0; i < diagram.getConnectionPoints().length; i++) {
                    double[] point = diagram.getConnectionPoints()[i];

                    if (isNear(event.getX(), event.getY(), point[0], point[1]) && diagram != startDiagram) {
                        // Validate the connection
                        if (isInvalidLineConnection(startDiagram, diagram, activeButton)) {
                            showError("Invalid connection: Interfaces cannot have association, aggregation, or composition lines.");
                            isDrawingLine = false; // Reset the line drawing state
                            redrawCanvas(gc);
                            return;
                        }

                        int startConnectionIndex = getNearestConnectionIndex(startDiagram, startX, startY);
                        int endConnectionIndex = i;

                        // Create a valid LineConnection
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

    /**
     * Finds the nearest connection point on a diagram based on the given coordinates.
     *
     * @param diagram the {@link ClassDiagram} to search for connection points
     * @param x the x-coordinate of the reference point
     * @param y the y-coordinate of the reference point
     * @return the index of the nearest connection point, or -1 if no point is found
     */
    private int getNearestConnectionIndex(ClassDiagram diagram, double x, double y) {
        double[][] connectionPoints = diagram.getConnectionPoints();
        for (int i = 0; i < connectionPoints.length; i++) {
            if (isNear(x, y, connectionPoints[i][0], connectionPoints[i][1])) {
                return i;
            }
        }
        return -1; // Should not happen for a valid start point
    }

    /**
     * Validates the connection between two diagrams based on the selected line type.
     *
     * <p>Disallows certain line types (association, aggregation, composition) between
     * interface diagrams and other diagrams.</p>
     *
     * @param startDiagram the starting {@link ClassDiagram} of the connection
     * @param endDiagram the ending {@link ClassDiagram} of the connection
     * @param lineType the {@link Button} representing the selected line type
     * @return {@code true} if the connection is invalid, {@code false} otherwise
     */
    private boolean isInvalidLineConnection(ClassDiagram startDiagram, ClassDiagram endDiagram, Button lineType) {
        // Check if either diagram is an interface
        boolean startIsInterface = startDiagram instanceof InterfaceDiagram;
        boolean endIsInterface = endDiagram instanceof InterfaceDiagram;

        // Disallow association, aggregation, or composition lines with interfaces
        if ((lineType == associationButton || lineType == aggregationButton || lineType == compositionButton) &&
                (startIsInterface || endIsInterface)) {
            return true; // Invalid connection
        }

        // Allow all other connections
        return false;
    }

    /**
     * Loads a saved diagram from a file and restores it to the canvas.
     *
     * <p>This method uses object deserialization to retrieve diagrams and line connections,
     * reinitializes necessary fields, and redraws the canvas.</p>
     */
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

    /**
     * Saves the current diagram and its connections to a file.
     *
     * <p>This method serializes the diagram data into a file with a ".diagram" extension.</p>
     */
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

    /**
     * Displays an informational message to the user in a dialog box.
     *
     * @param message the message to display
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays an error message to the user in a dialog box.
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Invalid Connection");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Redraws all class diagrams on the canvas.
     *
     * @param gc the {@link GraphicsContext} used for drawing
     */
    private void redrawDiagrams(GraphicsContext gc) {
        for (ClassDiagram diagram : diagrams.values()) {
            drawClassDiagram(gc, diagram);
        }
    }

    /**
     * Exports the canvas content as a JPEG image file.
     */
    @FXML
    private void exportAsJPEG() {
        saveCanvasToFile("jpeg");
    }

    /**
     * Exports the canvas content as a PNG image file.
     */
    @FXML
    private void exportAsPNG() {
        saveCanvasToFile("png");
    }

    /**
     * Saves the current canvas content as an image file in the specified format.
     *
     * <p>This method captures the canvas as a snapshot, converts it to a
     * {@link BufferedImage}, and saves it to a user-selected file. If the format is JPEG,
     * it removes the alpha channel to ensure compatibility.</p>
     *
     * @param format the file format to save the image in (e.g., "jpeg", "png")
     */
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

    /**
     * Converts a {@link WritableImage} to a {@link BufferedImage}.
     *
     * @param writableImage the {@link WritableImage} to convert
     * @return the resulting {@link BufferedImage}
     */
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

    /**
     * Removes the alpha channel from a {@link BufferedImage}, replacing it with a white background.
     *
     * @param originalImage the original {@link BufferedImage} with an alpha channel
     * @return a new {@link BufferedImage} without an alpha channel
     */
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

    /**
     * Adds a new attribute to the selected class diagram.
     *
     * <p>The attribute is prefixed with the selected access modifier symbol
     * and added to the diagram's attributes list. The canvas is redrawn to reflect
     * the changes.</p>
     *
     * @param gc the {@link GraphicsContext} used to redraw the canvas
     */
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

    /**
     * Adds a new operation to the selected class diagram.
     *
     * <p>The operation is prefixed with the selected access modifier symbol
     * and added to the diagram's operations list. The canvas is redrawn to reflect
     * the changes.</p>
     *
     * @param gc the {@link GraphicsContext} used to redraw the canvas
     */
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

    /**
     * Redraws the entire canvas, including the grid, diagrams, and line connections.
     *
     * <p>This method clears the canvas and sequentially redraws the grid,
     * class diagrams, and dynamic line connections with their respective styles and shapes.</p>
     *
     * @param gc the {@link GraphicsContext} used for drawing
     */
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

    /**
     * Calculates the intersection point of the base of a triangle with a given line segment.
     *
     * @param end         the endpoint of the line segment as an array [x, y].
     * @param start       the starting point of the line segment as an array [x, y].
     * @param triangleSize the size of the triangle base.
     * @return an array [x, y] representing the intersection point.
     */
    private double[] calculateTriangleBaseIntersection(double[] end, double[] start, double triangleSize) {
        double angle = Math.atan2(end[1] - start[1], end[0] - start[0]);
        double intersectionX = end[0] - triangleSize * Math.cos(angle);
        double intersectionY = end[1] - triangleSize * Math.sin(angle);
        return new double[]{intersectionX, intersectionY};
    }

    /**
     * Calculates the intersection point of the base of a diamond shape with a given line segment.
     *
     * @param end         the endpoint of the line segment as an array [x, y].
     * @param start       the starting point of the line segment as an array [x, y].
     * @param diamondSize the size of the diamond base.
     * @return an array [x, y] representing the intersection point.
     */
    private double[] calculateDiamondBaseIntersection(double[] end, double[] start, double diamondSize) {
        double angle = Math.atan2(end[1] - start[1], end[0] - start[0]);
        double intersectionX = end[0] - 2 * diamondSize * Math.cos(angle);
        double intersectionY = end[1] - 2 * diamondSize * Math.sin(angle);
        return new double[]{intersectionX, intersectionY};
    }

    /**
     * Draws a hollow triangle on the canvas with the given parameters.
     *
     * @param gc         the {@code GraphicsContext} used for drawing.
     * @param endX       the x-coordinate of the triangle's tip.
     * @param endY       the y-coordinate of the triangle's tip.
     * @param startX     the x-coordinate of the line's starting point.
     * @param startY     the y-coordinate of the line's starting point.
     * @param isSelected whether the triangle is selected, affecting its appearance.
     */
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

    /**
     * Displays a text field at the midpoint of a line, allowing the user to edit its label.
     *
     * @param mouseX  the x-coordinate of the mouse position.
     * @param mouseY  the y-coordinate of the mouse position.
     * @param line    the {@code LineConnection} object to edit.
     * @param gc      the {@code GraphicsContext} used for redrawing.
     */
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

    /**
     * Checks if a given point (mouse position) is near any segment of a line.
     *
     * @param mouseX the x-coordinate of the mouse position.
     * @param mouseY the y-coordinate of the mouse position.
     * @param line   the {@code LineConnection} object to check.
     * @return {@code true} if the point is near the line; {@code false} otherwise.
     */
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

    /**
     * Calculates the shortest distance from a point to a line segment.
     *
     * @param px the x-coordinate of the point.
     * @param py the y-coordinate of the point.
     * @param x1 the x-coordinate of the starting point of the segment.
     * @param y1 the y-coordinate of the starting point of the segment.
     * @param x2 the x-coordinate of the endpoint of the segment.
     * @param y2 the y-coordinate of the endpoint of the segment.
     * @return the shortest distance from the point to the segment.
     */
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

    /**
     * Draws a diamond shape at the specified position with optional fill and highlighting.
     *
     * @param gc         the {@code GraphicsContext} used for drawing.
     * @param endX       the x-coordinate of the diamond's tip.
     * @param endY       the y-coordinate of the diamond's tip.
     * @param startX     the x-coordinate of the starting point of the line.
     * @param startY     the y-coordinate of the starting point of the line.
     * @param isSelected whether the diamond is selected, affecting its appearance.
     * @param filled     whether the diamond should be filled with color.
     */
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

    /**
     * Determines whether two points are within a specified snapping tolerance.
     *
     * @param x1 the x-coordinate of the first point.
     * @param y1 the y-coordinate of the first point.
     * @param x2 the x-coordinate of the second point.
     * @param y2 the y-coordinate of the second point.
     * @return {@code true} if the two points are within the snap radius; {@code false} otherwise.
     */
    private boolean isNear(double x1, double y1, double x2, double y2) {
        double tolerance = 10.0; // Snap radius
        return Math.abs(x1 - x2) < tolerance && Math.abs(y1 - y2) < tolerance;
    }

    /**
     * Initializes the buttons for each {@code LineConnection} in the diagram.
     */
    private void initializeConnections() {
        for (LineConnection line : lineConnections) {
            line.initializeButton(aggregationButton, compositionButton, associationButton, InheritanceButton);
        }
    }

    /**
     * Exports the UML diagrams as Java code files to a user-selected directory.
     *
     * Opens a directory chooser to select the export location and generates Java code
     * for each class or interface in the UML diagram, saving them as .java files.
     */
    @FXML
    private void exportToJavaCode() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Folder");
        File folder = directoryChooser.showDialog(canvasContainer.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            for (ClassDiagram diagram : diagrams.values()) {
                String classCode = generateClassCode(diagram);

                // Check if it's an interface and modify the name accordingly
                String fileName = (diagram instanceof InterfaceDiagram)
                        ? ((InterfaceDiagram) diagram).interfaceName + ".java"
                        : diagram.className + ".java";

                File javaFile = new File(folder, fileName);

                try (PrintWriter writer = new PrintWriter(javaFile)) {
                    writer.write(classCode);
                    showInfo("File exported to " + javaFile.getAbsolutePath());
                } catch (IOException e) {
                    showError("Error exporting file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses an access modifier and declaration from a UML notation.
     *
     * For example, "+ name:Type" would return "public Type name".
     *
     * @param declaration the UML declaration string.
     * @return the parsed Java declaration as a string.
     */
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

    /**
     * Parses an attribute declaration in UML notation and converts it to Java syntax.
     *
     * For example, "+ attributeName:Type" would return "public Type attributeName".
     *
     * @param attribute the UML attribute declaration.
     * @return the parsed Java attribute declaration as a string, or a comment if invalid.
     */
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

    /**
     * Parses an operation declaration in UML notation and converts it to a Java method signature.
     *
     * For example, "+ anyFunction(String id, String name):boolean" would return
     * "public boolean anyFunction(String id, String name).
     *
     * @param operation the UML operation declaration.
     * @return the parsed Java method implementation as a string, or a comment if the format is invalid.
     */
    private String parseOperation(String operation) {
        String[] parts = operation.split(":");
        if (parts.length == 2) {
            String declaration = parts[0].trim(); // Example: "+ anyfunction(String id,String name)"
            String returnType = parts[1].trim(); // Example: "boolean"

            // Parse access modifier
            String accessModifier = getAccessModifierSymbol(declaration.substring(0, 1)); // First character (+, -, #, ~)
            String methodSignature = declaration.substring(1).trim(); // Remove the access modifier symbol

            // Generate the method
            return accessModifier + " " + returnType + " " + methodSignature + " {\n        // TODO: Implement this method\n    }";
        }
        return "// Invalid operation format: " + operation;
    }

    /**
     * Generates Java source code for a given class diagram, including handling interfaces, inheritance,
     * implemented interfaces, associations, attributes, and operations.
     *
     * This method determines if the given {@code ClassDiagram} represents a class or an interface
     * and generates appropriate code accordingly. It also processes relationships such as
     * inheritance, associations, and implementations based on the connections defined in the UML diagram.
     *
     * <p>For interfaces, it adds the "extends" clause for any parent interfaces and generates method
     * declarations for operations. For classes, it adds the "extends" clause for inheritance,
     * the "implements" clause for interfaces, and includes fields for associated classes.
     * It also generates method implementations, stubbing out methods from implemented interfaces.</p>
     *
     * @param classDiagram the {@code ClassDiagram} object representing the UML class or interface.
     * @return the generated Java source code as a {@code String}.
     */
    String generateClassCode(ClassDiagram classDiagram) {
        StringBuilder code = new StringBuilder();

        if (classDiagram instanceof InterfaceDiagram) {
            // Handle interface generation
            InterfaceDiagram interfaceDiagram = (InterfaceDiagram) classDiagram;

            // Interface declaration
            code.append("public interface ").append(interfaceDiagram.interfaceName);

            // Add any inheritance (extends other interfaces)
            List<String> inheritance = new ArrayList<>();
            for (LineConnection connection : lineConnections) {
                if (connection.startDiagram == interfaceDiagram && connection.lineType == InheritanceButton) {
                    inheritance.add(connection.endDiagram instanceof InterfaceDiagram ?
                            ((InterfaceDiagram) connection.endDiagram).interfaceName : null);
                }
            }
            if (!inheritance.isEmpty()) {
                code.append(" extends ").append(String.join(", ", inheritance));
            }
            code.append(" {\n");

            // Add operations
            for (String operation : interfaceDiagram.operations) {
                code.append("    ").append(parseInterfaceOperation(operation)).append("\n");
            }

            code.append("}\n");
        } else {
            // Handle class generation
            List<String> inheritance = new ArrayList<>();
            List<String> implementedInterfaces = new ArrayList<>();
            List<String> associations = new ArrayList<>();

            for (LineConnection connection : lineConnections) {
                if (connection.startDiagram == classDiagram) {
                    if (connection.lineType == InheritanceButton) {
                        if (connection.endDiagram instanceof InterfaceDiagram) {
                            implementedInterfaces.add(((InterfaceDiagram) connection.endDiagram).interfaceName);
                        } else {
                            inheritance.add(connection.endDiagram.className);
                        }
                    } else if (connection.lineType == associationButton || connection.lineType == aggregationButton ||
                            connection.lineType == compositionButton) {
                        associations.add(connection.endDiagram.className);
                    }
                }
            }

            // Class declaration
            code.append("public class ").append(classDiagram.className);

            // Add extends (inheritance) for parent class
            if (!inheritance.isEmpty()) {
                code.append(" extends ").append(inheritance.get(0)); // Handle only one parent class
            }

            // Add implements for interfaces
            if (!implementedInterfaces.isEmpty()) {
                code.append(" implements ").append(String.join(", ", implementedInterfaces));
            }

            code.append(" {\n\n");

            // Association fields
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

            // Add method stubs for implemented interfaces
            for (String interfaceName : implementedInterfaces) {
                // Locate the InterfaceDiagram based on its name
                InterfaceDiagram interfaceDiagram = null;
                for (ClassDiagram diagram : diagrams.values()) {
                    if (diagram instanceof InterfaceDiagram && ((InterfaceDiagram) diagram).interfaceName.equals(interfaceName)) {
                        interfaceDiagram = (InterfaceDiagram) diagram;
                        break;
                    }
                }

                // Generate method stubs
                if (interfaceDiagram != null) {
                    for (String operation : interfaceDiagram.operations) {
                        code.append("    @Override\n    ").append(parseOperation(operation)).append("\n\n");
                    }
                }
            }

            code.append("}\n");
        }

        return code.toString();
    }

    /**
     * Parses an operation declaration in UML notation and converts it to a Java interface method signature.
     *
     * For example, "+ anyFunction(String id, String name):boolean" would return
     * "boolean anyFunction(String id, String name);".
     *
     * @param operation the UML operation declaration.
     * @return the parsed Java interface method signature as a string, or a comment if the format is invalid.
     */
    private String parseInterfaceOperation(String operation) {
        String[] parts = operation.split(":");
        if (parts.length == 2) {
            String declaration = parts[0].trim(); // Example: "+ anyfunction(String id,String name)"
            String returnType = parts[1].trim(); // Example: "boolean"

            // Remove the access modifier and keep the raw signature
            String methodSignature = declaration.substring(1).trim(); // Remove the access modifier symbol

            // Generate the interface method
            return returnType + " " + methodSignature + ";";
        }
        return "// Invalid operation format: " + operation;
    }

    /**
     * Represents a class diagram in the UML Editor.
     * Provides attributes, operations, and methods for managing its graphical representation.
     */
    static class ClassDiagram implements Serializable {

        /** X and Y coordinates of the class diagram on the canvas. */
        double x, y;

        /** Width of the rectangle representing the class diagram. */
        double width = 120;  // Width of the rectangle

        /** Height of the rectangle representing the class diagram. */
        double height = 50;  // Height of the rectangle

        /** List of attributes in the class. */
        List<String> attributes = new ArrayList<>();

        /** List of operations (methods) in the class. */
        List<String> operations = new ArrayList<>();

        /** Name of the class. */
        String className = "Class"; // Default class name

        /** Serialization version ID for compatibility. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a ClassDiagram with specified coordinates.
         *
         * @param x the x-coordinate of the class diagram
         * @param y the y-coordinate of the class diagram
         */
        ClassDiagram(double x, double y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Calculates and retrieves connection points for the diagram.
         *
         * @return a 2D array of connection point coordinates
         */
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

        /**
         * Draws connection points on the canvas.
         *
         * @param gc the GraphicsContext used for drawing
         */
        public void drawConnectionPoints(GraphicsContext gc) {
            gc.setFill(Color.RED);
            double radius = 5.0;  // Radius of connection points
            for (double[] point : getConnectionPoints()) {
                gc.fillOval(point[0] - radius, point[1] - radius, 2 * radius, 2 * radius);
            }
        }

        /**
         * Calculates and returns the height of the class diagram based on its attributes and operations.
         *
         * @return the calculated height
         */
        double getHeight() {
            return 50 + 20 * attributes.size() + 20 * operations.size();
        }

    }

    /**
     * Represents a connection (e.g., line) between two class diagrams.
     * Supports different connection types such as association, inheritance, aggregation, etc.
     */
    private static class LineConnection implements Serializable {

        /** The starting diagram for the connection. */
        ClassDiagram startDiagram;

        /** The ending diagram for the connection. */
        ClassDiagram endDiagram;

        /** Index of the connection point on the starting diagram. */
        int startConnectionIndex;

        /** Index of the connection point on the ending diagram. */
        int endConnectionIndex;

        /**
         * The type of line, represented as a Button (e.g., Aggregation, Composition).
         * Marked as transient to avoid serialization.
         */
        transient Button lineType;

        /** Text representing the type of the line (e.g., "Aggregation"). */
        String lineTypeText;

        /** Optional label or text associated with the connection. */
        String text;

        /** Intermediate control points for the connection line. */
        List<double[]> controlPoints = new ArrayList<>(); // Intermediate control points

        /** Serialization version ID for compatibility. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a LineConnection between two class diagrams with specified connection points and line type.
         *
         * @param startDiagram        the starting diagram
         * @param startConnectionIndex the index of the connection point on the starting diagram
         * @param endDiagram          the ending diagram
         * @param endConnectionIndex  the index of the connection point on the ending diagram
         * @param lineType            the type of the line as a Button
         */
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

        /**
         * Retrieves the start point of the connection.
         *
         * @return the coordinates of the start point
         */
        public double[] getStartPoint() {
            return startDiagram.getConnectionPoints()[startConnectionIndex];
        }

        /**
         * Retrieves the end point of the connection.
         *
         * @return the coordinates of the end point
         */
        public double[] getEndPoint() {
            return endDiagram.getConnectionPoints()[endConnectionIndex];
        }

        /**
         * Retrieves all points in the connection, including start, control, and end points.
         *
         * @return a list of all connection points
         */
        public List<double[]> getAllPoints() {
            // Combine start, control points, and end points for rendering
            List<double[]> points = new ArrayList<>();
            points.add(getStartPoint());
            points.addAll(controlPoints);
            points.add(getEndPoint());
            return points;
        }

        /**
         * Initializes the line type Button based on its text identifier.
         *
         * @param aggregationButton the button for Aggregation
         * @param compositionButton the button for Composition
         * @param associationButton the button for Association
         * @param inheritanceButton the button for Inheritance
         */
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

    /**
     * Represents an interface diagram in the UML Editor.
     * Extends the ClassDiagram class but omits attributes.
     */
    static class InterfaceDiagram extends ClassDiagram implements Serializable {

        /** Name of the interface. */
        String interfaceName = "Interface"; // Default interface name

        /** Serialization version ID for compatibility. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs an InterfaceDiagram with specified coordinates.
         *
         * @param x the x-coordinate of the interface diagram
         * @param y the y-coordinate of the interface diagram
         */
        InterfaceDiagram(double x, double y) {
            super(x, y);
        }

        /**
         * Calculates and returns the height of the interface diagram based on its operations.
         * Interfaces do not include attributes.
         *
         * @return the calculated height
         */
        @Override
        public double getHeight() {
            return 50 + 20 * operations.size(); // Interfaces don't have attributes
        }
    }


}
