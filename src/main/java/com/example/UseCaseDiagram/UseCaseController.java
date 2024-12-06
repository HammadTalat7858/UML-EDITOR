package com.example.UseCaseDiagram;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

/**
 * Controller class for managing the logic and operations related to the Use Case Diagram
 * in the UML Editor.
 *
 * <p>This class handles all the logical functionality associated with the Use Case Diagram,
 * including the management of UML model components such as actors, use cases, and their
 * relationships (e.g., associations, extensions, inclusions). It bridges the UI and
 * business logic layers.</p>
 *
 * <p>Responsibilities of this class include:</p>
 * <ul>
 *   <li>Creating and updating UML components in the use case diagram.</li>
 *   <li>Validating relationships and constraints, such as extensions and inclusions.</li>
 *   <li>Saving and loading use case diagram data as part of a project.</li>
 *   <li>Providing export functionality for visual artifacts (e.g., PNG, JPEG).</li>
 * </ul>
 *
 * <p>Constraints:</p>
 * <ul>
 *   <li>Implements relevant design patterns to ensure modularity and ease of testing.</li>
 * </ul>
 *
 * @author Hammad Tallat
 * @author Ahmed Moeez
 * @author Muhammad Hassnain
 *
 */

public class UseCaseController {

    /** Button for creating an actor element. */
    @FXML
    Button actorButton;

    /** Container pane for the canvas where diagrams are drawn. */
    @FXML
    Pane canvasContainer;

    /** VBox layout for the toolbox panel. */
    @FXML
    VBox toolboxVBox;

    /** VBox layout for the properties panel. */
    @FXML
    private VBox propertiesPanel;

    /** Menu item for exporting the diagram as a JPEG file. */
    @FXML
    private MenuItem jpegMenuItem;

    /** Menu item for exporting the diagram as a PNG file. */
    @FXML
    private MenuItem pngMenuItem;

    /** The currently active button in the toolbar. */
    private Button activeButton;

    /** List to store all created actor elements. */
    List<Actor> actors = new ArrayList<>();

    /** The currently selected actor in the diagram. */
    private Actor selectedActor = null;

    /** Offset for drag operations along the X-axis. */
    private double dragOffsetX;

    /** Offset for drag operations along the Y-axis. */
    private double dragOffsetY;

    /** Button for creating a use case element. */
    @FXML
    Button useCaseButton;

    /** List to store all created use case elements. */
    List<UseCase> useCases = new ArrayList<>();

    /** The currently selected use case in the diagram. */
    private UseCase selectedUseCase = null;

    /** The currently selected line connection in the diagram. */
    private LineConnection selectedLine = null;

    /** Button for creating an association relationship. */
    @FXML
    private Button associationButton;

    /** Button for creating a subject element. */
    @FXML
    Button subjectButton;

    /** State flag indicating whether an association is being drawn. */
    private boolean isDrawingAssociation = false;

    /** Starting X-coordinate for a line being drawn. */
    private double startX;

    /** Starting Y-coordinate for a line being drawn. */
    private double startY;

    /** Ending X-coordinate for a line being drawn. */
    private double endX;

    /** Ending Y-coordinate for a line being drawn. */
    private double endY;

    /** Starting actor for an association relationship. */
    private Actor startActor = null;

    /** Starting use case for an association relationship. */
    private UseCase startUseCase = null;

    /** List to store all created association relationships. */
    private List<LineConnection> associations = new ArrayList<>();

    /** Index of the connection point for the starting element of an association. */
    private int startConnectionIndex = -1;

    /** List to store all created subject elements. */
    List<UseCaseSubject> subjects = new ArrayList<>();

    /** The currently selected subject element in the diagram. */
    private UseCaseSubject selectedSubject = null;

    /** State flag indicating whether a subject is being resized. */
    private boolean isResizingSubject = false;

    /** Offset for resizing operations along the X-axis. */
    private double resizeOffsetX;

    /** Offset for resizing operations along the Y-axis. */
    private double resizeOffsetY;

    /** Button for creating an include relationship. */
    @FXML
    private Button includeButton;

    /** State flag indicating whether an include relationship is being drawn. */
    private boolean isDrawingInclude = false;

    /** Button for creating an extend relationship. */
    @FXML
    private Button extendButton;

    /** State flag indicating whether an extend relationship is being drawn. */
    private boolean isDrawingExtend = false;

    /** Button for deleting selected elements in the diagram. */
    @FXML
    Button deleteButton;

    /** Menu item for saving the diagram. */
    @FXML
    private MenuItem SaveAs;

    /** Menu item for loading a saved diagram. */
    @FXML
    private MenuItem Load;

    /** Menu item for closing the current diagram. */
    @FXML
    private MenuItem Close;

    /** Menu item for loading a saved use case diagram. */
    @FXML
    private MenuItem loadusecase;

    /** Menu item for loading a saved class diagram. */
    @FXML
    private MenuItem loadClass;

    /**
     * Initializes the Use Case Diagram controller, setting up event listeners,
     * default configurations, and initial states for the canvas and UI elements.
     *
     * <p>This method is automatically called after the FXML file has been loaded
     * and is responsible for linking the UI components with their functionality.</p>
     *
     * <p>Key initialization tasks:</p>
     * <ul>
     *   <li>Creates and configures the canvas for drawing use case diagrams.</li>
     *   <li>Sets up event listeners for user interactions, including mouse and keyboard events.</li>
     *   <li>Ensures the canvas dynamically adapts to container size changes.</li>
     *   <li>Initializes grid drawing and handles export options.</li>
     * </ul>
     */
    @FXML
    public void initialize() {
        // Create a new canvas and add it to the canvasContainer
        Canvas canvas = new Canvas(910, 780); // Initial size
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);
        // Add mouse event listeners to the canvas
        canvas.setOnMousePressed(event -> onMousePressed(event, gc));
        canvas.setOnMouseDragged(event -> onMouseDragged(event, gc));
        canvas.setOnMouseReleased(event -> onMouseReleased(event, gc));


        // Handle actor button clicks
        actorButton.setOnAction(event -> handleActorButtonClick(gc));
        useCaseButton.setOnAction(event -> handleUseCaseButtonClick(gc));
        associationButton.setOnAction(event -> handleAssociationButtonClick());
        subjectButton.setOnAction(actionEvent -> handleSubjectButtonClick(gc));
        includeButton.setOnAction(actionEvent -> handleIncludeButtonClick(gc));
        extendButton.setOnAction(event -> handleExtendButtonClick(gc));
        deleteButton.setOnAction(event -> handleDeleteAction(gc));

        canvasContainer.setFocusTraversable(true); // Make it focusable
        canvasContainer.setCache(true);
        canvasContainer.setCacheHint(CacheHint.SPEED);


        canvasContainer.setOnMouseClicked(event -> {
            // Check if a TextField exists
            boolean hasTextField = canvasContainer.getChildren().stream()
                    .anyMatch(node -> node instanceof TextField);

            if (!hasTextField) {
                // Delay focus request to avoid stealing focus during editing
                Platform.runLater(canvasContainer::requestFocus);
            }
        });

        // Add listeners to ensure the grid is redrawn when the container size changes
        canvasContainer.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            drawGrid(gc);
            redrawCanvas(gc);
        });

        canvasContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setHeight(newValue.doubleValue());
            drawGrid(gc);
            redrawCanvas(gc);
        });
        canvasContainer.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                handleDeleteAction(gc);
            }
        });

        // Initial grid drawing
        drawGrid(gc);
        jpegMenuItem.setOnAction(event -> exportAsJPEG());
        pngMenuItem.setOnAction(event -> exportAsPNG());
        // Set up event listeners for actor creation
        actorButton.setOnAction(event -> handleActorButtonClick(gc));
        canvas.setOnMouseClicked(event -> {

            if (activeButton == actorButton) {
                createActor(gc, event.getX(), event.getY());


            } else if (activeButton == useCaseButton) {
                createUseCase(gc, event.getX(), event.getY());

            } else if (activeButton == subjectButton) {
                createUseCaseSubject(gc, event.getX(), event.getY());
            }


        });
        toolboxVBox.setOnMouseClicked(event -> deselectActiveButton());
        propertiesPanel.setOnMouseClicked(event -> deselectActiveButton());
    }

    /**
     * Draws a grid on the canvas for better alignment and layout of diagram elements.
     *
     * <p>The grid spacing is configurable and helps users visually organize
     * components within the diagram. This method ensures the canvas dimensions
     * are cleared and recalculated before redrawing the grid.</p>
     *
     * @param gc the GraphicsContext of the canvas used for drawing the grid
     */
    private void drawGrid(GraphicsContext gc) {
        // Ensure the canvas dimensions are correct
        double canvasWidth = canvasContainer.getWidth();
        double canvasHeight = canvasContainer.getHeight();

        // Clear the canvas
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        double gridSpacing = 10; // Grid spacing

        gc.setLineWidth(0.5);
        gc.setStroke(Color.rgb(180, 180, 180));

        // Draw vertical grid lines
        for (double x = 0; x <= canvasWidth; x += gridSpacing) {
            gc.strokeLine(x, 0, x, canvasHeight);
        }

        // Draw horizontal grid lines
        for (double y = 0; y <= canvasHeight; y += gridSpacing) {
            gc.strokeLine(0, y, canvasWidth, y);
        }
    }

    /**
     * Loads the Use Case Diagram view, replacing the current diagram scene.
     *
     * <p>This method prompts the user for confirmation before switching, ensuring
     * unsaved changes are acknowledged. If confirmed, it loads the Use Case Diagram
     * scene from the FXML file and replaces the current stage's scene.</p>
     *
     * <p>Handles errors gracefully by displaying an error message if the switch fails.</p>
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
     * Loads the Class Diagram view, replacing the current diagram scene.
     *
     * <p>This method prompts the user for confirmation before switching, ensuring
     * unsaved changes are acknowledged. If confirmed, it loads the Class Diagram
     * scene from the FXML file and replaces the current stage's scene.</p>
     *
     * <p>Handles errors gracefully by displaying an error message if the switch fails.</p>
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
     * Handles the event when the "Extend" button is clicked, toggling the active state of the button.
     *
     * <p>If the "Extend" button is already active, this method deselects it and clears any active elements
     * on the canvas. Otherwise, it activates the button to allow the user to draw "Extend" relationships.</p>
     *
     * @param gc the GraphicsContext for the canvas where the operations will be performed
     */
    private void handleExtendButtonClick(GraphicsContext gc) {
        if (activeButton == extendButton) {
            deselectActiveButton();
            deselectActiveElement(gc);
        } else {
            activateButton(extendButton);
        }
    }

    /**
     * Exports the current canvas content as a JPEG image file.
     *
     * <p>This method uses a file chooser to select the destination and calls {@link #saveCanvasToFile(String)}
     * with "jpeg" as the format. Handles saving errors gracefully.</p>
     */
    @FXML
    private void exportAsJPEG() {
        saveCanvasToFile("jpeg");
    }

    /**
     * Exports the current canvas content as a PNG image file.
     *
     * <p>This method uses a file chooser to select the destination and calls {@link #saveCanvasToFile(String)}
     * with "png" as the format. Handles saving errors gracefully.</p>
     */
    @FXML
    private void exportAsPNG() {
        saveCanvasToFile("png");
    }

    /**
     * Handles the close action for the application, displaying a confirmation dialog before exiting.
     *
     * <p>If the user confirms, the application exits. If the user cancels, the dialog simply closes without
     * further action.</p>
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
     * Saves the current canvas content to a file in the specified format.
     *
     * <p>This method creates a snapshot of the canvas and allows the user to save it using a file chooser.
     * It supports multiple formats (e.g., JPEG, PNG) and handles format-specific adjustments, such as removing
     * the alpha channel for JPEG images.</p>
     *
     * @param format the file format to save the canvas as (e.g., "jpeg", "png")
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
     * Converts a {@link WritableImage} to a {@link BufferedImage}, preserving its pixel data.
     *
     * <p>This method reads pixel data from the {@link WritableImage}'s {@link PixelReader} and writes
     * it to a new {@link BufferedImage} object.</p>
     *
     * @param writableImage the image to be converted
     * @return a BufferedImage representation of the provided WritableImage
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
     * Removes the alpha channel from a {@link BufferedImage}, replacing it with a solid white background.
     *
     * <p>This method is particularly useful for formats like JPEG that do not support transparency.</p>
     *
     * @param originalImage the original image with an alpha channel
     * @return a new BufferedImage without an alpha channel
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
     * Deselects the currently active element on the canvas and clears its selection state.
     *
     * <p>This method resets the selected actor, use case, subject, and line connection to {@code null}
     * and triggers a canvas redraw to update the view.</p>
     *
     * @param gc the GraphicsContext for the canvas where the operations will be performed
     */
    private void deselectActiveElement(GraphicsContext gc) {
        // Reset selected elements
        selectedActor = null;
        selectedUseCase = null;

        // Redraw canvas
        redrawCanvas(gc);
    }

    /**
     * Handles the delete action, removing the selected element from the diagram.
     *
     * <p>This method supports deletion of actors, use cases, subjects, and line connections,
     * along with their associated relationships. It also ensures the canvas is redrawn to reflect
     * the changes.</p>
     *
     * @param gc the GraphicsContext for the canvas where the operations will be performed
     */
    private void handleDeleteAction(GraphicsContext gc) {
        if (selectedLine != null) {
            // Remove the selected line
            associations.remove(selectedLine);
            selectedLine = null; // Clear the selected line
        } else if (selectedActor != null) {
            // Remove associated lines
            associations.removeIf(line -> line.startElement == selectedActor || line.endElement == selectedActor);
            // Remove the actor
            actors.remove(selectedActor);
            selectedActor = null;
        } else if (selectedUseCase != null) {
            // Remove associated lines
            associations.removeIf(line -> line.startElement == selectedUseCase || line.endElement == selectedUseCase);
            // Remove from the global useCases list
            useCases.remove(selectedUseCase);

            // Check if the use case is inside a subject and remove it from there as well
            for (UseCaseSubject subject : subjects) {
                subject.containedUseCases.remove(selectedUseCase);
            }

            selectedUseCase = null;
        } else if (selectedSubject != null) {

            subjects.remove(selectedSubject);
            selectedSubject = null;
        }

        // Redraw the canvas to reflect deletions
        redrawCanvas(gc);
    }

    /**
     * Loads a saved diagram from a file and updates the canvas with the loaded data.
     *
     * <p>This method uses a file chooser to allow the user to select a diagram file
     * and deserializes the data into the application's data structures for actors, use cases,
     * subjects, and connections. It also triggers a canvas redraw to display the loaded diagram.</p>
     *
     * <p>Displays error messages in case of exceptions during the loading process.</p>
     */
    @FXML
    private void loadDiagramFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Diagram");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Diagram Files", "*.diagram"));
        File file = fileChooser.showOpenDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                // Load actors, use cases, subjects, and connections from file
                actors = (ArrayList<Actor>) ois.readObject();
                useCases = (ArrayList<UseCase>) ois.readObject();
                subjects = (ArrayList<UseCaseSubject>) ois.readObject();
                associations = (ArrayList<LineConnection>) ois.readObject();

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
     * Saves the current diagram to a file for later use.
     *
     * <p>This method uses a file chooser to allow the user to specify the destination file
     * and serializes the application's data structures for actors, use cases, subjects,
     * and connections into the file. Displays success or error messages based on the operation's result.</p>
     */
    @FXML
    private void saveDiagramToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Diagram Files", "*.diagram"));
        File file = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Save actors, use cases, subjects, and connections to file
                oos.writeObject(new ArrayList<>(actors));
                oos.writeObject(new ArrayList<>(useCases));
                oos.writeObject(new ArrayList<>(subjects));
                oos.writeObject(new ArrayList<>(associations));

                showInfo("Diagram saved successfully to " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error saving diagram: " + e.getMessage());
            }
        }
    }

    /**
     * Displays an informational alert with the specified message.
     *
     * <p>This method creates a simple alert dialog box with the provided message to inform the user
     * about the outcome of an operation (e.g., success or failure).</p>
     *
     * @param message the message to display in the alert dialog
     */
    private void showInfo(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the "Include" button click event, toggling the active state for creating "Include" relationships.
     *
     * <p>If the "Include" button is already active, this method deselects it and clears any active elements.
     * Otherwise, it activates the button to allow drawing "Include" relationships.</p>
     *
     * @param gc the GraphicsContext for the canvas where the operations will be performed
     */
    private void handleIncludeButtonClick(GraphicsContext gc) {
        if (activeButton == includeButton) {
            deselectActiveButton();
            deselectActiveElement(gc);

        } else {
            activateButton(includeButton);
        }
    }

    /**
     * Handles mouse press events on the canvas, performing various actions based on the current context.
     *
     * <p>This method is central to managing user interactions with the canvas, including:
     * <ul>
     *   <li>Drawing association, include, and extend lines</li>
     *   <li>Selecting and editing actors, use cases, subjects, and line connections</li>
     *   <li>Dragging or resizing subjects</li>
     *   <li>Highlighting selected elements</li>
     *   <li>Clearing selection when clicking on empty areas</li>
     * </ul>
     * </p>
     *
     * @param event the MouseEvent containing details about the mouse press (e.g., position, click count)
     * @param gc the GraphicsContext used for rendering operations on the canvas
     */
    private void onMousePressed(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        // Reset dragging offsets
        dragOffsetX = 0;
        dragOffsetY = 0;

        // 1. Handle Association line drawing
        if (activeButton == associationButton) {
            startActor = null;
            startUseCase = null;
            deselectActiveElement(gc);
            startConnectionIndex = -1;

            // Check for actor connection points
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startActor = actor;
                        startConnectionIndex = i;
                        isDrawingAssociation = true;
                        return;
                    }
                }
            }

            // Check for use case connection points
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startUseCase = useCase;
                        startConnectionIndex = i;
                        isDrawingAssociation = true;
                        return;
                    }
                }
            }

            showError("Start a line from a valid connection point.");
            return;
        }

        // 2. Handle Include line drawing
       else if (activeButton == includeButton) {
            startActor = null;
            startUseCase = null;
            deselectActiveElement(gc);
            startConnectionIndex = -1;

            // Check for UseCase connection points
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startUseCase = useCase;
                        startConnectionIndex = i;
                        isDrawingInclude = true;
                        return;
                    }
                }
            }

            // Check for Actor connection points
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startActor = actor;
                        startConnectionIndex = i;
                        isDrawingInclude = true;
                        return;
                    }
                }
            }

            showError("Start an include line from a valid connection point.");
            return;
        }

        // 3. Handle Extend line drawing
       else if (activeButton == extendButton) {
            startActor = null; // Extend lines cannot connect to actors
            startUseCase = null;
            deselectActiveElement(gc);
            startConnectionIndex = -1;

            // Check for UseCase connection points only
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startUseCase = useCase;
                        startConnectionIndex = i;
                        isDrawingExtend = true;
                        return;
                    }
                }
            }
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startActor = actor;
                        startConnectionIndex = i;
                        isDrawingExtend = true;
                        return;
                    }
                }
            }

            showError("Start an extend line from a valid use case connection point.");
            return;
        }

        // 4. Check for items inside subjects (UseCases or Actors)
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOver(mouseX, mouseY)) {
                // Check for UseCases inside this subject
                for (UseCase useCase : useCases) {
                    if (subject.contains(useCase.getX(), useCase.getY()) && isMouseOverUseCase(mouseX, mouseY, useCase)) {
                        selectedUseCase = useCase;
                        selectedActor = null;
                        selectedSubject = null;
                        selectedLine = null;

                        // Handle double-click for editing text
                        if (event.getClickCount() == 2) {
                            editUseCaseText(useCase, gc);
                            return;
                        }

                        // Set drag offsets
                        dragOffsetX = mouseX - useCase.getX();
                        dragOffsetY = mouseY - useCase.getY();

                        // Highlight the selected use case
                        redrawCanvas(gc);
                        highlightUseCase(gc, useCase);
                        return;
                    }
                }

                // Check for actors inside this subject
                for (Actor actor : actors) {
                    if (subject.contains(actor.getX(), actor.getY()) && isMouseOverActor(mouseX, mouseY, actor)) {
                        selectedActor = actor;
                        selectedUseCase = null;
                        selectedSubject = null;
                        selectedLine = null;


                        // Handle double-click for editing text
                        if (event.getClickCount() == 2) {
                            editActorText(actor, gc);
                            return;
                        }

                        // Calculate drag offsets
                        dragOffsetX = mouseX - actor.getX();
                        dragOffsetY = mouseY - actor.getY();

                        // Highlight the selected actor
                        redrawCanvas(gc);
                        highlightActor(gc, actor);
                        return;
                    }
                }
            }
        }
        for (LineConnection line : associations) {
            if (isMouseNearLine(mouseX, mouseY, line)) {
                selectedLine = line; // Set the clicked line as selected
                selectedActor = null;
                selectedUseCase = null;
                selectedSubject = null;
                redrawCanvas(gc); // Highlight the selected line
                return;
            }
        }

        // 5. Handle resizing logic for subjects
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOverBorder(mouseX, mouseY)) {
                selectedSubject = subject;
                selectedActor=null;
                selectedUseCase=null;
                selectedLine=null;
                isResizingSubject = true;

                // Set resize offsets
                resizeOffsetX = mouseX;
                resizeOffsetY = mouseY;
                return;
            }
        }

        // 6. Handle subject dragging
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOver(mouseX, mouseY)) {
                selectedSubject = subject;
                selectedActor=null;
                selectedUseCase=null;
                selectedLine=null;
                isResizingSubject = false;

                // Handle double-click for editing heading
                if (event.getClickCount() == 2) {
                    editSubjectHeading(subject, gc);
                    return;
                }

                // Set drag offsets
                dragOffsetX = mouseX - subject.x;
                dragOffsetY = mouseY - subject.y;

                // Highlight the subject
                redrawCanvas(gc);
                highlightUseCaseSubject(gc, subject);
                return;
            }
        }

        // 7. Handle actor selection logic
        for (Actor actor : actors) {
            if (isMouseOverActor(mouseX, mouseY, actor)) {
                selectedActor = actor;
                selectedUseCase = null;
                selectedSubject = null;
                selectedLine = null;


                // Handle double-click for editing text
                if (event.getClickCount() == 2) {
                    editActorText(actor, gc);
                    return;
                }

                // Calculate drag offsets
                dragOffsetX = mouseX - actor.getX();
                dragOffsetY = mouseY - actor.getY();

                // Highlight the selected actor
                redrawCanvas(gc);
                highlightActor(gc, actor);
                return;
            }
        }

        // 8. Handle UseCase selection logic
        for (UseCase useCase : useCases) {
            if (isMouseOverUseCase(mouseX, mouseY, useCase)) {
                selectedUseCase = useCase;
                selectedActor = null;
                selectedSubject = null;
                selectedLine = null;


                // Handle double-click for editing text
                if (event.getClickCount() == 2) {
                    editUseCaseText(useCase, gc);
                    return;
                }

                // Highlight the selected use case
                redrawCanvas(gc);
                highlightUseCase(gc, useCase);
                return;
            }
        }

        // 9. If nothing is clicked, deselect everything
        selectedActor = null;
        selectedUseCase = null;
        selectedSubject = null;
        selectedLine=null;
        redrawCanvas(gc);
    }

    /**
     * Checks if the mouse is near a given line connection.
     *
     * <p>Calculates the perpendicular distance from the mouse pointer to the line
     * and compares it to a defined tolerance value.</p>
     *
     * @param mouseX the X-coordinate of the mouse pointer
     * @param mouseY the Y-coordinate of the mouse pointer
     * @param line the line connection to check
     * @return {@code true} if the mouse is near the line, {@code false} otherwise
     */
    private boolean isMouseNearLine(double mouseX, double mouseY, LineConnection line) {
        double[] start = line.getStartPoint();
        double[] end = line.getEndPoint();

        // Calculate the distance from the mouse point to the line
        double distance = Math.abs((end[1] - start[1]) * mouseX - (end[0] - start[0]) * mouseY + end[0] * start[1] - end[1] * start[0])
                / Math.sqrt(Math.pow(end[1] - start[1], 2) + Math.pow(end[0] - start[0], 2));

        double tolerance = 5.0; // Allowable distance to consider a click on the line
        return distance <= tolerance;
    }

    /**
     * Enables the user to edit the heading of a Use Case Subject using a text field.
     *
     * <p>A temporary {@link TextField} is added to the canvas for editing. Changes are committed
     * either on pressing Enter or when the field loses focus.</p>
     *
     * @param subject the {@link UseCaseSubject} whose heading is to be edited
     * @param gc the {@link GraphicsContext} for rendering updates
     */
    private void editSubjectHeading(UseCaseSubject subject, GraphicsContext gc) {
        // Create a TextField for editing the subject's name
        TextField textField = new TextField(subject.name);

        // Use a Text object to calculate the width of the current text
        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(subject.name);
        textHelper.setFont(Font.font("Arial", 14)); // Match the drawing font
        double textWidth = textHelper.getBoundsInLocal().getWidth();

        // Center the TextField horizontally and align it with the top heading position
        textField.setPrefWidth(subject.width - 20); // Constrain to the subject width
        double textFieldX = subject.x + (subject.width / 2) - (textField.getPrefWidth() / 2);
        double textFieldY = subject.y + 10; // Slightly below the top edge for alignment

        textField.setLayoutX(textFieldX);
        textField.setLayoutY(textFieldY);
        textField.setStyle("-fx-border-color: lightblue; -fx-font-size: 14px;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(textField);
        textField.requestFocus();

        // Commit changes on Enter key press
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                subject.name = textField.getText().trim();
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw to update the heading
            }
        });

        // Commit changes on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                subject.name = textField.getText().trim();
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw to update the heading
            }
        });
    }

    /**
     * Determines if a point is near a given reference point within a specified tolerance.
     *
     * <p>This is useful for detecting mouse clicks near specific connection points
     * on actors, use cases, or other diagram elements.</p>
     *
     * @param mouseX the X-coordinate of the mouse pointer
     * @param mouseY the Y-coordinate of the mouse pointer
     * @param pointX the X-coordinate of the reference point
     * @param pointY the Y-coordinate of the reference point
     * @return {@code true} if the mouse is within the tolerance of the point, {@code false} otherwise
     */
    private boolean isNear(double mouseX, double mouseY, double pointX, double pointY) {
        double tolerance = 10.0; // Snap radius
        return Math.abs(mouseX - pointX) < tolerance && Math.abs(mouseY - pointY) < tolerance;
    }

    /**
     * Handles the logic for toggling the subject button.
     *
     * <p>If the subject button is already active, it deselects it and clears any
     * selected elements. Otherwise, it activates the button for subject creation.</p>
     *
     * @param gc the {@link GraphicsContext} for rendering updates
     */
    private void handleSubjectButtonClick(GraphicsContext gc) {
        if (activeButton == subjectButton) {
            deselectActiveButton();
            deselectActiveElement(gc);

        } else {
            activateButton(subjectButton);
        }
    }

    /**
     * Creates a new Use Case Subject at the specified coordinates and draws it on the canvas.
     *
     * @param gc the {@link GraphicsContext} for rendering the subject
     * @param x the X-coordinate of the top-left corner of the subject
     * @param y the Y-coordinate of the top-left corner of the subject
     */
    private void createUseCaseSubject(GraphicsContext gc, double x, double y) {
        UseCaseSubject subject = new UseCaseSubject(x, y);
        subjects.add(subject);
        drawUseCaseSubject(gc, subject);
    }

    /**
     * Draws a Use Case Subject on the canvas, including its rectangle and heading text.
     *
     * <p>The heading text is horizontally centered and slightly below the top edge of the rectangle.</p>
     *
     * @param gc the {@link GraphicsContext} for rendering the subject
     * @param subject the {@link UseCaseSubject} to be drawn
     */
    private void drawUseCaseSubject(GraphicsContext gc, UseCaseSubject subject) {
        // Draw the rectangle for the subject
        gc.setFill(Color.WHITE);
        gc.fillRect(subject.x, subject.y, subject.width, subject.height);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(subject.x, subject.y, subject.width, subject.height);

        // Calculate the position for horizontally centered, top-aligned text
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));

        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(subject.name);
        textHelper.setFont(gc.getFont());
        double textWidth = textHelper.getBoundsInLocal().getWidth();

        double textX = subject.x + (subject.width / 2) - (textWidth / 2); // Center horizontally
        double textY = subject.y + 20; // Position slightly below the top edge

        // Draw the subject's name
        gc.fillText(subject.name, textX, textY);
    }

    /**
     * Highlights a Use Case Subject by drawing a blue border around it.
     *
     * @param gc the {@link GraphicsContext} for rendering the highlight
     * @param subject the {@link UseCaseSubject} to be highlighted
     */
    private void highlightUseCaseSubject(GraphicsContext gc, UseCaseSubject subject) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        gc.strokeRect(subject.x - 2, subject.y - 2, subject.width + 4, subject.height + 4);
    }

    /**
     * Handles the logic for toggling the use case button.
     *
     * <p>If the button is active, it deselects it and clears any selected elements.
     * Otherwise, it activates the button for use case creation.</p>
     *
     * @param gc the {@link GraphicsContext} for rendering updates
     */
    private void handleUseCaseButtonClick(GraphicsContext gc) {
        if (activeButton == useCaseButton) {
            deselectActiveButton();
            deselectActiveElement(gc);
        } else {
            activateButton(useCaseButton);
        }
    }

    /**
     * Checks if the mouse pointer is over a given use case.
     *
     * <p>This method considers the oval shape of the use case when determining whether
     * the mouse pointer is inside its boundaries.</p>
     *
     * @param mouseX the X-coordinate of the mouse pointer
     * @param mouseY the Y-coordinate of the mouse pointer
     * @param useCase the {@link UseCase} to check
     * @return {@code true} if the mouse is over the use case, {@code false} otherwise
     */
    private boolean isMouseOverUseCase(double mouseX, double mouseY, UseCase useCase) {
        double x = useCase.getX();
        double y = useCase.getY();
        double width = useCase.getWidth();
        double height = useCase.getHeight();

        // Check if the mouse is within the oval
        double dx = mouseX - x;
        double dy = mouseY - y;
        return (dx * dx) / (width * width / 4) + (dy * dy) / (height * height / 4) <= 1;
    }

    /**
     * Creates a new use case at the specified coordinates and adds it to the diagram.
     *
     * <p>If the coordinates fall within a {@link UseCaseSubject}, the use case is associated with that subject,
     * and its position is adjusted to stay within the subject's boundaries.</p>
     *
     * @param gc the {@link GraphicsContext} for rendering updates
     * @param x the X-coordinate where the use case is created
     * @param y the Y-coordinate where the use case is created
     */
    private void createUseCase(GraphicsContext gc, double x, double y) {
        UseCase useCase = new UseCase(x, y); // Create a new UseCase object

        // Check if it's inside any subject
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOver(x, y)) {
                subject.addUseCase(useCase);
                useCase.x = Math.max(subject.x + 10, Math.min(x, subject.x + subject.width - 10));
                useCase.y = Math.max(subject.y + 30, Math.min(y, subject.y + subject.height - 10));
                break;
            }
        }

        useCases.add(useCase); // Add to the global list
        redrawCanvas(gc); // Redraw to reflect the changes
    }

    /**
     * Highlights a use case by drawing a light blue background around it.
     *
     * <p>The highlight is rendered as a filled oval slightly larger than the use case itself.
     * After highlighting, the use case is redrawn on top to ensure it remains visible.</p>
     *
     * @param gc the {@link GraphicsContext} for rendering the highlight
     * @param useCase the {@link UseCase} to be highlighted
     */
    private void highlightUseCase(GraphicsContext gc, UseCase useCase) {
        double x = useCase.getX();
        double y = useCase.getY();
        double width = useCase.getWidth();
        double height = useCase.getHeight();

        // Draw a light blue highlight around the use case
        gc.setFill(Color.LIGHTBLUE);
        gc.fillOval(x - width / 2, y - height / 2, width, height);

        // Redraw the use case on top of the highlight
        drawUseCase(gc, useCase);
    }

    /**
     * Draws a use case as an oval with connection points and a label.
     *
     * <p>The use case is rendered as a black-bordered oval, with red connection points
     * indicating possible linkages. The label is centered within the oval.</p>
     *
     * @param gc the {@link GraphicsContext} for rendering the use case
     * @param useCase the {@link UseCase} to be drawn
     */
    private void drawUseCase(GraphicsContext gc, UseCase useCase) {
        double x = useCase.getX();
        double y = useCase.getY();
        double width = useCase.getWidth();
        double height = useCase.getHeight();

        // Draw oval for the use case
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - width / 2, y - height / 2, width, height);

        // Draw connection points
        gc.setFill(Color.RED);
        for (double[] point : useCase.getConnectionPoints()) {
            gc.fillOval(point[0] - 3, point[1] - 3, 6, 6);
        }

        // Draw use case label
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(useCase.getName());
        textHelper.setFont(gc.getFont());
        double textWidth = textHelper.getBoundsInLocal().getWidth();
        double textHeight = textHelper.getBoundsInLocal().getHeight();

        gc.fillText(useCase.getName(), x - textWidth / 2, y + textHeight / 4); // Center the text
    }

    /**
     * Enables editing of the text label for a given use case.
     *
     * <p>A {@link TextField} is displayed at the use case's position, allowing the user to modify its name.
     * Changes are applied when the user presses Enter or the text field loses focus, and the canvas is redrawn
     * to reflect the updated label.</p>
     *
     * @param useCase the {@link UseCase} whose label is to be edited
     * @param gc the {@link GraphicsContext} for rendering updates
     */
    private void editUseCaseText(UseCase useCase, GraphicsContext gc) {
        // Create a TextField for editing the use case name
        TextField textField = new TextField(useCase.getName());
        textField.setLayoutX(useCase.getX() - useCase.getWidth() / 2 + 10); // Center horizontally
        textField.setLayoutY(useCase.getY() - 10); // Center vertically
        textField.setPrefWidth(100);
        textField.setStyle("-fx-border-color: lightblue; -fx-font-size: 12px;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(textField);
        textField.requestFocus();

        // Commit changes on Enter key press
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                useCase.setName(textField.getText().trim(), gc);
                canvasContainer.getChildren().remove(textField);
                redrawCanvas(gc);
            }
        });

        // Commit changes on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                useCase.setName(textField.getText().trim(), gc);
                canvasContainer.getChildren().remove(textField);
                redrawCanvas(gc);
            }
        });
    }

    /**
     * Enables editing of the text label for a given actor.
     *
     * <p>A {@link TextField} is displayed near the actor's figure, allowing the user to modify its name.
     * Changes are applied when the user presses Enter or the text field loses focus, and the canvas is redrawn
     * to reflect the updated label.</p>
     *
     * @param actor the {@link Actor} whose label is to be edited
     * @param gc the {@link GraphicsContext} for rendering updates
     */
    private void editActorText(Actor actor, GraphicsContext gc) {
        // Create a TextField for editing the actor name
        TextField textField = new TextField(actor.getName());
        textField.setLayoutX(actor.getX() - 30); // Adjust to center the TextField
        textField.setLayoutY(actor.getY() + 30); // Place below the actor figure
        textField.setPrefWidth(100);
        textField.setStyle("-fx-border-color: lightblue; -fx-font-size: 12px;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(textField);
        textField.requestFocus();

        // Commit changes on Enter key press
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                actor.name = textField.getText().trim(); // Update the actor's name
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw the canvas to update the name
            }
        });

        // Commit changes on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                actor.name = textField.getText().trim(); // Update the actor's name
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw the canvas to update the name
            }
        });
    }

    /**
     * Handles mouse drag events for various elements on the canvas.
     *
     * <p>This method performs actions such as:
     * <ul>
     *   <li>Previewing association, include, or extend lines while drawing</li>
     *   <li>Dragging and repositioning subjects, actors, or use cases</li>
     *   <li>Resizing subjects while respecting minimum size constraints</li>
     * </ul>
     * The canvas is continuously updated to reflect these changes.</p>
     *
     * @param event the {@link MouseEvent} containing the current mouse position
     * @param gc the {@link GraphicsContext} for rendering updates
     */
    private void onMouseDragged(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Handle Association Line Preview
        if (activeButton == associationButton && isDrawingAssociation) {
            // Clear the canvas and redraw all elements except the association
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw other elements

            // Draw the temporary association line
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, mouseX, mouseY);
            return;
        }

        // Handle Include Line Preview
        else if (activeButton == includeButton && isDrawingInclude) {
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw other elements

            // Draw the temporary dotted line
            gc.setLineDashes(5); // Set dotted style
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, mouseX, mouseY);
            gc.setLineDashes(0); // Reset to solid lines
            return;
        }

        // Handle Extend Line Preview
        else if (activeButton == extendButton && isDrawingExtend) {
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw other elements

            // Draw the temporary dotted line with a distinct style for extend
            gc.setLineDashes(5); // Set dotted style
            gc.setStroke(Color.DARKBLUE); // Use a distinct color for extend
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, mouseX, mouseY);
            gc.setLineDashes(0); // Reset to solid lines
            return;
        }

        // Dragging a UseCaseSubject
        else if (selectedSubject != null && !isResizingSubject) {
            // Update subject position while respecting canvas boundaries
            double newX = Math.max(0, Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - selectedSubject.width));
            double newY = Math.max(0, Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - selectedSubject.height));

            selectedSubject.x = newX;
            selectedSubject.y = newY;

            // Redraw the canvas with the updated position
            redrawCanvas(gc);
            highlightUseCaseSubject(gc, selectedSubject);
            return;
        }

        // Resizing a UseCaseSubject
        else if (selectedSubject != null && isResizingSubject) {
            // Calculate new width and height while respecting boundaries
            double newWidth = Math.max(50, mouseX - selectedSubject.x); // Minimum width = 50
            double newHeight = Math.max(50, mouseY - selectedSubject.y); // Minimum height = 50

            selectedSubject.width = Math.min(newWidth, canvasContainer.getWidth() - selectedSubject.x);
            selectedSubject.height = Math.min(newHeight, canvasContainer.getHeight() - selectedSubject.y);

            // Redraw the canvas with the updated size
            redrawCanvas(gc);
            highlightUseCaseSubject(gc, selectedSubject);
            return;
        }

        // Handle dragging other elements (e.g., actors, use cases, etc.)
        if (selectedActor != null) {
            double newX = Math.max(20, Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - 20));
            double newY = Math.max(40, Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - 40));
            selectedActor.x = newX;
            selectedActor.y = newY;

            redrawCanvas(gc);
            highlightActor(gc, selectedActor);
        }

        if (selectedUseCase != null) {
            double newX = Math.max(selectedUseCase.getWidth() / 2,
                    Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - selectedUseCase.getWidth() / 2));
            double newY = Math.max(selectedUseCase.getHeight() / 2,
                    Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - selectedUseCase.getHeight() / 2));
            selectedUseCase.x = newX;
            selectedUseCase.y = newY;

            redrawCanvas(gc);
            highlightUseCase(gc, selectedUseCase);
        }
    }

    /**
     * Draws an arrow on the canvas from a start point to an end point.
     *
     * <p>The arrow includes a line from the start to the end point, and an arrowhead
     * at the end point. The arrowhead is drawn at an angle of 30 degrees on each side
     * of the line.</p>
     *
     * @param gc the {@link GraphicsContext} used to draw the arrow
     * @param start a double array representing the start point of the arrow ([x, y])
     * @param end a double array representing the end point of the arrow ([x, y])
     */
    private void drawArrow(GraphicsContext gc, double[] start, double[] end) {
        double arrowLength = 15; // Length of the arrowhead
        double arrowWidth = 7;   // Width of the arrowhead

        // Calculate the angle of the line
        double angle = Math.atan2(end[1] - start[1], end[0] - start[0]);

        // Calculate the two points of the arrowhead
        double x1 = end[0] - arrowLength * Math.cos(angle - Math.PI / 6);
        double y1 = end[1] - arrowLength * Math.sin(angle - Math.PI / 6);

        double x2 = end[0] - arrowLength * Math.cos(angle + Math.PI / 6);
        double y2 = end[1] - arrowLength * Math.sin(angle + Math.PI / 6);

        // Draw the arrowhead
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(end[0], end[1], x1, y1); // Left side of the arrowhead
        gc.strokeLine(end[0], end[1], x2, y2); // Right side of the arrowhead
    }

    /**
     * Handles mouse release events for various actions on the canvas.
     *
     * <p>This method finalizes actions such as:
     * <ul>
     *   <li>Drawing association, include, or extend lines between elements</li>
     *   <li>Resizing use case subjects</li>
     *   <li>Positioning actors or use cases</li>
     * </ul>
     * It validates the connection points for lines and provides visual feedback if
     * no valid connection is reached. Finally, the canvas is redrawn to reflect the updates.</p>
     *
     * @param event the {@link MouseEvent} containing details about the mouse release
     * @param gc the {@link GraphicsContext} used to render the updated state
     */
    private void onMouseReleased(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Handle Association Line
        if (activeButton == associationButton && isDrawingAssociation) {
            boolean validConnection = false;

            // Check for snapping to an actor's connection point
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        // Create a solid association line
                        associations.add(new LineConnection(
                                startActor != null ? startActor : startUseCase, // Start element
                                startConnectionIndex,                          // Start connection point
                                actor,                                         // End element
                                i,                                             // End connection point index
                                "association"                                  // Line type
                        ));
                        validConnection = true;
                        break;
                    }
                }
            }

            // Check for snapping to a use case's connection point
            if (!validConnection) {
                for (UseCase useCase : useCases) {
                    for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                        double[] point = useCase.getConnectionPoints()[i];
                        if (isNear(mouseX, mouseY, point[0], point[1])) {
                            // Create a solid association line
                            associations.add(new LineConnection(
                                    startActor != null ? startActor : startUseCase, // Start element
                                    startConnectionIndex,                          // Start connection point
                                    useCase,                                       // End element
                                    i,                                             // End connection point index
                                    "association"                                  // Line type
                            ));
                            validConnection = true;
                            break;
                        }
                    }
                }
            }

            if (!validConnection) {
                showError("Cannot draw association line. No valid connection point reached.");
            }

            isDrawingAssociation = false; // Reset association line drawing state
            redrawCanvas(gc);
        }

        // Handle Include Line
        else if (activeButton == includeButton && isDrawingInclude) {
            boolean validConnection = false;

            // Check for snapping to a use case's connection point
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        // Create a dotted include line
                        associations.add(new LineConnection(
                                startActor != null ? startActor : startUseCase, // Start element
                                startConnectionIndex,                           // Start connection point
                                useCase,                                        // End element
                                i,                                              // End connection point index
                                "include"                                       // Line type
                        ));
                        validConnection = true;
                        break;
                    }
                }
            }

            // Check for snapping to an actor's connection point
            if (!validConnection) {
                for (Actor actor : actors) {
                    for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                        double[] point = actor.getConnectionPoints()[i];
                        if (isNear(mouseX, mouseY, point[0], point[1])) {
                            // Create a dotted include line
                            associations.add(new LineConnection(
                                    startActor != null ? startActor : startUseCase, // Start element
                                    startConnectionIndex,                           // Start connection point
                                    actor,                                          // End element
                                    i,                                              // End connection point index
                                    "include"                                       // Line type
                            ));
                            validConnection = true;
                            break;
                        }
                    }
                }
            }

            if (!validConnection) {
                showError("Cannot draw include line. No valid connection point reached.");
            }

            isDrawingInclude = false; // Reset include line drawing state
            redrawCanvas(gc);
        }

        // Handle Extend Line
        else if (activeButton == extendButton && isDrawingExtend) {
            boolean validConnection = false;

            // Check for snapping to an actor's connection point
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        // Create a solid association line
                        associations.add(new LineConnection(
                                startActor != null ? startActor : startUseCase, // Start element
                                startConnectionIndex,                          // Start connection point
                                actor,                                         // End element
                                i,                                             // End connection point index
                                "extend"                                  // Line type
                        ));
                        validConnection = true;
                        break;
                    }
                }
            }

            // Check for snapping to a use case's connection point
            if (!validConnection) {
                for (UseCase useCase : useCases) {
                    for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                        double[] point = useCase.getConnectionPoints()[i];
                        if (isNear(mouseX, mouseY, point[0], point[1])) {
                            // Create a solid association line
                            associations.add(new LineConnection(
                                    startActor != null ? startActor : startUseCase, // Start element
                                    startConnectionIndex,                          // Start connection point
                                    useCase,                                       // End element
                                    i,                                             // End connection point index
                                    "extend"                                  // Line type
                            ));
                            validConnection = true;
                            break;
                        }
                    }
                }
            }

            if (!validConnection) {
                showError("Cannot draw extend line. No valid connection point reached.");
            }

            isDrawingExtend = false; // Reset association line drawing state
            redrawCanvas(gc);
        }

        // Finalize resizing of UseCaseSubject
        else if (selectedSubject != null && isResizingSubject) {
            isResizingSubject = false;
            redrawCanvas(gc);
            highlightUseCaseSubject(gc, selectedSubject);
            return;
        }

        // Finalize actor position
        if (selectedActor != null) {
            redrawCanvas(gc);
            highlightActor(gc, selectedActor);
        }

        // Finalize use case position
        if (selectedUseCase != null) {
            redrawCanvas(gc);
            highlightUseCase(gc, selectedUseCase);
        }
    }

    /**
     * Redraws the entire canvas, including the grid, subjects, use cases, actors, and association lines.
     *
     * <p>The method ensures that elements are rendered in the correct order:
     * <ol>
     *   <li>Background elements like use case subjects</li>
     *   <li>Use cases contained within subjects</li>
     *   <li>Other standalone use cases, actors, and association lines</li>
     * </ol>
     * If a line is selected, it highlights the line for better visibility.</p>
     *
     * @param gc the {@link GraphicsContext} used to draw elements on the canvas
     */
    private void redrawCanvas(GraphicsContext gc) {
        // Clear the canvas and redraw the grid
        gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
        drawGrid(gc);

        // Draw subjects first (background elements)
        for (UseCaseSubject subject : subjects) {
            drawUseCaseSubject(gc, subject);
        }

        // Draw contained use cases inside subjects
        for (UseCaseSubject subject : subjects) {
            for (UseCase useCase : subject.containedUseCases) {
                drawUseCase(gc, useCase);
            }
        }

        // Draw other elements (use cases, actors, and lines)
        for (UseCase useCase : useCases) {
            if (!isUseCaseInSubject(useCase)) { // Skip use cases already drawn within subjects
                drawUseCase(gc, useCase);
            }
        }
        for (Actor actor : actors) {
            drawActor(gc, actor);
        }
        for (LineConnection line : associations) {
            drawAssociationLine(gc, line);
        }
        if (selectedLine != null) {
            gc.setStroke(Color.web("#5DADE2"));
            gc.setLineWidth(3);
            double[] start = selectedLine.getStartPoint();
            double[] end = selectedLine.getEndPoint();
            gc.strokeLine(start[0], start[1], end[0], end[1]);
        }
    }

    /**
     * Checks whether a specific use case is contained within any use case subject.
     *
     * @param useCase the {@link UseCase} to check
     * @return {@code true} if the use case is inside a subject, {@code false} otherwise
     */
    private boolean isUseCaseInSubject(UseCase useCase) {
        for (UseCaseSubject subject : subjects) {
            if (subject.containedUseCases.contains(useCase)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Draws an association line on the canvas between two points, with specific styles
     * for different line types (e.g., solid, dotted) and optional labels like <<include>> or <<extend>>.
     *
     * <p>The method supports the following types:
     * <ul>
     *   <li><strong>Association:</strong> Solid line</li>
     *   <li><strong>Include:</strong> Dotted line with a <<include>> label and an arrow</li>
     *   <li><strong>Extend:</strong> Dotted line with a <<extend>> label and an arrow</li>
     * </ul>
     * Labels are positioned at the midpoint of the line.</p>
     *
     * @param gc the {@link GraphicsContext} used to draw the line
     * @param line the {@link LineConnection} object containing the line's start and end points,
     * type, and associated elements
     */
    private void drawAssociationLine(GraphicsContext gc, LineConnection line) {
        double[] start = line.getStartPoint();
        double[] end = line.getEndPoint();

        if ("include".equals(line.getType())) {
            gc.setLineDashes(5); // Dotted line for <<include>>
        }
       else if ("extend".equals(line.getType())) {
            gc.setLineDashes(5); // Dotted line for <<extend>>
        }
        else {
            gc.setLineDashes(0); // Solid line for association
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(start[0], start[1], end[0], end[1]);
        gc.setLineDashes(0); // Reset line style

        if ("include".equals(line.getType())) {
            // Draw <<include>> label
            double midX = (start[0] + end[0]) / 2;
            double midY = (start[1] + end[1]) / 2;
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText("<<include>>", midX, midY - 5);

            // Draw arrow for <<include>>
            drawArrow(gc, start, end);
        }
        else if ("extend".equals(line.getType())) {
            // Draw <<extend>> label
            double midX = (start[0] + end[0]) / 2;
            double midY = (start[1] + end[1]) / 2;
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText("<<extend>>", midX, midY - 5);

            // Draw arrow for <<extend>>
            drawArrow(gc, start, end);
        }
    }

    /**
     * Highlights the specified actor on the canvas by drawing a light blue background around its head and body,
     * then redraws the actor on top of the highlight for emphasis.
     *
     * <p>This method visually distinguishes the selected or hovered actor for better interaction feedback.</p>
     *
     * @param gc    the {@link GraphicsContext} used for drawing on the canvas
     * @param actor the {@link Actor} to be highlighted
     */
    private void highlightActor(GraphicsContext gc, Actor actor) {
        double x = actor.getX();
        double y = actor.getY();

        // Highlight actor with a light blue background
        gc.setFill(Color.LIGHTBLUE);
        gc.fillOval(x - 12, y - 42, 24, 24); // Slightly larger highlight for the head
        gc.fillRect(x - 20, y - 20, 40, 40); // Body and arm highlight

        // Redraw the actor on top of the highlight
        drawActor(gc, actor);
    }

    /**
     * Determines whether the mouse pointer is currently over the specified actor's head or body.
     *
     * <p>The method checks both the head (a circular area above the body) and the body (a rectangular area)
     * to provide accurate detection for interaction purposes.</p>
     *
     * @param mouseX the x-coordinate of the mouse pointer
     * @param mouseY the y-coordinate of the mouse pointer
     * @param actor  the {@link Actor} to check for mouse hover
     * @return {@code true} if the mouse pointer is over the actor, {@code false} otherwise
     */
    private boolean isMouseOverActor(double mouseX, double mouseY, Actor actor) {
        double x = actor.getX();
        double y = actor.getY();

        // Check if mouse is within the actor's head or body area
        return (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y - 40 && mouseY <= y) || // Head
                (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y && mouseY <= y + 20);  // Body
    }

    /**
     * Deselects the currently active tool button, resets its style, and clears any drawing or selection state.
     *
     * <p>This method ensures:
     * <ul>
     *   <li>Removal of the "tool-button-selected" class from the active button</li>
     *   <li>Reapplication of the default "tool-button" style if necessary</li>
     *   <li>Resetting of all line-drawing states and de-selection of any active elements on the canvas</li>
     * </ul>
     * </p>
     *
     * @see #deselectActiveElement(GraphicsContext)
     */
    private void deselectActiveButton() {
        if (activeButton != null) {
            // Remove the "tool-button-selected" class and reset it to "tool-button"
            activeButton.getStyleClass().removeAll("tool-button-selected");
            if (!activeButton.getStyleClass().contains("tool-button")) {
                activeButton.getStyleClass().add("tool-button");
            }
            activeButton = null; // Clear the active button
        }

        // Clear any drawing or selection state
        isDrawingAssociation = false;
        isDrawingInclude = false;
        isDrawingExtend = false;

        // Deselect any active elements
        deselectActiveElement(((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D());
    }

    /**
     * Handles the click event for the Actor button, toggling its active state.
     *
     * <p>If the Actor button is already active, it is deactivated and the canvas is cleared of any active elements.
     * If it is not active, it becomes the active button, and its style is updated accordingly.</p>
     *
     * @param gc the {@link GraphicsContext} used for updating the canvas
     * @see #deselectActiveButton()
     * @see #activateButton(Button)
     */
    private void handleActorButtonClick(GraphicsContext gc) {
        if (activeButton == actorButton) {
            // Deactivate the actor button if already active
            deselectActiveButton();
            deselectActiveElement(gc);
        } else {
            // Activate the actor button
            activateButton(actorButton);
        }
    }

    /**
     * Activates the specified button by resetting styles for all buttons and applying the "tool-button-selected" style to it.
     *
     * <p>This method ensures that only one button is visually highlighted and marked as active at a time.</p>
     *
     * @param button the {@link Button} to be activated
     * @see #resetAllButtonStyles()
     */
    private void activateButton(Button button) {
        // Reset styles for all buttons
        resetAllButtonStyles();

        // Add the "tool-button-selected" class to the active button
        activeButton = button;
        button.getStyleClass().add("tool-button-selected");
    }

    /**
     * Resets the styles of all tool buttons to their default state.
     *
     * <p>This method removes the "tool-button-selected" class and ensures that the default "tool-button" or
     * "action-button" style is applied to each button. It ensures consistent UI appearance before activating a new button.</p>
     */
    private void resetAllButtonStyles() {
        actorButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        actorButton.getStyleClass().add("tool-button");

        useCaseButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        useCaseButton.getStyleClass().add("tool-button");

        associationButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        associationButton.getStyleClass().add("tool-button");

        subjectButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        subjectButton.getStyleClass().add("tool-button");
        includeButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        includeButton.getStyleClass().add("action-button");
        extendButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        extendButton.getStyleClass().add("action-button");
    }

    /**
     * Handles the click event for the Association button, toggling its active state.
     *
     * <p>If the Association button is already active, it is deactivated by deselecting it. Otherwise, it is activated
     * and its style is updated to indicate selection.</p>
     *
     * @see #deselectActiveButton()
     * @see #activateButton(Button)
     */
    private void handleAssociationButtonClick() {
        if (activeButton == associationButton) {
            deselectActiveButton();

        } else {
            activateButton(associationButton);
        }
    }

    /**
     * Displays an error message in a modal alert dialog.
     *
     * <p>The dialog contains the specified error message, an "OK" button, and is of type {@code AlertType.ERROR}.</p>
     *
     * @param message the error message to display
     */
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Creates a new Actor at the specified coordinates and draws it on the canvas.
     *
     * <p>The actor is added to the list of actors, and its graphical representation is rendered on the canvas.</p>
     *
     * @param gc the {@link GraphicsContext} used for drawing
     * @param x  the x-coordinate of the actor's position
     * @param y  the y-coordinate of the actor's position
     * @see #drawActor(GraphicsContext, Actor)
     */

    private void createActor(GraphicsContext gc, double x, double y) {
        Actor actor = new Actor(x, y); // Create a new Actor object
        actors.add(actor); // Add to the list of actors
        drawActor(gc, actor); // Draw the actor on the canvas
    }

    /**
     * Draws a stick-figure representation of an actor and its connection points on the canvas.
     *
     * <p>This method renders the actor's head, body, arms, legs, and connection points. It also draws the actor's
     * name as a label below the figure, centered horizontally.</p>
     *
     * @param gc    the {@link GraphicsContext} used for drawing
     * @param actor the {@link Actor} object to draw
     */
    private void drawActor(GraphicsContext gc, Actor actor) {
        double x = actor.getX();
        double y = actor.getY();

        // Draw stick figure for the actor
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - 10, y - 40, 20, 20); // Head
        gc.strokeLine(x, y - 20, x, y); // Body
        gc.strokeLine(x, y, x - 10, y + 20); // Left leg
        gc.strokeLine(x, y, x + 10, y + 20); // Right leg
        gc.strokeLine(x, y - 15, x - 20, y - 15); // Left arm
        gc.strokeLine(x, y - 15, x + 20, y - 15); // Right arm

        // Draw connection points
        gc.setFill(Color.RED);
        for (double[] point : actor.getConnectionPoints()) {
            gc.fillOval(point[0] - 3, point[1] - 3, 6, 6); // Draw small red circles
        }

        // Draw actor label centered horizontally below the figure
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(actor.getName());
        textHelper.setFont(gc.getFont());
        double textWidth = textHelper.getBoundsInLocal().getWidth();

        // Position the text centered horizontally relative to the actor figure
        gc.fillText(actor.getName(), x - textWidth / 2, y + 40); // 40px below the actor's head
    }

    public void clear() {

    }


    /**
     * Represents an actor in a UML use-case diagram with coordinates, a name, and connection points.
     */
    private static class Actor implements Serializable {

        /** X-coordinate of the actor's position. */
        private double x;

        /** Y-coordinate of the actor's position. */
        private double y;

        /** Name of the actor, generated uniquely. */
        private String name;

        /** Counter for generating unique actor names. */
        private static int count = 0;

        /**
         * Constructs an Actor at a specified position.
         *
         * @param x the X-coordinate of the actor's position
         * @param y the Y-coordinate of the actor's position
         */
        public Actor(double x, double y) {
            this.x = x;
            this.y = y;
            this.name = "Actor" + (++count); // Generate unique actor name
        }

        /**
         * Returns the X-coordinate of the actor's position.
         *
         * @return the X-coordinate
         */
        public double getX() {
            return x;
        }

        /**
         * Returns the Y-coordinate of the actor's position.
         *
         * @return the Y-coordinate
         */
        public double getY() {
            return y;
        }

        /**
         * Returns the name of the actor.
         *
         * @return the actor's name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the connection points for the actor's position.
         * Connection points include top, bottom, left, and right.
         *
         * @return a 2D array of connection points, where each entry represents a point as [x, y]
         */
        public double[][] getConnectionPoints() {
            return new double[][]{
                    {x, y - 40}, // Top
                    {x, y + 20}, // Bottom
                    {x - 15, y - 10}, // Left
                    {x + 15, y - 10}  // Right
            };
        }
    }

    /**
     * Represents a use case in a UML use-case diagram with coordinates, dimensions, and a name.
     * Provides functionality to adjust dimensions based on text size and to retrieve connection points.
     */
    private static class UseCase implements Serializable {
        /** X-coordinate of the use case's position. */
        private double x;
        /** Y-coordinate of the use case's position. */
        private double y;
        /** Width of the use case. Default is 120. */
        private double width = 120;
        /** Height of the use case. Default is 60. */
        private double height = 60;
        /** Name of the use case, generated uniquely by default. */
        private String name;
        /** Counter for generating unique use case names. */
        private static int count = 0;

        /**
         * Constructs a UseCase at a specified position with default dimensions.
         *
         * @param x the X-coordinate of the use case's position
         * @param y the Y-coordinate of the use case's position
         */
        public UseCase(double x, double y) {
            this.x = x;
            this.y = y;
            this.name = "UseCase" + (++count);
        }

        /**
         * Returns the X-coordinate of the use case's position.
         *
         * @return the X-coordinate
         */
        public double getX() {
            return x;
        }

        /**
         * Returns the Y-coordinate of the use case's position.
         *
         * @return the Y-coordinate
         */
        public double getY() {
            return y;
        }

        /**
         * Returns the width of the use case.
         *
         * @return the width
         */
        public double getWidth() {
            return width;
        }

        /**
         * Returns the height of the use case.
         *
         * @return the height
         */
        public double getHeight() {
            return height;
        }

        /**
         * Returns the name of the use case.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets a new name for the use case and adjusts its size based on the new text dimensions.
         *
         * @param name the new name of the use case
         * @param gc   the graphics context used to calculate text dimensions
         */
        public void setName(String name, GraphicsContext gc) {
            this.name = name;
            adjustSize(gc);
        }

        /**
         * Adjusts the dimensions of the use case based on the current name's text size.
         * Ensures a minimum width of 120 and height of 60.
         *
         * @param gc the graphics context used to calculate text dimensions
         */
        public void adjustSize(GraphicsContext gc) {
            gc.setFont(Font.font("Arial", 12));
            javafx.scene.text.Text textHelper = new javafx.scene.text.Text(name);
            textHelper.setFont(gc.getFont());

            double textWidth = textHelper.getBoundsInLocal().getWidth();
            double textHeight = textHelper.getBoundsInLocal().getHeight();

            this.width = Math.max(120, textWidth + 20); // Minimum width 120
            this.height = Math.max(60, textHeight + 20); // Minimum height 60
        }

        /**
         * Returns the connection points for the use case's dimensions.
         * Connection points include top, bottom, left, and right.
         *
         * @return a 2D array of connection points, where each entry represents a point as [x, y]
         */
        public double[][] getConnectionPoints() {
            return new double[][]{
                    {x, y - height / 2}, // Top
                    {x, y + height / 2}, // Bottom
                    {x - width / 2, y},  // Left
                    {x + width / 2, y}   // Right
            };
        }
    }

    /**
     * Represents a connection line between two elements (Actor or UseCase) in a UML diagram.
     * Supports different connection types such as "association" and "include."
     */
    private static class LineConnection implements Serializable {

        /** The starting element of the connection, either an Actor or a UseCase. */
        private Object startElement;

        /** The ending element of the connection, either an Actor or a UseCase. */
        private Object endElement;

        /** Index of the connection point on the starting element. */
        private int startConnectionIndex;

        /** Index of the connection point on the ending element. */
        private int endConnectionIndex;

        /** The type of connection, e.g., "association" or "include." */
        private String type;

        /**
         * Constructs a LineConnection between two elements with specified connection points and type.
         *
         * @param startElement        the starting element of the connection (Actor or UseCase)
         * @param startConnectionIndex the index of the connection point on the starting element
         * @param endElement          the ending element of the connection (Actor or UseCase)
         * @param endConnectionIndex   the index of the connection point on the ending element
         * @param type                the type of connection (e.g., "association" or "include")
         */
        public LineConnection(Object startElement, int startConnectionIndex, Object endElement, int endConnectionIndex, String type) {
            this.startElement = startElement;
            this.startConnectionIndex = startConnectionIndex;
            this.endElement = endElement;
            this.endConnectionIndex = endConnectionIndex;
            this.type = type; // Type of the connection (e.g., "association", "include")
        }

        /**
         * Returns the starting point of the connection based on the starting element and connection index.
         *
         * @return an array representing the [x, y] coordinates of the starting point
         */
        public double[] getStartPoint() {
            if (startElement instanceof Actor) {
                return ((Actor) startElement).getConnectionPoints()[startConnectionIndex];
            } else if (startElement instanceof UseCase) {
                return ((UseCase) startElement).getConnectionPoints()[startConnectionIndex];
            }
            return new double[]{0, 0};
        }

        /**
         * Returns the ending point of the connection based on the ending element and connection index.
         *
         * @return an array representing the [x, y] coordinates of the ending point
         */
        public double[] getEndPoint() {
            if (endElement instanceof Actor) {
                return ((Actor) endElement).getConnectionPoints()[endConnectionIndex];
            } else if (endElement instanceof UseCase) {
                return ((UseCase) endElement).getConnectionPoints()[endConnectionIndex];
            }
            return new double[]{0, 0};
        }

        /**
         * Returns the type of the connection.
         *
         * @return the connection type, e.g., "association" or "include"
         */
        public String getType() {
            return type;
        }
    }

    /**
     * Represents a subject in a UML use-case diagram that contains multiple use cases.
     * Defines boundaries, position, and functionality to manage contained use cases.
     */
    private static class UseCaseSubject implements Serializable {

        /** X-coordinate of the top-left corner of the subject. */
        private double x;

        /** Y-coordinate of the top-left corner of the subject. */
        private double y;

        /** Width of the subject. Default is 200. */
        private double width;

        /** Height of the subject. Default is 150. */
        private double height;

        /** Counter for generating unique subject names. */
        private static int count = 0;

        /** Name of the subject, generated uniquely by default. */
        private String name;

        /** List of use cases contained within the subject. */
        private List<UseCase> containedUseCases = new ArrayList<>();

        /**
         * Constructs a UseCaseSubject at a specified position with default size.
         *
         * @param x the X-coordinate of the top-left corner of the subject
         * @param y the Y-coordinate of the top-left corner of the subject
         */
        public UseCaseSubject(double x, double y) {
            this.x = x;
            this.y = y;
            this.width = 200; // Default size
            this.height = 150; // Default size
            this.name = "Subject" + (++count);
        }

        /**
         * Checks if the mouse is over the subject's area.
         *
         * @param mouseX the X-coordinate of the mouse
         * @param mouseY the Y-coordinate of the mouse
         * @return true if the mouse is over the subject's area, false otherwise
         */
        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        /**
         * Checks if the mouse is over the border of the subject for resizing purposes.
         *
         * @param mouseX the X-coordinate of the mouse
         * @param mouseY the Y-coordinate of the mouse
         * @return true if the mouse is over the border, false otherwise
         */
        public boolean isMouseOverBorder(double mouseX, double mouseY) {
            double tolerance = 10.0; // Border thickness for resizing
            return (Math.abs(mouseX - x) < tolerance || Math.abs(mouseX - (x + width)) < tolerance ||
                    Math.abs(mouseY - y) < tolerance || Math.abs(mouseY - (y + height)) < tolerance);
        }

        /**
         * Adds a use case to the subject's list of contained use cases.
         *
         * @param useCase the UseCase to be added
         */
        public void addUseCase(UseCase useCase) {
            containedUseCases.add(useCase);
        }

        /**
         * Checks if a point is within the subject's boundaries.
         *
         * @param itemX the X-coordinate of the point
         * @param itemY the Y-coordinate of the point
         * @return true if the point is within the subject's boundaries, false otherwise
         */
        public boolean contains(double itemX, double itemY) {
            return itemX >= x && itemX <= x + width && itemY >= y && itemY <= y + height;
        }
    }
}